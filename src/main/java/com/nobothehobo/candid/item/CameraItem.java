package com.nobothehobo.candid.item;

import com.nobothehobo.candid.content.CandidItems;
import com.nobothehobo.candid.data.CameraData;
import com.nobothehobo.candid.film.FilmStock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CameraItem extends Item {
    public CameraItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack camera = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        FilmStock loaded = CameraData.film(camera);
        if (loaded != null && CameraData.frames(camera) > 0) {
            player.displayClientMessage(Component.literal(loaded.displayName() + " • " + CameraData.frames(camera) + "/36 exposures remaining"), true);
            return InteractionResult.SUCCESS;
        }

        if (loaded != null) CameraData.clearFilm(camera);
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            FilmStock stock = CandidItems.stockFor(candidate.getItem());
            if (stock != null && !candidate.isEmpty()) {
                if (!player.getAbilities().instabuild) candidate.shrink(1);
                CameraData.load(camera, stock);
                player.displayClientMessage(Component.literal("Loaded " + stock.displayName() + " • ISO " + stock.iso() + " • 36 exposures"), true);
                return InteractionResult.SUCCESS;
            }
        }

        player.displayClientMessage(Component.literal("No film roll found. Put Candid film in your inventory."), true);
        return InteractionResult.SUCCESS;
    }
}
