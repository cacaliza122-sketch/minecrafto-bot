package com.example.superheroes.effect;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.transform.HeroData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class MadnessFlightController {
	private static final double MIN_SPEED_FOR_BREAK = 0.35;
	private static final int BREAK_RADIUS = 2;
	private static final float HARDNESS_LIMIT = 20.0f;

	private MadnessFlightController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});
	}

	private static void tick(ServerPlayer player) {
		if (!ModEffects.isMadness(player)) {
			return;
		}
		HeroData data = player.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero() || !data.isActive(AbilityIds.FLIGHT)) {
			return;
		}
		Vec3 motion = player.getDeltaMovement();
		double speedSq = motion.lengthSqr();
		if (speedSq < MIN_SPEED_FOR_BREAK * MIN_SPEED_FOR_BREAK) {
			return;
		}
		if (!player.horizontalCollision && !player.verticalCollision && !player.minorHorizontalCollision) {
			return;
		}
		Vec3 dir = motion.normalize();
		Vec3 ahead = player.position().add(0, player.getBbHeight() * 0.5, 0).add(dir.scale(0.6));
		breakAround(player.serverLevel(), player, BlockPos.containing(ahead));
	}

	private static void breakAround(ServerLevel level, ServerPlayer player, BlockPos center) {
		boolean broke = false;
		for (int dx = -BREAK_RADIUS; dx <= BREAK_RADIUS; dx++) {
			for (int dy = -BREAK_RADIUS; dy <= BREAK_RADIUS; dy++) {
				for (int dz = -BREAK_RADIUS; dz <= BREAK_RADIUS; dz++) {
					if (dx * dx + dy * dy + dz * dz > BREAK_RADIUS * BREAK_RADIUS) {
						continue;
					}
					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || state.liquid()) {
						continue;
					}
					float hardness = state.getDestroySpeed(level, pos);
					if (hardness < 0f || hardness >= HARDNESS_LIMIT) {
						continue;
					}
					level.destroyBlock(pos, false, player);
					broke = true;
				}
			}
		}
		if (broke) {
			level.sendParticles(ParticleTypes.EXPLOSION,
					center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
					1, 0, 0, 0, 0);
			level.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(),
					SoundSource.PLAYERS, 0.4f, 1.6f);
		}
	}
}
