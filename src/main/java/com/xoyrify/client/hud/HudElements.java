package com.xoyrify.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of all built-in HUD elements, in a stable iteration order. */
public final class HudElements {

	public static final Map<String, HudElement> ALL = new LinkedHashMap<>();

	static {
		register("fps", "FPS", client ->
				"FPS: " + client.getCurrentFps());

		register("coords", "Coordinates", client -> {
			if (client.player == null) return "XYZ: --";
			BlockPos pos = client.player.getBlockPos();
			return String.format("XYZ: %d / %d / %d", pos.getX(), pos.getY(), pos.getZ());
		});

		register("biome", "Biome", client -> {
			if (client.player == null || client.world == null) return "Biome: --";
			RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
			return "Biome: " + biome.getKey()
					.map(key -> key.getValue().getPath())
					.orElse("unknown");
		});

		register("time", "World time", client -> {
			if (client.world == null) return "Time: --";
			long ticks = client.world.getTimeOfDay() % 24000;
			int hours = (int) ((ticks / 1000 + 6) % 24);
			int minutes = (int) ((ticks % 1000) * 60 / 1000);
			return String.format("Time: %02d:%02d", hours, minutes);
		});

		register("memory", "Memory usage", client -> {
			Runtime r = Runtime.getRuntime();
			long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
			long max = r.maxMemory() / 1024 / 1024;
			return "Mem: " + used + "/" + max + " MB";
		});

		register("ping", "Ping", client -> {
			if (client.player == null || client.getNetworkHandler() == null) return "Ping: --";
			PlayerEntity player = client.player;
			var entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
			return "Ping: " + (entry != null ? entry.getLatency() : 0) + " ms";
		});
	}

	private static void register(String key, String label, java.util.function.Function<MinecraftClient, String> textFn) {
		ALL.put(key, new HudElement() {
			@Override
			public String key() {
				return key;
			}

			@Override
			public String label() {
				return label;
			}

			@Override
			public String text(MinecraftClient client) {
				return textFn.apply(client);
			}
		});
	}

	private HudElements() {}
}
