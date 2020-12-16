package io.github.coolmineman.donatello;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class Donatello implements ModInitializer {

	public static final BlueprintItem BLUEPRINT = new BlueprintItem(new FabricItemSettings());

	@Override
	public void onInitialize() {
		DefaultedList<ItemStack> defaultedList = DefaultedList.of();
		defaultedList.add(new ItemStack(BLUEPRINT));
		BitsAndChisels.OTHER_GROUP.appendStacks(defaultedList);
		Registry.register(Registry.ITEM, new Identifier("donatello", "blueprints"), BLUEPRINT);
	}
}
