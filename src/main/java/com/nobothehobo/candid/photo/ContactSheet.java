package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.core.RollState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapId;
import java.util.*;

public final class ContactSheet extends ChestMenu {
    private final ServerPlayer owner;private final UUID rollId;private final SimpleContainer icons;
    private ContactSheet(int sync,ServerPlayer p,UUID roll,SimpleContainer container){super(MenuType.GENERIC_9x6,sync,p.getInventory(),container,6);owner=p;rollId=roll;icons=container;refresh();}
    public static void open(ServerPlayer p,UUID roll){p.openMenu(new SimpleMenuProvider((id,inventory,player)->new ContactSheet(id,p,roll,new SimpleContainer(54)),Component.literal("Candid • Contact Sheet")));}
    @Override public boolean stillValid(Player p){
        if(p!=owner||!p.isAlive())return false;
        for(int i=0;i<p.getInventory().getContainerSize();i++)try{var s=p.getInventory().getItem(i);if(CandidItems.stockFor(s.getItem())!=null&&rollId.equals(RollManager.token(s)))return true;}catch(IllegalArgumentException ignored){}
        return false;
    }
    @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
    @Override public void clicked(int slot,int button,ClickType type,Player p){
        if(!stillValid(p)){owner.closeContainer();return;}
        if(type!=ClickType.PICKUP||button!=0||slot<0||slot>=36)return;
        RollManager.safely(owner,()->{
            var repo=RollManager.store(owner);var roll=repo.get(rollId);int paper=RollManager.find(owner,CandidItems.PHOTO_PAPER);
            roll.canPrint(slot,paper<0?0:owner.getInventory().getItem(paper).getCount());var frame=roll.frames().get(slot);
            ItemStack print;
            if(frame.mapId()>=0&&owner.level().getMapData(new MapId(frame.mapId()))!=null){print=new ItemStack(Items.FILLED_MAP);print.set(DataComponents.MAP_ID,new MapId(frame.mapId()));}
            else {print=PhotoMaps.createPrint(owner,Base64.getDecoder().decode(frame.colors()));frame=frame.withMap(print.get(DataComponents.MAP_ID).id());roll=roll.withFrame(slot,frame);repo.put(roll);}
            print.set(DataComponents.CUSTOM_NAME,Component.literal("Candid Photograph • "+frame.photographer()+" • Frame "+frame.number()));
            PhotoMaps.markDeveloped(print);owner.getInventory().getItem(paper).shrink(1);RollManager.give(owner,print);refresh();broadcastChanges();
        });
    }
    private void refresh(){
        var r=RollManager.store(owner).get(rollId);
        for(int i=0;i<r.frames().size();i++){var f=r.frames().get(i);var s=new ItemStack(Items.PAPER);s.set(DataComponents.CUSTOM_NAME,Component.literal("Frame "+f.number()+" • "+f.photographer()));
            s.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal(java.time.Instant.ofEpochMilli(f.timestamp()).toString()),Component.literal(r.stock()+" • ISO "+f.iso()),Component.literal("f/"+f.aperture()+" • 1/"+f.shutter()+String.format(Locale.ROOT," • %+.1f EV",f.offset())),Component.literal("Click to print • 1 Candid Photo Paper"))));icons.setItem(i,s);}
        var help=new ItemStack(CandidItems.PHOTO_PAPER);help.set(DataComponents.CUSTOM_NAME,Component.literal("Each print costs 1 Photo Paper. Negatives are reusable."));icons.setItem(49,help);
    }
}
