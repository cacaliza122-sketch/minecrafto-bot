package com.example.superheroes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LaserBeamRenderer {
	private static final long LIFETIME_MS = 320L;
	private static final float SIDE_OFFSET = 0.04f;
	private static final List<Beam> BEAMS = new ArrayList<>();

	private LaserBeamRenderer() {
	}

	public static void register() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LaserBeamRenderer::render);
	}

	public static void add(Vec3 start, Vec3 end) {
		BEAMS.add(new Beam(start, end, System.currentTimeMillis()));
	}

	private static void render(WorldRenderContext context) {
		long now = System.currentTimeMillis();
		Iterator<Beam> it = BEAMS.iterator();
		while (it.hasNext()) {
			if (now - it.next().spawnedAtMs() > LIFETIME_MS) {
				it.remove();
			}
		}
		if (BEAMS.isEmpty()) {
			return;
		}
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
		for (Beam beam : BEAMS) {
			float age = (now - beam.spawnedAtMs()) / (float) LIFETIME_MS;
			float alpha = Math.max(0f, 1f - age);
			float dx = (float) (beam.end().x - beam.start().x);
			float dy = (float) (beam.end().y - beam.start().y);
			float dz = (float) (beam.end().z - beam.start().z);
			float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (len < 1e-4f) {
				continue;
			}
			float nx = dx / len;
			float ny = dy / len;
			float nz = dz / len;
			float refX = Math.abs(ny) < 0.95f ? 0f : 1f;
			float refY = Math.abs(ny) < 0.95f ? 1f : 0f;
			float sx = ny * 0f - nz * refY;
			float sy = nz * refX - nx * 0f;
			float sz = nx * refY - ny * refX;
			float sideLen = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
			if (sideLen > 1e-4f) {
				sx = sx / sideLen * SIDE_OFFSET;
				sy = sy / sideLen * SIDE_OFFSET;
				sz = sz / sideLen * SIDE_OFFSET;
			} else {
				sx = SIDE_OFFSET;
				sy = 0f;
				sz = 0f;
			}
			drawLine(buffer, matrix, beam, nx, ny, nz, 0f, 0f, 0f, 1f, 0.96f, 0.78f, alpha);
			drawLine(buffer, matrix, beam, nx, ny, nz, sx, sy, sz, 1f, 0.18f, 0.12f, alpha * 0.75f);
			drawLine(buffer, matrix, beam, nx, ny, nz, -sx, -sy, -sz, 1f, 0.18f, 0.12f, alpha * 0.75f);
		}
		ps.popPose();
	}

	private static void drawLine(VertexConsumer buffer, Matrix4f matrix, Beam beam,
			float nx, float ny, float nz,
			float ox, float oy, float oz,
			float r, float g, float b, float alpha) {
		float sx = (float) beam.start().x + ox;
		float sy = (float) beam.start().y + oy;
		float sz = (float) beam.start().z + oz;
		float ex = (float) beam.end().x + ox;
		float ey = (float) beam.end().y + oy;
		float ez = (float) beam.end().z + oz;
		buffer.addVertex(matrix, sx, sy, sz)
				.setColor(r, g, b, alpha)
				.setNormal(nx, ny, nz);
		buffer.addVertex(matrix, ex, ey, ez)
				.setColor(r, g, b, alpha)
				.setNormal(nx, ny, nz);
	}

	private record Beam(Vec3 start, Vec3 end, long spawnedAtMs) {
	}
}
