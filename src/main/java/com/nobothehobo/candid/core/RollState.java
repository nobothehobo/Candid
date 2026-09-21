package com.nobothehobo.candid.core;

import java.util.*;

/** Immutable authoritative negative state; no Minecraft dependencies. */
public record RollState(UUID id, String stock, int used, String camera, Stage stage, long readyAt, List<Frame> frames) {
    public enum Stage { EXPOSED, DEVELOPING, DEVELOPED }
    public record Frame(UUID id, int number, String colors, String photographer, long timestamp,
                        float aperture, int shutter, int iso, double offset, int mapId) {
        public Frame {
            Objects.requireNonNull(id); Objects.requireNonNull(photographer);
            if(number<1||number>36||Base64.getDecoder().decode(colors).length!=16384||!Double.isFinite(offset))
                throw new IllegalArgumentException("Invalid negative");
        }
        public Frame withMap(int map) { return new Frame(id,number,colors,photographer,timestamp,aperture,shutter,iso,offset,map); }
    }
    public RollState {
        Objects.requireNonNull(id); Objects.requireNonNull(stock); Objects.requireNonNull(stage);
        frames=List.copyOf(frames);
        if(used<0||used>36||frames.size()>used||frames.stream().map(Frame::id).distinct().count()!=frames.size())throw new IllegalArgumentException("Corrupt roll");
    }
    public static RollState fresh(String stock) { return new RollState(UUID.randomUUID(),stock,0,null,Stage.EXPOSED,0,List.of()); }
    public RollState load(String target) {
        if(camera!=null||used==36||stage!=Stage.EXPOSED)throw new IllegalStateException("Roll is full, processing, developed, or already loaded");
        return new RollState(id,stock,used,target,stage,0,frames);
    }
    public RollState unload(String source) { requireCamera(source);return new RollState(id,stock,used,null,stage,0,frames); }
    public void requireCamera(String source) { if(camera==null||!camera.equals(source))throw new IllegalStateException("Film belongs to a different camera"); }
    public RollState expose(String source,Frame frame) {
        requireCamera(source);
        if(used>=36||stage!=Stage.EXPOSED||frame.number()!=used+1||frames.stream().anyMatch(f->f.id().equals(frame.id())))throw new IllegalStateException("Frame is full or already recorded");
        var next=new ArrayList<>(frames);next.add(frame);return new RollState(id,stock,used+1,camera,stage,0,next);
    }
    public RollState develop(long now,long duration) {
        if(camera!=null||frames.isEmpty()||stage!=Stage.EXPOSED)throw new IllegalStateException("Unload a roll with exposed frames first");
        return new RollState(id,stock,used,null,Stage.DEVELOPING,now+duration,frames);
    }
    public RollState finish(long now) {
        if(stage==Stage.DEVELOPED)return this;
        if(stage!=Stage.DEVELOPING||now<readyAt)throw new IllegalStateException("Film is still processing");
        return new RollState(id,stock,used,null,Stage.DEVELOPED,readyAt,frames);
    }
    public void canPrint(int index,int paper) { if(stage!=Stage.DEVELOPED||index<0||index>=frames.size()||paper<1)throw new IllegalStateException("A developed negative and one Photo Paper are required"); }
    public RollState withFrame(int index,Frame frame) {var next=new ArrayList<>(frames);next.set(index,frame);return new RollState(id,stock,used,camera,stage,readyAt,next);}
}
