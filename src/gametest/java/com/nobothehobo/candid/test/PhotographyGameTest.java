package com.nobothehobo.candid.test;

import com.nobothehobo.candid.client.*;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.core.RollState;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import com.nobothehobo.candid.photo.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Runs the real client + integrated server, including screenshot capture and a saved-world reopen. */
public final class PhotographyGameTest implements FabricClientGameTest {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static ItemStack rollItem(ServerPlayer p,UUID id){
        for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(CandidItems.stockFor(s.getItem())!=null&&id.equals(RollManager.token(s)))return s;}throw new AssertionError("Roll item missing");
    }
    private static net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext createWorld(ClientGameTestContext context){
        try{return context.worldBuilder().create();}
        catch(AssertionError failure){
            Thread.getAllStackTraces().forEach((thread,stack)->{
                System.err.println("Candid startup diagnostic: "+thread.getName()+" "+thread.getState());
                for(var line:stack)System.err.println("  at "+line);
            });
            throw failure;
        }
    }
    @Override public void runTest(ClientGameTestContext context){
        UUID[] rollId={null};int[] mapId={-1};
        var world=createWorld(context);var save=world.getWorldSave();
        try(world){
            world.getServer().runCommand("time set noon");
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();
                p.getInventory().setItem(0,new ItemStack(CandidItems.CAMERA));
                p.getInventory().setItem(1,new ItemStack(CandidItems.FILM_SUN_200));
                p.getInventory().setItem(2,new ItemStack(CandidItems.DEVELOPER));
                p.getInventory().setItem(3,new ItemStack(CandidItems.PHOTO_PAPER,8));
            });
            context.waitFor(c->c.player!=null&&c.player.getMainHandItem().is(CandidItems.CAMERA));
            world.getServer().runCommand("execute at @p run fill ~-4 ~ ~8 ~4 ~4 ~8 minecraft:bricks");
            world.getServer().runCommand("execute at @p run fill ~-3 ~2 ~8 ~-2 ~3 ~8 minecraft:glass");
            world.getServer().runCommand("execute at @p run fill ~2 ~2 ~8 ~3 ~3 ~8 minecraft:glass");
            world.getServer().runCommand("execute at @p run fill ~ ~ ~8 ~ ~2 ~8 minecraft:dark_oak_planks");
            world.getServer().runCommand("execute at @p run fill ~-5 ~5 ~8 ~5 ~5 ~8 minecraft:stone_bricks");
            context.runOnClient(c->{c.player.setYRot(0);c.player.setXRot(0);});
            context.waitTicks(20);context.takeScreenshot("candid-camera-held");
            context.setScreen(()->new FilmLoadScreen(FilmStock.WARM_200));
            context.waitFor(c->CameraData.isWound(c.player.getMainHandItem()),400);
            context.setScreen(CameraControlScreen::new);context.waitTicks(3);context.takeScreenshot("candid-controls");
            context.setScreen(CameraScreen::new);context.waitTicks(5);context.takeScreenshot("candid-viewfinder");
            context.runOnClient(c->{check(PhotoCapture.queue(c.player.getMainHandItem(),4,3,0),"Capture queue rejected");c.setScreen(null);});
            context.waitFor(c->CameraData.frames(c.player.getMainHandItem())==35,600);
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var camera=p.getMainHandItem();rollId[0]=UUID.fromString(CameraData.rollId(camera));
                check(!CameraData.isWound(camera),"Must wind after exposure");RollManager.unload(p,camera);
                var r=RollManager.store(p).get(rollId[0]);check(r.used()==1&&r.frames().size()==1,"Frame missing");
                byte[] pixels=Base64.getDecoder().decode(r.frames().getFirst().colors());check(pixels.length==16384,"Wrong map dimensions");
                check(java.util.stream.IntStream.range(0,pixels.length).map(i->pixels[i]&255).distinct().count()>3,"Capture was blank");
                RollManager.load(p,camera,FilmStock.WARM_200.ordinal());check(CameraData.frames(camera)==35,"Partial reload reset count");RollManager.unload(p,camera);
                RollManager.develop(p,rollItem(p,rollId[0]));check(RollManager.find(p,CandidItems.DEVELOPER)<0,"Developer not consumed");
            });
            long ready=world.getServer().computeOnServer(s->RollManager.store(s.getPlayerList().getPlayers().getFirst()).get(rollId[0]).readyAt());
            context.setScreen(GuideScreen::new);
            context.takeScreenshot("candid-guide");
            context.waitFor(c->System.currentTimeMillis()>=ready,2000);
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();RollManager.open(p,rollItem(p,rollId[0]));
                check(p.containerMenu instanceof ContactSheet,"Contact sheet not opened");
                p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.containerMenu.clicked(0,0,ClickType.PICKUP,p);
                check(p.getInventory().getItem(3).getCount()==6,"Two prints must consume two paper");
                var r=RollManager.store(p).get(rollId[0]);mapId[0]=r.frames().getFirst().mapId();var map=p.level().getMapData(new MapId(mapId[0]));
                check(map!=null&&map.locked,"Print must be locked");check(Arrays.equals(map.colors,Base64.getDecoder().decode(r.frames().getFirst().colors())),"Print changed photo colors");
            });
            context.waitTicks(4);context.takeScreenshot("candid-contact-sheet");
            context.runOnClient(c->{
                c.setScreen(null);
                for(int i=0;i<9;i++)if(c.player.getInventory().getItem(i).is(Items.FILLED_MAP)){c.player.getInventory().setSelectedSlot(i);break;}
                c.player.setXRot(20);
            });
            context.waitTicks(20);context.takeScreenshot("candid-photo-print");
        }
        try(var reopened=save.open()){
            reopened.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var r=RollManager.store(p).get(rollId[0]);
                check(r.stage()==RollState.Stage.DEVELOPED&&r.used()==1,"Roll did not survive restart");check(r.frames().getFirst().mapId()==mapId[0],"Map ID not preserved");
                var map=p.level().getMapData(new MapId(mapId[0]));check(map!=null&&map.locked,"Print not saved");check(Arrays.equals(map.colors,Base64.getDecoder().decode(r.frames().getFirst().colors())),"Saved map colors changed");
            });
        }
    }
}
