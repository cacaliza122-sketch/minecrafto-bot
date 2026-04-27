package com.example.superheroes.hero;

import com.example.superheroes.ModId;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class HeroAttributes {
	public static final ResourceLocation HOMELANDER_ARMOR = ModId.of("modifiers/homelander/armor");
	public static final ResourceLocation HOMELANDER_DAMAGE = ModId.of("modifiers/homelander/damage");
	public static final ResourceLocation HOMELANDER_SPEED = ModId.of("modifiers/homelander/speed");

	private HeroAttributes() {
	}

	public static void applyHomelander(LivingEntity entity) {
		addOrReplace(entity, Attributes.ARMOR, HOMELANDER_ARMOR, 10.0, AttributeModifier.Operation.ADD_VALUE);
		addOrReplace(entity, Attributes.ATTACK_DAMAGE, HOMELANDER_DAMAGE, 6.0, AttributeModifier.Operation.ADD_VALUE);
		addOrReplace(entity, Attributes.MOVEMENT_SPEED, HOMELANDER_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	public static void removeHomelander(LivingEntity entity) {
		remove(entity, Attributes.ARMOR, HOMELANDER_ARMOR);
		remove(entity, Attributes.ATTACK_DAMAGE, HOMELANDER_DAMAGE);
		remove(entity, Attributes.MOVEMENT_SPEED, HOMELANDER_SPEED);
	}

	private static void addOrReplace(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id,
			double amount, AttributeModifier.Operation op) {
		AttributeInstance instance = entity.getAttribute(attribute);
		if (instance != null) {
			instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, op));
		}
	}

	private static void remove(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
		AttributeInstance instance = entity.getAttribute(attribute);
		if (instance != null) {
			instance.removeModifier(id);
		}
	}
}
