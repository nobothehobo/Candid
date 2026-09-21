package com.nobothehobo.candid.core;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.photo.RollRepository;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class PhotographyTest {
    @TempDir Path directory;
    private RollState.Frame frame(int n){return new RollState.Frame(UUID.randomUUID(),n,Base64.getEncoder().encodeToString(new byte[16384]),"Noah",1,5.6f,125,200,0,-1);}
    @Test void sunny16(){assertEquals(15,Exposure.ev(new Exposure.Settings(16,125)),.04);}
    @Test void shutterStop(){assertEquals(1,Exposure.ev(new Exposure.Settings(4,250))-Exposure.ev(new Exposure.Settings(4,125)),1e-8);}
    @Test void apertureStop(){assertEquals(2,Exposure.ev(new Exposure.Settings(8,125))-Exposure.ev(new Exposure.Settings(4,125)),1e-8);}
    @Test void isoStops(){var s=new Exposure.Settings(5.6,125);assertEquals(3,Exposure.offset(15,s,800)-Exposure.offset(15,s,100),1e-8);}
    @Test void meterWeather(){assertTrue(LightMeter.daylight(6000,true,false)<LightMeter.daylight(6000,false,false));}
    @Test void meterIndoor(){assertTrue(LightMeter.estimate(List.of(new LightMeter.Sample(0,5,1)),1,true,0)<LightMeter.estimate(List.of(new LightMeter.Sample(15,0,1)),1,true,0));}
    @Test void capacity(){var r=RollState.fresh("WARM_200").load("camera");for(int i=1;i<=36;i++)r=r.expose("camera",frame(i));var full=r;assertThrows(IllegalStateException.class,()->full.expose("camera",frame(36)));}
    @Test void custody(){var r=RollState.fresh("WARM_200").load("a");assertThrows(IllegalStateException.class,()->r.load("b"));assertThrows(IllegalStateException.class,()->r.unload("b"));}
    @Test void partialUnload(){var r=RollState.fresh("WARM_200").load("a").expose("a",frame(1)).unload("a").load("b");assertEquals(1,r.used());assertEquals(1,r.frames().size());}
    @Test void repeatedFrame(){var f=frame(1);var r=RollState.fresh("WARM_200").load("a").expose("a",f);assertThrows(IllegalStateException.class,()->r.expose("a",f));}
    @Test void freshCannotDevelop(){assertThrows(IllegalStateException.class,()->RollState.fresh("WARM_200").develop(0,20000));}
    @Test void developmentOnce(){var r=RollState.fresh("WARM_200").load("a").expose("a",frame(1)).unload("a").develop(0,20);assertThrows(IllegalStateException.class,()->r.develop(0,20));assertThrows(IllegalStateException.class,()->r.finish(19));assertEquals(RollState.Stage.DEVELOPED,r.finish(20).stage());}
    @Test void printsNeedPaper(){var r=RollState.fresh("WARM_200").load("a").expose("a",frame(1)).unload("a").develop(0,20).finish(20);assertThrows(IllegalStateException.class,()->r.canPrint(0,0));assertDoesNotThrow(()->r.canPrint(0,1));assertDoesNotThrow(()->r.canPrint(0,1));assertThrows(IllegalStateException.class,()->r.canPrint(1,1));}
    @Test void roundTrip()throws Exception{var r=RollState.fresh("WARM_200").load("a").expose("a",frame(1));try(var repo=new RollRepository(directory)){repo.put(r);}try(var repo=new RollRepository(directory)){assertEquals(r,repo.get(r.id()));}}
    @Test void corruptionFailsClosed()throws Exception{Files.writeString(directory.resolve(UUID.randomUUID()+".json"),"not json");assertThrows(Exception.class,()->new RollRepository(directory));}
    @Test void snapshotsSaveInOrder()throws Exception{var r=RollState.fresh("WARM_200").load("a");try(var repo=new RollRepository(directory)){repo.put(r);repo.flush();repo.put(r.expose("a",frame(1)));}try(var repo=new RollRepository(directory)){assertEquals(1,repo.get(r.id()).used());}}
    @Test void cropIsThreeByTwo(){for(int[] s:List.of(new int[]{1280,800},new int[]{1920,1080},new int[]{3440,1440},new int[]{320,240})){var c=FrameGeometry.of(s[0],s[1]);assertEquals(1.5,(double)c.width()/c.height(),1e-8);assertTrue(c.x()>=0&&c.y()>=0);assertEquals(s[0]/2,c.x()+c.width()/2,1);}}
    @Test void exposureMonotonic(){for(var stock:FilmStock.values()){int dark=FilmSignal.process(0x808080,stock,-2,new Random(1))&255,normal=FilmSignal.process(0x808080,stock,0,new Random(1))&255,bright=FilmSignal.process(0x808080,stock,2,new Random(1))&255;assertTrue(dark<normal&&normal<bright,stock.name());}}
    @Test void monochromeNeutral(){int c=FilmSignal.process(0xcc4466,FilmStock.MONO_400,0,new Random(1));assertEquals((c>>16)&255,(c>>8)&255);assertEquals(c&255,(c>>8)&255);}
    @Test void isoFixedByStock(){assertEquals(200,FilmStock.WARM_200.iso());assertEquals(800,FilmStock.NIGHT_800.iso());}

    @Test void shadeChangesMeter(){
        var sunny=List.of(new LightMeter.Surface(15,0,1,.18,1,true));
        var shade=List.of(new LightMeter.Surface(15,0,1,.18,1,false));
        assertEquals(2,LightMeter.surfaces(sunny,1,true)-LightMeter.surfaces(shade,1,true),.01);
    }
    @Test void brightSubjectReadsHigher(){
        assertTrue(LightMeter.surfaces(List.of(new LightMeter.Surface(15,0,1,.7,1,true)),1,true)>
            LightMeter.surfaces(List.of(new LightMeter.Surface(15,0,1,.04,1,true)),1,true)+3);
    }
    @Test void dayNightMeter(){
        var scene=List.of(new LightMeter.Surface(15,0,1,.18,1,true));
        assertTrue(LightMeter.surfaces(scene,LightMeter.daylight(6000,false,false),true)-LightMeter.surfaces(scene,LightMeter.daylight(18000,false,false),true)>10);
    }
    @Test void lensesChangeAngle(){assertTrue(Optics.verticalFov(28)>Optics.verticalFov(90)*2);}
    @Test void focusPlaneSharp(){assertEquals(0,Optics.blurRadius(50,1.4,2,2,252),1e-10);}
    @Test void apertureAndFocusAffectBlur(){assertTrue(Optics.blurRadius(90,2,2,50,252)>Optics.blurRadius(90,16,2,50,252));}
    @Test void longShutterStops(){assertEquals(1,Exposure.ev(new Exposure.Settings(4,-1))-Exposure.ev(new Exposure.Settings(4,-2)),1e-10);}
    @Test void longShutterLabels(){assertEquals("4 s",Optics.label(-4));assertEquals("1/125",Optics.label(125));}
    @Test void malformedHighResolution(){assertThrows(IllegalArgumentException.class,()->frame(1).withScan("bad image"));}
    @Test void highResolutionRoundTrip()throws Exception{
        var r=RollState.fresh("WARM_200").load("a").expose("a",frame(1).withScan(Base64.getEncoder().encodeToString(new byte[65536])).withTiles(List.of(1,2,3,4)));
        try(var repo=new RollRepository(directory)){repo.put(r);}try(var repo=new RollRepository(directory)){assertEquals(r,repo.get(r.id()));}
    }
    @Test void legacyFramesStillRead(){var f=frame(1);assertNull(f.highColors());assertNull(f.tiles());assertEquals(f.id(),f.withMap(2).id());}
    @Test void focusImageKeepsDimensions(){int[] pixels=new int[252*168];Arrays.fill(pixels,0x808080);float[] depth=new float[32*21];Arrays.fill(depth,20);assertArrayEquals(pixels,DepthOfField.apply(pixels,depth,252,168,50,2,2));}
    @Test void severeExposureHasVisibleConsequence(){
        int neutral=FilmSignal.process(0x808080,FilmStock.WARM_200,0,new Random(1))&255;
        int under=FilmSignal.process(0x808080,FilmStock.WARM_200,-2,new Random(1))&255;
        int over=FilmSignal.process(0x808080,FilmStock.WARM_200,2,new Random(1))&255;
        assertTrue(neutral-under>40);assertTrue(over-neutral>40);
    }
    @Test void offPlaneDetailSoftens(){
        int[] rgb=new int[252*168];for(int i=0;i<rgb.length;i++)rgb[i]=i%2==0?0xffffff:0;
        float[] d=new float[32*21];Arrays.fill(d,10);
        assertArrayEquals(rgb,DepthOfField.apply(rgb,d,252,168,90,1.4,10));
        assertFalse(Arrays.equals(rgb,DepthOfField.apply(rgb,d,252,168,90,1.4,.7)));
    }
    @Test void scanFilesSurviveReopen()throws Exception{
        UUID id=UUID.randomUUID();byte[] data={1,2,3,4};
        try(var repo=new RollRepository(directory.resolve("rolls"))){repo.saveScan(id,data);assertArrayEquals(data,repo.readScan(id).join());}
        try(var repo=new RollRepository(directory.resolve("rolls"))){assertArrayEquals(data,repo.readScan(id).join());}
    }
    @Test void exportPathCannotEscape(){assertThrows(IllegalArgumentException.class,()->PhotoExport.write(directory,"../escape.png",new byte[24]));}
    @Test void exportIsRepeatable()throws Exception{
        byte[] bytes=new byte[24];java.nio.ByteBuffer.wrap(bytes).putLong(0x89504e470d0a1a0aL);String name=UUID.randomUUID()+".png";
        Path first=PhotoExport.write(directory,name,bytes);assertEquals(first,PhotoExport.write(directory,name,bytes));assertArrayEquals(bytes,Files.readAllBytes(first));
    }
}
