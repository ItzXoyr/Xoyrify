package com.xoyrify.client;

import com.xoyrify.client.config.HudConfig;
import com.xoyrify.client.gui.ModDownloaderScreen;
import com.xoyrify.client.gui.HudEditorScreen;
import com.xoyrify.client.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Entry point for Xoyrify: an in-game Modrinth mod browser/downloader
 * (similar to Resourcify) plus a fully customizable HUD.
 */
public class XoyrifyClient implements ClientModInitializer {

	public static final String MOD_ID = "xoyrify";

	private static KeyBinding openDownloaderKey;
	private static KeyBinding openHudEditorKey;

	@Override
	public void onInitializeClient() {
		HudConfig.load();

		openDownloaderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.xoyrify.open_downloader",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"key.categories.xoyrify"
		));

		openHudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.xoyrify.open_hud_editor",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"key.categories.xoyrify"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.currentScreen != null) return;
			while (openDownloaderKey.wasPressed()) {
				client.setScreen(new ModDownloaderScreen(null));
			}
			while (openHudEditorKey.wasPressed()) {
				client.setScreen(new HudEditorScreen(null));
			}
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.options.hudHidden) return;
			HudRenderer.render(drawContext, client);
		});
	}
}
