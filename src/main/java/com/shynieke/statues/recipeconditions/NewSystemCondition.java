package com.shynieke.statues.recipeconditions;

import com.google.gson.JsonObject;
import com.shynieke.statues.config.StatuesConfigGen;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class NewSystemCondition implements IConditionFactory{

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		return () -> StatuesConfigGen.general.Tier1Crafting;
	}
}