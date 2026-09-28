package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.block.TripodBlockEntity;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.network.TripodViewPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

/** Validated, connection-local access. No chunk loading or persistent player locks. */
public final class TripodSessions {
    private record Session(BlockPos pos,String dimension,String camera,boolean remote){}
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private TripodSessions(){}
    public static void clear(UUID player){SESSIONS.remove(player);}
    public static void clear(){SESSIONS.clear();}
    public static void open(ServerPlayer p,TripodBlockEntity stand,boolean remote){
        if(stand.camera().isEmpty())throw new IllegalStateException("Place a camera on this tripod first");
        RollManager.sync(p,stand.camera());stand.changed();
        SESSIONS.put(p.getUUID(),new Session(stand.getBlockPos(),p.level().dimension().location().toString(),CameraData.cameraId(stand.camera()),remote));
        ServerPlayNetworking.send(p,new TripodViewPayload(stand.getBlockPos(),stand.yaw(),stand.pitch(),remote&&!p.isShiftKeyDown()));
    }
    public static TripodBlockEntity active(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());if(s==null)return null;
        if(!p.isAlive()||!p.level().dimension().location().toString().equals(s.dimension)||p.distanceToSqr(s.pos.getX()+.5,s.pos.getY()+.5,s.pos.getZ()+.5)>(s.remote?1024:64)||!p.level().hasChunkAt(s.pos))return null;
        if(s.remote&&!matchesRemote(p.getMainHandItem(),s)&&!matchesRemote(p.getOffhandItem(),s))return null;
        return p.level().getBlockEntity(s.pos) instanceof TripodBlockEntity b&&CameraData.cameraId(b.camera()).equals(s.camera)?b:null;
    }
    private static boolean matchesRemote(ItemStack stack,Session s){
        if(!stack.is(CandidItems.REMOTE))return false;
        var t=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        return t.getStringOr("camera","").equals(s.camera)&&t.getStringOr("dimension","").equals(s.dimension)&&t.getLongOr("position",0)==s.pos.asLong();
    }
    public static void bind(ServerPlayer p,ItemStack remote,TripodBlockEntity stand){
        RollManager.sync(p,stand.camera());stand.changed();
        CustomData.update(DataComponents.CUSTOM_DATA,remote,t->{t.putString("camera",CameraData.cameraId(stand.camera()));t.putString("dimension",p.level().dimension().location().toString());t.putLong("position",stand.getBlockPos().asLong());});
        remote.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(java.util.List.of(
            net.minecraft.network.chat.Component.literal("Paired • Use: release shutter"),
            net.minecraft.network.chat.Component.literal("Crouch + Use: compose • range 32 blocks"))));
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Cable paired • Use: fire • Crouch + Use: compose (32 blocks)"),true);
    }
    public static void remote(ServerPlayer p,ItemStack remote){
        var t=remote.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var pos=BlockPos.of(t.getLongOr("position",0));
        if(!p.level().dimension().location().toString().equals(t.getStringOr("dimension",""))||p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>1024||!p.level().hasChunkAt(pos))throw new IllegalStateException("Pair this release with a tripod camera, then stay within 32 blocks");
        if(!(p.level().getBlockEntity(pos) instanceof TripodBlockEntity b)||b.camera().isEmpty()||!CameraData.cameraId(b.camera()).equals(t.getStringOr("camera","")))throw new IllegalStateException("The paired camera has been removed");
        open(p,b,true);
    }
}
