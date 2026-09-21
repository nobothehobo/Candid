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
    private boolean inverted,registered;
    private int size;
    public NegativePreviewScreen(Screen parent,PreviewPayload photo){super(Component.literal(photo.title()));this.parent=parent;this.photo=photo;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        upload();
        addRenderableWidget(Button.builder(Component.literal("Positive / negative"),b->{inverted=!inverted;upload();}).bounds(width/2-154,height-26,150,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back to darkroom"),b->onClose()).bounds(width/2+4,height-26,150,20).build());
    }
    private void upload(){
        size=photo.colors().length==65536?256:128;
        if(photo.colors().length!=size*size)throw new IllegalArgumentException("Invalid preview dimensions");
        var image=new NativeImage(size,size,false);
        for(int y=0;y<size;y++)for(int x=0;x<size;x++){
            int c=MapColor.getColorFromPackedId(photo.colors()[y*size+x]&255);
            if(inverted){int r=255-((c>>16)&255),g=255-((c>>8)&255),b=255-(c&255);c=0xff000000|(r<<16)|(g<<8)|b;}
            image.setPixel(x,y,c);
        }
        if(registered)minecraft.getTextureManager().release(texture);
        minecraft.getTextureManager().register(texture,new DynamicTexture(()->"Candid negative preview",image));registered=true;
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        g.fill(0,0,width,height,0xff171a1c);
        int side=Math.min(width-24,height-64),left=(width-side)/2,top=28;
        g.blit(RenderPipelines.GUI_TEXTURED,texture,left,top,0,0,side,side,size,size,size,size);
        g.drawCenteredString(font,font.plainSubstrByWidth(title.getString(),width-16),width/2,10,0xffeee5d5);
        super.render(g,x,y,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void removed(){if(registered){minecraft.getTextureManager().release(texture);registered=false;}}
}
