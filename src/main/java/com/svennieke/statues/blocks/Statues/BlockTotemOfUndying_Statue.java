package com.svennieke.statues.blocks.Statues;

import com.svennieke.statues.blocks.iStatue;
import com.svennieke.statues.blocks.StatueBase.BlockTotemOfUndying;

public class BlockTotemOfUndying_Statue extends BlockTotemOfUndying implements iStatue{
		
	public BlockTotemOfUndying_Statue(String unlocalised, String registry) {
		super();
		setUnlocalizedName(unlocalised);
		setRegistryName(registry);
	}
}