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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EyeLasersAbility implements Ability {
	private static final double RANGE = 64.0;
	private static final float DAMAGE_PER_TICK = 0.5f;

	@Override
	public ResourceLocation getId() {
		return AbilityIds.EYE_LASERS;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 2f;
	}

	@Override
	public float costPerTick() {
		return 0.6f;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.8f);
		fireBeam(player);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		fireBeam(player);
		if (player.tickCount % 6 == 0) {
			player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.35f, 1.6f);
		}
	}

	private static void fireBeam(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 dir = player.getViewVector(1f);
		Vec3 end = eye.add(dir.scale(RANGE));
		ServerLevel level = player.serverLevel();
		BlockHitResult blockHit = level.clip(new ClipContext(
				eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 entitySearchEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
		AABB box = player.getBoundingBox().expandTowards(dir.scale(RANGE)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				level, player, eye, entitySearchEnd, box,
				e -> e instanceof LivingEntity && e.isAlive() && e != player && !e.isSpectator());
		Vec3 actualEnd = entitySearchEnd;
		if (hit != null) {
			LivingEntity target = (LivingEntity) hit.getEntity();
			target.hurt(ModDamageTypes.eyeLaser(level, player), DAMAGE_PER_TICK);
			actualEnd = hit.getLocation();
			level.sendParticles(ModParticles.LASER_SPARK,
					actualEnd.x, actualEnd.y, actualEnd.z,
					3, 0.10, 0.10, 0.10, 0.04);
		}
		ModNetworking.broadcastLaser(player, eye, actualEnd);
	}
}
