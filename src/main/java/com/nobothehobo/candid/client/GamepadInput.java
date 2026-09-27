package com.nobothehobo.candid.client;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

/** Small input boundary so SDL and GLFW releases share the same camera controls. */
public final class GamepadInput {
    public static final int A=0,B=1,X=2,Y=3,LEFT_BUMPER=4,RIGHT_BUMPER=5,
        BACK=6,START=7,GUIDE=8,LEFT_THUMB=9,RIGHT_THUMB=10,
        DPAD_UP=11,DPAD_RIGHT=12,DPAD_DOWN=13,DPAD_LEFT=14,RIGHT_X=2,RIGHT_Y=3;
    private GamepadInput(){}
    public static boolean present(){return GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1);}
    public static State read(){
        byte[] buttons=new byte[15];float[] axes=new float[6];
        try(var nativeState=GLFWGamepadState.calloc()){
            boolean connected=GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1,nativeState);
            if(connected){for(int i=0;i<buttons.length;i++)buttons[i]=nativeState.buttons(i);for(int i=0;i<axes.length;i++)axes[i]=nativeState.axes(i);}
            return new State(connected,buttons,axes);
        }
    }
    public static void shutdown(){}
    public record State(boolean connected, byte[] buttons, float[] axes) implements AutoCloseable {
        public int buttons(int button){return buttons[button];}
        public float axes(int axis){return axes[axis];}
        @Override public void close(){}
    }
}
