package com.xoyrify.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persisted configuration for every HUD element. Stored as
 * config/xoyrify/hud.json, hot-saved whenever the HUD editor closes.
 */
public class HudConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Path path;

	public static Map<String, HudElementConfig> elements = new LinkedHashMap<>();

	public static void load() {
		path = FabricLoader.getInstance().getConfigDir().resolve("xoyrify").resolve("hud.json");

		registerDefault("fps", 4, 4);
		registerDefault("coords", 4, 16);
		registerDefault("biome", 4, 28);
		registerDefault("time", 4, 40);
		registerDefault("memory", 4, 52);
		registerDefault("ping", 4, 64);

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				Map<String, HudElementConfig> loaded = GSON.fromJson(reader,
						com.google.gson.reflect.TypeToken.getParameterized(Map.class, String.class, HudElementConfig.class).getType());
				if (loaded != null) {
					elements.putAll(loaded);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			save();
		}
	}

	private static void registerDefault(String key, float x, float y) {
		elements.putIfAbsent(key, new HudElementConfig(x, y));
	}

	public static HudElementConfig get(String key) {
		return elements.computeIfAbsent(key, k -> new HudElementConfig(4, 4));
	}

	public static void save() {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(elements, writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
