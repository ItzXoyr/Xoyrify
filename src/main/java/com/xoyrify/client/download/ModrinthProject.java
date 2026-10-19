package com.xoyrify.client.download;

/** Minimal view of a Modrinth search hit, enough to list and download it. */
public class ModrinthProject {
	public String projectId;
	public String slug;
	public String title;
	public String description;
	public String iconUrl;
	public int downloads;

	// Filled in lazily once the player tries to download.
	public String resolvedVersionId;
	public String resolvedFileUrl;
	public String resolvedFileName;
	public DownloadState state = DownloadState.IDLE;

	public enum DownloadState {
		IDLE, RESOLVING, DOWNLOADING, DONE, INCOMPATIBLE, ERROR
	}
}
