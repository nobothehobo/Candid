package com.nobothehobo.candid.client;

import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Recipe pages read the shipped recipe JSON, so ingredient changes do not leave stale diagrams. */
public final class GuideScreen extends Screen {
    private int page;private boolean primed;private final boolean[] pad=new boolean[15];
    private static final String[] RECIPES={"camera","film_daylight_100","film_sun_200","film_everyday_400","film_portrait_400","film_night_800","film_mono_400","film_fine_mono_400","darkroom_basin","developer","enlarger","photo_paper","guide"};
    private static final String[] TITLES={"Welcome to Candid","Camera controls","Reading the light","Film character","Your darkroom"};
    private static final String[] TEXT={
        "Craft a camera and one film roll. Every roll holds 36 exposures. Crouch + Use opens the camera controls. Select FILM, choose a stock and operate it. Watch the cartridge load, the back close and the first frame wind. Use normally to enter the viewfinder.",
        "Viewfinder: arrows or D-pad adjust aperture and shutter. Enter / A fires. R / X winds. C / Y opens controls. Escape / B closes. Aim with the right stick, or hold right mouse and drag. In body controls: Y / U or the button rewinds and unloads film.",
        "The bright 3:2 frame is the photograph. ISO comes from the film. Smaller f-number or slower shutter gives more exposure. Aim the meter near zero. Minus means darker; plus means brighter. It samples the scene ahead, including shade, lamps, sky and weather.",
        "Golden 200: warm everyday color. Everyday 400: punchier color. Portrait 400/800: gentler color and contrast; 800 is useful in dim light. Vivid 100: fine grain. Classic Mono: stronger B&W grain; Fine Mono: smoother B&W. These are inspired looks, not exact Kodak emulations.",
        "Rewind and unload full or partial rolls. Partial rolls can be reloaded without losing frames. Use an exposed roll on the Darkroom Basin with one Developer. After 20 seconds, use the roll to open its contact sheet. Click a frame to print for one Photo Paper. Print again anytime; display prints in item frames."
    };
    public GuideScreen(){super(Component.literal("Candid Field Guide"));}
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.literal("Previous"),b->move(-1)).bounds(width/2-124,height-28,76,20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(width/2-38,height-28,76,20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"),b->move(1)).bounds(width/2+48,height-28,76,20).build());
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        poll();int left=width/2-146;g.fill(0,0,width,height,0xEB0D1216);g.fill(left,10,left+292,height-36,0xFFF0E6CF);
        g.drawCenteredString(font,"CANDID • FIELD GUIDE",width/2,20,0xFF342D26);
        g.drawCenteredString(font,(page+1)+" / "+(TITLES.length+RECIPES.length),width/2,height-48,0xFF655440);
        if(page<TITLES.length){g.drawCenteredString(font,TITLES[page],width/2,40,0xFF785733);text(g,TEXT[page],left+16,62,260);}
        else recipe(g,RECIPES[page-TITLES.length],left+16,44);
        super.render(g,mx,my,delta);
    }
    private void text(GuiGraphics g,String text,int x,int y,int wrap){for(var line:font.split(Component.literal(text),wrap)){g.drawString(font,line,x,y,0xFF41382C,false);y+=11;}}
    private void recipe(GuiGraphics g,String id,int x,int y){
        try(InputStream stream=GuideScreen.class.getResourceAsStream("/data/candid/recipe/"+id+".json")){
            if(stream==null)throw new IOException("Recipe missing");var data=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            var result=data.getAsJsonObject("result");var output=stack(result.get("id").getAsString());
            g.drawCenteredString(font,output.getHoverName().getString()+" × "+result.get("count").getAsInt(),width/2,y,0xFF785733);
            List<String> slots=new ArrayList<>(Collections.nCopies(9,""));
            if(data.has("pattern")){var pattern=data.getAsJsonArray("pattern");var key=data.getAsJsonObject("key");for(int r=0;r<pattern.size();r++){String row=pattern.get(r).getAsString();for(int c=0;c<row.length();c++)if(row.charAt(c)!=' ')slots.set(r*3+c,key.get(""+row.charAt(c)).getAsString());}}
            else {var ingredients=data.getAsJsonArray("ingredients");for(int i=0;i<ingredients.size();i++)slots.set(i,ingredients.get(i).getAsString());}
            for(int i=0;i<9;i++){int px=x+(i%3)*20,py=y+22+(i/3)*20;g.fill(px-1,py-1,px+18,py+18,0x664B3C2C);if(!slots.get(i).isEmpty())g.renderItem(stack(slots.get(i)),px,py);}
            g.drawString(font,"→",x+69,y+44,0xFF41382C,false);g.renderItem(output,x+88,y+42);
            Map<String,Integer> totals=new LinkedHashMap<>();for(String ingredient:slots)if(!ingredient.isEmpty())totals.merge(ingredient,1,Integer::sum);
            int line=y+23;for(var ingredient:totals.entrySet()){text(g,ingredient.getValue()+" × "+stack(ingredient.getKey()).getHoverName().getString(),x+120,line,140);line+=22;}
            String note=data.has("pattern")?"Place ingredients as shown.":"Shapeless: any arrangement works.";
            if(id.startsWith("film_"))note+=" One complete 36-shot roll.";
            text(g,note,x,y+92,260);
        }catch(Exception e){text(g,"Recipe unavailable; check your installed data pack.",x,y+20,250);}
    }
    private static ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(id)));}
    private void move(int direction){page=Math.floorMod(page+direction,TITLES.length+RECIPES.length);}
    @Override public boolean keyPressed(KeyEvent e){if(e.key()==GLFW.GLFW_KEY_LEFT){move(-1);return true;}if(e.key()==GLFW.GLFW_KEY_RIGHT){move(1);return true;}return super.keyPressed(e);}
    private void poll(){if(!GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1))return;try(var s=GLFWGamepadState.calloc()){if(!GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1,s))return;
        if(!primed){for(int i=0;i<pad.length;i++)pad[i]=s.buttons(i)==GLFW.GLFW_PRESS;primed=true;return;}
        edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT,()->move(-1));edge(s,GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT,()->move(1));edge(s,GLFW.GLFW_GAMEPAD_BUTTON_B,this::onClose);
    }}
    private void edge(GLFWGamepadState s,int b,Runnable action){boolean now=s.buttons(b)==GLFW.GLFW_PRESS;if(now&&!pad[b])action.run();pad[b]=now;}
}
