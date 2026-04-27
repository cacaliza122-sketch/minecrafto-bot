package com.example.superheroes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BeamRenderer {
	private BeamRenderer() {
	}

	public static void draw(WorldRenderContext ctx, Vec3 start, Vec3 end, float intensity) {
		MultiBufferSource consumers = ctx.consumers();
		if (consumers == null) {
			return;
		}
		Vec3 cam = ctx.camera().getPosition();
		PoseStack ps = ctx.matrixStack();
		ps.pushPose();
		ps.translate(-cam.x, -cam.y, -cam.z);
		VertexConsumer buf = consumers.getBuffer(RenderType.lightning());
		Matrix4f matrix = ps.last().pose();

		Vec3 beam = end.subtract(start);
		double len = beam.length();
		if (len < 1e-3) {
			ps.popPose();
			return;
		}
		Vec3 beamDir = beam.scale(1.0 / len);
		Vec3 mid = start.add(end).scale(0.5);
		Vec3 toCam = cam.subtract(mid);
		Vec3 side = beamDir.cross(toCam);
		if (side.length() < 1e-3) {
			side = beamDir.cross(new Vec3(0, 1, 0));
			if (side.length() < 1e-3) {
				side = new Vec3(1, 0, 0);
			}
		}
		side = side.normalize();

		long t = System.currentTimeMillis();
		float pulse = 0.92f + 0.08f * (float) Math.sin(t * 0.018);
		float a = intensity;

		drawQuadDouble(buf, matrix, start, end, side, 0.22 * pulse, 1f, 0.18f, 0.10f, 0.55f * a);
		drawQuadDouble(buf, matrix, start, end, side, 0.13 * pulse, 1f, 0.55f, 0.28f, 0.85f * a);
		drawQuadDouble(buf, matrix, start, end, side, 0.06 * pulse, 1f, 0.95f, 0.85f, 1f * a);
		drawQuadDouble(buf, matrix, start, end, side, 0.022 * pulse, 1f, 1f, 1f, 1f * a);

		ps.popPose();
	}

	private static void drawQuadDouble(VertexConsumer buf, Matrix4f matrix,
			Vec3 a, Vec3 b, Vec3 side, double width,
			float r, float g, float bl, float alpha) {
		float w = (float) width;
		Vec3 sw = side.scale(w);
		Vec3 a0 = a.add(sw);
		Vec3 a1 = a.subtract(sw);
		Vec3 b0 = b.add(sw);
		Vec3 b1 = b.subtract(sw);
		addV(buf, matrix, a0, r, g, bl, alpha);
		addV(buf, matrix, a1, r, g, bl, alpha);
		addV(buf, matrix, b1, r, g, bl, alpha);
		addV(buf, matrix, b0, r, g, bl, alpha);
		addV(buf, matrix, a0, r, g, bl, alpha);
		addV(buf, matrix, b0, r, g, bl, alpha);
		addV(buf, matrix, b1, r, g, bl, alpha);
		addV(buf, matrix, a1, r, g, bl, alpha);
	}

	private static void addV(VertexConsumer buf, Matrix4f matrix, Vec3 v,
			float r, float g, float b, float alpha) {
		buf.addVertex(matrix, (float) v.x, (float) v.y, (float) v.z)
				.setColor(r, g, b, alpha);
	}
}
