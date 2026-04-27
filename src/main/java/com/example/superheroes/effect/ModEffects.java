package com.example.superheroes.effect;

import com.example.superheroes.ModId;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ModEffects {
	public static final Holder<MobEffect> MADNESS = Registry.registerForHolder(
			BuiltInRegistries.MOB_EFFECT, ModId.of("madness"),
			new MadnessMobEffect(MobEffectCategory.HARMFUL, 0xFF1F2D)
	);

	private ModEffects() {
	}

	public static void init() {
	}

	public static boolean isMadness(net.minecraft.world.entity.LivingEntity entity) {
		return entity != null && entity.hasEffect(MADNESS);
	}
}
