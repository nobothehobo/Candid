package com.nobothehobo.candid.core;

/** One geometry shared by finder and capture: centered 3:2 frame, never stretched into a square. */
public record FrameGeometry(int x,int y,int width,int height) {
    public static FrameGeometry of(int width,int height){
        int h=Math.max(2,(int)(Math.min(height*.60,width*.80/1.5)/2)*2),w=h*3/2;
        return new FrameGeometry((width-w)/2,(height-h)/2,w,h);
    }
}
