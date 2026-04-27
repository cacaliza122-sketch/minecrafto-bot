package com.example.superheroes.ability;

import com.example.superheroes.damage.ModDamageTypes;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.particle.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class EyeLasersAbility implements Ability {
	private static final double RANGE = 64.0;
	private static final float DAMAGE = 8f;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.EYE_LASERS;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 8f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RANGE));
		AABB box = player.getBoundingBox().expandTowards(dir.scale(RANGE)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				player.level(), player, eye, end, box,
				e -> e instanceof LivingEntity && e.isAlive() && e != player && !e.isSpectator());
		Vec3 actualEnd = end;
		ServerLevel level = player.serverLevel();
		if (hit != null) {
			LivingEntity target = (LivingEntity) hit.getEntity();
			target.hurt(ModDamageTypes.eyeLaser(level, player), DAMAGE);
			actualEnd = hit.getLocation();
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.8f);
		level.sendParticles(ModParticles.LASER_SPARK,
				actualEnd.x, actualEnd.y, actualEnd.z,
				18, 0.18, 0.18, 0.18, 0.04);
		ModNetworking.broadcastLaser(player, eye, actualEnd);
		return true;
	}
}
