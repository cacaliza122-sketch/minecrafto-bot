package com.example.superheroes.network;

import com.example.superheroes.ability.AbilityRouter;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(ActivateAbilityC2SPayload.TYPE, ActivateAbilityC2SPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(DeactivateAbilityC2SPayload.TYPE, DeactivateAbilityC2SPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(BindAbilityResourceC2SPayload.TYPE, BindAbilityResourceC2SPayload.STREAM_CODEC);

		PayloadTypeRegistry.playS2C().register(ResourceUpdateS2CPayload.TYPE, ResourceUpdateS2CPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(HeroDataSyncS2CPayload.TYPE, HeroDataSyncS2CPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(LaserFiredS2CPayload.TYPE, LaserFiredS2CPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ActivateAbilityC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> AbilityRouter.activate(player, payload.abilityId()));
		});
		ServerPlayNetworking.registerGlobalReceiver(DeactivateAbilityC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> AbilityRouter.deactivate(player, payload.abilityId()));
		});
		ServerPlayNetworking.registerGlobalReceiver(BindAbilityResourceC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> AbilityRouter.bind(player, payload.abilityId(), payload.kind()));
		});
	}

	public static void syncResources(ServerPlayer player, HeroData data) {
		ServerPlayNetworking.send(player, new ResourceUpdateS2CPayload(data.energy(), data.mana()));
	}

	public static void syncHeroData(ServerPlayer player, HeroData data) {
		ServerPlayNetworking.send(player, new HeroDataSyncS2CPayload(data));
	}

	public static void syncHeroDataFromAttachment(ServerPlayer player) {
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		syncHeroData(player, data);
	}

	public static void broadcastLaser(ServerPlayer shooter, Vec3 start, Vec3 end) {
		LaserFiredS2CPayload payload = new LaserFiredS2CPayload(shooter.getUUID(), start, end);
		ServerPlayNetworking.send(shooter, payload);
		for (ServerPlayer observer : PlayerLookup.tracking(shooter)) {
			if (observer != shooter) {
				ServerPlayNetworking.send(observer, payload);
			}
		}
	}
}
