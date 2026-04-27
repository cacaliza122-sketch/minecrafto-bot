package com.example.superheroes.client.render;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientHeroState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class LocalLaserOverlay {
	private static final double RANGE = 64.0;
	private static final float SIDE_OFFSET = 0.05f;
	private static final float OUTER_OFFSET = 0.12f;

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
		float partial = context.tickCounter().getGameTimeDeltaPartialTick(false);
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

		Vec3 offset = eye.add(dir.scale(0.4));
		drawBeam(context, offset, actualEnd);
	}

	static void drawBeam(WorldRenderContext context, Vec3 start, Vec3 end) {
		MultiBufferSource consumers = context.consumers();
		if (consumers == null) {
			return;
		}
		Vec3 cam = context.camera().getPosition();
		PoseStack ps = context.matrixStack();
		ps.pushPose();
		ps.translate(-cam.x, -cam.y, -cam.z);
		VertexConsumer buffer = consumers.getBuffer(RenderType.lines());
		Matrix4f matrix = ps.last().pose();

		float dx = (float) (end.x - start.x);
		float dy = (float) (end.y - start.y);
		float dz = (float) (end.z - start.z);
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 1e-4f) {
			ps.popPose();
			return;
		}
		float nx = dx / len;
		float ny = dy / len;
		float nz = dz / len;

		float refX = Math.abs(ny) < 0.95f ? 0f : 1f;
		float refY = Math.abs(ny) < 0.95f ? 1f : 0f;
		float ux = ny * 0f - nz * refY;
		float uy = nz * refX - nx * 0f;
		float uz = nx * refY - ny * refX;
		float ulen = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
		if (ulen > 1e-4f) {
			ux /= ulen;
			uy /= ulen;
			uz /= ulen;
		} else {
			ux = 1f;
			uy = 0f;
			uz = 0f;
		}
		float vx = ny * uz - nz * uy;
		float vy = nz * ux - nx * uz;
		float vz = nx * uy - ny * ux;

		drawLine(buffer, matrix, start, end, nx, ny, nz, 0f, 0f, 0f, 1f, 1f, 0.92f, 1f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, ux * SIDE_OFFSET, uy * SIDE_OFFSET, uz * SIDE_OFFSET, 1f, 0.95f, 0.5f, 0.95f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, -ux * SIDE_OFFSET, -uy * SIDE_OFFSET, -uz * SIDE_OFFSET, 1f, 0.95f, 0.5f, 0.95f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, vx * SIDE_OFFSET, vy * SIDE_OFFSET, vz * SIDE_OFFSET, 1f, 0.95f, 0.5f, 0.95f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, -vx * SIDE_OFFSET, -vy * SIDE_OFFSET, -vz * SIDE_OFFSET, 1f, 0.95f, 0.5f, 0.95f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, ux * OUTER_OFFSET, uy * OUTER_OFFSET, uz * OUTER_OFFSET, 1f, 0.25f, 0.18f, 0.7f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, -ux * OUTER_OFFSET, -uy * OUTER_OFFSET, -uz * OUTER_OFFSET, 1f, 0.25f, 0.18f, 0.7f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, vx * OUTER_OFFSET, vy * OUTER_OFFSET, vz * OUTER_OFFSET, 1f, 0.25f, 0.18f, 0.7f);
		drawLine(buffer, matrix, start, end, nx, ny, nz, -vx * OUTER_OFFSET, -vy * OUTER_OFFSET, -vz * OUTER_OFFSET, 1f, 0.25f, 0.18f, 0.7f);

		ps.popPose();
	}

	private static void drawLine(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end,
			float nx, float ny, float nz,
			float ox, float oy, float oz,
			float r, float g, float b, float alpha) {
		float sx = (float) start.x + ox;
		float sy = (float) start.y + oy;
		float sz = (float) start.z + oz;
		float ex = (float) end.x + ox;
		float ey = (float) end.y + oy;
		float ez = (float) end.z + oz;
		buffer.addVertex(matrix, sx, sy, sz)
				.setColor(r, g, b, alpha)
				.setNormal(nx, ny, nz);
		buffer.addVertex(matrix, ex, ey, ez)
				.setColor(r, g, b, alpha)
				.setNormal(nx, ny, nz);
	}
}
