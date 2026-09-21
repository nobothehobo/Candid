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
@Mixin(ItemInHandRenderer.class)
public abstract class LoadingHandsMixin {
    @Shadow public abstract void renderItem(LivingEntity entity,ItemStack item,ItemDisplayContext context,PoseStack pose,SubmitNodeCollector collector,int light);
    @Shadow private void renderPlayerArm(PoseStack pose,SubmitNodeCollector collector,int light,float equip,float swing,HumanoidArm arm){throw new AssertionError();}

    @Inject(method="renderHandsWithItems",at=@At("HEAD"),cancellable=true)
    private void candid$load(float partialTick,PoseStack pose,SubmitNodeCollector collector,LocalPlayer player,int light,CallbackInfo ci){
        if(!(Minecraft.getInstance().screen instanceof FilmLoadScreen loading))return;
        ItemStack held=CameraOptics.camera();if(held.isEmpty())return;
        float age=loading.animationAge()+partialTick;int phase=age<8?0:age<16?1:age<32?2:age<40?3:age<53?4:age<61?1:0;
        ItemStack camera=held.copy();
        if(phase>0)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_loading_"+phase));
        pose.pushPose();pose.translate(0,-.30,-1.25);pose.mulPose(Axis.XP.rotationDegrees(18));pose.scale(1.15f,1.15f,1.15f);
        renderItem(player,camera,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        // Minecraft renders the player's actual skin, including the chosen arm width.
        pose.pushPose();pose.translate(-.34,-.02,-.28);renderPlayerArm(pose,collector,light,0,0,HumanoidArm.RIGHT);pose.popPose();
        float reach=age>=16&&age<50?(float)Math.sin(Math.PI*Math.min(1,(age-16)/34))*.25f:0;
        pose.pushPose();pose.translate(.22+reach,.02,-.23-reach);renderPlayerArm(pose,collector,light,0,0,HumanoidArm.LEFT);pose.popPose();
        if(age>=16&&age<32){
            float progress=Math.min(1,(age-16)/16);
            var cartridge=new ItemStack(CandidItems.itemFor(loading.stock()));cartridge.set(DataComponents.ITEM_MODEL,Candid.id("film_loading_cartridge"));
            pose.pushPose();pose.translate(-.46+progress*.18,-.18-progress*.09,-.9-progress*.29);pose.scale(.24f,.24f,.24f);
            renderItem(player,cartridge,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        }
        ci.cancel();
    }
}
