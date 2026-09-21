package com.nobothehobo.candid.core;

/** Thin-lens approximations in millimetres on a 36 × 24 mm image area. */
public final class Optics {
    public static final int[] LENSES={28,35,50,90};
    public static final double[] FOCUS={.5,.7,1,1.5,2,3,5,8,12,20,40,1000};
    private Optics(){}
    public static double verticalFov(int mm){if(mm<10||mm>500)throw new IllegalArgumentException("Invalid lens");return Math.toDegrees(2*Math.atan(12.0/mm));}
    public static double blurRadius(int mm,double aperture,double focusMeters,double subjectMeters,int width){
        if(aperture<=0||focusMeters<=0||subjectMeters<=0)throw new IllegalArgumentException("Invalid focus");
        double f=mm,s=focusMeters*1000,z=subjectMeters*1000;
        double diameter=f*f*Math.abs(z-s)/(aperture*Math.max(f,s-f)*z);
        return Math.min(12,diameter*width/36/2);
    }
    public static double seconds(int shutter){if(shutter==0)throw new IllegalArgumentException("Invalid shutter");return shutter>0?1.0/shutter:-shutter;}
    public static String label(int shutter){return shutter>0?"1/"+shutter:Math.abs(shutter)+" s";}
}
