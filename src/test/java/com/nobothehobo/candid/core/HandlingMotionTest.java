package com.nobothehobo.candid.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HandlingMotionTest {
    @Test void doorOpensAndClosesContinuously(){
        assertEquals(0,HandlingMotion.backAngle(0,false));assertEquals(95,HandlingMotion.backAngle(16,false));assertEquals(0,HandlingMotion.backAngle(63,false));
        assertTrue(HandlingMotion.backAngle(8.5f,false)>HandlingMotion.backAngle(8,false));
        assertTrue(HandlingMotion.backAngle(8.5f,false)<HandlingMotion.backAngle(9,false));
    }
    @Test void rewindThenOpen(){assertEquals(0,HandlingMotion.backAngle(30,true));assertEquals(95,HandlingMotion.backAngle(40,true));assertEquals(0,HandlingMotion.backAngle(62,true));}
    @Test void leaderExtendsAndRewinds(){assertEquals(0,HandlingMotion.leader(32,false));assertEquals(1,HandlingMotion.leader(50,false));assertEquals(1,HandlingMotion.leader(10,true));assertEquals(0,HandlingMotion.leader(32,true));}
}
