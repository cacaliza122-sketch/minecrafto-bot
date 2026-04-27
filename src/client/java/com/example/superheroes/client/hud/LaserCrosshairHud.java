package com.example.superheroes.client.hud;

import com.example.superheroes.ability.AbilityIds;
import com.example.superheroes.client.ClientHeroState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class LaserCrosshairHud {
	private static final int CORE_HALF = 4;
	private static final int RING_HALF = 14;
	private static final int OUTER_HALF = 28;

	private LaserCrosshairHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientHeroState.data().isActive(AbilityIds.EYE_LASERS)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.getCameraType() != null && !mc.options.getCameraType().isFirstPerson()) {
			return;
		}
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();
		int cx = w / 2;
		int cy = h / 2;
		float pulse = 0.85f + 0.15f * (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.012));
		int outerA = (int) (90f * pulse);
		int ringA = (int) (180f * pulse);
		int coreA = (int) (220f * pulse);
		int outer = (outerA << 24) | 0xFF2A18;
		int ring = (ringA << 24) | 0xFF6A2A;
		int core = (coreA << 24) | 0xFFEEC0;
		int outerEdge = 0x00FF2A18;
		graphics.fillGradient(cx - OUTER_HALF, cy - OUTER_HALF, cx + OUTER_HALF, cy + OUTER_HALF, outer, outerEdge);
		graphics.fill(cx - RING_HALF, cy - RING_HALF, cx + RING_HALF, cy - RING_HALF + 2, ring);
		graphics.fill(cx - RING_HALF, cy + RING_HALF - 2, cx + RING_HALF, cy + RING_HALF, ring);
		graphics.fill(cx - RING_HALF, cy - RING_HALF, cx - RING_HALF + 2, cy + RING_HALF, ring);
		graphics.fill(cx + RING_HALF - 2, cy - RING_HALF, cx + RING_HALF, cy + RING_HALF, ring);
		graphics.fill(cx - CORE_HALF, cy - 1, cx + CORE_HALF, cy + 1, core);
		graphics.fill(cx - 1, cy - CORE_HALF, cx + 1, cy + CORE_HALF, core);
	}
}
