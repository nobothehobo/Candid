package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.content.*;
import com.nobothehobo.candid.core.RollState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import java.util.*;

/** A supervised work tray. Closing returns all real inputs, including a processing roll. */
public final class DarkroomMenu extends AbstractContainerMenu {
    private final ServerPlayer owner;
    private final BlockPos position;
    private final boolean enlarger;
    private final SimpleContainer tray = new SimpleContainer(54);
    private int selected;
    private long refreshed;
    private boolean returned;

    private DarkroomMenu(int id, ServerPlayer player, BlockPos pos, boolean enlarger) {
        super(MenuType.GENERIC_9x6, id);
        this.owner = player; this.position = pos.immutable(); this.enlarger = enlarger;
        for (int i=0;i<54;i++) {
            final int index=i;
            addSlot(new Slot(tray,i,8+(i%9)*18,18+(i/9)*18) {
                @Override public boolean mayPlace(ItemStack s) {
                    return index==0 ? CandidItems.stockFor(s.getItem())!=null && s.getCount()==1
                        : index==1 && s.is(enlarger?CandidItems.PHOTO_PAPER:CandidItems.DEVELOPER);
                }
                @Override public boolean mayPickup(Player p) { return index<2; }
                @Override public int getMaxStackSize() { return index==0?1:64; }
            });
        }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(player.getInventory(),col+row*9+9,8+col*18,140+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(player.getInventory(),col,8+col*18,198));
        refresh();
    }
    public static void open(ServerPlayer player, BlockPos pos, boolean enlarger) {
        player.openMenu(new SimpleMenuProvider((id,inventory,p)->new DarkroomMenu(id,player,pos,enlarger),
            Component.literal(enlarger?"Enlarger • Film / Paper":"Developing Tank • Film / Chemistry")));
    }
    @Override public boolean stillValid(Player p) {
        return p==owner && p.isAlive() && p.distanceToSqr(position.getX()+.5,position.getY()+.5,position.getZ()+.5)<64
            && p.level().getBlockState(position).is(enlarger?CandidBlocks.ENLARGER:CandidBlocks.DARKROOM_BASIN);
    }
    @Override public void removed(Player p) {
        super.removed(p);
        if(!returned){returned=true;for(int i=0;i<2;i++){var s=tray.removeItemNoUpdate(i);if(!s.isEmpty())RollManager.give(owner,s);}}
    }
    @Override public ItemStack quickMoveStack(Player p,int index) {
        if(index<0||index>=slots.size()||index>=2&&index<54)return ItemStack.EMPTY;
        Slot slot=slots.get(index);ItemStack s=slot.getItem();if(s.isEmpty())return ItemStack.EMPTY;
        ItemStack old=s.copy();
        if(index<2){if(!moveItemStackTo(s,54,90,true))return ItemStack.EMPTY;}
        else if(CandidItems.stockFor(s.getItem())!=null){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}
        else if(s.is(enlarger?CandidItems.PHOTO_PAPER:CandidItems.DEVELOPER)){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}
        else return ItemStack.EMPTY;
        if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();refresh();return old;
    }
    @Override public void clicked(int index,int button,ClickType type,Player p) {
        if(!stillValid(p)){owner.closeContainer();return;}
        if(index>=2&&index<54){
            if(type!=ClickType.PICKUP||button!=0)return;
            RollManager.safely(owner,()->{
                var roll=roll();
                if(enlarger){
                    if(index>=9&&index<45){selected=index-9;Photos.preview(owner,roll,selected);}
                    else if(index==49)Photos.print(owner,roll,selected,tray.getItem(1),false);
                    else if(index==50)Photos.print(owner,roll,selected,tray.getItem(1),true);
                }else if(index==49){
                    if(roll.stage()==RollState.Stage.EXPOSED){
                        var next=roll.develop(System.currentTimeMillis(),20_000);
                        if(!tray.getItem(1).is(CandidItems.DEVELOPER))throw new IllegalStateException("Place Developer Chemistry in the second slot");
                        RollManager.store(owner).put(next);tray.getItem(1).shrink(1);tray.setItem(0,RollManager.item(next));
                    }
                }
                refresh();
            });
        }else{super.clicked(index,button,type,p);refresh();}
        broadcastChanges();
    }
    private RollState roll(){
        var s=tray.getItem(0);UUID id=RollManager.token(s);
        if(CandidItems.stockFor(s.getItem())==null||id==null)throw new IllegalStateException("Insert an unloaded roll with exposed frames in the first slot");
        var r=RollManager.store(owner).get(id);
        if(r.camera()!=null)throw new IllegalStateException("This roll is loaded in a camera");
        if(r.stage()==RollState.Stage.DEVELOPING && System.currentTimeMillis()>=r.readyAt()){
            r=r.finish(System.currentTimeMillis());RollManager.store(owner).put(r);tray.setItem(0,RollManager.item(r));
        }
        return r;
    }
    @Override public void broadcastChanges(){
        if(System.currentTimeMillis()-refreshed>500)refresh();
        super.broadcastChanges();
    }
    private void refresh(){
        refreshed=System.currentTimeMillis();
        for(int i=2;i<54;i++)tray.setItem(i,ItemStack.EMPTY);
        tray.setItem(4,label(Items.BOOK,enlarger?"Insert film + paper • select a frame to preview":"Insert exposed film + developer • click Start",
            "Closing returns your supplies. Processing continues on the roll."));
        try {
            var r=roll();
            if(enlarger){
                if(r.stage()!=RollState.Stage.DEVELOPED)throw new IllegalStateException("Develop this roll before enlarging");
                for(int i=0;i<r.frames().size();i++){
                    var f=r.frames().get(i);
                    tray.setItem(9+i,label(Items.PAPER,(i==selected?"Selected • ":"")+"Frame "+f.number()+" • "+f.photographer(),
                        java.time.Instant.ofEpochMilli(f.timestamp()).toString(),r.stock()+" • ISO "+f.iso(),
                        "f/"+f.aperture()+" • "+com.nobothehobo.candid.data.CameraData.shutterLabel(f.shutter()),"Click: free preview"));
                }
                tray.setItem(49,label(CandidItems.PHOTO_PAPER,"Print selected • 1 sheet","One map, cream mat, fits an item frame"));
                tray.setItem(50,label(Items.ITEM_FRAME,"Large matted print • 1 sheet","Four labeled maps: arrange in a 2 × 2 square","New negatives contain twice the image resolution"));
            }else{
                String status=switch(r.stage()){
                    case EXPOSED -> "Start development • 1 Developer • 20 seconds";
                    case DEVELOPING -> "Developing • "+Math.max(0,(r.readyAt()-System.currentTimeMillis()+999)/1000)+" seconds";
                    case DEVELOPED -> "Ready • take your developed roll to the enlarger";
                };
                tray.setItem(49,label(r.stage()==RollState.Stage.DEVELOPED?Items.LIME_DYE:Items.CLOCK,status,r.used()+" exposed frames"));
            }
        }catch(IllegalArgumentException|IllegalStateException e){tray.setItem(49,label(Items.GRAY_DYE,e.getMessage()));}
    }
    public static ItemStack label(Item item,String name,String... lore){
        var s=new ItemStack(item);s.set(DataComponents.CUSTOM_NAME,Component.literal(name));
        s.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(Arrays.stream(lore).map(Component::literal).map(c->(Component)c).toList()));return s;
    }
}
