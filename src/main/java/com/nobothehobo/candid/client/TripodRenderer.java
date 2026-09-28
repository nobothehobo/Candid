package com.nobothehobo.candid.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nobothehobo.candid.block.TripodBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
public final class TripodRenderer implements BlockEntityRenderer<TripodBlockEntity,TripodRenderer.State> {
    public static final class State extends BlockEntityRenderState {final ItemStackRenderState camera=new ItemStackRenderState();float yaw,pitch;boolean hidden;}
    private final ItemModelResolver resolver;
    public TripodRenderer(BlockEntityRendererProvider.Context context){resolver=context.itemModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(TripodBlockEntity b,State s,float partial,Vec3 pos,ModelFeatureRenderer.CrumblingOverlay crumble){
        BlockEntityRenderer.super.extractRenderState(b,s,partial,pos,crumble);s.yaw=b.yaw();s.pitch=b.pitch();
        s.hidden=CameraOptics.active()&&b.getBlockPos().equals(CameraOptics.mountedPosition());
        resolver.updateForTopItem(s.camera,b.camera(),ItemDisplayContext.NONE,b.getLevel(),null,0);
    }
    @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState view){
        if(s.hidden||s.camera.isEmpty())return;
        pose.pushPose();pose.translate(.5,1.55,.5);pose.mulPose(Axis.YP.rotationDegrees(180-s.yaw));pose.mulPose(Axis.XP.rotationDegrees(-s.pitch));pose.scale(.65f,.65f,.65f);
        s.camera.submit(pose,collector,s.lightCoords,OverlayTexture.NO_OVERLAY,0);pose.popPose();
    }
}
