package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.resource.ResourceKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class HomelanderHero implements Hero {
	public static final ResourceLocation ID = ModId.of("homelander");
	public static final ResourceLocation SKIN = ModId.of("textures/entity/hero/homelander.png");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public float getEnergyMax() {
		return 100f;
	}

	@Override
	public float getEnergyRegenPerTick() {
		return 0.5f;
	}

	@Override
	public float getManaMax() {
		return 100f;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return switch (pose) {
			case CROUCHING -> EntityDimensions.scalable(0.6f, 1.5f).withEyeHeight(1.27f);
			case SWIMMING, FALL_FLYING, SPIN_ATTACK -> EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f);
			default -> EntityDimensions.scalable(0.6f, 1.8f).withEyeHeight(1.62f);
		};
	}

	@Override
	public List<ResourceLocation> getAbilities() {
		return List.of(AbilityIds.FLIGHT, AbilityIds.EYE_LASERS, AbilityIds.X_RAY);
	}

	@Override
	public ResourceKind getDefaultBinding(ResourceLocation abilityId) {
		return abilityId.equals(AbilityIds.X_RAY) ? ResourceKind.MANA : ResourceKind.ENERGY;
	}

	@Override
	public void applyPassives(Player player) {
		HeroAttributes.applyHomelander(player);
	}

	@Override
	public void removePassives(Player player) {
		HeroAttributes.removeHomelander(player);
	}

	@Override
	public boolean cancelsFallDamage(Player player) {
		return true;
	}

	@Override
	public ResourceLocation getSkinTexture() {
		return SKIN;
	}
}
