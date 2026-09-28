package com.nobothehobo.candid.core;

/** Continuous curves sampled at render partial ticks, never discrete model poses. */
public final class HandlingMotion {
    private HandlingMotion(){}
    public static float ease(float value){float t=Math.max(0,Math.min(1,value));return t*t*(3-2*t);}
    public static float backAngle(float age,boolean unloading){
        return 95*(unloading?ease((age-32)/8)*(1-ease((age-54)/8)):ease((age-2)/14)*(1-ease((age-53)/10)));
    }
    public static float leader(float age,boolean unloading){return unloading?1-ease((age-10)/22):ease((age-32)/18);}
}
