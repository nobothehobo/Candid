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
        }catch(java.io.IOException e){throw new IllegalArgumentException("Invalid scan image",e);}
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
    private void export(){
        byte[] png=photo.png();if(png.length==0){status="This older negative has no full-color scan.";return;}
        String name=photo.filename();if(!name.matches("[a-fA-F0-9-]{36}\\.png")){status="Invalid photo filename";return;}
        java.nio.file.Path destination=minecraft.gameDirectory.toPath().resolve("candid-exports").resolve(name);
        status="Saving PNG…";var client=minecraft;
        java.util.concurrent.CompletableFuture.runAsync(()->{
            try{java.nio.file.Files.createDirectories(destination.getParent());java.nio.file.Files.write(destination,png);
                client.execute(()->{status="Saved in candid-exports • ready to share";net.minecraft.Util.getPlatform().openPath(destination.getParent());});
            }catch(Exception e){client.execute(()->status="Could not export — see game log");org.slf4j.LoggerFactory.getLogger("Candid").error("PNG export failed",e);}
        });
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void removed(){if(registered){minecraft.getTextureManager().release(texture);registered=false;}}
}
