package com.example.superheroes.client.hud;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.ModKeys;
import com.example.superheroes.network.ActivateAbilityC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RadialMenuHud {
	private static final float DEAD_ZONE = 5f;
	private static final int ITEM_RADIUS = 110;
	private static final int BACKPLATE_RADIUS = 104;
	private static final int SLOT_MIN_WIDTH = 96;
	private static final int SLOT_HEIGHT = 26;
	private static final int SLOT_PADDING_X = 14;
	private static final int CURSOR_RADIUS = 56;

	private static final int COLOR_TEXT_IDLE = 0xFFEDEDF5;
	private static final int COLOR_TEXT_ACTIVE = 0xFFFFF3B0;
	private static final int COLOR_KEY_IDLE = 0xFF7C8499;
	private static final int COLOR_KEY_ACTIVE = 0xFFFF4655;
	private static final int COLOR_BORDER_IDLE = 0x55F2D16B;
	private static final int COLOR_BORDER_ACTIVE = 0xFFFF4655;
	private static final int COLOR_GLOW = 0x44FFD27A;

	private static boolean open;
	private static float startYaw;
	private static float startPitch;
	private static int selected = -1;

	private RadialMenuHud() {
	}

	public static void clientTick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			closeWithoutActivate();
			return;
		}
		boolean down = ModKeys.RADIAL != null && ModKeys.RADIAL.isDown();
		List<ResourceLocation> abilities = ClientHeroState.abilities();
		if (down && !open) {
			if (abilities.isEmpty()) {
				return;
			}
			open = true;
			startYaw = mc.player.getYRot();
			startPitch = mc.player.getXRot();
			selected = -1;
			return;
		}
		if (!down && open) {
			closeAndActivate(abilities);
			return;
		}
		if (open) {
			updateSelection(mc, abilities.size());
		}
	}

	private static void updateSelection(Minecraft mc, int n) {
		if (n <= 0) {
			selected = -1;
			return;
		}
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		if (magSq < DEAD_ZONE * DEAD_ZONE) {
			selected = -1;
			return;
		}
		double angle = Math.toDegrees(Math.atan2(dpitch, dyaw)) + 90.0;
		angle = ((angle % 360.0) + 360.0) % 360.0;
		float per = 360f / n;
		selected = ((int) Math.floor(angle / per)) % n;
	}

	private static void closeAndActivate(List<ResourceLocation> abilities) {
		int idx = selected;
		open = false;
		selected = -1;
		if (idx >= 0 && idx < abilities.size()) {
			ClientPlayNetworking.send(new ActivateAbilityC2SPayload(abilities.get(idx)));
		}
	}

	private static void closeWithoutActivate() {
		open = false;
		selected = -1;
	}

	public static boolean isOpen() {
		return open;
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!open) {
			return;
		}
		List<ResourceLocation> abilities = ClientHeroState.abilities();
		if (abilities.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int n = abilities.size();
		drawBackplate(graphics, cx, cy);
		drawCursor(graphics, mc, cx, cy);
		for (int i = 0; i < n; i++) {
			double angle = (i * 2 * Math.PI / n) - Math.PI / 2;
			int x = cx + (int) (Math.cos(angle) * ITEM_RADIUS);
			int y = cy + (int) (Math.sin(angle) * ITEM_RADIUS);
			ResourceLocation aid = abilities.get(i);
			Component name = Component.translatable("ability." + aid.getNamespace() + "." + aid.getPath());
			Component key = keyForSlot(i);
			boolean active = i == selected;
			int textWidth = mc.font.width(name);
			int slotWidth = Math.max(SLOT_MIN_WIDTH, textWidth + SLOT_PADDING_X * 2);
			int slotX = x - slotWidth / 2;
			int slotY = y - SLOT_HEIGHT / 2;
			drawSlot(graphics, slotX, slotY, slotWidth, SLOT_HEIGHT, active);
			graphics.drawCenteredString(mc.font, name, x, y - 9, active ? COLOR_TEXT_ACTIVE : COLOR_TEXT_IDLE);
			graphics.drawCenteredString(mc.font, key, x, y + 3, active ? COLOR_KEY_ACTIVE : COLOR_KEY_IDLE);
		}
	}

	private static void drawBackplate(GuiGraphics graphics, int cx, int cy) {
		graphics.fillGradient(cx - BACKPLATE_RADIUS, cy - BACKPLATE_RADIUS,
				cx + BACKPLATE_RADIUS, cy + BACKPLATE_RADIUS, 0x66101422, 0x22050710);
		graphics.fill(cx - 30, cy - 30, cx + 30, cy + 30, 0xCC070810);
		graphics.fill(cx - 30, cy - 30, cx + 30, cy - 29, COLOR_BORDER_IDLE);
		graphics.fill(cx - 30, cy + 29, cx + 30, cy + 30, 0x66000000);
		graphics.fill(cx - 30, cy - 30, cx - 29, cy + 30, COLOR_BORDER_IDLE);
		graphics.fill(cx + 29, cy - 30, cx + 30, cy + 30, 0x55000000);
	}

	private static void drawCursor(GuiGraphics graphics, Minecraft mc, int cx, int cy) {
		if (mc.player == null) {
			return;
		}
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		boolean inDeadZone = magSq < DEAD_ZONE * DEAD_ZONE;
		if (inDeadZone) {
			graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0xFFEDEDF5);
			return;
		}
		double a = Math.atan2(dpitch, dyaw);
		double r = CURSOR_RADIUS;
		int px = cx + (int) Math.round(r * Math.cos(a));
		int py = cy + (int) Math.round(r * Math.sin(a));
		drawDottedLine(graphics, cx, cy, px, py, COLOR_GLOW);
		graphics.fill(px - 6, py - 1, px + 7, py + 2, COLOR_BORDER_ACTIVE);
		graphics.fill(px - 1, py - 6, px + 2, py + 7, COLOR_BORDER_ACTIVE);
		graphics.fill(px - 3, py - 3, px + 4, py + 4, COLOR_TEXT_ACTIVE);
	}

	private static void drawDottedLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
		int dx = x1 - x0;
		int dy = y1 - y0;
		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		if (steps == 0) {
			return;
		}
		float fx = (float) dx / steps;
		float fy = (float) dy / steps;
		for (int i = 0; i < steps; i += 4) {
			int px = x0 + Math.round(fx * i);
			int py = y0 + Math.round(fy * i);
			graphics.fill(px, py, px + 2, py + 2, color);
		}
	}

	private static void drawSlot(GuiGraphics graphics, int x, int y, int width, int height, boolean selectedSlot) {
		if (selectedSlot) {
			graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, COLOR_GLOW);
		}
		int top = selectedSlot ? 0xF02A1620 : 0xD0141722;
		int bottom = selectedSlot ? 0xE0140710 : 0xB0070810;
		int border = selectedSlot ? COLOR_BORDER_ACTIVE : COLOR_BORDER_IDLE;
		graphics.fillGradient(x, y, x + width, y + height, top, bottom);
		graphics.fill(x, y, x + width, y + 1, border);
		graphics.fill(x, y + height - 1, x + width, y + height, 0x66000000);
		graphics.fill(x, y, x + 1, y + height, border);
		graphics.fill(x + width - 1, y, x + width, y + height, 0x55000000);
	}

	private static Component keyForSlot(int index) {
		if (ModKeys.ABILITY_SLOTS != null && index < ModKeys.ABILITY_SLOTS.length) {
			return ModKeys.ABILITY_SLOTS[index].getTranslatedKeyMessage();
		}
		return Component.literal(String.valueOf(index + 1));
	}
}
