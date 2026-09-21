package com.nobothehobo.candid.item;
import com.nobothehobo.candid.photo.RollManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
public final class FilmRollItem extends Item {
    public FilmRollItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer p)RollManager.safely(p,()->RollManager.open(p,p.getItemInHand(hand)));
        return InteractionResult.SUCCESS;
    }
}
