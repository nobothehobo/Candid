package com.nobothehobo.candid.block;

import com.nobothehobo.candid.content.CandidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Real station custody; GUI decorations are never persisted or dropped. */
public final class DevelopingTankBlockEntity extends BlockEntity {
    private final SimpleContainer inputs=new SimpleContainer(2);
    public DevelopingTankBlockEntity(BlockPos pos,BlockState state){
        super(CandidBlocks.TANK_ENTITY,pos,state);inputs.addListener(c->setChanged());
    }
    public SimpleContainer inputs(){return inputs;}
    @Override protected void saveAdditional(ValueOutput out){
        super.saveAdditional(out);
        for(int i=0;i<2;i++)if(!inputs.getItem(i).isEmpty())out.store("input"+i,ItemStack.CODEC,inputs.getItem(i));
    }
    @Override protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);
        for(int i=0;i<2;i++)inputs.setItem(i,in.read("input"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level!=null&&!level.isClientSide())for(int i=0;i<2;i++){
            var removed=inputs.removeItemNoUpdate(i);
            if(!removed.isEmpty())Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,removed);
        }
        super.preRemoveSideEffects(pos,state);
    }
}
