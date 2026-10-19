package com.xoyrify.client.gui;

import com.xoyrify.client.download.ModrinthClient;
import com.xoyrify.client.download.ModrinthProject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-game browser for Modrinth mods: search, see compatibility with the
 * current game version, and download straight into mods/ - like Resourcify.
 */
public class ModDownloaderScreen extends Screen {

	private final Screen parent;
	private TextFieldWidget searchBox;
	private List<ModrinthProject> results = new ArrayList<>();
	private int scrollOffset = 0;
	private static final int ROW_HEIGHT = 24;
	private boolean needsRestart = false;
	private String statusMessage = "";

	public ModDownloaderScreen(Screen parent) {
		super(Text.translatable("xoyrify.gui.downloader.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 24, 200, 20,
				Text.translatable("xoyrify.gui.downloader.search"));
		searchBox.setMaxLength(100);
		searchBox.setChangedListener(this::onSearchChanged);
		addSelectableChild(searchBox);
		setInitialFocus(searchBox);

		addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> close())
				.dimensions(this.width - 28, 8, 20, 20).build());
	}

	private long lastTypeTime = 0;
	private String pendingQuery = "";

	private void onSearchChanged(String text) {
		pendingQuery = text;
		lastTypeTime = System.currentTimeMillis();
	}

	@Override
	public void tick() {
		super.tick();
		// Debounce: fire the search 300ms after the player stops typing.
		if (!pendingQuery.isEmpty() && System.currentTimeMillis() - lastTypeTime > 300) {
			String q = pendingQuery;
			pendingQuery = "";
			runSearch(q);
		}
	}

	private void runSearch(String query) {
		if (query.isBlank()) {
			results = new ArrayList<>();
			return;
		}
		statusMessage = "Ricerca in corso...";
		ModrinthClient.search(query).thenAccept(found -> {
			if (this.client == null) return;
			this.client.execute(() -> {
				results = found;
				scrollOffset = 0;
				statusMessage = found.isEmpty() ? "Nessun risultato." : "";
			});
		});
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
		searchBox.render(context, mouseX, mouseY, delta);

		if (!statusMessage.isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(statusMessage), this.width / 2, 50, 0xAAAAAA);
		}

		int listTop = 56;
		int listBottom = this.height - (needsRestart ? 36 : 12);
		int listLeft = this.width / 2 - 160;
		int listWidth = 320;

		context.enableScissor(listLeft, listTop, listLeft + listWidth, listBottom);
		int y = listTop - scrollOffset;
		for (ModrinthProject project : results) {
			if (y + ROW_HEIGHT > listTop && y < listBottom) {
				drawRow(context, project, listLeft, y, listWidth, mouseX, mouseY);
			}
			y += ROW_HEIGHT;
		}
		context.disableScissor();

		if (needsRestart) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("Alcune mod scaricate richiedono un riavvio del gioco per essere caricate."),
					this.width / 2, this.height - 24, 0xFFFF55);
		}
	}

	private void drawRow(DrawContext context, ModrinthProject project, int x, int y, int width, int mouseX, int mouseY) {
		context.fill(x, y, x + width, y + ROW_HEIGHT - 2, 0x40000000);
		context.drawTextWithShadow(this.textRenderer, Text.literal(project.title), x + 6, y + 3, 0xFFFFFF);

		String desc = project.description;
		if (desc != null && desc.length() > 48) desc = desc.substring(0, 45) + "...";
		context.drawTextWithShadow(this.textRenderer, Text.literal(desc == null ? "" : desc), x + 6, y + 13, 0x999999);

		String buttonLabel = switch (project.state) {
			case IDLE -> Text.translatable("xoyrify.gui.downloader.download").getString();
			case RESOLVING, DOWNLOADING -> "...";
			case DONE -> Text.translatable("xoyrify.gui.downloader.downloaded").getString();
			case INCOMPATIBLE -> Text.translatable("xoyrify.gui.downloader.incompatible").getString();
			case ERROR -> "Errore";
		};

		int btnX = x + width - 74;
		int btnY = y + 2;
		int btnW = 70;
		int btnH = ROW_HEIGHT - 6;
		boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
		boolean clickable = project.state == ModrinthProject.DownloadState.IDLE
				|| project.state == ModrinthProject.DownloadState.ERROR;

		int bg = clickable ? (hovered ? 0xFF2F8F2F : 0xFF1F6F1F) : 0xFF555555;
		context.fill(btnX, btnY, btnX + btnW, btnY + btnH, bg);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(buttonLabel),
				btnX + btnW / 2, btnY + (btnH - 8) / 2, 0xFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int listTop = 56;
			int listLeft = this.width / 2 - 160;
			int listWidth = 320;
			int y = listTop - scrollOffset;
			for (ModrinthProject project : results) {
				int btnX = listLeft + listWidth - 74;
				int btnY = y + 2;
				int btnW = 70;
				int btnH = ROW_HEIGHT - 6;
				if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
					boolean clickable = project.state == ModrinthProject.DownloadState.IDLE
							|| project.state == ModrinthProject.DownloadState.ERROR;
					if (clickable) {
						startDownload(project);
					}
					return true;
				}
				y += ROW_HEIGHT;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button) || searchBox.mouseClicked(mouseX, mouseY, button);
	}

	private void startDownload(ModrinthProject project) {
		ModrinthClient.resolveAndDownload(project, () -> {
			if (this.client == null) return;
			this.client.execute(() -> {
				if (project.state == ModrinthProject.DownloadState.DONE) {
					needsRestart = true;
				}
			});
		});
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int maxScroll = Math.max(0, results.size() * ROW_HEIGHT - (this.height - 80));
		scrollOffset = (int) MathHelper.clamp(scrollOffset - verticalAmount * ROW_HEIGHT, 0, maxScroll);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchBox.keyPressed(keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (searchBox.charTyped(chr, modifiers)) return true;
		return super.charTyped(chr, modifiers);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
