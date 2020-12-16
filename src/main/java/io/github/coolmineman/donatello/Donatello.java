package io.github.coolmineman.donatello;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Donatello implements ModInitializer {

	public static final BlueprintItem BLUEPRINT = new BlueprintItem(new FabricItemSettings().group(BitsAndChisels.OTHER_GROUP));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("donatello", "blueprints"), BLUEPRINT);
	}
}
