package com.example.superheroes.client.network;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import com.example.superheroes.client.fx.ScreenShakeManager;
import com.example.superheroes.client.render.LaserBeamRenderer;
import com.example.superheroes.network.HeroDataSyncS2CPayload;
import com.example.superheroes.network.LaserFiredS2CPayload;
import com.example.superheroes.network.RemoteHeroSkinS2CPayload;
import com.example.superheroes.network.ResourceUpdateS2CPayload;
import com.example.superheroes.network.ScreenShakeS2CPayload;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientNetworking {
	private ClientNetworking() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(HeroDataSyncS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> {
					HeroData data = payload.data();
					ClientHeroState.update(data);
					LocalPlayer self = Minecraft.getInstance().player;
					if (self != null) {
						HeroData previous = self.getAttachedOrCreate(ModAttachments.HERO_DATA);
						self.setAttached(ModAttachments.HERO_DATA, data);
						if (previous.hasHero() != data.hasHero()
								|| (data.hasHero() && !data.heroId().equals(previous.heroId()))) {
							self.refreshDimensions();
						}
					}
				}));

		ClientPlayNetworking.registerGlobalReceiver(ResourceUpdateS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientHeroState.updateResources(payload.energy(), payload.mana())));

		ClientPlayNetworking.registerGlobalReceiver(LaserFiredS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> LaserBeamRenderer.add(payload.start(), payload.end())));

		ClientPlayNetworking.registerGlobalReceiver(ScreenShakeS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ScreenShakeManager.shake(payload.intensity(), payload.durationTicks())));

		ClientPlayNetworking.registerGlobalReceiver(RemoteHeroSkinS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> RemoteHeroSkins.put(payload.playerId(), payload.heroId().orElse(null))));
	}
}
