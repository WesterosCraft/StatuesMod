package com.shynieke.statues.blocks.Statues;

import com.shynieke.statues.blocks.IStatue;
import com.shynieke.statues.blocks.StatueBase.BlockWastelandPig;
import com.shynieke.statues.compat.list.StatueLootList;
import com.shynieke.statues.init.StatuesItems;
import com.shynieke.statues.tileentity.StatueTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockWastelandPig_Statue extends BlockWastelandPig implements ITileEntityProvider, IStatue {
	
	private int TIER;
	
	public BlockWastelandPig_Statue(String unlocalised) {
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
	        	
	        	ArrayList<ItemStack> stackList = new ArrayList<>(StatueLootList.getStacksForStatue("wasteland_pig"));
	        	ItemStack stack1 = stackList.get(0);
        		ItemStack stack2 = stackList.get(1);
        		ItemStack stack3 = stackList.get(2);
        		
        		if(stack1.getItem() != StatuesItems.tea)
        		{
        			tile.PlaySound(SoundEvents.ENTITY_PIG_AMBIENT, pos, worldIn);
    	        	tile.GiveItem(stack1, stack2, stack3, playerIn);
        		}
        		else
        		{
    	        	tile.WastelandBehavior(worldIn, pos, playerIn, stack1, stack2, stack3);
        		}
        		
        		EntityPig pig = new EntityPig(worldIn);
        		pig.setCustomNameTag("Wasteland Pig");
	        	tile.SpawnMob(pig, worldIn);
	        }
	        return true;
		}
		else
		return false;
	}
}