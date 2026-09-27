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

    @Inject(method="renderHandsWithItems",at=@At("HEAD"))
    private void candid$load(float partialTick,PoseStack pose,SubmitNodeCollector collector,LocalPlayer player,int light,CallbackInfo ci){
        var screen=Minecraft.getInstance().screen;
        FilmLoadScreen loading=screen instanceof FilmLoadScreen f?f:null;
        FilmUnloadScreen unloading=screen instanceof FilmUnloadScreen f?f:null;
        CameraRaiseScreen raising=screen instanceof CameraRaiseScreen f?f:null;
        CameraScreen finder=screen instanceof CameraScreen f&&f.winding()?f:null;
        if(loading==null&&unloading==null&&raising==null&&finder==null)return;
        ItemStack held=CameraOptics.camera();if(held.isEmpty())return;
        float age=(loading!=null?loading.animationAge():unloading!=null?unloading.animationAge():raising!=null?raising.animationAge():finder.windingAge())+partialTick;
        boolean advancing=finder!=null||raising!=null&&raising.winding();
        float leverAngle=advancing?com.nobothehobo.candid.core.WindingMotion.angle(age):0;
        int phase=loading!=null?(age<8?0:age<16?1:age<32?2:age<40?3:age<53?4:age<61?1:0):unloading!=null?(age<40?0:age<50?2:age<56?1:0):0;
        ItemStack camera=held.copy();
        if(phase>0)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_loading_"+phase+"_"+com.nobothehobo.candid.data.CameraData.lens(held)));
        if(advancing)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_winding_"+com.nobothehobo.candid.data.CameraData.lens(held)));
        pose.pushPose();pose.translate(0,raising==null?-.30:-.45+.50*Math.min(1,age/12),raising==null?-1.25:-1.25+.55*Math.min(1,age/12));pose.mulPose(Axis.XP.rotationDegrees(18));pose.scale(1.15f,1.15f,1.15f);
        if(finder!=null)pose.translate(.20,-.24,0);
        renderItem(player,camera,ItemDisplayContext.NONE,pose,collector,light);
        if(advancing){
            var lever=held.copy();lever.set(DataComponents.ITEM_MODEL,Candid.id("camera_advance_lever"));
            // Item models are centered at (8,8,8); pivot is the inner end of the lever.
            double px=(10.8-8)/16,py=(13.1-8)/16,pz=(9.3-8)/16;
            pose.translate(px,py,pz);pose.mulPose(Axis.YP.rotationDegrees(-leverAngle));pose.translate(-px,-py,-pz);
            renderItem(player,lever,ItemDisplayContext.NONE,pose,collector,light);
        }
        pose.popPose();
        // Minecraft renders the player's actual skin, including the chosen arm width.
        pose.pushPose();pose.translate(-.34+leverAngle/700,-.02+(advancing?.12:0),-.28-leverAngle/900);
        if(advancing)pose.mulPose(Axis.ZP.rotationDegrees(-leverAngle*.35f));
        renderPlayerArm(pose,collector,light,0,0,HumanoidArm.RIGHT);pose.popPose();
        float reach=raising==null&&age>=16&&age<50?(float)Math.sin(Math.PI*Math.min(1,(age-16)/34))*.25f:0;
        pose.pushPose();pose.translate(.22+reach,unloading!=null&&age<10?-.35:.02,-.23-reach);
        if(unloading!=null&&age>=10&&age<40)pose.mulPose(Axis.ZP.rotationDegrees((float)Math.sin(age*.7)*24));renderPlayerArm(pose,collector,light,0,0,HumanoidArm.LEFT);pose.popPose();
        if(loading!=null&&age>=16&&age<32||unloading!=null&&age>=40&&age<54){
            float progress=loading!=null?Math.min(1,(age-16)/16):1-Math.min(1,(age-40)/14);
            var cartridge=new ItemStack(CandidItems.itemFor(loading!=null?loading.stock():unloading.stock()));cartridge.set(DataComponents.ITEM_MODEL,Candid.id("film_loading_cartridge"));
            pose.pushPose();pose.translate(-.46+progress*.18,-.18-progress*.09,-.9-progress*.29);pose.scale(.24f,.24f,.24f);
            renderItem(player,cartridge,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        }
    }

    // Keep renderHandsWithItems' tail: it flushes the deferred submit collector in 1.21.10.
    // Cancelling that outer method silently discards every model/skin submitted above.
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void candid$replaceVanillaArms(CallbackInfo ci){
        if((Minecraft.getInstance().screen instanceof FilmLoadScreen||Minecraft.getInstance().screen instanceof FilmUnloadScreen||Minecraft.getInstance().screen instanceof CameraRaiseScreen||Minecraft.getInstance().screen instanceof CameraScreen f&&f.winding())&&!CameraOptics.camera().isEmpty())ci.cancel();
    }
}
