package com.example.superheroes.item;

import com.example.superheroes.hero.HomelanderHero;
import com.example.superheroes.transform.TransformationItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class HomelanderSuitItem extends TransformationItem {
	public HomelanderSuitItem(Properties properties) {
		super(HomelanderHero.ID, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.superheroes.homelander_suit.lore.line1").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.superheroes.homelander_suit.lore.line2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.empty());
		tooltip.add(Component.translatable("item.superheroes.homelander_suit.lore.usage").withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("item.superheroes.homelander_suit.lore.untransform").withStyle(ChatFormatting.YELLOW));
	}
}
