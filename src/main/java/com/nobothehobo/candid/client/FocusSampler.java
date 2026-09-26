package com.nobothehobo.candid.client;

import com.nobothehobo.candid.core.Optics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** 32 × 21 depth samples, at most 64 rays per tick. No per-frame depth readbacks. */
final class FocusSampler {
    private final Vec3 origin,forward,right,up;
    private final double tangent;
    private final float[] depth=new float[32*21];
    private int next;
    FocusSampler(Minecraft mc,int lens){
        origin=CameraOptics.anchor();forward=mc.player.getLookAngle();double yaw=Math.toRadians(mc.player.getYRot());
        right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));up=right.cross(forward);tangent=Math.tan(Math.toRadians(Optics.verticalFov(lens))/2);
    }
    boolean tick(Minecraft mc){
        int end=Math.min(depth.length,next+64);
        var entities=mc.level.getEntities(mc.player,new AABB(origin,origin.add(forward.scale(64))).inflate(16)).stream().limit(32).toList();
        for(;next<end;next++){
            double x=((next%32+.5)/32*2-1)*1.5*tangent,y=(1-(next/32+.5)/21*2)*tangent;
            Vec3 direction=forward.add(right.scale(x)).add(up.scale(y)).normalize(),far=origin.add(direction.scale(96));
            var hit=mc.level.clip(new ClipContext(origin,far,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,mc.player));
            double distance=hit.getType()==HitResult.Type.MISS?1000:origin.distanceTo(hit.getLocation());
            for(var entity:entities){var point=entity.getBoundingBox().clip(origin,far);if(point.isPresent())distance=Math.min(distance,origin.distanceTo(point.get()));}
            depth[next]=(float)Math.max(.1,distance);
        }
        return next==depth.length;
    }
    float[] depths(){return depth;}
}
