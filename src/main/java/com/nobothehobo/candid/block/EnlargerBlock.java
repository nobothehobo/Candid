package com.nobothehobo.candid.block;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.photo.PhotoMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EnlargerBlock extends Block {
    public EnlargerBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return open(player,pos);
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return open(player,pos);
    }
    private InteractionResult open(Player player,BlockPos pos) {
        if(player instanceof net.minecraft.server.level.ServerPlayer p)com.nobothehobo.candid.photo.DarkroomMenu.open(p,pos,true);
        return InteractionResult.SUCCESS;
    }
}
