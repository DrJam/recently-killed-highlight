package com.recentlyKilledHighlight;

import net.runelite.client.config.*;
import net.runelite.client.util.Text;

import java.awt.*;
import java.util.List;

@ConfigGroup("recentlyKilledHighlight")
public interface RecentlyKilledHighlightConfig extends Config {
	@ConfigItem(
			keyName = "maxNpcs",
			name = "NPCs remembered",
			description = "How many recent NPC kills to track",
			position = 0
	)
	@Range(min = 0, max = 25)
	default int maxNpcs() { return 10; }

	@ConfigItem(
			position = 1,
			keyName = "highlightMenuNames",
			name = "Highlight menu names",
			description = "Highlight NPC names in right click menu"
	)
	default boolean highlightMenuNames()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "npcIgnoreList",
			name = "Ignore List",
			description = "List of NPC names to ignore"
	)
	default String npcsToIgnoreString()
	{
		return "";
	}

	@ConfigSection(
			name = "Render Style",
			description = "The render style of Killed NPCs",
			position = 3
	)
	String renderStyleSection = "renderStyleSection";

	@ConfigItem(
			position = 0,
			keyName = "highlightHull",
			name = "Highlight hull",
			description = "Configures whether or not NPC should be highlighted by hull",
			section = renderStyleSection
	)
	default boolean highlightHull()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "highlightTile",
			name = "Highlight tile",
			description = "Configures whether or not NPC should be highlighted by tile",
			section = renderStyleSection
	)
	default boolean highlightTile()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "highlightOutline",
			name = "Highlight outline",
			description = "Configures whether or not the model of the NPC should be highlighted by outline",
			section = renderStyleSection
	)
	default boolean highlightOutline()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "npcColor",
			name = "Highlight Color",
			description = "Color of the NPC highlight border, menu, and text",
			section = renderStyleSection
	)
	default Color highlightColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "fillColor",
			name = "Fill Color",
			description = "Color of the NPC highlight fill",
			section = renderStyleSection
	)
	default Color fillColor()
	{
		return new Color(0, 255, 255, 20);
	}

	@ConfigItem(
			position = 5,
			keyName = "borderWidth",
			name = "Border Width",
			description = "Width of the highlighted NPC border",
			section = renderStyleSection
	)
	default double borderWidth()
	{
		return 2;
	}

	@ConfigItem(
			position = 6,
			keyName = "outlineFeather",
			name = "Outline feather",
			description = "Specify between 0-4 how much of the model outline should be faded",
			section = renderStyleSection
	)
	@Range(
			min = 0,
			max = 4
	)
	default int outlineFeather()
	{
		return 0;
	}




}
