package com.xoyrify.client.hud;

import com.xoyrify.client.config.HudElementConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Draws every enabled HUD element at its configured position/style. */
public final class HudRenderer {

	public static void render(DrawContext context, MinecraftClient client) {
		if (client.player == null) return;

		for (HudElement element : HudElements.ALL.values()) {
			HudElementConfig cfg = element.config();
			if (!cfg.enabled) continue;

			String text = element.text(client);
			renderLine(context, client, text, cfg);
		}
	}

	public static void renderLine(DrawContext context, MinecraftClient client, String text, HudElementConfig cfg) {
		context.getMatrices().push();
		context.getMatrices().translate(cfg.x, cfg.y, 0);
		context.getMatrices().scale(cfg.scale, cfg.scale, 1f);

		int textWidth = client.textRenderer.getWidth(text);
		if (cfg.background) {
			context.fill(-2, -2, textWidth + 2, 10, cfg.backgroundColor);
		}

		if (cfg.shadow) {
			context.drawTextWithShadow(client.textRenderer, text, 0, 0, cfg.color);
		} else {
			context.drawText(client.textRenderer, text, 0, 0, cfg.color, false);
		}

		context.getMatrices().pop();
	}

	private HudRenderer() {}
}
