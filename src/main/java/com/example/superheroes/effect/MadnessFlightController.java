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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class MadnessFlightController {
	private static final double MIN_SPEED_FOR_BREAK = 0.05;
	private static final float HARDNESS_LIMIT = 20.0f;
	private static final int CHECKS_PER_TICK = 28;
	private static final int JITTER_RADIUS = 4;
	private static final double JAGGED_SPHERE_RADIUS_SQ = 9.0;
	private static final float SKIP_PROBABILITY = 0.32f;

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
		Vec3 dir = speedSq > MIN_SPEED_FOR_BREAK * MIN_SPEED_FOR_BREAK
				? motion.normalize()
				: player.getViewVector(1f);
		ServerLevel level = player.serverLevel();
		Vec3 head = player.position().add(0, player.getBbHeight() * 0.5, 0);
		boolean broke = false;
		for (int s = 0; s <= 2; s++) {
			Vec3 ahead = head.add(dir.scale(0.8 + s * 1.2));
			BlockPos center = BlockPos.containing(ahead);
			broke |= breakJagged(level, player, center);
		}
		if (broke) {
			level.sendParticles(ParticleTypes.EXPLOSION,
					player.getX() + dir.x, player.getY() + 1.0 + dir.y, player.getZ() + dir.z,
					1, 0.2, 0.2, 0.2, 0.0);
			if (player.tickCount % 5 == 0) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.7f);
			}
		}
	}

	private static boolean breakJagged(ServerLevel level, ServerPlayer player, BlockPos center) {
		RandomSource rand = level.getRandom();
		boolean broke = false;
		for (int i = 0; i < CHECKS_PER_TICK; i++) {
			int dx = rand.nextInt(JITTER_RADIUS * 2 + 1) - JITTER_RADIUS;
			int dy = rand.nextInt(JITTER_RADIUS * 2 + 1) - JITTER_RADIUS;
			int dz = rand.nextInt(JITTER_RADIUS * 2 + 1) - JITTER_RADIUS;
			if (dx * dx + dy * dy + dz * dz > JAGGED_SPHERE_RADIUS_SQ) {
				continue;
			}
			if (rand.nextFloat() < SKIP_PROBABILITY) {
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
		return broke;
	}
}
