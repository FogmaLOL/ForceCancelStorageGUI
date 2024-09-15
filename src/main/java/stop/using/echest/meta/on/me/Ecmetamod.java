package stop.using.echest.meta.on.me;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Ecmetamod implements ModInitializer {
	public static final String MOD_ID = "ecmetamod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding toggleKeybind;
	private static boolean isModActive = false;
	private static final File configPath = new File(MinecraftClient.getInstance().runDirectory, "config/ecmod.json");

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Config config = new Config();

	@Override
	public void onInitialize() {
		loadConfig();

		toggleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"keybindforforcelosestoragegui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U, // Default key is "U" change if u want
				"ForceCloseStorageGUI"
		));

		// regrister keybnd
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKeybind.wasPressed()) {
				isModActive = !isModActive;
				String status = isModActive ? "ON" : "OFF";
				if (client.player != null) {
					client.player.sendMessage(Text.literal("[ECMOD] ").append(Text.literal(status).formatted(Formatting.BLUE)), false);
				}
			}
		});

		// sends message if u try to open the block
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (isModActive && hitResult != null) {
				BlockPos blockPos = hitResult.getBlockPos();
				BlockState blockState = world.getBlockState(blockPos);
				if (isBlockedContainer(blockState)) {
					// Send a message to the player and prevent opening the GUI
					player.sendMessage(
							Text.literal("[ECMOD] storage gui blocked :) .").formatted(Formatting.WHITE), false
					);
					return ActionResult.FAIL; // Cancel the interaction
				}
			}
			return ActionResult.PASS; // allows u to open GUI
		});
	}
	// add more blocks with
	// state.getBlock() instanceof ChestBlock ||
	// make sure the end of this chain always end with ; (required for java syntax)

	// note from Fogma if u find a way to block Spawner gui from spesific servers please make a github comment on how
	private boolean isBlockedContainer(BlockState state) {
		return state.getBlock() instanceof EnderChestBlock ||
				state.getBlock() instanceof ChestBlock ||
				state.getBlock() instanceof FurnaceBlock||
				state.getBlock() instanceof FurnaceBlock;
	}

	private void loadConfig() {
		if (configPath.exists()) {
			try (FileReader reader = new FileReader(configPath)) {
				config = gson.fromJson(reader, Config.class);
			} catch (IOException e) {
				LOGGER.error("[ECMOD] Failed to load the config, you can find it at! {}", configPath.getPath());
				setDefaultConfig();
			}
		} else {
			setDefaultConfig();
		}
	}

	private void setDefaultConfig() {
		// Set default values
		config.keybind = "U";
		saveConfig();
	}

	private void saveConfig() {
		try (FileWriter writer = new FileWriter(configPath)) {
			gson.toJson(config, writer);
		} catch (IOException e) {
			LOGGER.error("[ECMOD] Failed to save the config to", configPath.getPath());
		}
	}



	private static class Config {
		String keybind;
	}
}

