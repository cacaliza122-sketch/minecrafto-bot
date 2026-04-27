package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;

public final class FlightAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return AbilityIds.FLIGHT;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		return 0.5f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		Abilities a = player.getAbilities();
		a.mayfly = true;
		a.flying = true;
		player.onUpdateAbilities();
		return true;
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		Abilities a = player.getAbilities();
		a.flying = false;
		if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
			a.mayfly = false;
		}
		player.onUpdateAbilities();
	}
}
