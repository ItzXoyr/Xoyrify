package com.xoyrify.client.gui;

import com.xoyrify.client.config.HudConfig;
import com.xoyrify.client.config.HudElementConfig;
import com.xoyrify.client.hud.HudElement;
import com.xoyrify.client.hud.HudElements;
import com.xoyrify.client.hud.HudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets the player drag every HUD element around the screen, and toggle,
 * recolor or rescale it with a side panel. Mirrors the "edit HUD" style
 * used by mods like Resourcify's own overlay editor.
 */
public class HudEditorScreen extends Screen {

	private final Screen parent;
	private String draggingKey = null;
	private double dragOffsetX, dragOffsetY;
	private final Map<String, ButtonWidget> toggleButtons = new HashMap<>();
	private String selectedKey = null;

	public HudEditorScreen(Screen parent) {
		super(Text.translatable("xoyrify.gui.hud.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int panelX = this.width - 150;
		int y = 24;
		for (HudElement element : HudElements.ALL.values()) {
			HudElementConfig cfg = element.config();
			ButtonWidget btn = ButtonWidget.builder(
					Text.literal(element.label() + ": " + (cfg.enabled ? "ON" : "OFF")),
					b -> {
						cfg.enabled = !cfg.enabled;
						b.setMessage(Text.literal(element.label() + ": " + (cfg.enabled ? "ON" : "OFF")));
					}
			).dimensions(panelX, y, 140, 18).build();
			toggleButtons.put(element.key(), btn);
			addDrawableChild(btn);
			y += 20;
		}

		y += 10;
		addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"), b -> adjustScale(-0.1f))
				.dimensions(panelX, y, 68, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"), b -> adjustScale(0.1f))
				.dimensions(panelX + 72, y, 68, 18).build());
		y += 20;
		addDrawableChild(ButtonWidget.builder(Text.literal("Color"), b -> cycleColor())
				.dimensions(panelX, y, 68, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Background"), b -> toggleBackground())
				.dimensions(panelX + 72, y, 68, 18).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("xoyrify.gui.hud.done"), b -> close())
				.dimensions(panelX, this.height - 28, 140, 20).build());
	}

	private HudElementConfig selectedConfig() {
		return selectedKey != null ? HudConfig.get(selectedKey) : null;
	}

	private void adjustScale(float delta) {
		HudElementConfig cfg = selectedConfig();
		if (cfg == null) return;
		cfg.scale = Math.max(0.5f, Math.min(3.0f, cfg.scale + delta));
	}

	private static final int[] PALETTE = {
			0xFFFFFFFF, 0xFFFFFF55, 0xFF55FFFF, 0xFF55FF55,
			0xFFFF5555, 0xFFFF55FF, 0xFFAAAAAA, 0xFF000000
	};

	private void cycleColor() {
		HudElementConfig cfg = selectedConfig();
		if (cfg == null) return;
		int idx = 0;
		for (int i = 0; i < PALETTE.length; i++) {
			if (PALETTE[i] == cfg.color) {
				idx = i;
				break;
			}
		}
		cfg.color = PALETTE[(idx + 1) % PALETTE.length];
	}

	private void toggleBackground() {
		HudElementConfig cfg = selectedConfig();
		if (cfg == null) return;
		cfg.background = !cfg.background;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);

		for (HudElement element : HudElements.ALL.values()) {
			HudElementConfig cfg = element.config();
			String text = element.text(this.client);
			HudRenderer.renderLine(context, this.client, text, cfg);

			boolean selected = element.key().equals(selectedKey);
			int w = (int) (this.client.textRenderer.getWidth(text) * cfg.scale);
			int h = (int) (10 * cfg.scale);
			int color = selected ? 0xFF00FF00 : 0x66FFFFFF;
			context.drawBorder((int) cfg.x - 2, (int) cfg.y - 2, w + 4, h + 2, color);
		}

		super.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, this.title, 8, 8, 0xFFFFFF);
		context.drawTextWithShadow(this.textRenderer,
				Text.literal("Trascina gli elementi per spostarli. Clicca per selezionarli."),
				8, this.height - 20, 0xAAAAAA);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (HudElement element : HudElements.ALL.values()) {
				HudElementConfig cfg = element.config();
				String text = element.text(this.client);
				int w = (int) (this.client.textRenderer.getWidth(text) * cfg.scale);
				int h = (int) (10 * cfg.scale);
				if (mouseX >= cfg.x - 2 && mouseX <= cfg.x + w + 2
						&& mouseY >= cfg.y - 2 && mouseY <= cfg.y + h + 2) {
					draggingKey = element.key();
					selectedKey = element.key();
					dragOffsetX = mouseX - cfg.x;
					dragOffsetY = mouseY - cfg.y;
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (draggingKey != null) {
			HudElementConfig cfg = HudConfig.get(draggingKey);
			cfg.x = (float) (mouseX - dragOffsetX);
			cfg.y = (float) (mouseY - dragOffsetY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingKey = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		HudConfig.save();
		this.client.setScreen(parent);
	}
}
