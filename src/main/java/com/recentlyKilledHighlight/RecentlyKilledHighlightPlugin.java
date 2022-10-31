package com.recentlyKilledHighlight;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;


@Slf4j
@PluginDescriptor(name = "Recently Killed Highlight")
public class RecentlyKilledHighlightPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RecentlyKilledHighlightConfig config;

	@Inject
	private NpcOverlayService npcOverlayService;

	@Inject
	private NpcUtil npcUtil;

	@Getter(AccessLevel.PACKAGE)
	private final Map<NPC, HighlightedNpc> highlightedNpcs = new HashMap<>();

	private final Function<NPC, HighlightedNpc> isHighlighted = highlightedNpcs::get;

	private final ArrayDeque<NPC> killedNpcs = new ArrayDeque<NPC>();

	private List<String> ignoreFilters = new ArrayList<>();

	@Override
	protected void startUp() throws Exception {
		log.info("RecentlyKilledHighlight started!");
		npcOverlayService.registerHighlighter(isHighlighted);
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("RecentlyKilledHighlight stopped!");
		npcOverlayService.unregisterHighlighter(isHighlighted);
		killedNpcs.clear();
		highlightedNpcs.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		final NPC npc = npcSpawned.getNpc();
		final String npcName = npc.getName();

		if (npcName == null)
		{
			return;
		}

		if (containsNpc(killedNpcs, npc))
		{
			highlightedNpcs.put(npc, getHighlightedNpc(npc));
		}

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		final NPC npc = npcDespawned.getNpc();
		highlightedNpcs.remove(npc);
	}
	@Subscribe
	public void onNpcLootReceived(NpcLootReceived npcLootReceived)
	{
		final NPC npc = npcLootReceived.getNpc();
		final String npcName = npc.getName();


		if (!containsNpc(killedNpcs, npc) && !nameIsIgnored(npcName)) {
			killedNpcs.push(npc);

			if (killedNpcs.size() > config.maxNpcs()) {
				killedNpcs.removeLast();
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event) {
		if (!(event.getSource() instanceof NPC)) {
			return;
		}

		final NPC sourceNpc = (NPC) event.getSource();
		NPC highlightedSourceNpc = findNpc(highlightedNpcs.keySet(), sourceNpc);

		// not targeting player, and not currently highlighted
		if (event.getTarget() != client.getLocalPlayer() && highlightedSourceNpc == null) {
			// not in remembered list, do nothing
			if (!containsNpc(killedNpcs, sourceNpc)) {
				return;
			}
			if (sourceNpc.isDead()) {
				return;
			}
			highlightedNpcs.put(sourceNpc, getHighlightedNpc(sourceNpc));
			npcOverlayService.rebuild();
			return;
		}

		// targeting player & highlighted, unhighlight
		if (highlightedSourceNpc != null) {
			highlightedNpcs.remove(highlightedSourceNpc);
			npcOverlayService.rebuild();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final MenuEntry menuEntry = event.getMenuEntry();
		final NPC npc = menuEntry.getNpc();

		if (npc == null)
		{
			return;
		}

		Color color = null;

		if (highlightedNpcs.containsKey(npc) && config.highlightMenuNames() && !npcUtil.isDying(npc))
		{
			color = config.highlightColor();
		}

		if (color != null)
		{
			final String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), color);
			menuEntry.setTarget(target);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals("recentlyKilledHighlight"))
		{
			return;
		}

		clientThread.invoke(this::rebuild);
	}

	@Provides
	RecentlyKilledHighlightConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RecentlyKilledHighlightConfig.class);
	}

	private void rebuild()
	{
		ignoreFilters = getIgnoreFilters();
		highlightedNpcs.clear();

		if (client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING)
		{
			// NPCs are still in the client after logging out,
			// but we don't want to highlight those.
			return;
		}

		while (killedNpcs.size() > config.maxNpcs() && killedNpcs.size() > 0) {
			killedNpcs.removeLast();
		}

		for (NPC npc : client.getNpcs())
		{
			if (containsNpc(killedNpcs, npc))
			{
				if (nameIsIgnored(npc.getName())) {
					killedNpcs.removeIf(x -> npc.getIndex()==x.getIndex());
				} else {
					if (npc.getInteracting() != client.getLocalPlayer()) {
					highlightedNpcs.put(npc, getHighlightedNpc(npc));
				}
			}
		}
		}

		npcOverlayService.rebuild();
	}

	private HighlightedNpc getHighlightedNpc(NPC npc)
	{
		return HighlightedNpc.builder()
				.npc(npc)
				.highlightColor(config.highlightColor())
				.fillColor(config.fillColor())
				.hull(config.highlightHull())
				.tile(config.highlightTile())
				.outline(config.highlightOutline())
				.borderWidth((float) config.borderWidth())
				.outlineFeather(config.outlineFeather())
				.build();
	}

	private List<String> getIgnoreFilters() {
		final String configNpcsString = config.npcsToIgnoreString();

		if (configNpcsString.isEmpty()) {
			return Collections.emptyList();
		}

		return Text.fromCSV(configNpcsString);
	}

	private boolean nameIsIgnored(String npcName) {
		if (npcName == null) {
			return false;
		}

		for (String configName : ignoreFilters)  {
			if (WildcardMatcher.matches(configName.trim(), npcName)) {
				return true;
			}
		}

		return false;
	}

	private boolean containsNpc(Collection<NPC> npcs, NPC npc)
	{
		return npcs.stream().anyMatch((collectionNPC) -> collectionNPC.getIndex() == npc.getIndex());
	}

	private NPC findNpc(Collection<NPC> npcs, NPC npc)
	{
		return npcs.stream().filter((collectionNpc) -> collectionNpc.getIndex() == npc.getIndex()).findFirst().orElse(null);
	}
}
