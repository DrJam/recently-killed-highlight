package com.recentlyKilledHighlight;

import com.recentlyKilledHighlight.RecentlyKilledHighlightPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RecentlyKilledHighlightPluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(RecentlyKilledHighlightPlugin.class);
		RuneLite.main(args);
	}
}