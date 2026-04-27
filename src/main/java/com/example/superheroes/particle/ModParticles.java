package com.example.superheroes.particle;

import com.example.superheroes.ModId;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	public static final SimpleParticleType TRANSFORM_SPARK = register("transform_spark", FabricParticleTypes.simple());
	public static final SimpleParticleType LASER_SPARK = register("laser_spark", FabricParticleTypes.simple());

	private ModParticles() {
	}

	private static SimpleParticleType register(String name, SimpleParticleType type) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ModId.of(name), type);
	}

	public static void init() {
	}
}
