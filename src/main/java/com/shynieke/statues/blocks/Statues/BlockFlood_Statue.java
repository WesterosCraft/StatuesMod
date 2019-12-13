package com.shynieke.statues.blocks.Statues;

import com.shynieke.statues.blocks.IStatue;
import com.shynieke.statues.blocks.StatueBase.BlockFlood;
import com.shynieke.statues.compat.list.StatueLootList;
import com.shynieke.statues.tileentity.StatueTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockFlood_Statue extends BlockFlood implements ITileEntityProvider, IStatue {
	
	private int TIER;
	
	public BlockFlood_Statue(String unlocalised) {
		super();
		setTranslationKey(unlocalised);
	}
	
	@Override
	public Block setTier(int tier)
	{
		this.TIER = tier;
		setTranslationKey(super.getTranslationKey().replace("tile.", "") + (tier > 1 ? "t" + tier : ""));
		setRegistryName("block" + super.getTranslationKey().replace("tile.", ""));
		return this;
	}
	
	@Override
	public int getTier()
	{
		return this.TIER;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (this.TIER >= 2)
		{
			return new StatueTileEntity(this.TIER);
		}
		else
		return null;
	}
	
	private StatueTileEntity getTE(World world, BlockPos pos) {
        return (StatueTileEntity) world.getTileEntity(pos);
    }

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(this.TIER >= 2)
		{
	        if (!worldIn.isRemote) {
	        	StatueTileEntity tile = getTE(worldIn, pos);
	        	
	        	int statuetier = tile.getTier();
	        	if(statuetier != this.TIER)
	        	{
	        		tile.setTier(this.TIER);
	        	}
	        	
	        	ArrayList<ItemStack> stackList = new ArrayList<>(StatueLootList.getStacksForStatue("flood"));
	        	ItemStack stack1 = stackList.get(0);
        		ItemStack stack2 = stackList.get(1);
        		ItemStack stack3 = stackList.get(2);
        		
	        	tile.PlaySound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, pos, worldIn);
	        	tile.SpecialInteraction(false, false, true, this, playerIn, worldIn, pos, hand, hitX, hitY, hitZ);
	        	tile.GiveItem(stack1, stack2, stack3, playerIn);
	        }
	        return true;
		}
		else
		return false;
	}
}