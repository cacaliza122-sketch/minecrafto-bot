package com.example.superheroes.client.render;

import com.example.superheroes.client.ClientHeroState;
import com.example.superheroes.client.RemoteHeroSkins;
import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.Heroes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HeroSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public HeroSkinLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		ResourceLocation heroId;
		if (player == Minecraft.getInstance().player) {
			if (!ClientHeroState.data().hasHero()) {
				return;
			}
			heroId = ClientHeroState.data().heroId();
		} else {
			heroId = RemoteHeroSkins.get(player.getUUID());
			if (heroId == null) {
				return;
			}
		}
		Hero hero = Heroes.get(heroId);
		if (hero == null) {
			return;
		}
		ResourceLocation texture = hero.getSkinTexture();
		if (texture == null) {
			return;
		}
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
		this.getParentModel().renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
	}
}
