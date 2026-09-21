package com.nobothehobo.candid.core;

/** Depth-aware sparse lens blur. Edges retain foreground coverage; large apertures soften distant subjects. */
public final class DepthOfField {
    private DepthOfField(){}
    public static int[] apply(int[] rgb,float[] depth,int width,int height,int lens,double aperture,double focus){
        if(rgb.length!=width*height||depth.length!=32*21)throw new IllegalArgumentException("Invalid depth image");
        int[] out=rgb.clone();
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            double z=depth[(y*21/height)*32+x*32/width];int radius=(int)Math.round(Optics.blurRadius(lens,aperture,focus,z,width));if(radius<1)continue;
            int r=0,g=0,b=0,n=0;
            for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++){
                if(dx*dx+dy*dy>5)continue;
                int sx=Math.max(0,Math.min(width-1,x+dx*radius/2)),sy=Math.max(0,Math.min(height-1,y+dy*radius/2));
                double other=depth[(sy*21/height)*32+sx*32/width];
                if(other<z*.75)continue;
                int c=rgb[sy*width+sx];r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;n++;
            }
            if(n>0)out[y*width+x]=(r/n<<16)|(g/n<<8)|b/n;
        }
        return out;
    }
}
