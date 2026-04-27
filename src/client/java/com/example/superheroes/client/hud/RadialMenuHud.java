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
	private static final int ITEM_RADIUS = 72;
	private static final int BACKPLATE_RADIUS = 96;
	private static final int SLOT_WIDTH = 104;
	private static final int SLOT_HEIGHT = 22;

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
		for (int i = 0; i < n; i++) {
			double angle = (i * 2 * Math.PI / n) - Math.PI / 2;
			int x = cx + (int) (Math.cos(angle) * ITEM_RADIUS);
			int y = cy + (int) (Math.sin(angle) * ITEM_RADIUS);
			ResourceLocation aid = abilities.get(i);
			Component name = Component.translatable("ability." + aid.getNamespace() + "." + aid.getPath());
			boolean active = i == selected;
			drawSlot(graphics, x - SLOT_WIDTH / 2, y - SLOT_HEIGHT / 2, SLOT_WIDTH, SLOT_HEIGHT, active);
			graphics.drawCenteredString(mc.font, name, x, y - 8, active ? 0xFFFFF3B0 : 0xFFEDEDF5);
			graphics.drawCenteredString(mc.font, keyForSlot(i), x, y + 4, active ? 0xFFFF4655 : 0xFF7C8499);
		}
	}

	private static void drawBackplate(GuiGraphics graphics, int cx, int cy) {
		graphics.fillGradient(cx - BACKPLATE_RADIUS, cy - BACKPLATE_RADIUS, cx + BACKPLATE_RADIUS, cy + BACKPLATE_RADIUS, 0x66101422, 0x22050710);
		graphics.fill(cx - 28, cy - 28, cx + 28, cy + 28, 0xCC070810);
		graphics.fill(cx - 34, cy - 1, cx + 34, cy + 1, 0x66F2D16B);
		graphics.fill(cx - 1, cy - 34, cx + 1, cy + 34, 0x66F2D16B);
		graphics.fill(cx - 20, cy - 20, cx + 20, cy + 20, 0xAA141722);
		graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFFFF4655);
	}

	private static void drawSlot(GuiGraphics graphics, int x, int y, int width, int height, boolean selectedSlot) {
		int top = selectedSlot ? 0xF02A1620 : 0xD0141722;
		int bottom = selectedSlot ? 0xE0140710 : 0xB0070810;
		int border = selectedSlot ? 0xCCFF4655 : 0x55F2D16B;
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
