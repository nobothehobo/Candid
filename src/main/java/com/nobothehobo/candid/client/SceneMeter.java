package com.nobothehobo.candid.client;

import com.nobothehobo.candid.core.LightMeter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Nine center-weighted rays; no chunk loading, no framebuffer reads, refreshed at 4 Hz. */
public final class SceneMeter {
    private long last;private double reading=15;
    public double read(Minecraft client){
        long now=System.nanoTime();if(now-last<250_000_000)return reading;last=now;
        if(client.level==null||client.player==null)return reading;
        var level=client.level;var p=client.player;Vec3 origin=p.getEyePosition(),forward=p.getLookAngle();
        double yaw=Math.toRadians(p.getYRot());Vec3 right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));Vec3 up=right.cross(forward);
        List<LightMeter.Sample> samples=new ArrayList<>();
        for(int y=-1;y<=1;y++)for(int x=-1;x<=1;x++){
            Vec3 direction=forward.add(right.scale(x*.20)).add(up.scale(y*.14)).normalize();BlockPos sample=BlockPos.containing(origin);
            for(double d=.5;d<=32;d+=.5){var next=BlockPos.containing(origin.add(direction.scale(d)));if(!level.hasChunkAt(next))break;
                if(!level.getBlockState(next).getCollisionShape(level,next).isEmpty())break;sample=next;}
            samples.add(new LightMeter.Sample(level.getBrightness(LightLayer.SKY,sample),level.getBrightness(LightLayer.BLOCK,sample),x==0&&y==0?4:1));
        }
        reading=LightMeter.estimate(samples,LightMeter.daylight(level.getDayTime(),level.isRaining(),level.isThundering()),level.dimensionType().hasSkyLight(),0);
        return reading;
    }
}
