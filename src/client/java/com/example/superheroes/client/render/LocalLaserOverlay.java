package com.example.superheroes.client.render;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientHeroState;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LocalLaserOverlay {
	private static final double RANGE = 64.0;

	private LocalLaserOverlay() {
	}

	public static void register() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LocalLaserOverlay::render);
	}

	private static void render(WorldRenderContext context) {
		if (!ClientHeroState.data().isActive(AbilityIds.EYE_LASERS)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null) {
			return;
		}
		float partial = context.tickCounter().getGameTimeDeltaPartialTick(true);
		Vec3 eye = player.getEyePosition(partial);
		Vec3 dir = player.getViewVector(partial);
		Vec3 end = eye.add(dir.scale(RANGE));
		BlockHitResult blockHit = level.clip(new ClipContext(
				eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 entitySearchEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
		AABB box = player.getBoundingBox().expandTowards(dir.scale(RANGE)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				level, player, eye, entitySearchEnd, box,
				e -> e instanceof LivingEntity && e.isAlive() && e != player && !e.isSpectator());
		Vec3 actualEnd = hit != null ? hit.getLocation() : entitySearchEnd;
		Vec3 start = eye.add(dir.scale(0.3));
		BeamRenderer.draw(context, start, actualEnd, 1.0f);
	}
}
