package com.example.superheroes.ability;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AbilityRegistry {
	private static final Map<ResourceLocation, Ability> REGISTRY = new LinkedHashMap<>();

	public static final FlightAbility FLIGHT = new FlightAbility();
	public static final EyeLasersAbility EYE_LASERS = new EyeLasersAbility();
	public static final XRayAbility X_RAY = new XRayAbility();

	private AbilityRegistry() {
	}

	public static void init() {
		register(FLIGHT);
		register(EYE_LASERS);
		register(X_RAY);
	}

	public static void register(Ability ability) {
		REGISTRY.put(ability.getId(), ability);
	}

	@Nullable
	public static Ability get(@Nullable ResourceLocation id) {
		return id == null ? null : REGISTRY.get(id);
	}

	public static Map<ResourceLocation, Ability> all() {
		return Collections.unmodifiableMap(REGISTRY);
	}
}
