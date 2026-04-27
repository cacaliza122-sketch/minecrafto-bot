package com.example.superheroes.client;

import com.example.superheroes.client.hud.RadialMenuHud;
import com.example.superheroes.client.hud.ResourceBarHud;
import com.example.superheroes.client.hud.LowResourceVignetteHud;
import com.example.superheroes.client.hud.ScreenFlashHud;
import com.example.superheroes.client.network.ClientNetworking;
import com.example.superheroes.client.render.HeroSkinLayer;
import com.example.superheroes.client.render.LaserBeamRenderer;
import com.example.superheroes.client.screen.BindingsScreen;
import com.example.superheroes.network.ActivateAbilityC2SPayload;
import com.example.superheroes.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class SuperheroesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModKeys.init();
		ClientNetworking.init();
		LaserBeamRenderer.register();
		ParticleFactoryRegistry.getInstance().register(ModParticles.TRANSFORM_SPARK, EndRodParticle.Provider::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.LASER_SPARK, EndRodParticle.Provider::new);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
			if (entityRenderer instanceof PlayerRenderer playerRenderer) {
				helper.register(new HeroSkinLayer(playerRenderer));
			}
		});

		HudRenderCallback.EVENT.register((graphics, tracker) -> {
			LowResourceVignetteHud.render(graphics, tracker);
			ResourceBarHud.render(graphics, tracker);
			RadialMenuHud.render(graphics, tracker);
			ScreenFlashHud.render(graphics, tracker);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			RadialMenuHud.clientTick(client);
			while (ModKeys.BINDINGS.consumeClick()) {
				if (client.player != null && ClientHeroState.data().hasHero()) {
					client.setScreen(new BindingsScreen());
				}
			}
			for (int i = 0; i < ModKeys.ABILITY_SLOTS.length; i++) {
				while (ModKeys.ABILITY_SLOTS[i].consumeClick()) {
					if (client.player == null || !ClientHeroState.data().hasHero()) {
						continue;
					}
					List<ResourceLocation> abilities = ClientHeroState.abilities();
					if (i < abilities.size()) {
						ClientPlayNetworking.send(new ActivateAbilityC2SPayload(abilities.get(i)));
					}
				}
			}
		});
	}
}
