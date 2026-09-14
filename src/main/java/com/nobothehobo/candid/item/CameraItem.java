package com.nobothehobo.candid.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CameraItem extends Item {
    public CameraItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Client-side Candid screens own camera interaction so controller input stays predictable.
        return InteractionResult.SUCCESS;
    }
}
