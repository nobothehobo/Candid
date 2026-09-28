package com.nobothehobo.candid.core;

/** One short outward thumb stroke and spring return, in client ticks. */
public final class WindingMotion {
    public static final int TICKS=12;
    private WindingMotion(){}
    public static float angle(float age){
        if(!Float.isFinite(age)||age<=0||age>=TICKS)return 0;
        float t=age<8?age/8:(TICKS-age)/4;
        return 70*t*t*(3-2*t);
    }
}
