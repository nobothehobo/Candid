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
        var screen=Minecraft.getInstance().screen;
        FilmLoadScreen loading=screen instanceof FilmLoadScreen f?f:null;
        FilmUnloadScreen unloading=screen instanceof FilmUnloadScreen f?f:null;
        CameraRaiseScreen raising=screen instanceof CameraRaiseScreen f?f:null;
        if(loading==null&&unloading==null&&raising==null)return;
        var player=Minecraft.getInstance().player;if(player==null||playerState.avatarRenderState==null)return;
        int light=playerState.avatarRenderState.lightCoords;
        ItemStack held=CameraOptics.camera();if(held.isEmpty())return;
        float age=(loading!=null?loading.animationAge():unloading!=null?unloading.animationAge():raising.animationAge())+partialTick;
        boolean advancing=loading!=null&&age>=68&&age<80||raising!=null&&raising.winding();
        float leverAngle=advancing?com.nobothehobo.candid.core.WindingMotion.angle(loading!=null?age-68:age*com.nobothehobo.candid.core.WindingMotion.TICKS/CameraRaiseScreen.DURATION):0;
        ItemStack camera=held.copy();
        if(loading!=null||unloading!=null)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_open_"+com.nobothehobo.candid.data.CameraData.lens(held)));
        if(advancing)camera.set(DataComponents.ITEM_MODEL,Candid.id("camera_winding_"+com.nobothehobo.candid.data.CameraData.lens(held)));
        float lift=raising==null?0:Math.min(1,age/CameraRaiseScreen.DURATION);lift=lift*lift*(3-2*lift);
        pose.pushPose();pose.translate(0,raising==null?-.30:-.45+.40*lift,raising==null?-1.25:-1.25+.35*lift);pose.rotate(Axis.XP.rotationDegrees(18));pose.scale(1.15f,1.15f,1.15f);
        renderItem(player,camera,ItemDisplayContext.NONE,pose,collector,light);
        if(!advancing&&(loading!=null||unloading!=null)){
            var part=held.copy();part.set(DataComponents.ITEM_MODEL,Candid.id("camera_back"));
            pose.pushPose();pose.translate((14.8-8)/16,0,(10.3-8)/16);
            pose.rotate(Axis.YP.rotationDegrees(com.nobothehobo.candid.core.HandlingMotion.backAngle(age,unloading!=null)));
            pose.translate(-(14.8-8)/16,0,-(10.3-8)/16);
            renderItem(player,part,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
            if(loading!=null&&age>=32||unloading!=null&&age<40){
                part.set(DataComponents.ITEM_MODEL,Candid.id("camera_loaded_cartridge"));
                renderItem(player,part,ItemDisplayContext.NONE,pose,collector,light);
                float leader=com.nobothehobo.candid.core.HandlingMotion.leader(age,unloading!=null);
                if(leader>.001){part.set(DataComponents.ITEM_MODEL,Candid.id("camera_leader"));
                    pose.pushPose();pose.translate(-.25,0,0);pose.scale(leader,1,1);pose.translate(.25,0,0);
                    renderItem(player,part,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();}
            }
        }
        if(advancing){
            var lever=held.copy();lever.set(DataComponents.ITEM_MODEL,Candid.id("camera_advance_lever"));
            // Item models are centered at (8,8,8); pivot is the inner end of the lever.
            double px=(10.8-8)/16,py=(13.1-8)/16,pz=(9.3-8)/16;
            pose.pushPose();pose.translate(px,py,pz);pose.rotate(Axis.YP.rotationDegrees(-leverAngle));pose.translate(-px,-py,-pz);
            renderItem(player,lever,ItemDisplayContext.NONE,pose,collector,light);
            pose.popPose();
            // Follow the grip position, not its full rotation: rotating the entire
            // forearm by the lever angle made the wrist look twisted.
            double a=Math.toRadians(leverAngle),gx=10.8+3.4*Math.cos(a)-.15*Math.sin(a),gz=9.3+3.4*Math.sin(a)+.15*Math.cos(a);
            pose.translate((gx-8)/16,(13.55-8)/16,(gz-8)/16);
            pose.rotate(Axis.YP.rotationDegrees(-leverAngle*.15f));pose.scale(.82f,.82f,.82f);
            pose.translate(-.75517044,.43795376,1.09521622);
            renderPlayerArm(pose,collector,light,0,0,HumanoidArm.RIGHT,playerState);
        }
        pose.popPose();
        // Minecraft renders the player's actual skin, including the chosen arm width.
        if(!advancing){pose.pushPose();pose.translate(.43,-.25,-1.02);pose.scale(.82f,.82f,.82f);pose.translate(-.75517044,.43795376,1.09521622);
            renderPlayerArm(pose,collector,light,0,0,HumanoidArm.RIGHT,playerState);pose.popPose();}
        float progress=loading!=null?Math.max(0,Math.min(1,(age-16)/16)):unloading!=null?1-Math.max(0,Math.min(1,(age-40)/14)):0;
        pose.pushPose();pose.translate(-.48+progress*.18,-.28-progress*.09,-.86-progress*.29);
        pose.scale(.82f,.82f,.82f);
        if(unloading!=null&&age>=10&&age<40)pose.rotate(Axis.ZP.rotationDegrees((float)Math.sin(age*.7)*8));
        pose.translate(.75517044,.43795376,1.09521622);
        renderPlayerArm(pose,collector,light,0,0,HumanoidArm.LEFT,playerState);pose.popPose();
        if(loading!=null&&age>=16&&age<32||unloading!=null&&age>=40&&age<54){
            float cartridgeProgress=loading!=null?Math.min(1,(age-16)/16):1-Math.min(1,(age-40)/14);
            var cartridge=new ItemStack(CandidItems.itemFor(loading!=null?loading.stock():unloading.stock()));cartridge.set(DataComponents.ITEM_MODEL,Candid.id("film_loading_cartridge"));
            pose.pushPose();pose.translate(-.46+cartridgeProgress*.18,-.18-cartridgeProgress*.09,-.9-cartridgeProgress*.29);pose.scale(.24f,.24f,.24f);
            renderItem(player,cartridge,ItemDisplayContext.NONE,pose,collector,light);pose.popPose();
        }
    }

    // Keep renderHandsWithItems' tail: it flushes the deferred submit collector in 1.21.10.
    // Cancelling that outer method silently discards every model/skin submitted above.
    @Inject(method="submitArmWithItem",at=@At("HEAD"),cancellable=true)
    private void candid$replaceVanillaArms(CallbackInfo ci){
        if((Minecraft.getInstance().screen instanceof FilmLoadScreen||Minecraft.getInstance().screen instanceof FilmUnloadScreen||Minecraft.getInstance().screen instanceof CameraRaiseScreen)&&!CameraOptics.camera().isEmpty())ci.cancel();
    }
}
