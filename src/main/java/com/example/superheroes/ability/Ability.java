package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface Ability {
	ResourceLocation getId();

	boolean isToggle();

	float costOnActivate();

	float costPerTick();

	boolean tryActivate(ServerPlayer player);

	default void onTickActive(ServerPlayer player) {
	}

	default void onDeactivate(ServerPlayer player) {
	}
}
