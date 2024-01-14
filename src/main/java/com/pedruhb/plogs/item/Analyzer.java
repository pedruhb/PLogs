package com.pedruhb.plogs.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.mojang.logging.LogUtils;
import com.pedruhb.plogs.PLogs;

import org.slf4j.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Analyzer extends LoreItem {

    public Analyzer() {
        super(new Item.Properties().tab(PLogs.ITEM_GROUP), "Right-click to show block details");
    }

    private static final Logger LOGGER = LogUtils.getLogger();

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
                Component.literal("Analyzing -> X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getX()));

        /* Block Break */
        ItemStack stack_break = new ItemStack(Items.WRITTEN_BOOK);
        stack_break.setTag(new CompoundTag());
        stack_break.getTag().putString("author", "PedruHB");
        stack_break.getTag().putString("title", "Block Break");
        ListTag book_pages_break = new ListTag();
        List<String> pages_break = Lists.newArrayList();

        try {

            PreparedStatement stmt = PLogs.connection
                    .prepareStatement("SELECT block_break.*, users.last_name FROM block_break, users WHERE users.uuid = block_break.uuid AND block_break.x = ? AND block_break.y = ? AND block_break.z = ?");
            stmt.setInt(1, pos.getX());
            stmt.setInt(2, pos.getY());
            stmt.setInt(3, pos.getZ());

            ResultSet rs;

            rs = stmt.executeQuery();

            int count = 0;

            String tmp_page =  "X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getX()+"\n";
            
            while (rs.next()) {

                Date date = new Date(rs.getInt("timestamp") * 1000L);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                tmp_page += rs.getString("last_name") + " - " + format.format(date) + " - " + rs.getString("block")+"\n";

                count++;

                if (count == 3) {
                    pages_break.add(tmp_page);
                    tmp_page = "";
                    count = 0;
                }

            }

            pages_break.add(tmp_page);
            tmp_page = "";
            count = 0;

        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("Error getting block break data."));
            LOGGER.error(e.getSQLState());
        }

        pages_break.stream()
                .map(page_break -> Component.Serializer.toJson(Component.literal(page_break)))
                .map(StringTag::valueOf).forEach(book_pages_break::add);

        stack_break.addTagElement("pages", book_pages_break);

        player.drop(stack_break, false, false);

        return InteractionResult.SUCCESS;
    }

}