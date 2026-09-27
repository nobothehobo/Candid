package com.nobothehobo.candid.block;
import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.photo.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
public final class TripodBlock extends Block implements EntityBlock {
    public TripodBlock(BlockBehaviour.Properties p){super(p);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TripodBlockEntity(pos,state);}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(2,0,2,14,22,14);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){return interact(stack,l,pos,player);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){return interact(ItemStack.EMPTY,l,pos,p);}
    private InteractionResult interact(ItemStack stack,Level l,BlockPos pos,Player player){
        if(player instanceof ServerPlayer p&&l.getBlockEntity(pos) instanceof TripodBlockEntity stand)RollManager.safely(p,()->{
            if(stand.camera().isEmpty()){
                if(!stack.is(CandidItems.CAMERA))throw new IllegalStateException("Use a camera on the tripod to mount it");
                RollManager.sync(p,stack);stand.place(stack,p.getYRot(),p.getXRot());
                p.displayClientMessage(net.minecraft.network.chat.Component.literal("Camera mounted • use to aim • crouch + use to retrieve"),true);
            }else if(player.isShiftKeyDown()){RollManager.give(p,stand.take());TripodSessions.clear(p.getUUID());}
            else if(stack.is(CandidItems.REMOTE))TripodSessions.bind(p,stack,stand);
            else TripodSessions.open(p,stand,false);
        });
        return InteractionResult.SUCCESS;
    }
}
