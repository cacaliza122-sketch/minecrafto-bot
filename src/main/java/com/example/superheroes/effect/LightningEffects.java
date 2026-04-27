package com.example.superheroes.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class LightningEffects {
	private static final Random RNG = new Random();

	private LightningEffects() {
	}

	public static void summonRandom(ServerLevel level, Vec3 pos, Entity cause) {
		if (RNG.nextBoolean()) {
			summonSingle(level, pos, cause);
		} else {
			summonCluster(level, pos, cause);
		}
	}

	public static void summonSingle(ServerLevel level, Vec3 pos, Entity cause) {
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
		if (bolt == null) {
			return;
		}
		bolt.moveTo(pos.x, pos.y, pos.z);
		bolt.setVisualOnly(false);
		level.addFreshEntity(bolt);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
				pos.x, pos.y + 1.0, pos.z,
				40, 0.6, 1.0, 0.6, 0.4);
	}

	public static void summonCluster(ServerLevel level, Vec3 center, Entity cause) {
		double[][] offsets = {
				{0, 0, 0},
				{2.0, 0, 0},
				{-2.0, 0, 0},
				{0, 0, 2.0},
				{0, 0, -2.0}
		};
		for (int i = 0; i < offsets.length; i++) {
			double[] o = offsets[i];
			double x = center.x + o[0] + (RNG.nextDouble() - 0.5) * 0.6;
			double y = center.y + o[1];
			double z = center.z + o[2] + (RNG.nextDouble() - 0.5) * 0.6;
			LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
			if (bolt == null) {
				continue;
			}
			bolt.moveTo(x, y, z);
			bolt.setVisualOnly(i != 0);
			level.addFreshEntity(bolt);
		}
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
				center.x, center.y + 1.5, center.z,
				120, 1.4, 1.5, 1.4, 0.6);
		level.playSound(null, center.x, center.y, center.z,
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 0.9f);
	}
}
