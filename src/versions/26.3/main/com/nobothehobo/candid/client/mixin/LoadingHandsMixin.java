package com.nobothehobo.candid.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nobothehobo.candid.Candid;
import com.nobothehobo.candid.client.*;
import com.nobothehobo.candid.content.CandidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Render-only choreography. Neither the player's real offhand nor camera components are mutated. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class LoadingHandsMixin {
    private void renderItem(LivingEntity entity,ItemStack item,ItemDisplayContext context,PoseStack pose,SubmitNodeCollector collector,int light){
        var state=new net.minecraft.client.renderer.item.ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(state,item,context,entity);
        state.submit(pose,collector,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0);
    }
    @Shadow private void renderPlayerArm(PoseStack pose,SubmitNodeCollector collector,int light,float equip,float swing,HumanoidArm arm,net.minecraft.client.renderer.state.level.PlayerRenderState player){throw new AssertionError();}

    @Inject(method="submitHandsWithItems",at=@At("HEAD"))
    private void candid$load(float partialTick,PoseStack pose,SubmitNodeCollector collector,net.minecraft.client.renderer.state.level.PlayerRenderState playerState,net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState hands,CallbackInfo ci){
        if(!(Minecraft.getInstance().screen instanceof FilmLoadScreen loading))return;
        var player=Minecraft.getInstance().player;if(player==null||playerState.avatarRenderState==null)return;
        int light=playerState.avatarRenderState.lightCoords;
        ItemStack held=CameraOptics.camera();if(held.isEmpty())return;
        float age=loading.animationAge()+partialTick;int phase=age<8?0:age<16?1:age<32?2:age<40?3:age<53?4:age<61?1:0;
        ItemStack camera=held.copy();
        if(phase>0)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_loading_"+phase));
        pose.pushPose();pose.translate(0,-.30,-1.25);pose.rotate(Axis.XP.rotationDegrees(18));pose.scale(1.15f,1.15f,1.15f);
        renderItem(player,camera,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        // Minecraft renders the player's actual skin, including the chosen arm width.
        pose.pushPose();pose.translate(-.34,-.02,-.28);renderPlayerArm(pose,collector,light,0,0,HumanoidArm.RIGHT,playerState);pose.popPose();
        float reach=age>=16&&age<50?(float)Math.sin(Math.PI*Math.min(1,(age-16)/34))*.25f:0;
        pose.pushPose();pose.translate(.22+reach,.02,-.23-reach);renderPlayerArm(pose,collector,light,0,0,HumanoidArm.LEFT,playerState);pose.popPose();
        if(age>=16&&age<32){
            float progress=Math.min(1,(age-16)/16);
            var cartridge=new ItemStack(CandidItems.itemFor(loading.stock()));cartridge.set(DataComponents.ITEM_MODEL,Candid.id("film_loading_cartridge"));
            pose.pushPose();pose.translate(-.46+progress*.18,-.18-progress*.09,-.9-progress*.29);pose.scale(.24f,.24f,.24f);
            renderItem(player,cartridge,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        }
    }

    // Keep renderHandsWithItems' tail: it flushes the deferred submit collector in 1.21.10.
    // Cancelling that outer method silently discards every model/skin submitted above.
    @Inject(method="submitArmWithItem",at=@At("HEAD"),cancellable=true)
    private void candid$replaceVanillaArms(CallbackInfo ci){
        if(Minecraft.getInstance().screen instanceof FilmLoadScreen&&!CameraOptics.camera().isEmpty())ci.cancel();
    }
}
