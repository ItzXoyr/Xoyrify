package com.xoyrify.client.hud;

import com.xoyrify.client.config.HudElementConfig;
import net.minecraft.client.MinecraftClient;

/** A single piece of information the HUD can show (FPS, coords, ...). */
public interface HudElement {
	String key();

	String label();

	/** Returns the current text to display, e.g. "FPS: 144". */
	String text(MinecraftClient client);

	default HudElementConfig config() {
		return com.xoyrify.client.config.HudConfig.get(key());
	}
}
