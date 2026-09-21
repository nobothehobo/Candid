package com.nobothehobo.candid.client;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class CameraOptics {
    private CameraOptics(){}
    public static ItemStack camera(){var p=Minecraft.getInstance().player;if(p==null)return ItemStack.EMPTY;return p.getMainHandItem().is(CandidItems.CAMERA)?p.getMainHandItem():p.getOffhandItem().is(CandidItems.CAMERA)?p.getOffhandItem():ItemStack.EMPTY;}
    public static boolean active(){var mc=Minecraft.getInstance();return (mc.screen instanceof CameraScreen||mc.screen instanceof CaptureScreen||PhotoCapture.busy())&&!camera().isEmpty();}
    public static Vec3 anchor(){var p=Minecraft.getInstance().player;if(p==null)return Vec3.ZERO;var pos=CameraData.tripod(camera(),p);return pos==null?p.getEyePosition():new Vec3(pos.getX()+.5,pos.getY()+1.45,pos.getZ()+.5);}
    public static void focus(int direction){
        var c=camera();if(c.isEmpty())return;
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.nobothehobo.candid.network.CameraActionPayload(com.nobothehobo.candid.network.CameraActionPayload.FOCUS,CameraData.focusIndex(c)+direction));
    }
}
