package com.nobothehobo.candid.block;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

public final class TripodBlock extends Block {
    public TripodBlock(BlockBehaviour.Properties p){super(p);}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(2,0,2,14,22,14);}
    @Override protected InteractionResult useItemOn(ItemStack camera,BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!camera.is(CandidItems.CAMERA))return InteractionResult.PASS;
        if(player instanceof net.minecraft.server.level.ServerPlayer p){
            com.nobothehobo.candid.photo.RollManager.safely(p,()->{com.nobothehobo.candid.photo.RollManager.sync(p,camera);CameraData.mount(camera,pos);});
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Tripod attached • Use camera to aim • controls to detach"),true);
        }
        return InteractionResult.SUCCESS;
    }
}
