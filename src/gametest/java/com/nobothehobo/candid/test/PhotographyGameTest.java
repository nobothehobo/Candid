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
            if("true".equals(System.getenv("CANDID_SHADER_TEST")))context.runOnClient(c->{
                try{Class<?> api=Class.forName("net.irisshaders.iris.api.v0.IrisApi");Object instance=api.getMethod("getInstance").invoke(null);check((boolean)api.getMethod("isShaderPackInUse").invoke(instance),"Iris shader pack is not active");}
                catch(ReflectiveOperationException e){throw new AssertionError("Iris integration unavailable",e);}
            });
            context.setScreen(()->new FilmLoadScreen(FilmStock.WARM_200));
            context.waitFor(c->c.screen instanceof FilmLoadScreen load&&load.animationAge()>=22,300);
            context.takeScreenshot("candid-loading-hands");
            context.waitFor(c->CameraData.isWound(c.player.getMainHandItem()),400);
            context.setScreen(CameraControlScreen::new);context.waitTicks(3);context.takeScreenshot("candid-controls");
            context.setScreen(CameraScreen::new);context.waitTicks(5);context.takeScreenshot("candid-viewfinder");
            context.runOnClient(c->{c.options.keyJump.setDown(true);c.screen.keyPressed(new net.minecraft.client.input.KeyEvent(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE,0,0));check(PhotoCapture.busy(),"Shutter did not start capture");check(c.screen instanceof CaptureScreen,"Capture must keep an input-blocking screen open");check(!c.options.keyJump.isDown(),"Shutter leaked jump input");});
            context.waitFor(c->CameraData.frames(c.player.getMainHandItem())==35,600);
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();rollId[0]=UUID.fromString(CameraData.rollId(p.getMainHandItem()));});
            context.setScreen(FilmUnloadScreen::new);
            context.runOnClient(c->c.screen.onClose());
            context.waitFor(c->CameraData.film(c.player.getMainHandItem())==null);
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var camera=p.getMainHandItem();
                check(!CameraData.isWound(camera),"Must wind after exposure");
                var r=RollManager.store(p).get(rollId[0]);check(r.used()==1&&r.frames().size()==1,"Frame missing");
                byte[] pixels=Base64.getDecoder().decode(r.frames().getFirst().colors());check(pixels.length==16384,"Wrong map dimensions");
                check(java.util.stream.IntStream.range(0,pixels.length).map(i->pixels[i]&255).distinct().count()>3,"Capture was blank");
                RollManager.load(p,camera,FilmStock.WARM_200.ordinal());check(CameraData.frames(camera)==35,"Partial reload reset count");RollManager.unload(p,camera);
                check(r.frames().getFirst().highColors()!=null,"High-resolution scan missing");
                var basin=p.blockPosition().offset(1,0,0);p.level().setBlockAndUpdate(basin,com.nobothehobo.candid.content.CandidBlocks.DARKROOM_BASIN.defaultBlockState());
                DarkroomMenu.open(p,basin,false);
                var film=rollItem(p,rollId[0]);int filmSlot=-1;for(int i=0;i<36;i++)if(p.getInventory().getItem(i)==film)filmSlot=i;
                p.containerMenu.quickMoveStack(p,filmSlot<9?81+filmSlot:54+filmSlot-9);
                p.containerMenu.quickMoveStack(p,83);
                check(!p.containerMenu.getSlot(0).getItem().isEmpty(),"Film did not enter tray");
                check(p.containerMenu.getSlot(1).getItem().is(CandidItems.DEVELOPER),"Developer did not enter tray");
                p.containerMenu.clicked(49,0,ClickType.PICKUP,p);
                check(p.containerMenu.getSlot(1).getItem().isEmpty(),"Developer not consumed by tank");
                p.containerMenu.clicked(49,0,ClickType.PICKUP,p);
                check(RollManager.store(p).get(rollId[0]).stage()==RollState.Stage.DEVELOPING,"Tank did not start");
            });
            context.waitTicks(5);context.takeScreenshot("candid-tank-menu");
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.closeContainer();
                check(rollItem(p,rollId[0])!=null,"Closing tank lost film");
            });
            var scanFuture=world.getServer().computeOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();return RollManager.store(p).readScan(RollManager.store(p).get(rollId[0]).frames().getFirst().id());});
            byte[] png=scanFuture.join();check(png.length>0,"Full-color scan not saved");
            try{var image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));check(image.getWidth()==504&&image.getHeight()==336,"Wrong full-color scan resolution");
                if("true".equals(System.getenv("CANDID_SHADER_TEST"))){long r=0,g=0,b=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;}check(r>3*g&&r>3*b,"Shader result was not preserved in the scan");}
            }catch(java.io.IOException e){throw new AssertionError("Unreadable exported scan",e);}
            long ready=world.getServer().computeOnServer(s->RollManager.store(s.getPlayerList().getPlayers().getFirst()).get(rollId[0]).readyAt());
            context.setScreen(GuideScreen::new);
            context.takeScreenshot("candid-guide");
            context.waitFor(c->System.currentTimeMillis()>=ready,2000);
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();RollManager.open(p,rollItem(p,rollId[0]));
                check(p.containerMenu instanceof ContactSheet,"Contact sheet not opened");
                p.containerMenu.clicked(0,1,ClickType.PICKUP,p);
                check(RollManager.store(p).get(rollId[0]).frames().getFirst().mapId()==-1,"Preview allocated map IDs");
                check(p.getInventory().getItem(3).getCount()==8,"Preview consumed paper");
            });
            context.waitFor(c->c.screen instanceof NegativePreviewScreen);context.takeScreenshot("candid-free-preview");
            java.util.concurrent.atomic.AtomicReference<java.util.concurrent.CompletableFuture<java.nio.file.Path>> exported=new java.util.concurrent.atomic.AtomicReference<>();
            context.runOnClient(c->exported.set(((NegativePreviewScreen)c.screen).exportScan(false)));
            try{check(Arrays.equals(png,java.nio.file.Files.readAllBytes(exported.get().join())),"PNG sharing export changed the scan");}catch(java.io.IOException e){throw new AssertionError(e);}
            context.runOnClient(c->c.screen.onClose());
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();
                p.containerMenu.clicked(0,0,ClickType.PICKUP,p);p.containerMenu.clicked(0,0,ClickType.PICKUP,p);
                check(p.getInventory().getItem(3).getCount()==6,"Two prints must consume two paper");
                var r=RollManager.store(p).get(rollId[0]);mapId[0]=r.frames().getFirst().mapId();var map=p.level().getMapData(new MapId(mapId[0]));
                check(map!=null&&map.locked,"Print must be locked");check(Arrays.equals(map.colors,Base64.getDecoder().decode(r.frames().getFirst().colors())),"Print changed photo colors");
            });
            context.waitTicks(4);context.takeScreenshot("candid-contact-sheet");
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();
                var station=p.blockPosition().offset(-1,0,0);p.level().setBlockAndUpdate(station,com.nobothehobo.candid.content.CandidBlocks.ENLARGER.defaultBlockState());
                DarkroomMenu.open(p,station,true);var film=rollItem(p,rollId[0]);int slot=-1;
                for(int i=0;i<36;i++)if(p.getInventory().getItem(i)==film)slot=i;
                p.containerMenu.quickMoveStack(p,slot<9?81+slot:54+slot-9);p.containerMenu.quickMoveStack(p,84);
                p.containerMenu.clicked(50,0,ClickType.PICKUP,p);
                var ids=RollManager.store(p).get(rollId[0]).frames().getFirst().tiles();check(ids!=null&&ids.size()==4,"Large print tiles missing");
                p.containerMenu.clicked(50,0,ClickType.PICKUP,p);
                check(ids.equals(RollManager.store(p).get(rollId[0]).frames().getFirst().tiles()),"Repeat large print leaked map IDs");
                check(p.containerMenu.getSlot(1).getItem().getCount()==4,"Large prints consumed wrong paper amount");
            });
            context.waitTicks(5);context.takeScreenshot("candid-enlarger-menu");
            world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().closeContainer());
            context.runOnClient(c->{
                c.setScreen(null);
                for(int i=0;i<9;i++)if(c.player.getInventory().getItem(i).is(Items.FILLED_MAP)){c.player.getInventory().setSelectedSlot(i);break;}
                c.player.setXRot(20);
            });
            context.waitTicks(20);context.takeScreenshot("candid-photo-print");
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(CandidItems.CAMERA));
                RollManager.sync(p,p.getMainHandItem());RollManager.give(p,new ItemStack(CandidItems.LENS_90));
                RollManager.swapLens(p,p.getMainHandItem(),3);check(CameraData.lens(p.getMainHandItem())==90,"Telephoto lens did not attach");
                check(RollManager.find(p,CandidItems.LENS_35)>=0,"Previous lens not returned");
                RollManager.swapLens(p,p.getMainHandItem(),1);check(CameraData.lens(p.getMainHandItem())==35,"Standard lens did not reattach");
                RollManager.give(p,new ItemStack(CandidItems.FILM_SUN_200));RollManager.load(p,p.getMainHandItem(),FilmStock.WARM_200.ordinal());CameraData.wind(p.getMainHandItem());
                CameraData.setShutterIndex(p.getMainHandItem(),10);
                var stand=p.blockPosition().offset(0,0,1);p.level().setBlockAndUpdate(stand,com.nobothehobo.candid.content.CandidBlocks.TRIPOD.defaultBlockState());CameraData.mount(p.getMainHandItem(),stand);
            });
            context.runOnClient(c->c.player.getInventory().setSelectedSlot(0));
            context.waitFor(c->CameraData.tripod(c.player.getMainHandItem(),c.player)!=null&&CameraData.isWound(c.player.getMainHandItem()));
            context.setScreen(CameraScreen::new);context.waitTicks(10);context.takeScreenshot("candid-tripod-viewfinder");
            context.runOnClient(c->{check(c.gameRenderer.getMainCamera().getPosition().distanceTo(CameraOptics.anchor())<.05,"Tripod viewpoint did not move to head");});
            double[] bright={0},dark={0};
            world.getServer().runCommand("time set noon");context.waitTicks(30);context.runOnClient(c->bright[0]=new SceneMeter().read(c));
            world.getServer().runCommand("time set midnight");context.waitTicks(30);context.runOnClient(c->dark[0]=new SceneMeter().read(c));
            check(bright[0]-dark[0]>7,"Live meter did not respond to day/night sunlight");
            long start=System.currentTimeMillis();
            context.runOnClient(c->check(PhotoCapture.queue(c.player.getMainHandItem(),4,10,0),"Tripod long exposure rejected"));
            context.waitFor(c->CameraData.frames(c.player.getMainHandItem())==35,1000);
            check(System.currentTimeMillis()-start>=1000,"Long exposure did not wait for actual scene samples");
            context.runOnClient(c->c.player.closeContainer());

        }
        try(var reopened=save.open()){
            reopened.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var r=RollManager.store(p).get(rollId[0]);
                check(r.stage()==RollState.Stage.DEVELOPED&&r.used()==1,"Roll did not survive restart");check(r.frames().getFirst().mapId()==mapId[0],"Map ID not preserved");
                check(r.frames().getFirst().highColors()!=null&&r.frames().getFirst().tiles().size()==4,"Large negative or tile IDs not saved");
                for(int tile:r.frames().getFirst().tiles())check(p.level().getMapData(new MapId(tile))!=null,"Large print tile missing after reopen");
                var map=p.level().getMapData(new MapId(mapId[0]));check(map!=null&&map.locked,"Print not saved");check(Arrays.equals(map.colors,Base64.getDecoder().decode(r.frames().getFirst().colors())),"Saved map colors changed");
            });
        }
    }
}
