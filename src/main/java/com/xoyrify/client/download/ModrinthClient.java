package com.xoyrify.client.download;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Talks to the public Modrinth API (api.modrinth.com) to search mods and
 * download the build matching the player's current Minecraft version and
 * mod loader, dropping the jar straight into the mods/ folder - the same
 * flow Resourcify offers.
 */
public final class ModrinthClient {

	private static final String API_BASE = "https://api.modrinth.com/v2";
	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private static final String USER_AGENT = "xoyrify/1.0.0 (in-game Modrinth downloader)";

	public static CompletableFuture<List<ModrinthProject>> search(String query) {
		String facets = URLEncoder.encode(
				"[[\"project_type:mod\"]]", StandardCharsets.UTF_8);
		String url = API_BASE + "/search?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
				+ "&facets=" + facets + "&limit=30";

		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("User-Agent", USER_AGENT)
				.GET()
				.build();

		return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(ModrinthClient::parseSearchResults);
	}

	private static List<ModrinthProject> parseSearchResults(HttpResponse<String> response) {
		List<ModrinthProject> out = new ArrayList<>();
		if (response.statusCode() != 200) return out;

		JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
		JsonArray hits = root.getAsJsonArray("hits");
		for (JsonElement el : hits) {
			JsonObject obj = el.getAsJsonObject();
			ModrinthProject p = new ModrinthProject();
			p.projectId = getString(obj, "project_id");
			p.slug = getString(obj, "slug");
			p.title = getString(obj, "title");
			p.description = getString(obj, "description");
			p.iconUrl = getString(obj, "icon_url");
			p.downloads = obj.has("downloads") ? obj.get("downloads").getAsInt() : 0;
			out.add(p);
		}
		return out;
	}

	private static String getString(JsonObject obj, String key) {
		return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
	}

	/** Resolves the newest version file compatible with the running MC version + Fabric. */
	public static CompletableFuture<Void> resolveAndDownload(ModrinthProject project, Runnable onFinished) {
		project.state = ModrinthProject.DownloadState.RESOLVING;

		String mcVersion = SharedConstants.getGameVersion().getName();
		String url = API_BASE + "/project/" + project.projectId + "/version"
				+ "?loaders=" + URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8)
				+ "&game_versions=" + URLEncoder.encode("[\"" + mcVersion + "\"]", StandardCharsets.UTF_8);

		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("User-Agent", USER_AGENT)
				.GET()
				.build();

		return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenAccept(response -> {
					if (response.statusCode() != 200) {
						project.state = ModrinthProject.DownloadState.ERROR;
						onFinished.run();
						return;
					}
					JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
					if (versions.isEmpty()) {
						project.state = ModrinthProject.DownloadState.INCOMPATIBLE;
						onFinished.run();
						return;
					}

					JsonObject best = versions.get(0).getAsJsonObject();
					JsonArray files = best.getAsJsonArray("files");
					JsonObject primaryFile = null;
					for (JsonElement fEl : files) {
						JsonObject f = fEl.getAsJsonObject();
						if (f.has("primary") && f.get("primary").getAsBoolean()) {
							primaryFile = f;
							break;
						}
					}
					if (primaryFile == null && !files.isEmpty()) {
						primaryFile = files.get(0).getAsJsonObject();
					}
					if (primaryFile == null) {
						project.state = ModrinthProject.DownloadState.ERROR;
						onFinished.run();
						return;
					}

					project.resolvedVersionId = getString(best, "id");
					project.resolvedFileUrl = getString(primaryFile, "url");
					project.resolvedFileName = getString(primaryFile, "filename");

					downloadFile(project);
					onFinished.run();
				})
				.exceptionally(ex -> {
					project.state = ModrinthProject.DownloadState.ERROR;
					onFinished.run();
					return null;
				});
	}

	private static void downloadFile(ModrinthProject project) {
		project.state = ModrinthProject.DownloadState.DOWNLOADING;
		try {
			Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
			Files.createDirectories(modsDir);
			Path target = modsDir.resolve(project.resolvedFileName);
			Path tmp = modsDir.resolve(project.resolvedFileName + ".download");

			HttpRequest request = HttpRequest.newBuilder(URI.create(project.resolvedFileUrl))
					.header("User-Agent", USER_AGENT)
					.GET()
					.build();

			HttpResponse<Path> response = HTTP.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
			if (response.statusCode() == 200) {
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
				project.state = ModrinthProject.DownloadState.DONE;
			} else {
				Files.deleteIfExists(tmp);
				project.state = ModrinthProject.DownloadState.ERROR;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			project.state = ModrinthProject.DownloadState.ERROR;
		}
	}

	private ModrinthClient() {}
}
