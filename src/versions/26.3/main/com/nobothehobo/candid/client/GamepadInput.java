package com.nobothehobo.candid.client;

import static org.lwjgl.sdl.SDLGamepad.*;
import static org.lwjgl.sdl.SDLInit.*;
import org.lwjgl.sdl.SDLStdinc;

/** SDL owns one reference to the gamepad subsystem, released with the client. */
public final class GamepadInput {
    public static final int A=0,B=1,X=2,Y=3,LEFT_BUMPER=4,RIGHT_BUMPER=5,
        BACK=6,START=7,GUIDE=8,LEFT_THUMB=9,RIGHT_THUMB=10,
        DPAD_UP=11,DPAD_RIGHT=12,DPAD_DOWN=13,DPAD_LEFT=14,RIGHT_X=2,RIGHT_Y=3;
    private static final int[] BUTTONS={SDL_GAMEPAD_BUTTON_SOUTH,SDL_GAMEPAD_BUTTON_EAST,SDL_GAMEPAD_BUTTON_WEST,SDL_GAMEPAD_BUTTON_NORTH,SDL_GAMEPAD_BUTTON_LEFT_SHOULDER,SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER,SDL_GAMEPAD_BUTTON_BACK,SDL_GAMEPAD_BUTTON_START,SDL_GAMEPAD_BUTTON_GUIDE,SDL_GAMEPAD_BUTTON_LEFT_STICK,SDL_GAMEPAD_BUTTON_RIGHT_STICK,SDL_GAMEPAD_BUTTON_DPAD_UP,SDL_GAMEPAD_BUTTON_DPAD_RIGHT,SDL_GAMEPAD_BUTTON_DPAD_DOWN,SDL_GAMEPAD_BUTTON_DPAD_LEFT};
    private static boolean initialized;private static long handle,nextDiscovery;
    private GamepadInput(){}
    public static boolean present(){
        if(handle!=0&&SDL_GamepadConnected(handle))return true;
        if(handle!=0){SDL_CloseGamepad(handle);handle=0;}
        long now=System.nanoTime();if(now<nextDiscovery)return false;nextDiscovery=now+1_000_000_000L;
        if(!initialized){if(!SDL_InitSubSystem(SDL_INIT_GAMEPAD))return false;initialized=true;}
        var ids=SDL_GetGamepads();
        if(ids!=null){try{if(ids.hasRemaining())handle=SDL_OpenGamepad(ids.get(0));}finally{SDLStdinc.SDL_free(ids);}}
        return handle!=0;
    }
    public static State read(){
        byte[] buttons=new byte[15];float[] axes=new float[6];boolean connected=present();
        if(connected){
            SDL_UpdateGamepads();
            for(int i=0;i<buttons.length;i++)buttons[i]=(byte)(SDL_GetGamepadButton(handle,BUTTONS[i])?1:0);
            axes[RIGHT_X]=Math.max(-1,SDL_GetGamepadAxis(handle,SDL_GAMEPAD_AXIS_RIGHTX)/32767f);
            axes[RIGHT_Y]=Math.max(-1,SDL_GetGamepadAxis(handle,SDL_GAMEPAD_AXIS_RIGHTY)/32767f);
        }
        return new State(connected,buttons,axes);
    }
    public static void shutdown(){
        if(handle!=0){SDL_CloseGamepad(handle);handle=0;}
        if(initialized){SDL_QuitSubSystem(SDL_INIT_GAMEPAD);initialized=false;}
    }
    public record State(boolean connected, byte[] buttons, float[] axes) implements AutoCloseable {
        public int buttons(int button){return buttons[button];}
        public float axes(int axis){return axes[axis];}
        @Override public void close(){}
    }
}
