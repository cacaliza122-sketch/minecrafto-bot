package com.example.superheroes.item;

import com.example.superheroes.attachment.ModAttachments;
import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.Heroes;
import com.example.superheroes.network.ModNetworking;
import com.example.superheroes.transform.HeroData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CompoundVItem extends Item {
	private static final float MANA_PER_USE = 50f;

	public CompoundVItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(stack);
		}
		HeroData data = serverPlayer.getAttachedOrCreate(ModAttachments.HERO_DATA);
		if (!data.hasHero()) {
			return InteractionResultHolder.fail(stack);
		}
		Hero hero = Heroes.get(data.heroId());
		if (hero == null) {
			return InteractionResultHolder.fail(stack);
		}
		float newMana = Math.min(hero.getManaMax(), data.mana() + MANA_PER_USE);
		if (newMana <= data.mana()) {
			return InteractionResultHolder.fail(stack);
		}
		HeroData updated = data.withMana(newMana);
		serverPlayer.setAttached(ModAttachments.HERO_DATA, updated);
		ModNetworking.syncResources(serverPlayer, updated);
		serverPlayer.serverLevel().playSound(null,
				serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
				SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1f, 1.1f);
		if (!serverPlayer.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResultHolder.consume(stack);
	}
}
