package com.pedruhb.plogs.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import com.pedruhb.plogs.PLogs;

public class Analyzer extends LoreItem {

    public Analyzer() {
        super(new Item.Properties().tab(PLogs.ITEM_GROUP), "Right-click to show block details");
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        player.sendSystemMessage(
                Component.literal("Clicou no bloco X " + pos.getX() + " / Y" + pos.getY() + " / Z " + pos.getX()));

        return InteractionResult.SUCCESS;
    }

}