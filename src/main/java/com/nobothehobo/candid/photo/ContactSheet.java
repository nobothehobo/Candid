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
        if(type!=ClickType.PICKUP||slot<0||slot>=36)return;
        RollManager.safely(owner,()->{
            var roll=RollManager.store(owner).get(rollId);
            if(button==1)Photos.preview(owner,roll,slot);
            else if(button==0){int paper=RollManager.find(owner,CandidItems.PHOTO_PAPER);Photos.print(owner,roll,slot,paper<0?ItemStack.EMPTY:owner.getInventory().getItem(paper),false);}
            refresh();broadcastChanges();
        });
    }
    private void refresh(){
        var r=RollManager.store(owner).get(rollId);
        for(int i=0;i<r.frames().size();i++){var f=r.frames().get(i);var s=new ItemStack(Items.PAPER);s.set(DataComponents.CUSTOM_NAME,Component.literal("Frame "+f.number()+" • "+f.photographer()));
            s.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal(java.time.Instant.ofEpochMilli(f.timestamp()).toString()),Component.literal(r.stock()+" • ISO "+f.iso()),Component.literal("f/"+f.aperture()+" • 1/"+f.shutter()+String.format(Locale.ROOT," • %+.1f EV",f.offset())),Component.literal("Left: print (1 paper) • Right: free preview"))));icons.setItem(i,s);}
        var help=new ItemStack(CandidItems.PHOTO_PAPER);help.set(DataComponents.CUSTOM_NAME,Component.literal("Each print costs 1 Photo Paper. Negatives are reusable."));icons.setItem(49,help);
    }
}
