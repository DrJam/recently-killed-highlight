package com.recentlyKilledHighlight;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("recentlyKilledHighlight")
public interface RecentlyKilledHighlightConfig extends Config {
	@ConfigItem(keyName = "greeting", name = "Welcome Greeting", description = "The message to show to the user when they login")
	default String greeting() {
		return "Hello";
	}
}
