package com.example.superheroes.damage;

import com.example.superheroes.ModId;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class ModDamageTypes {
	public static final ResourceKey<DamageType> EYE_LASER = ResourceKey.create(
			Registries.DAMAGE_TYPE, ModId.of("eye_laser"));

	private ModDamageTypes() {
	}

	public static void bootstrap(BootstrapContext<DamageType> context) {
		context.register(EYE_LASER, new DamageType("eye_laser", DamageScaling.NEVER, 0.0F, DamageEffects.BURNING));
	}

	public static DamageSource eyeLaser(ServerLevel level, Entity attacker) {
		Holder<DamageType> holder = level.registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(EYE_LASER);
		return new DamageSource(holder, attacker, attacker);
	}
}
