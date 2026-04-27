package com.example.superheroes.ability;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.Heroes;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.resource.ResourceController;
import com.example.superheroes.resource.ResourceKind;
import com.example.superheroes.transform.HeroData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AbilityRouter {
	private AbilityRouter() {
	}

	public static void activate(ServerPlayer player, ResourceLocation abilityId) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero()) {
			return;
		}
		Hero hero = Heroes.get(data.heroId());
		if (hero == null || !hero.getAbilities().contains(abilityId)) {
			return;
		}
		Ability ability = AbilityRegistry.get(abilityId);
		if (ability == null) {
			return;
		}
		if (ability.isToggle() && data.isActive(abilityId)) {
			deactivate(player, abilityId);
			return;
		}
		float cost = ability.costOnActivate();
		if (cost > 0f && !ResourceController.tryConsume(player, abilityId, cost)) {
			return;
		}
		boolean ok = ability.tryActivate(player);
		if (!ok) {
			return;
		}
		if (ability.isToggle()) {
			HeroData updated = player.getAttachedOrCreate(ModAttachments.HERO_DATA).withActive(abilityId, true);
			player.setAttached(ModAttachments.HERO_DATA, updated);
			ModNetworking.syncHeroData(player, updated);
		}
	}

	public static void deactivate(ServerPlayer player, ResourceLocation abilityId) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.isActive(abilityId)) {
			return;
		}
		Ability ability = AbilityRegistry.get(abilityId);
		if (ability == null) {
			return;
		}
		ability.onDeactivate(player);
		HeroData updated = data.withActive(abilityId, false);
		player.setAttached(ModAttachments.HERO_DATA, updated);
		ModNetworking.syncHeroData(player, updated);
	}

	public static void bind(ServerPlayer player, ResourceLocation abilityId, ResourceKind kind) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero()) {
			return;
		}
		Hero hero = Heroes.get(data.heroId());
		if (hero == null || !hero.getAbilities().contains(abilityId)) {
			return;
		}
		HeroData updated = data.withBinding(abilityId, kind);
		player.setAttached(ModAttachments.HERO_DATA, updated);
		ModNetworking.syncHeroData(player, updated);
	}
}
