package com.example.superheroes.item;

import com.example.superheroes.ModId;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
	public static final HomelanderSuitItem HOMELANDER_SUIT = register(
			"homelander_suit",
			new HomelanderSuitItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final CompoundVItem COMPOUND_V = register(
			"compound_v",
			new CompoundVItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))
	);

	private ModItems() {
	}

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM, ModId.of(name), item);
	}

	public static void init() {
	}
}
