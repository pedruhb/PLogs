package com.pedruhb.plogs;

import com.mojang.logging.LogUtils;
import com.pedruhb.plogs.item.Analyzer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

@Mod(PLogs.MODID)
public class PLogs {

    public static final String MODID = "plogs";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> ANALYZER = ITEMS.register("analyzer", Analyzer::new);

    Connection connection = null;
    Statement statement = null;

    public PLogs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        File folder = new File("." + File.separator + "config" + File.separator + PLogs.MODID + File.separator);
        folder.mkdir();

        try {

            connection = DriverManager.getConnection("jdbc:sqlite:config/" + PLogs.MODID + "/database.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"block_break\" ( \"uuid\" VARCHAR(100) NULL, \"timestamp\" INTEGER NULL, \"x\" INTEGER NULL, \"y\" INTEGER NULL, \"z\" INTEGER NULL, \"block\" VARCHAR(50) NULL ) ;");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"block_interact\" ( \"uuid\" VARCHAR(100) NULL, \"timestamp\" INTEGER NULL, \"x\" INTEGER NULL, \"y\" INTEGER NULL, \"z\" INTEGER NULL, \"block\" VARCHAR(50) NULL ) ;");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"block_place\" ( \"uuid\" VARCHAR(100) NULL, \"timestamp\" INTEGER NULL, \"x\" INTEGER NULL, \"y\" INTEGER NULL, \"z\" INTEGER NULL, \"block\" VARCHAR(50) NULL ) ;");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"farmland_trample\" ( \"uuid\" VARCHAR(100) NULL, \"timestamp\" INTEGER NULL, \"x\" INTEGER NULL, \"y\" INTEGER NULL, \"z\" INTEGER NULL, \"block\" VARCHAR(50) NULL ) ;");
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS \"users\" ( \"uuid\" VARCHAR(100) UNIQUE , \"first_login\" INTEGER NULL, \"last_login\" INTEGER NULL) ;");

        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("PLogs") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ANALYZER.get());
        }
    };

    @SubscribeEvent
    public void onEvent(EntityJoinLevelEvent event) throws SQLException {
        if ((event.getEntity() instanceof Player)) {

            PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM users WHERE uuid = ?");
            stmt.setString(1, event.getEntity().getUUID().toString());
            ResultSet rs = stmt.executeQuery();

            int count = 0;

            while (rs.next()) {
                count++;
            }

            if (count == 0) {

                PreparedStatement stmt2 = connection.prepareStatement(
                        "INSERT INTO users (\"uuid\", \"first_login\", \"last_login\") VALUES (?, ?, ?);");
                stmt2.setString(1, event.getEntity().getStringUUID());
                stmt2.setInt(2, Math.toIntExact(Instant.now().getEpochSecond()));
                stmt2.setInt(3, Math.toIntExact(Instant.now().getEpochSecond()));
                stmt2.executeUpdate();

            } else {

                PreparedStatement stmt2 = connection.prepareStatement(
                        "UPDATE users set last_login = ? WHERE uuid = ?;");
                stmt2.setInt(1, Math.toIntExact(Instant.now().getEpochSecond()));
                stmt2.setString(2, event.getEntity().getStringUUID());
                stmt2.executeUpdate();

            }

        }
    }

    @SubscribeEvent
    public void blockBreakInteract(RightClickBlock event) throws SQLException {
        if (event.getLevel() instanceof Level && event.getHand() == InteractionHand.MAIN_HAND
                && event.getEntity() instanceof Player && event.getSide() == LogicalSide.SERVER) {

            Level level = event.getEntity().getLevel();
            BlockState state = level.getBlockState(event.getPos());
            Block block = state.getBlock();

            if (state.hasBlockEntity()) {

                LOGGER.info(block.getDescriptionId());

                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO block_interact (\"uuid\", \"timestamp\", \"x\", \"y\", \"z\", \"block\") VALUES (?, ?, ?, ?, ?, ?);");
                stmt.setString(1, event.getEntity().getUUID().toString());
                stmt.setInt(2, Math.toIntExact(Instant.now().getEpochSecond()));
                stmt.setInt(3, event.getPos().getX());
                stmt.setInt(4, event.getPos().getY());
                stmt.setInt(5, event.getPos().getZ());
                stmt.setString(6, block.getDescriptionId());
                stmt.executeUpdate();
            }
        }
    }

    @SubscribeEvent
    public void blockBreakInteract(BreakEvent event) throws SQLException {
        if (event.getPlayer() instanceof Player) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO block_break (\"uuid\", \"timestamp\", \"x\", \"y\", \"z\", \"block\") VALUES (?, ?, ?, ?, ?, ?);");
            stmt.setString(1, event.getPlayer().getUUID().toString());
            stmt.setInt(2, Math.toIntExact(Instant.now().getEpochSecond()));
            stmt.setInt(3, event.getPos().getX());
            stmt.setInt(4, event.getPos().getY());
            stmt.setInt(5, event.getPos().getZ());
            stmt.setString(6, event.getState().getBlock().getDescriptionId());
            stmt.executeUpdate();
        }
    }

    @SubscribeEvent
    public void blockPlaceInteract(EntityPlaceEvent event) throws SQLException {
        if (event.getEntity() instanceof Player) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO block_place (\"uuid\", \"timestamp\", \"x\", \"y\", \"z\", \"block\") VALUES (?, ?, ?, ?, ?, ?);");
            stmt.setString(1, event.getEntity().getUUID().toString());
            stmt.setInt(2, Math.toIntExact(Instant.now().getEpochSecond()));
            stmt.setInt(3, event.getPos().getX());
            stmt.setInt(4, event.getPos().getY());
            stmt.setInt(5, event.getPos().getZ());
            stmt.setString(6, event.getState().getBlock().getDescriptionId());
            stmt.executeUpdate();
        }
    }

    @SubscribeEvent
    public void farmlandTrampleInteract(FarmlandTrampleEvent event) throws SQLException {
        if (event.getEntity() instanceof Player) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO farmland_trample (\"uuid\", \"timestamp\", \"x\", \"y\", \"z\", \"block\") VALUES (?, ?, ?, ?, ?, ?);");
            stmt.setString(1, event.getEntity().getUUID().toString());
            stmt.setInt(2, Math.toIntExact(Instant.now().getEpochSecond()));
            stmt.setInt(3, event.getPos().getX());
            stmt.setInt(4, event.getPos().getY());
            stmt.setInt(5, event.getPos().getZ());
            stmt.setString(6, event.getState().getBlock().getDescriptionId());
            stmt.executeUpdate();
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("PLogs Started!");
        }
    }

}
