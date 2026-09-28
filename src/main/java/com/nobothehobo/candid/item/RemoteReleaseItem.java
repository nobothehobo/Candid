package com.nobothehobo.candid.item;
import com.nobothehobo.candid.photo.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
public final class RemoteReleaseItem extends Item {
    public RemoteReleaseItem(Properties properties){super(properties);}
    @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context){
        var player=context.getPlayer();if(player==null)return InteractionResult.PASS;
        return use(context.getLevel(),player,context.getHand());
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer p)RollManager.safely(p,()->TripodSessions.remote(p,p.getItemInHand(hand)));
        return InteractionResult.SUCCESS;
    }
}
