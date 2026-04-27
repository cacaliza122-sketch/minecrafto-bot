package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class LowResourceVignetteHud {
	private static final float WARN_THRESHOLD = 0.25f;
	private static final float CRITICAL_THRESHOLD = 0.10f;
	private static final int VIGNETTE_THICKNESS = 80;

	private LowResourceVignetteHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientHeroState.data().hasHero()) {
			return;
		}
		float energyMax = ClientHeroState.energyMax();
		float manaMax = ClientHeroState.manaMax();
		float energyPct = energyMax <= 0f ? 1f : ClientHeroState.data().energy() / energyMax;
		float manaPct = manaMax <= 0f ? 1f : ClientHeroState.data().mana() / manaMax;
		float worst = Math.min(energyPct, manaPct);
		if (worst >= WARN_THRESHOLD) {
			return;
		}
		float intensity = 1f - (worst / WARN_THRESHOLD);
		boolean critical = worst < CRITICAL_THRESHOLD;
		float pulse = critical ? (0.6f + 0.4f * (float) Math.abs(Math.sin(System.currentTimeMillis() / 180.0))) : 1f;
		int baseAlpha = (int) (140f * intensity * pulse);
		baseAlpha = Math.max(0, Math.min(200, baseAlpha));
		int color = critical ? 0xFF4655 : 0xF2D16B;
		int packed = (baseAlpha << 24) | color;
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();
		drawVignetteEdges(graphics, w, h, packed);
	}

	private static void drawVignetteEdges(GuiGraphics graphics, int w, int h, int color) {
		int transparent = color & 0x00FFFFFF;
		graphics.fillGradient(0, 0, w, VIGNETTE_THICKNESS, color, transparent);
		graphics.fillGradient(0, h - VIGNETTE_THICKNESS, w, h, transparent, color);
		graphics.fillGradient(0, 0, VIGNETTE_THICKNESS, h, color, transparent);
		graphics.fillGradient(w - VIGNETTE_THICKNESS, 0, w, h, transparent, color);
	}
}
