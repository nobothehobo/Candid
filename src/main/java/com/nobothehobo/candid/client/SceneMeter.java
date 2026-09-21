package com.nobothehobo.candid.client;

import com.nobothehobo.candid.core.*;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Nine reflected-light samples and sun-occlusion tests at 4 Hz, on the client thread. */
public final class SceneMeter {
    private long last;private double reading=15;
    public double read(Minecraft client){
        long now=System.nanoTime();if(now-last<250_000_000)return reading;last=now;
        if(client.level==null||client.player==null)return reading;
        var level=client.level;var p=client.player;Vec3 origin=CameraOptics.anchor(),forward=p.getLookAngle();
        double yaw=Math.toRadians(p.getYRot());Vec3 right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw)),up=right.cross(forward);
        double spread=Math.tan(Math.toRadians(Optics.verticalFov(CameraData.lens(CameraOptics.camera())))/2);
        double angle=Math.floorMod(level.getDayTime(),24000)/12000.0*Math.PI;
        Vec3 sun=new Vec3(-Math.cos(angle),Math.sin(angle),0);
        List<LightMeter.Surface> samples=new ArrayList<>();
        for(int y=-1;y<=1;y++)for(int x=-1;x<=1;x++){
            Vec3 direction=forward.add(right.scale(x*spread*.9)).add(up.scale(y*spread*.6)).normalize();
            var hit=level.clip(new ClipContext(origin,origin.add(direction.scale(48)),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
            double weight=x==0&&y==0?4:1;
            if(hit.getType()==HitResult.Type.MISS){samples.add(new LightMeter.Surface(15,0,weight,.18,1,true));continue;}
            var normal=Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
            Vec3 surface=hit.getLocation().add(normal.scale(.04));BlockPos air=BlockPos.containing(surface);
            int rgb=level.getBlockState(hit.getBlockPos()).getMapColor(level,hit.getBlockPos()).col;
            double reflectance=.2126*linear((rgb>>16)&255)+.7152*linear((rgb>>8)&255)+.0722*linear(rgb&255);
            boolean lit=sun.y>0&&level.clip(new ClipContext(surface,surface.add(sun.scale(64)),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
            samples.add(new LightMeter.Surface(level.getBrightness(LightLayer.SKY,air),level.getBrightness(LightLayer.BLOCK,air),weight,reflectance,normal.dot(sun),lit));
        }
        reading=LightMeter.surfaces(samples,LightMeter.daylight(level.getDayTime(),level.isRaining(),level.isThundering()),level.dimensionType().hasSkyLight());
        return reading;
    }
    private static double linear(int channel){double c=channel/255.0;return c<=.04045?c/12.92:Math.pow((c+.055)/1.055,2.4);}
}
