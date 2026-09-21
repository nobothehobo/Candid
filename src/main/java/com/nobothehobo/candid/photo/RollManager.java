package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.core.RollState;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.network.CapturePhotoPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import java.util.*;
import org.slf4j.*;

public final class RollManager {
    private static final Logger LOG=LoggerFactory.getLogger("Candid");
    private static final Map<MinecraftServer,RollRepository> STORES=new IdentityHashMap<>();
    private static final Map<UUID,Long> LAST_SHOT=new HashMap<>();
    public static void initialize(){
        ServerLifecycleEvents.SERVER_STARTED.register(s->{try{STORES.put(s,new RollRepository(s.getWorldPath(LevelResource.ROOT).resolve("candid/rolls")));}catch(Exception e){LOG.error("Cannot load Candid rolls; photography disabled to protect data",e);}});
        ServerTickEvents.END_SERVER_TICK.register(s->{if(s.getTickCount()%20==0){var r=STORES.get(s);if(r!=null)try{r.flush();}catch(Exception e){LOG.error("Candid storage failed",e);}}});
        ServerLifecycleEvents.SERVER_STOPPING.register(s->{var r=STORES.remove(s);if(r!=null)try{r.close();}catch(Exception e){LOG.error("Candid final save failed",e);}LAST_SHOT.clear();ScanUploads.clear();});
    }
    public static RollRepository store(ServerPlayer p){var r=STORES.get(p.level().getServer());if(r==null)throw new IllegalStateException("Photography storage unavailable");return r;}
    public interface Action {void run()throws Exception;}
    public static void safely(ServerPlayer p,Action action){try{action.run();}catch(IllegalArgumentException|IllegalStateException e){p.displayClientMessage(Component.literal(e.getMessage()==null?"Invalid camera or roll data":e.getMessage()),true);}catch(Exception e){LOG.error("Photography failed",e);p.displayClientMessage(Component.literal("Photography failed; see game log."),true);}}
    public static UUID token(ItemStack s){String id=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr("candid_roll_id","");return id.isEmpty()?null:UUID.fromString(id);}
    public static void sync(ServerPlayer p,ItemStack camera){
        boolean first=CameraData.cameraId(camera).isEmpty();CameraData.identify(camera);
        if(first&&!p.getInventory().contains(new ItemStack(CandidItems.GUIDE)))give(p,new ItemStack(CandidItems.GUIDE));
        FilmStock stock=CameraData.film(camera);
        if(stock!=null&&CameraData.rollId(camera).isEmpty()){
            // Preserve remaining frames in pre-0.4 cameras. Their old loose negatives remain usable.
            var roll=new RollState(UUID.randomUUID(),stock.name(),36-CameraData.frames(camera),CameraData.cameraId(camera),RollState.Stage.EXPOSED,0,List.of());store(p).put(roll);CameraData.bind(camera,roll);
        }
        if(!CameraData.rollId(camera).isEmpty()){var r=store(p).get(UUID.fromString(CameraData.rollId(camera)));r.requireCamera(CameraData.cameraId(camera));CameraData.bind(camera,r);}
    }
    public static void swapLens(ServerPlayer p,ItemStack camera,int index){
        if(index<0||index>3)throw new IllegalArgumentException("Unknown lens");
        int old=CameraData.lensIndex(camera);if(old==index)return;
        int slot=find(p,CandidItems.lensItem(index));
        if(slot<0&&!p.getAbilities().instabuild)throw new IllegalStateException("Craft this lens and carry it to attach it");
        if(slot>=0)p.getInventory().getItem(slot).shrink(1);
        CameraData.setLens(camera,index);give(p,new ItemStack(CandidItems.lensItem(old)));
    }
    public static void load(ServerPlayer p,ItemStack camera,int ordinal){
        sync(p,camera);if(CameraData.film(camera)!=null)throw new IllegalStateException("Rewind and unload the current roll first");
        if(ordinal<0||ordinal>=FilmStock.values().length)throw new IllegalArgumentException("Invalid film stock");FilmStock wanted=FilmStock.values()[ordinal];
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var s=p.getInventory().getItem(i);if(s.isEmpty()||CandidItems.stockFor(s.getItem())!=wanted)continue;
            UUID id=token(s);RollState r=id==null?RollState.fresh(wanted.name()):store(p).get(id);
            if(r.camera()!=null||r.stage()!=RollState.Stage.EXPOSED||r.used()==36)continue;
            if(!r.stock().equals(wanted.name()))throw new IllegalStateException("Film stock record mismatch");
            r=r.load(CameraData.cameraId(camera));store(p).put(r);CameraData.load(camera,wanted);CameraData.bind(camera,r);s.shrink(1);return;
        }
        if(p.getAbilities().instabuild){var r=RollState.fresh(wanted.name()).load(CameraData.cameraId(camera));store(p).put(r);CameraData.load(camera,wanted);CameraData.bind(camera,r);return;}
        throw new IllegalStateException("No loadable "+wanted.displayName()+" roll in your inventory");
    }
    public static void unload(ServerPlayer p,ItemStack camera){sync(p,camera);if(CameraData.film(camera)==null)throw new IllegalStateException("No film loaded");var r=store(p).get(UUID.fromString(CameraData.rollId(camera))).unload(CameraData.cameraId(camera));store(p).put(r);CameraData.clearFilm(camera);give(p,item(r));}
    public static void capture(ServerPlayer p,ItemStack camera,CapturePhotoPayload shot){
        sync(p,camera);if(!CameraData.cameraId(camera).equals(shot.cameraId())||!CameraData.rollId(camera).equals(shot.rollId()))throw new IllegalStateException("Camera changed while exposing; no frame used");
        long now=System.currentTimeMillis();if(now-LAST_SHOT.getOrDefault(p.getUUID(),0L)<700)return;
        if(!CameraData.isWound(camera)||CameraData.frames(camera)<=0||shot.colors().length!=16384)return;
        if(shot.apertureIndex()<0||shot.apertureIndex()>=CameraData.APERTURES.length||shot.shutterIndex()<0||shot.shutterIndex()>=CameraData.SHUTTERS.length||!Double.isFinite(shot.offset())||Math.abs(shot.offset())>32)return;
        if(CameraData.SHUTTERS[shot.shutterIndex()]<0&&CameraData.tripod(camera,p)==null)throw new IllegalStateException("Use a tripod for exposures of one second or longer");
        for(byte b:shot.colors())if((b&255)<4||(b&255)>247)throw new IllegalArgumentException("Invalid photo palette");
        var r=store(p).get(UUID.fromString(shot.rollId()));var stock=FilmStock.byName(r.stock());
        var frame=new RollState.Frame(UUID.fromString(shot.shotId()),r.used()+1,Base64.getEncoder().encodeToString(shot.colors()),p.getName().getString(),now,CameraData.APERTURES[shot.apertureIndex()],CameraData.SHUTTERS[shot.shutterIndex()],stock.iso(),shot.offset(),-1);
        frame=frame.withScan(ScanUploads.take(p,shot.shotId()));
        r=r.expose(CameraData.cameraId(camera),frame);store(p).put(r);CameraData.consumeFrame(camera);CameraData.bind(camera,r);LAST_SHOT.put(p.getUUID(),now);
        p.displayClientMessage(Component.literal("Frame "+r.used()+" recorded • "+(36-r.used())+" remaining • wind for next frame"),true);
    }
    public static ItemStack item(RollState r){
        FilmStock stock=FilmStock.byName(r.stock());if(stock==null)throw new IllegalStateException("Unknown film stock");var s=new ItemStack(CandidItems.itemFor(stock));
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putString("candid_roll_id",r.id().toString());t.putString("candid_stage",r.stage().name());t.putInt("candid_used",r.used());});
        s.set(DataComponents.CUSTOM_NAME,Component.literal(stock.displayName()+" • "+r.used()+"/36 • "+r.stage().name().toLowerCase(Locale.ROOT)));return s;
    }
    public static void develop(ServerPlayer p,ItemStack stack){
        if(CandidItems.stockFor(stack.getItem())==null||token(stack)==null)throw new IllegalStateException("Hold an unloaded exposed film roll");
        var r=store(p).get(token(stack));
        if(r.stage()==RollState.Stage.DEVELOPING){finish(p,r,stack);return;}
        if(r.stage()==RollState.Stage.DEVELOPED){ContactSheet.open(p,r.id());return;}
        var next=r.develop(System.currentTimeMillis(),20_000);int ingredient=find(p,CandidItems.DEVELOPER);
        if(ingredient<0)throw new IllegalStateException("One Developer Chemistry develops the entire roll");
        store(p).put(next);p.getInventory().getItem(ingredient).shrink(1);update(stack,next);p.displayClientMessage(Component.literal("Developing for 20 seconds. Keep the roll; use it when ready."),true);
    }
    private static RollState finish(ServerPlayer p,RollState r,ItemStack stack){
        long seconds=Math.max(0,(r.readyAt()-System.currentTimeMillis()+999)/1000);if(seconds>0)throw new IllegalStateException("Developing • "+seconds+" seconds remaining");
        r=r.finish(System.currentTimeMillis());store(p).put(r);update(stack,r);return r;
    }
    private static void update(ItemStack stack,RollState r){ItemStack n=item(r);stack.set(DataComponents.CUSTOM_DATA,n.get(DataComponents.CUSTOM_DATA));stack.set(DataComponents.CUSTOM_NAME,n.get(DataComponents.CUSTOM_NAME));}
    public static void open(ServerPlayer p,ItemStack s){
        UUID id=token(s);if(id==null){p.displayClientMessage(Component.literal("Fresh 36-frame film. Crouch + Use your camera to load."),true);return;}
        var r=store(p).get(id);if(r.camera()!=null)throw new IllegalStateException("This roll is loaded in a camera");
        if(r.stage()==RollState.Stage.DEVELOPING)r=finish(p,r,s);
        if(r.stage()==RollState.Stage.DEVELOPED)ContactSheet.open(p,id);else p.displayClientMessage(Component.literal(r.used()+" exposed frames. Use this roll on the Darkroom Basin with Developer Chemistry."),true);
    }
    public static int find(ServerPlayer p,net.minecraft.world.item.Item item){for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))return i;return -1;}
    public static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
}
