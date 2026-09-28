package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.block.TripodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Pick the camera above the stand's block cell, without adding an interaction entity. */
public final class TripodTarget {
    private TripodTarget(){}
    public static BlockPos find(Player player){
        if(!player.isAlive()||player.isSpectator())return null;
        Vec3 eye=player.getEyePosition(),end=eye.add(player.getViewVector(1).scale(4.5));
        var wall=player.level().clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
        double limit=wall.getType()==HitResult.Type.MISS?20.25:eye.distanceToSqr(wall.getLocation())+.01;
        var checked=new java.util.HashSet<BlockPos>();BlockPos closest=null;
        for(int step=0;step<=18;step++){
            var cell=BlockPos.containing(eye.lerp(end,step/18.0));
            for(int down=0;down<=2;down++){
                var pos=cell.below(down);if(!checked.add(pos)||!player.level().hasChunkAt(pos))continue;
                if(!(player.level().getBlockEntity(pos) instanceof TripodBlockEntity stand)||stand.camera().isEmpty())continue;
                var hit=new AABB(pos.getX()+.08,pos.getY()+1.15,pos.getZ()+.08,pos.getX()+.92,pos.getY()+1.98,pos.getZ()+.92).clip(eye,end);
                if(hit.isPresent()&&eye.distanceToSqr(hit.get())<limit){limit=eye.distanceToSqr(hit.get());closest=pos;}
            }
        }
        return closest;
    }
}
