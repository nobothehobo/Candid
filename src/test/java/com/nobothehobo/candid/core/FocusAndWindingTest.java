package com.nobothehobo.candid.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FocusAndWindingTest {
    @Test void standardLensReference(){
        var r=Optics.focusRange(50,8,3);
        assertEquals(2.338,r.nearMeters(),.001);
        assertEquals(4.185,r.farMeters(),.002);
        assertEquals(10.467,r.hyperfocalMeters(),.001);
        assertTrue(r.contains(3));assertFalse(r.contains(1));
    }
    @Test void stoppingDownWidensRange(){
        var wide=Optics.focusRange(50,2,3);var stopped=Optics.focusRange(50,16,3);
        assertTrue(stopped.nearMeters()<wide.nearMeters());
        assertTrue(stopped.farMeters()>wide.farMeters());
    }
    @Test void telephotoNarrowsRange(){
        var wide=Optics.focusRange(28,4,3);var tele=Optics.focusRange(90,4,3);
        assertTrue(tele.nearMeters()>wide.nearMeters());assertTrue(tele.farMeters()<wide.farMeters());
    }
    @Test void infinityStopAndHyperfocal(){
        var infinity=Optics.focusRange(50,8,1000);
        assertEquals(10.4166667,infinity.nearMeters(),.0001);
        assertEquals(Double.POSITIVE_INFINITY,infinity.farMeters());
        assertTrue(infinity.contains(Double.POSITIVE_INFINITY));
        assertEquals(Double.POSITIVE_INFINITY,Optics.focusRange(50,8,11).farMeters());
    }
    @Test void limitsMatchImageBlur(){
        var r=Optics.focusRange(50,8,3);
        double threshold=Optics.CIRCLE_OF_CONFUSION_MM*504/36/2;
        assertEquals(threshold,Optics.blurRadius(50,8,3,r.nearMeters(),504),1e-8);
        assertEquals(threshold,Optics.blurRadius(50,8,3,r.farMeters(),504),1e-8);
        assertEquals(threshold,Optics.blurRadius(50,8,1000,Optics.focusRange(50,8,1000).nearMeters(),504),1e-8);
    }
    @Test void rejectMalformedOptics(){
        assertThrows(IllegalArgumentException.class,()->Optics.focusRange(50,Double.NaN,3));
        assertThrows(IllegalArgumentException.class,()->Optics.focusRange(50,8,-1));
        assertThrows(IllegalArgumentException.class,()->Optics.focusRange(0,8,3));
        assertThrows(IllegalArgumentException.class,()->Optics.focusRange(50,8,Double.NaN));
    }
    @Test void leverStrokeAndReturn(){
        assertEquals(0,WindingMotion.angle(0));assertEquals(70,WindingMotion.angle(8));
        assertEquals(0,WindingMotion.angle(12));assertEquals(0,WindingMotion.angle(100));
        assertTrue(WindingMotion.angle(4)<WindingMotion.angle(6));
        assertTrue(WindingMotion.angle(9)>WindingMotion.angle(11));
        assertEquals(0,WindingMotion.angle(Float.NaN));
    }
}
