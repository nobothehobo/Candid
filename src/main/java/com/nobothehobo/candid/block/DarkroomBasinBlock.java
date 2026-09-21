package com.nobothehobo.candid.block;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.photo.PhotoMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DarkroomBasinBlock extends Block {
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = Block.box(2, 1, 2, 14, 14.5, 14);
    public DarkroomBasinBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(CandidItems.stockFor(stack.getItem())!=null){
            if(player instanceof net.minecraft.server.level.ServerPlayer p)com.nobothehobo.candid.photo.RollManager.safely(p,()->com.nobothehobo.candid.photo.RollManager.develop(p,stack));
            return InteractionResult.SUCCESS;
        }
        if (!PhotoMaps.isExposed(stack)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        if (!player.getAbilities().instabuild && !consumeDeveloper(player)) {
            player.displayClientMessage(Component.literal("You need Developer Chemistry in your inventory."), true);
            return InteractionResult.FAIL;
        }
        if (PhotoMaps.develop(stack, serverLevel)) {
            player.displayClientMessage(Component.literal("Negative developed into a positive print."), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private boolean consumeDeveloper(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(CandidItems.DEVELOPER)) { s.shrink(1); return true; }
        }
        return false;
    }
}
