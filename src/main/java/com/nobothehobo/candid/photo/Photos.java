package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.core.RollState;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.network.PreviewPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapId;
import java.util.*;

public final class Photos {
    private Photos(){}
    public static void preview(ServerPlayer p,RollState roll,int index){
        roll.canPrint(index,1);var f=roll.frames().get(index);
        var menu=p.containerMenu;var server=p.level().getServer();
        RollManager.store(p).readScan(f.id()).thenAccept(png->server.execute(()->{
            if(p.isAlive()&&p.containerMenu==menu)ServerPlayNetworking.send(p,new PreviewPayload(Base64.getDecoder().decode(f.highColors()==null?f.colors():f.highColors()),
                "Frame "+f.number()+" • "+f.photographer()+" • free preview",png,f.id()+".png"));
        }));
    }
    public static void print(ServerPlayer p,RollState roll,int index,ItemStack paper,boolean large){
        roll.canPrint(index,paper.is(CandidItems.PHOTO_PAPER)?paper.getCount():0);
        var f=roll.frames().get(index);List<ItemStack> prints=new ArrayList<>();
        if(large){
            byte[] source=Base64.getDecoder().decode(f.highColors()==null?f.colors():f.highColors());
            List<Integer> maps=f.tiles()==null?List.of():f.tiles();List<Integer> ids=new ArrayList<>();
            for(int tile=0;tile<4;tile++){
                byte[] colors=new byte[16384];int size=source.length==65536?256:128;
                for(int y=0;y<128;y++)for(int x=0;x<128;x++)colors[y*128+x]=source[((y+tile/2*128)*size/256)*size+(x+tile%2*128)*size/256];
                var s=map(p,colors,maps.size()==4?maps.get(tile):-1);ids.add(s.get(DataComponents.MAP_ID).id());
                s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Frame "+f.number()+" • "+(tile<2?"Top":"Bottom")+" "+(tile%2==0?"left":"right")));
                prints.add(s);
            }
            f=f.withTiles(ids);
        }else{
            var s=map(p,Base64.getDecoder().decode(f.colors()),f.mapId());f=f.withMap(s.get(DataComponents.MAP_ID).id());
            s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Candid Photograph • "+f.photographer()+" • Frame "+f.number()));prints.add(s);
        }
        RollManager.store(p).put(roll.withFrame(index,f));paper.shrink(1);
        for(var s:prints){PhotoMaps.markDeveloped(s);RollManager.give(p,s);}
    }
    private static ItemStack map(ServerPlayer p,byte[] colors,int id){
        if(id>=0&&p.level().getMapData(new MapId(id))!=null){var s=new ItemStack(Items.FILLED_MAP);s.set(DataComponents.MAP_ID,new MapId(id));return s;}
        return PhotoMaps.createPrint(p,colors);
    }
}
