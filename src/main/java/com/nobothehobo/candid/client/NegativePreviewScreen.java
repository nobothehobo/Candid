package com.nobothehobo.candid.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nobothehobo.candid.Candid;
import com.nobothehobo.candid.network.PreviewPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;

public final class NegativePreviewScreen extends Screen {
    private final Screen parent;
    private final PreviewPayload photo;
    private final ResourceLocation texture=Candid.id("preview/"+java.util.UUID.randomUUID());
    private boolean inverted,registered,mapProof;
    private int imageWidth,imageHeight;
    private String status="";
    private boolean exporting;
    private int size;
    public NegativePreviewScreen(Screen parent,PreviewPayload photo){super(Component.literal(photo.title()));this.parent=parent;this.photo=photo;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        upload();
        addRenderableWidget(Button.builder(Component.literal("Positive / negative"),b->{inverted=!inverted;upload();}).bounds(width/2-154,height-26,150,20).build());
        addRenderableWidget(Button.builder(Component.literal("Export PNG"),b->export()).bounds(width/2-154,height-50,150,20).build());
        addRenderableWidget(Button.builder(Component.literal("Scan / map proof"),b->{mapProof=!mapProof;upload();}).bounds(width/2+4,height-50,150,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back to darkroom"),b->onClose()).bounds(width/2+4,height-26,150,20).build());
    }
    private void upload(){
        size=photo.colors().length==65536?256:128;
        if(photo.colors().length!=size*size)throw new IllegalArgumentException("Invalid preview dimensions");
        NativeImage image;
        try{
            if(photo.png().length>0&&!mapProof)image=NativeImage.read(photo.png());
            else{image=new NativeImage(size,size,false);for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixel(x,y,MapColor.getColorFromPackedId(photo.colors()[y*size+x]&255));}
        }catch(java.io.IOException e){
            status="Scan unreadable • displaying map proof";image=new NativeImage(size,size,false);
            for(int y=0;y<size;y++)for(int x=0;x<size;x++)image.setPixel(x,y,MapColor.getColorFromPackedId(photo.colors()[y*size+x]&255));
        }
        imageWidth=image.getWidth();imageHeight=image.getHeight();
        if(inverted)for(int y=0;y<imageHeight;y++)for(int x=0;x<imageWidth;x++)image.setPixel(x,y,0xff000000|(~image.getPixel(x,y)&0xffffff));
        if(registered)minecraft.getTextureManager().release(texture);
        minecraft.getTextureManager().register(texture,new DynamicTexture(()->"Candid negative preview",image));registered=true;
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        g.fill(0,0,width,height,0xff171a1c);
        int side=Math.min(width-24,height-96),left=(width-side)/2,top=28;
        g.fill(left,top,left+side,top+side,0xffe5dfd1);
        int h=imageWidth==imageHeight?side:side*2/3,offset=(side-h)/2;
        g.blit(RenderPipelines.GUI_TEXTURED,texture,left,top+offset,0,0,side,h,imageWidth,imageHeight,imageWidth,imageHeight);
        if(!status.isEmpty())g.drawCenteredString(font,status,width/2,height-63,0xffe9d7b5);
        g.drawCenteredString(font,font.plainSubstrByWidth(title.getString(),width-16),width/2,10,0xffeee5d5);
        super.render(g,x,y,delta);
    }
    private void export(){exportScan(true);}
    public java.util.concurrent.CompletableFuture<java.nio.file.Path> exportScan(boolean openFolder){
        if(exporting)return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("Export already running"));
        exporting=true;status="Saving PNG…";var client=minecraft;
        var destination=minecraft.gameDirectory.toPath().resolve("candid-exports");
        var result=java.util.concurrent.CompletableFuture.supplyAsync(()->{
            try{
                byte[] png=photo.png();
                if(png.length==0){
                    int side=photo.colors().length==65536?256:128;
                    var image=new java.awt.image.BufferedImage(side,side,java.awt.image.BufferedImage.TYPE_INT_RGB);
                    for(int y=0;y<side;y++)for(int x=0;x<side;x++)image.setRGB(x,y,MapColor.getColorFromPackedId(photo.colors()[y*side+x]&255));
                    var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",bytes);png=bytes.toByteArray();
                }
                return com.nobothehobo.candid.core.PhotoExport.write(destination,photo.filename(),png);
            }catch(Exception e){throw new java.util.concurrent.CompletionException(e);}
        });
        result.whenComplete((path,error)->client.execute(()->{
            exporting=false;
            if(error!=null){status="Could not export — see game log";org.slf4j.LoggerFactory.getLogger("Candid").error("PNG export failed",error);}
            else{status="Saved in candid-exports • ready to share";if(openFolder)net.minecraft.Util.getPlatform().openPath(path.getParent());}
        }));
        return result;
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void removed(){if(registered){minecraft.getTextureManager().release(texture);registered=false;}}
}
