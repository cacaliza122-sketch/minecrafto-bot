package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ResourceBarHud {
	private static final int PANEL_WIDTH = 236;
	private static final int PANEL_HEIGHT = 58;
	private static final int BAR_WIDTH = 124;
	private static final int BAR_HEIGHT = 7;
	private static final int X = 12;
	private static final int Y = 12;
	private static final int GAP = 9;

	private static final int PANEL_TOP = 0xD4141722;
	private static final int PANEL_BOTTOM = 0xB4070810;
	private static final int PANEL_BORDER = 0x66F2D16B;
	private static final int BAR_BG = 0xD0050610;
	private static final int ENERGY_START = 0xFFFFF176;
	private static final int ENERGY_END = 0xFFFF8F00;
	private static final int MANA_START = 0xFF66D9FF;
	private static final int MANA_END = 0xFF4169E1;

	private ResourceBarHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientHeroState.data().hasHero()) {
			return;
		}
		float energyMax = ClientHeroState.energyMax();
		float manaMax = ClientHeroState.manaMax();
		float energyPct = energyMax <= 0f ? 0f : Math.min(1f, ClientHeroState.data().energy() / energyMax);
		float manaPct = manaMax <= 0f ? 0f : Math.min(1f, ClientHeroState.data().mana() / manaMax);

		Minecraft mc = Minecraft.getInstance();
		ResourceLocation heroId = ClientHeroState.data().heroId();
		Component heroName = Component.translatable("hero." + heroId.getNamespace() + "." + heroId.getPath());
		drawPanel(graphics, X, Y, PANEL_WIDTH, PANEL_HEIGHT);
		graphics.drawString(mc.font, heroName, X + 12, Y + 8, 0xFFFFF3B0, false);
		drawBar(graphics, mc, X + 12, Y + 24, energyPct, ENERGY_START, ENERGY_END, "ENERGY", ClientHeroState.data().energy(), energyMax);
		drawBar(graphics, mc, X + 12, Y + 24 + BAR_HEIGHT + GAP, manaPct, MANA_START, MANA_END, "MANA", ClientHeroState.data().mana(), manaMax);
	}

	private static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
		g.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOTTOM);
		g.fill(x, y, x + w, y + 1, PANEL_BORDER);
		g.fill(x, y + h - 1, x + w, y + h, 0x44000000);
		g.fill(x, y, x + 1, y + h, 0x44FFFFFF);
		g.fill(x + w - 1, y, x + w, y + h, 0x55000000);
	}

	private static void drawBar(GuiGraphics g, Minecraft mc, int x, int y, float pct, int startColor, int endColor, String label, float value, float max) {
		g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BG);
		int filled = (int) (BAR_WIDTH * pct);
		if (filled > 0) {
			g.fillGradient(x, y, x + filled, y + BAR_HEIGHT, startColor, endColor);
			g.fill(x, y, x + filled, y + 1, 0x66FFFFFF);
		}
		String text = String.format("%s  %d/%d", label, (int) value, (int) max);
		g.drawString(mc.font, text, x + BAR_WIDTH + 7, y - 1, 0xFFEDEDF5, false);
	}
}
