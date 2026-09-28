package com.nobothehobo.candid.block;

import com.nobothehobo.candid.content.CandidBlocks;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The station owns the actual stack, never a copy left in the player's inventory. */
public final class TripodBlockEntity extends BlockEntity {
    private ItemStack camera=ItemStack.EMPTY;
    private float yaw,pitch;
    public TripodBlockEntity(BlockPos pos,BlockState state){super(CandidBlocks.TRIPOD_ENTITY,pos,state);}
    public ItemStack camera(){return camera;}
    public float yaw(){return yaw;}
    public float pitch(){return pitch;}
    public void place(ItemStack source,float yaw,float pitch){
        if(!camera.isEmpty()||!source.is(CandidItems.CAMERA))throw new IllegalStateException("Tripod already occupied");
        camera=source.split(1);CameraData.mount(camera,worldPosition);aim(yaw,pitch);
    }
    public ItemStack take(){var result=camera;camera=ItemStack.EMPTY;if(!result.isEmpty())CameraData.unmount(result);changed();return result;}
    public void aim(float y,float p){if(!Float.isFinite(y)||!Float.isFinite(p))return;yaw=net.minecraft.util.Mth.wrapDegrees(y);pitch=Math.max(-85,Math.min(85,p));changed();}
    public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(!camera.isEmpty())out.store("camera",ItemStack.CODEC,camera);out.putFloat("yaw",yaw);out.putFloat("pitch",pitch);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);camera=in.read("camera",ItemStack.CODEC).filter(s->s.is(CandidItems.CAMERA)).orElse(ItemStack.EMPTY);yaw=in.getFloatOr("yaw",0);pitch=in.getFloatOr("pitch",0);if(!Float.isFinite(yaw))yaw=0;if(!Float.isFinite(pitch))pitch=0;pitch=Math.max(-85,Math.min(85,pitch));}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level!=null&&!level.isClientSide()&&!camera.isEmpty()){
            var removed=take();Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+1.4,pos.getZ()+.5,removed);
        }
        super.preRemoveSideEffects(pos,state);
    }
}
