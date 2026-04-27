package com.example.superheroes.client.mixin;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientHeroState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LocalPlayerFlightMixin {
	private static final double MAX_HORIZONTAL_SPEED = 1.5;
	private static final double MAX_VERTICAL_SPEED = 1.0;
	private static final double ACCEL = 0.12;
	private static final double FRICTION_HORIZONTAL = 0.92;
	private static final double FRICTION_VERTICAL = 0.90;

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void superheroes$inertialFlight(Vec3 input, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.level().isClientSide || !(self instanceof LocalPlayer player)) {
			return;
		}
		if (!ClientHeroState.data().isActive(AbilityIds.FLIGHT)) {
			return;
		}

		float forward = player.zza;
		float strafe = player.xxa;
		boolean jumping = player.input != null && player.input.jumping;
		boolean sneaking = player.input != null && player.input.shiftKeyDown;

		float yawRad = (float) Math.toRadians(player.getYRot());
		float pitchRad = (float) Math.toRadians(player.getXRot());
		double sinYaw = Math.sin(yawRad);
		double cosYaw = Math.cos(yawRad);
		double sinPitch = Math.sin(pitchRad);
		double cosPitch = Math.cos(pitchRad);

		double inputForwardX = -sinYaw * cosPitch;
		double inputForwardY = -sinPitch;
		double inputForwardZ = cosYaw * cosPitch;

		double inputStrafeX = cosYaw;
		double inputStrafeZ = sinYaw;

		double accelX = inputForwardX * forward + inputStrafeX * strafe;
		double accelY = inputForwardY * forward;
		double accelZ = inputForwardZ * forward + inputStrafeZ * strafe;

		if (jumping) {
			accelY += 1.0;
		}
		if (sneaking) {
			accelY -= 1.0;
		}

		double inputMag = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
		if (inputMag > 1e-4) {
			double scale = ACCEL / Math.max(inputMag, 1.0);
			accelX *= scale;
			accelY *= scale;
			accelZ *= scale;
		} else {
			accelX = 0;
			accelY = 0;
			accelZ = 0;
		}

		Vec3 motion = self.getDeltaMovement();
		double mx = motion.x + accelX;
		double my = motion.y + accelY;
		double mz = motion.z + accelZ;

		double horizSq = mx * mx + mz * mz;
		if (horizSq > MAX_HORIZONTAL_SPEED * MAX_HORIZONTAL_SPEED) {
			double s = MAX_HORIZONTAL_SPEED / Math.sqrt(horizSq);
			mx *= s;
			mz *= s;
		}
		if (Math.abs(my) > MAX_VERTICAL_SPEED) {
			my = Math.signum(my) * MAX_VERTICAL_SPEED;
		}

		boolean noHorizInput = forward == 0 && strafe == 0;
		boolean noVertInput = !jumping && !sneaking && forward == 0;
		if (noHorizInput) {
			mx *= FRICTION_HORIZONTAL;
			mz *= FRICTION_HORIZONTAL;
		}
		if (noVertInput) {
			my *= FRICTION_VERTICAL;
		}

		Vec3 newMotion = new Vec3(mx, my, mz);
		self.setDeltaMovement(newMotion);
		self.move(MoverType.SELF, newMotion);
		self.fallDistance = 0f;
		ci.cancel();
	}
}
