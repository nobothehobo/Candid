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
    private PhotoCapture(){}
    public static boolean hiding(){return pending!=null||reading;}
    public static boolean busy(){return pending!=null||reading||processing;}
    public static boolean queue(ItemStack camera,int aperture,int shutter,float offset){
        Minecraft mc=Minecraft.getInstance();FilmStock stock=CameraData.film(camera);
        if(busy()||stock==null||CameraData.frames(camera)<=0||!CameraData.isWound(camera)||CameraData.cameraId(camera).isEmpty()||CameraData.rollId(camera).isEmpty())return false;
        pending=new Pending(stock,aperture,shutter,offset,CameraData.cameraId(camera),CameraData.rollId(camera),UUID.randomUUID().toString());
        generation++;waitTicks=2;started=System.currentTimeMillis();level=mc.level;return true;
    }
    public static void tick(Minecraft client){
        if(!busy())return;
        if(client.level==null||client.player==null||client.level!=level||System.currentTimeMillis()-started>10000){generation++;pending=null;reading=false;processing=false;return;}
        if(pending==null||waitTicks-->0)return;
        Pending shot=pending;long captureGeneration=generation;Object capturedLevel=level;pending=null;reading=true;
        try{Screenshot.takeScreenshot(client.getMainRenderTarget(),image->{
            try {
                if(captureGeneration!=generation)return;
                int[] samples=sample(image);reading=false;processing=true;
                PROCESSOR.execute(()->{
                    try {byte[] result=convert(samples,shot);client.execute(()->{
                        if(captureGeneration!=generation)return;
                        processing=false;
                        if(client.level!=capturedLevel||client.player==null)return;
                        if(ClientPlayNetworking.canSend(CapturePhotoPayload.ID))ClientPlayNetworking.send(new CapturePhotoPayload(result,shot.aperture,shot.shutter,shot.camera,shot.roll,shot.id,shot.offset));
                        if(client.screen==null)client.setScreen(new CameraScreen());
                    });}catch(Exception e){client.execute(()->failure(client,e,captureGeneration));}
                });
            }catch(Exception e){failure(client,e,captureGeneration);}finally{image.close();}
        });}catch(Exception e){failure(client,e,captureGeneration);}
    }
    private static void failure(Minecraft client,Exception e,long captureGeneration){if(captureGeneration!=generation)return;pending=null;reading=false;processing=false;org.slf4j.LoggerFactory.getLogger("Candid").error("Capture failed",e);if(client.player!=null)client.player.displayClientMessage(Component.literal("Capture failed. No frame used."),true);}
    private static int[] sample(NativeImage image){
        FrameGeometry crop=FrameGeometry.of(image.getWidth(),image.getHeight());int[] out=new int[126*84];
        // Screenshot already returns top-left-oriented pixels. Do not flip a second time.
        for(int y=0;y<84;y++)for(int x=0;x<126;x++){
            int r=0,g=0,b=0;for(double dy:new double[]{.25,.75})for(double dx:new double[]{.25,.75}){
                int sx=crop.x()+Math.min(crop.width()-1,(int)((x+dx)*crop.width()/126)),sy=crop.y()+Math.min(crop.height()-1,(int)((y+dy)*crop.height()/84));
                int c=image.getPixel(sx,sy);r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;
            }out[y*126+x]=((r/4)<<16)|((g/4)<<8)|(b/4);
        }return out;
    }
    private static byte[] convert(int[] rgb,Pending shot){
        byte[] out=new byte[16384];Arrays.fill(out,(byte)nearest(0xe5dfd1,false));Random noise=new Random(UUID.fromString(shot.id).getLeastSignificantBits());
        for(int y=0;y<84;y++)for(int x=0;x<126;x++)out[(y+22)*128+x+1]=(byte)nearest(FilmSignal.process(rgb[y*126+x],shot.stock,shot.offset,noise),shot.stock.monochrome());
        return out;
    }
    private static int[] palette(){int[] p=new int[248];for(int i=4;i<p.length;i++)p[i]=MapColor.getColorFromPackedId(i);return p;}
    private static int nearest(int rgb,boolean mono){
        int best=4;long distance=Long.MAX_VALUE;
        for(int i=4;i<PALETTE.length;i++){int c=PALETTE[i];if((c>>>24)==0)continue;int r=(c>>16)&255,g=(c>>8)&255,b=c&255;if(mono&&(Math.abs(r-g)>3||Math.abs(g-b)>3))continue;
            long dr=((rgb>>16)&255)-r,dg=((rgb>>8)&255)-g,db=(rgb&255)-b,d=2*dr*dr+4*dg*dg+db*db;if(d<distance){distance=d;best=i;}}
        return best;
    }
    private record Pending(FilmStock stock,int aperture,int shutter,float offset,String camera,String roll,String id){}
}
