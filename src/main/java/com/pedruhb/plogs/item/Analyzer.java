package com.pedruhb.plogs.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.mojang.logging.LogUtils;
import com.pedruhb.plogs.PLogs;
import org.slf4j.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.Instant;

public class Analyzer extends LoreItem {

    public Analyzer() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS), "Right-click to show block details");
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

        player.sendSystemMessage(Component.literal("Analyzing -> X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ()));

        /* Block Break */
        try {

            PreparedStatement stmt = PLogs.connection.prepareStatement(
                    "SELECT block_break.*, users.last_name FROM block_break, users WHERE users.uuid = block_break.uuid AND block_break.x = ? AND block_break.y = ? AND block_break.z = ?");
            stmt.setInt(1, pos.getX());
            stmt.setInt(2, pos.getY());
            stmt.setInt(3, pos.getZ());

            ResultSet rs;

            rs = stmt.executeQuery();

            String log_broke = "Block Break Logs - " + (new Date(Instant.now().toEpochMilli())) + " - X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ() + "\n";

            while (rs.next()) {

                Date date = new Date(rs.getInt("timestamp") * 1000L);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                log_broke += rs.getString("last_name") + " - " + format.format(date) + " - " + rs.getString("block")
                        + "\n";

            }

            player.sendSystemMessage(copy(log_broke, ChatFormatting.GREEN, "Block Break Logs"));

        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("Error getting Block Break data."));
            LOGGER.error(e.getSQLState());
        }

        /* Block Place */
        try {

            PreparedStatement stmt = PLogs.connection.prepareStatement(
                    "SELECT block_place.*, users.last_name FROM block_place, users WHERE users.uuid = block_place.uuid AND block_place.x = ? AND block_place.y = ? AND block_place.z = ?");
            stmt.setInt(1, pos.getX());
            stmt.setInt(2, pos.getY());
            stmt.setInt(3, pos.getZ());

            ResultSet rs;

            rs = stmt.executeQuery();

            String log_place = "Block Place Logs - " + (new Date(Instant.now().toEpochMilli())) + " - X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ() + "\n";

            while (rs.next()) {

                Date date = new Date(rs.getInt("timestamp") * 1000L);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                log_place += rs.getString("last_name") + " - " + format.format(date) + " - " + rs.getString("block")
                        + "\n";

            }

            player.sendSystemMessage(copy(log_place, ChatFormatting.GREEN, "Block Place Logs"));

        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("Error getting Block Place data."));
            LOGGER.error(e.getSQLState());
        }

        /* Block Interact */
        try {

            PreparedStatement stmt = PLogs.connection.prepareStatement(
                    "SELECT block_interact.*, users.last_name FROM block_interact, users WHERE users.uuid = block_interact.uuid AND block_interact.x = ? AND block_interact.y = ? AND block_interact.z = ?");
            stmt.setInt(1, pos.getX());
            stmt.setInt(2, pos.getY());
            stmt.setInt(3, pos.getZ());

            ResultSet rs;

            rs = stmt.executeQuery();

            String log_interact = "Block Interact Logs - " + (new Date(Instant.now().toEpochMilli())) + " - X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ() + "\n";

            while (rs.next()) {

                Date date = new Date(rs.getInt("timestamp") * 1000L);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                log_interact += rs.getString("last_name") + " - " + format.format(date) + " - " + rs.getString("block")
                        + "\n";

            }

            player.sendSystemMessage(copy(log_interact, ChatFormatting.GREEN, "Block Interact Logs"));

        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("Error getting Block Interact data."));
            LOGGER.error(e.getSQLState());
        }

        /* Farmland Trample */
        try {

            PreparedStatement stmt = PLogs.connection.prepareStatement(
                    "SELECT farmland_trample.*, users.last_name FROM farmland_trample, users WHERE users.uuid = farmland_trample.uuid AND farmland_trample.x = ? AND farmland_trample.y = ? AND farmland_trample.z = ?");
            stmt.setInt(1, pos.getX());
            stmt.setInt(2, pos.getY());
            stmt.setInt(3, pos.getZ());

            ResultSet rs;

            rs = stmt.executeQuery();

            String log_farmland = "Farmland Trample Logs - " + (new Date(Instant.now().toEpochMilli())) + " - X " + pos.getX() + " / Y " + pos.getY() + " / Z " + pos.getZ() + "\n";

            while (rs.next()) {

                Date date = new Date(rs.getInt("timestamp") * 1000L);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                log_farmland += rs.getString("last_name") + " - " + format.format(date) + " - " + rs.getString("block") + "\n";

            }

            player.sendSystemMessage(copy(log_farmland, ChatFormatting.GREEN, "Farmland Trample Logs"));

        } catch (SQLException e) {
            player.sendSystemMessage(Component.literal("Error getting Farmland Trample data."));
            LOGGER.error(e.getSQLState());
        }

        return InteractionResult.SUCCESS;
    }

    private static Component copy(String s, ChatFormatting col, String info) {
        var component = Component.literal("- ");
        component.setStyle(component.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
        component.setStyle(component.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, s)));
        component.setStyle(component.getStyle().withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(info + " (Click to copy)"))));
        component.append(Component.literal(info).withStyle(col));
        return component;
    }

}