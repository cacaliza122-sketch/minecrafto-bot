package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ResourceBarHud {
	private static final int X = 12;
	private static final int Y = 12;
	private static final int PANEL_WIDTH = 260;
	private static final int PANEL_HEIGHT = 70;
	private static final int BAR_WIDTH = 162;
	private static final int BAR_HEIGHT = 10;
	private static final int BAR_X_OFFSET = 38;
	private static final int ICON_SIZE = 18;
	private static final int VALUE_GAP = 6;

	private static final int SHADOW = 0x66000000;
	private static final int PANEL_TOP = 0xE0181C2A;
	private static final int PANEL_BOTTOM = 0xD0080A14;
	private static final int PANEL_BORDER = 0x88FFD27A;
	private static final int PANEL_HIGHLIGHT = 0x33FFFFFF;

	private static final int BAR_BG = 0xFF02030A;
	private static final int BAR_INNER_SHADOW = 0xAA000000;

	private static final int ENERGY_DARK = 0xFFB35900;
	private static final int ENERGY_BRIGHT = 0xFFFFD060;
	private static final int ENERGY_GLOW = 0x55FFE08A;
	private static final int ENERGY_ICON = 0xFFFFC538;

	private static final int MANA_DARK = 0xFF3B1F8A;
	private static final int MANA_BRIGHT = 0xFFB58CFF;
	private static final int MANA_GLOW = 0x55C7A8FF;
	private static final int MANA_ICON = 0xFFB58CFF;

	private static final int LABEL_COLOR = 0xFFEFEFF7;
	private static final int VALUE_COLOR = 0xFFEFEFF7;
	private static final int VALUE_DIM = 0xFF8B8FA0;
	private static final int HERO_NAME_COLOR = 0xFFFFE07A;

	private ResourceBarHud() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!ClientHeroState.data().hasHero()) {
			return;
		}
		float energyMax = ClientHeroState.energyMax();
		float manaMax = ClientHeroState.manaMax();
		float energy = ClientHeroState.data().energy();
		float mana = ClientHeroState.data().mana();
		float energyPct = energyMax <= 0f ? 0f : Math.min(1f, energy / energyMax);
		float manaPct = manaMax <= 0f ? 0f : Math.min(1f, mana / manaMax);

		Minecraft mc = Minecraft.getInstance();
		ResourceLocation heroId = ClientHeroState.data().heroId();
		Component heroName = Component.translatable("hero." + heroId.getNamespace() + "." + heroId.getPath());

		HudUtil.dropShadow(graphics, X, Y, PANEL_WIDTH, PANEL_HEIGHT, 3, SHADOW);
		HudUtil.roundedRectGradient(graphics, X, Y, PANEL_WIDTH, PANEL_HEIGHT, PANEL_TOP, PANEL_BOTTOM);
		HudUtil.roundedRectBorder(graphics, X, Y, PANEL_WIDTH, PANEL_HEIGHT, PANEL_BORDER);
		graphics.fill(X + 3, Y + 2, X + PANEL_WIDTH - 3, Y + 3, PANEL_HIGHLIGHT);

		graphics.drawString(mc.font, heroName, X + 12, Y + 6, HERO_NAME_COLOR, true);
		graphics.fill(X + 12, Y + 18, X + PANEL_WIDTH - 12, Y + 19, 0x33FFD27A);

		int row1Y = Y + 26;
		int row2Y = Y + 48;
		drawIcon(graphics, X + 12, row1Y - 4, ENERGY_ICON, "E");
		drawBar(graphics, mc, X + BAR_X_OFFSET, row1Y, energyPct, ENERGY_DARK, ENERGY_BRIGHT, ENERGY_GLOW);
		drawValue(graphics, mc, X + BAR_X_OFFSET + BAR_WIDTH + VALUE_GAP, row1Y - 1, energy, energyMax);

		drawIcon(graphics, X + 12, row2Y - 4, MANA_ICON, "M");
		drawBar(graphics, mc, X + BAR_X_OFFSET, row2Y, manaPct, MANA_DARK, MANA_BRIGHT, MANA_GLOW);
		drawValue(graphics, mc, X + BAR_X_OFFSET + BAR_WIDTH + VALUE_GAP, row2Y - 1, mana, manaMax);
	}

	private static void drawIcon(GuiGraphics g, int x, int y, int color, String letter) {
		HudUtil.roundedRectFill(g, x, y, ICON_SIZE, ICON_SIZE, 0xFF0A0B14);
		HudUtil.roundedRectBorder(g, x, y, ICON_SIZE, ICON_SIZE, color);
		g.drawCenteredString(Minecraft.getInstance().font, Component.literal(letter).withStyle(ChatFormatting.BOLD),
				x + ICON_SIZE / 2, y + (ICON_SIZE - 8) / 2, color);
	}

	private static void drawBar(GuiGraphics g, Minecraft mc, int x, int y, float pct, int dark, int bright, int glow) {
		HudUtil.roundedRectFill(g, x, y, BAR_WIDTH, BAR_HEIGHT, BAR_BG);
		g.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + 2, BAR_INNER_SHADOW);

		int filled = (int) (BAR_WIDTH * pct);
		if (filled >= 4) {
			HudUtil.roundedRectGradient(g, x, y, filled, BAR_HEIGHT, bright, dark);
			g.fill(x + 1, y + 1, x + filled - 1, y + 2, 0x55FFFFFF);
			if (filled - 8 > 0) {
				g.fill(x + filled - 8, y, x + filled, y + BAR_HEIGHT, glow);
			}
		} else if (filled > 0) {
			g.fillGradient(x, y, x + filled, y + BAR_HEIGHT, bright, dark);
		}
		HudUtil.roundedRectBorder(g, x, y, BAR_WIDTH, BAR_HEIGHT, 0x55000000);
	}

	private static void drawValue(GuiGraphics g, Minecraft mc, int x, int y, float value, float max) {
		String cur = String.valueOf((int) value);
		String tot = "/" + (int) max;
		g.drawString(mc.font, cur, x, y, VALUE_COLOR, true);
		g.drawString(mc.font, tot, x + mc.font.width(cur), y, VALUE_DIM, true);
	}
}
