package com.xoyrify.client.config;

/** Per-element HUD settings: position, visibility, color, scale. */
public class HudElementConfig {
	public boolean enabled = true;
	public float x;
	public float y;
	public float scale = 1.0f;
	public int color = 0xFFFFFFFF; // ARGB
	public boolean shadow = true;
	public boolean background = true;
	public int backgroundColor = 0x80000000; // ARGB, semi-transparent black

	public HudElementConfig() {}

	public HudElementConfig(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
