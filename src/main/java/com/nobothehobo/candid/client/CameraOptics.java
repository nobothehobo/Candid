package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class CameraOptics {
    private static net.minecraft.core.BlockPos mounted;
    private static Object mountedLevel;
    private static float yaw,pitch;
    private static boolean dirty;
    private CameraOptics(){}
    public static void mount(net.minecraft.core.BlockPos pos,float initialYaw,float initialPitch){
        var mc=Minecraft.getInstance();mounted=pos;mountedLevel=mc.level;
        yaw=initialYaw;pitch=initialPitch;dirty=false;
    }
    public static void clearMount(){mounted=null;mountedLevel=null;dirty=false;}
    public static net.minecraft.core.BlockPos mountedPosition(){return mounted;}
    public static float yaw(){var p=Minecraft.getInstance().player;return mounted!=null?yaw:p==null?0:p.getYRot();}
    public static float pitch(){var p=Minecraft.getInstance().player;return mounted!=null?pitch:p==null?0:p.getXRot();}
    public static Vec3 direction(){return Vec3.directionFromRotation(pitch(),yaw());}
    public static void aim(float dx,float dy){var p=Minecraft.getInstance().player;if(p==null)return;if(mounted==null){p.setYRot(p.getYRot()+dx);p.setXRot(Math.max(-85,Math.min(85,p.getXRot()+dy)));}else{yaw=net.minecraft.util.Mth.wrapDegrees(yaw+dx);pitch=Math.max(-85,Math.min(85,pitch+dy));dirty=true;}}
    public static void tick(Minecraft mc){
        if(mounted==null)return;
        if(mc.level!=mountedLevel||mc.player==null||mc.player.distanceToSqr(mounted.getX()+.5,mounted.getY()+.5,mounted.getZ()+.5)>1024||!(mc.level.getBlockEntity(mounted) instanceof com.nobothehobo.candid.block.TripodBlockEntity b)||b.camera().isEmpty()){clearMount();return;}
        if(dirty){dirty=false;
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.nobothehobo.candid.network.CameraActionPayload(com.nobothehobo.candid.network.CameraActionPayload.AIM_YAW,Math.round(yaw*100)));
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.nobothehobo.candid.network.CameraActionPayload(com.nobothehobo.candid.network.CameraActionPayload.AIM_PITCH,Math.round(pitch*100)));
        }
    }
    public static ItemStack camera(){var p=Minecraft.getInstance().player;if(p==null)return ItemStack.EMPTY;if(mounted!=null&&p.level().getBlockEntity(mounted) instanceof com.nobothehobo.candid.block.TripodBlockEntity b)return b.camera();return p.getMainHandItem().is(CandidItems.CAMERA)?p.getMainHandItem():p.getOffhandItem().is(CandidItems.CAMERA)?p.getOffhandItem():ItemStack.EMPTY;}
    public static boolean hideHud(){var screen=Minecraft.getInstance().screen;
        return screen instanceof CameraScreen||screen instanceof CaptureScreen||screen instanceof FilmLoadScreen||screen instanceof FilmUnloadScreen||screen instanceof CameraRaiseScreen||PhotoCapture.busy();
    }
    public static boolean active(){var mc=Minecraft.getInstance();return (mc.screen instanceof CameraScreen||mc.screen instanceof CaptureScreen||PhotoCapture.busy())&&!camera().isEmpty();}
    public static Vec3 anchor(){var p=Minecraft.getInstance().player;if(p==null)return Vec3.ZERO;var pos=CameraData.tripod(camera(),p);return pos==null?p.getEyePosition():new Vec3(pos.getX()+.5,pos.getY()+1.62,pos.getZ()+.5).add(direction().scale(.28));}
    public static void focus(int direction){
        var c=camera();if(c.isEmpty())return;
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.nobothehobo.candid.network.CameraActionPayload(com.nobothehobo.candid.network.CameraActionPayload.FOCUS,CameraData.focusIndex(c)+direction));
    }
}
