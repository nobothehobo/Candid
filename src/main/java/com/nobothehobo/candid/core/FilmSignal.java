package com.nobothehobo.candid.core;

import com.nobothehobo.candid.film.FilmStock;
import java.util.Random;

/** Stock-inspired response in linear light, followed by restrained scan grain. */
public final class FilmSignal {
    private FilmSignal(){}
    public static int process(int rgb, FilmStock stock, double stops, Random noise){
        stops=Math.max(-8,Math.min(8,stops));double gain=Math.pow(2,stops*(stops<0?Math.max(.9,stock.underResponse()):Math.max(.82,stock.overResponse())));
        double[] c={linear((rgb>>16)&255),linear((rgb>>8)&255),linear(rgb&255)};
        for(int i=0;i<3;i++){
            double v=c[i]*gain;
            // Exposure latitude: rational shoulder keeps high values separated without clipping.
            c[i]=v/(1+c[i]*Math.max(0,gain-1));
            if(stops<0)c[i]=Math.pow(c[i],1+Math.max(0,stock.shadowToe()-1)*Math.min(1.5,-stops/3));
        }
        double luma=.2126*c[0]+.7152*c[1]+.0722*c[2];
        double[] tint={stock.red(),stock.green(),stock.blue()};
        for(int i=0;i<3;i++)c[i]=clamp(((luma+(c[i]-luma)*stock.saturation()-.18)*stock.contrast()+.18)*tint[i]);
        if(stock.monochrome()){double mono=.30*c[0]+.59*c[1]+.11*c[2];c[0]=c[1]=c[2]=mono;}
        double grain=(noise.nextDouble()-.5)*stock.grain()*(.7+.75*(1-luma))*(1+Math.max(0,-stops)*.14);
        int out=0;for(int i=0;i<3;i++){double chroma=stock.monochrome()?0:(noise.nextDouble()-.5)*stock.chromaGrain()*stock.grain();out|=(int)Math.round(255*clamp(encode(c[i])+grain+chroma))<<(16-i*8);}return out;
    }
    private static double linear(int v){double x=v/255.0;return x<=.04045?x/12.92:Math.pow((x+.055)/1.055,2.4);}
    private static double encode(double x){return x<=.0031308?x*12.92:1.055*Math.pow(x,1/2.4)-.055;}
    private static double clamp(double x){return Math.max(0,Math.min(1,x));}
}
