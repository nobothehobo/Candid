package com.nobothehobo.candid.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nobothehobo.candid.core.*;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CapturePhotoPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import java.util.*;
import java.util.concurrent.*;

/** One bounded capture at a time. GPU access on the render thread; pure film processing off-thread. */
public final class PhotoCapture {
    private static final ExecutorService PROCESSOR=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Candid-film-response");t.setDaemon(true);return t;});
    private static final int[] PALETTE=palette();
    private static Pending pending;
    private static boolean reading,processing;
    private static int waitTicks;
    private static long started, generation;
    private static Object level;
    private static FocusSampler focusSampler;
    private static long exposureStart,nextSample;
    private static int samplesTaken,sampleCount;
    private static double[] accumulated;
    private static final double[] LINEAR=new double[256];
    static {for(int i=0;i<256;i++){double c=i/255.0;LINEAR[i]=c<=.04045?c/12.92:Math.pow((c+.055)/1.055,2.4);}}
    private PhotoCapture(){}
    public static void cancel(){generation++;pending=null;reading=false;processing=false;accumulated=null;focusSampler=null;}
    public static boolean hiding(){return pending!=null||reading;}
    public static boolean busy(){return pending!=null||reading||processing;}
    public static boolean queue(ItemStack camera,int aperture,int shutter,float offset){
        Minecraft mc=Minecraft.getInstance();FilmStock stock=CameraData.film(camera);
        if(busy()||stock==null||CameraData.frames(camera)<=0||!CameraData.isWound(camera)||CameraData.cameraId(camera).isEmpty()||CameraData.rollId(camera).isEmpty())return false;
        if(CameraData.SHUTTERS[shutter]<0&&CameraData.tripod(camera,mc.player)==null){mc.player.displayClientMessage(Component.literal("Attach the camera to a tripod for exposures of one second or longer."),true);return false;}
        pending=new Pending(stock,aperture,shutter,offset,CameraData.cameraId(camera),CameraData.rollId(camera),UUID.randomUUID().toString(),CameraData.lens(camera),CameraData.focus(camera));
        focusSampler=new FocusSampler(mc,pending.lens);samplesTaken=0;
        sampleCount=CameraData.SHUTTERS[shutter]<0?Math.min(16,Math.max(2,Math.abs(CameraData.SHUTTERS[shutter])*2)):1;
        accumulated=new double[504*336*3];exposureStart=0;nextSample=0;
        net.minecraft.client.KeyMapping.releaseAll();
        mc.setScreen(new CaptureScreen());
        generation++;waitTicks=2;started=System.currentTimeMillis();level=mc.level;return true;
    }
    public static void tick(Minecraft client){
        if(!busy())return;
        if(client.level==null||client.player==null||client.level!=level||System.currentTimeMillis()-started>45000){cancel();if(client.screen instanceof CaptureScreen)client.setScreen(new CameraScreen());return;}
        client.options.keyJump.setDown(false);
        if(pending==null||reading||processing||waitTicks-->0)return;
        if(!focusSampler.tick(client))return;
        if(exposureStart==0){exposureStart=System.currentTimeMillis();nextSample=exposureStart;CandidClient.playLocal(sampleCount>1?com.nobothehobo.candid.content.CandidSounds.BACK_OPEN:com.nobothehobo.candid.content.CandidSounds.SHUTTER);}
        if(System.currentTimeMillis()<nextSample)return;
        Pending shot=pending;long captureGeneration=generation;Object capturedLevel=level;reading=true;
        try{Screenshot.takeScreenshot(client.getMainRenderTarget(),image->{
            try {
                if(captureGeneration!=generation)return;
                int[] samples=sample(image);reading=false;
                for(int i=0;i<samples.length;i++){
                    accumulated[i*3]+=LINEAR[(samples[i]>>16)&255];accumulated[i*3+1]+=LINEAR[(samples[i]>>8)&255];accumulated[i*3+2]+=LINEAR[samples[i]&255];
                }
                samplesTaken++;
                if(samplesTaken<sampleCount){nextSample=exposureStart+(long)(Optics.seconds(CameraData.SHUTTERS[shot.shutter])*1000*samplesTaken/(sampleCount-1));return;}
                if(sampleCount>1)CandidClient.playLocal(com.nobothehobo.candid.content.CandidSounds.SHUTTER);
                double[] sum=accumulated;float[] depths=focusSampler.depths();int count=samplesTaken;accumulated=null;pending=null;processing=true;
                PROCESSOR.execute(()->{
                    try {
                        int[] averaged=new int[504*336];
                        for(int i=0;i<averaged.length;i++)averaged[i]=(encode(sum[i*3]/count)<<16)|(encode(sum[i*3+1]/count)<<8)|encode(sum[i*3+2]/count);
                        int[] focused=DepthOfField.apply(averaged,depths,504,336,shot.lens,CameraData.APERTURES[shot.aperture],shot.focus);
                        int[] scan=film(focused,shot);byte[] png=png(scan),result=convert(scan,shot),small=compact(result);
                        client.execute(()->{
                            if(captureGeneration!=generation)return;
                            processing=false;
                            if(client.level!=capturedLevel||client.player==null)return;
                            if(ClientPlayNetworking.canSend(CapturePhotoPayload.ID)){
                                for(int part=0;part<4;part++)ClientPlayNetworking.send(new com.nobothehobo.candid.network.ScanChunkPayload(shot.id,part,Arrays.copyOfRange(result,part*16384,(part+1)*16384)));
                                for(int part=0;part<(png.length+16383)/16384;part++)ClientPlayNetworking.send(new com.nobothehobo.candid.network.ScanChunkPayload(shot.id,1,png.length,part,Arrays.copyOfRange(png,part*16384,Math.min(png.length,(part+1)*16384))));
                                ClientPlayNetworking.send(new CapturePhotoPayload(small,shot.aperture,shot.shutter,shot.camera,shot.roll,shot.id,shot.offset));
                            }
                            net.minecraft.client.KeyMapping.releaseAll();
                            if(client.screen==null||client.screen instanceof CaptureScreen)client.setScreen(new CameraScreen());
                        });
                    }catch(Exception e){client.execute(()->failure(client,e,captureGeneration));}
                });
            }catch(Exception e){failure(client,e,captureGeneration);}finally{image.close();}
        });}catch(Exception e){failure(client,e,captureGeneration);}
    }
    private static int encode(double linear){double c=linear<=.0031308?linear*12.92:1.055*Math.pow(linear,1/2.4)-.055;return (int)Math.round(Math.max(0,Math.min(1,c))*255);}
    private static void failure(Minecraft client,Exception e,long captureGeneration){if(captureGeneration!=generation)return;pending=null;reading=false;processing=false;org.slf4j.LoggerFactory.getLogger("Candid").error("Capture failed",e);if(client.player!=null)client.player.displayClientMessage(Component.literal("Capture failed. No frame used."),true);if(client.screen instanceof CaptureScreen)client.setScreen(new CameraScreen());}
    private static int[] sample(NativeImage image){
        FrameGeometry crop=FrameGeometry.of(image.getWidth(),image.getHeight());int[] out=new int[504*336];
        // Screenshot already returns top-left-oriented pixels. Do not flip a second time.
        for(int y=0;y<336;y++)for(int x=0;x<504;x++){
            int r=0,g=0,b=0;for(double dy=.25;dy<1;dy+=.5)for(double dx=.25;dx<1;dx+=.5){
                int sx=crop.x()+Math.min(crop.width()-1,(int)((x+dx)*crop.width()/504)),sy=crop.y()+Math.min(crop.height()-1,(int)((y+dy)*crop.height()/336));
                int c=image.getPixel(sx,sy);r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;
            }out[y*504+x]=((r/4)<<16)|((g/4)<<8)|(b/4);
        }return out;
    }
    private static int[] film(int[] rgb,Pending shot){
        int[] out=new int[rgb.length];Random noise=new Random(UUID.fromString(shot.id).getLeastSignificantBits());
        for(int i=0;i<rgb.length;i++)out[i]=FilmSignal.process(rgb[i],shot.stock,shot.offset,noise);return out;
    }
    private static byte[] png(int[] rgb)throws java.io.IOException{
        var image=new java.awt.image.BufferedImage(504,336,java.awt.image.BufferedImage.TYPE_INT_RGB);image.setRGB(0,0,504,336,rgb,0,504);
        var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",bytes);return bytes.toByteArray();
    }
    private static byte[] convert(int[] rgb,Pending shot){
        byte[] out=new byte[65536];Arrays.fill(out,(byte)nearest(0xe5dfd1,false));
        int[] bayer={0,8,2,10,12,4,14,6,3,11,1,9,15,7,13,5};
        for(int y=0;y<168;y++)for(int x=0;x<252;x++){
            int r=0,g=0,b=0;for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++){int c=rgb[(y*2+dy)*504+x*2+dx];r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;}
            int rgbOut=(r/4<<16)|(g/4<<8)|b/4,d=(bayer[(y&3)*4+(x&3)]-8)/2,c=0;
            for(int shift:new int[]{16,8,0})c|=Math.max(0,Math.min(255,((rgbOut>>shift)&255)+d))<<shift;
            out[(y+44)*256+x+2]=(byte)nearest(c,shot.stock.monochrome());
        }
        return out;
    }
    private static byte[] compact(byte[] high){
        byte[] out=new byte[16384];
        for(int y=0;y<128;y++)for(int x=0;x<128;x++){
            int r=0,g=0,b=0;for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++){
                int c=PALETTE[high[(2*y+dy)*256+2*x+dx]&255];r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;
            }
            out[y*128+x]=(byte)nearest((r/4<<16)|(g/4<<8)|b/4,false);
        }
        return out;
    }
    private static int[] palette(){int[] p=new int[248];for(int i=4;i<p.length;i++)p[i]=MapColor.getColorFromPackedId(i);return p;}
    private static int nearest(int rgb,boolean mono){
        int best=4;long distance=Long.MAX_VALUE;
        for(int i=4;i<PALETTE.length;i++){int c=PALETTE[i];if((c>>>24)==0)continue;int r=(c>>16)&255,g=(c>>8)&255,b=c&255;if(mono&&(Math.abs(r-g)>3||Math.abs(g-b)>3))continue;
            long dr=((rgb>>16)&255)-r,dg=((rgb>>8)&255)-g,db=(rgb&255)-b,d=2*dr*dr+4*dg*dg+db*db;if(d<distance){distance=d;best=i;}}
        return best;
    }
    private record Pending(FilmStock stock,int aperture,int shutter,float offset,String camera,String roll,String id,int lens,double focus){}
}
