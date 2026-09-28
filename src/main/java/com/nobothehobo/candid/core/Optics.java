package com.nobothehobo.candid.core;

/** Thin-lens approximations in millimetres on a 36 × 24 mm image area. */
public final class Optics {
    public static final int[] LENSES={28,35,50,90};
    public static final double[] FOCUS={.5,.7,1,1.5,2,3,5,8,12,20,40,1000};
    /** Traditional 35 mm acceptable circle of confusion; not a hard sharp/blur boundary. */
    public static final double CIRCLE_OF_CONFUSION_MM=.03;
    public record FocusRange(double nearMeters,double farMeters,double hyperfocalMeters) {
        public boolean contains(double meters){return meters>=nearMeters&&meters<=farMeters;}
    }
    public static FocusRange focusRange(int mm,double aperture,double focusMeters){
        if(mm<10||mm>500||!Double.isFinite(aperture)||aperture<=0||Double.isNaN(focusMeters)||focusMeters*1000<=mm)
            throw new IllegalArgumentException("Invalid optics");
        double h=mm*mm/(aperture*CIRCLE_OF_CONFUSION_MM),s=focusMeters*1000;
        // 1000 is the existing item-data infinity stop, shared with image processing.
        if(focusMeters>=1000)return new FocusRange(h/1000,Double.POSITIVE_INFINITY,(h+mm)/1000);
        double near=h*s/(h+s-mm),denominator=h-s+mm;
        return new FocusRange(near/1000,denominator<=0?Double.POSITIVE_INFINITY:h*s/denominator/1000,(h+mm)/1000);
    }
    public static String distanceLabel(double meters){
        return Double.isInfinite(meters)?"infinity":String.format(java.util.Locale.ROOT,meters<10?"%.2f m":"%.1f m",meters);
    }
    private Optics(){}
    public static double verticalFov(int mm){if(mm<10||mm>500)throw new IllegalArgumentException("Invalid lens");return Math.toDegrees(2*Math.atan(12.0/mm));}
    public static double blurRadius(int mm,double aperture,double focusMeters,double subjectMeters,int width){
        if(aperture<=0||focusMeters<=0||subjectMeters<=0)throw new IllegalArgumentException("Invalid focus");
        double f=mm,s=focusMeters*1000,z=subjectMeters*1000;
        double diameter=focusMeters>=1000?f*f/(aperture*z):f*f*Math.abs(z-s)/(aperture*Math.max(f,s-f)*z);
        return Math.min(12,diameter*width/36/2);
    }
    public static double seconds(int shutter){if(shutter==0)throw new IllegalArgumentException("Invalid shutter");return shutter>0?1.0/shutter:-shutter;}
    public static String label(int shutter){return shutter>0?"1/"+shutter:Math.abs(shutter)+" s";}
}
