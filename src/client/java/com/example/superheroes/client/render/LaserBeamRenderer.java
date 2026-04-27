package com.example.superheroes.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LaserBeamRenderer {
	private static final long LIFETIME_MS = 220L;
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
		for (Beam beam : BEAMS) {
			float age = (now - beam.spawnedAtMs()) / (float) LIFETIME_MS;
			float alpha = Math.max(0f, 1f - age);
			BeamRenderer.draw(context, beam.start(), beam.end(), alpha);
		}
	}

	private record Beam(Vec3 start, Vec3 end, long spawnedAtMs) {
	}
}
