package com.example.superheroes.item;

import com.example.superheroes.ModId;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModItemGroups {
	public static final ResourceKey<CreativeModeTab> SUPERHEROES_TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, ModId.of("superheroes"));

	public static final CreativeModeTab SUPERHEROES_TAB = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.HOMELANDER_SUIT))
			.title(Component.translatable("itemGroup.superheroes"))
			.displayItems((params, output) -> {
				output.accept(ModItems.HOMELANDER_SUIT);
				output.accept(ModItems.COMPOUND_V);
				output.accept(ModItems.MILK_BOTTLE);
			})
			.build();

	private ModItemGroups() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SUPERHEROES_TAB_KEY, SUPERHEROES_TAB);
	}
}
