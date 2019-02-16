package com.svennieke.statues.blocks.BaseBlock;

import com.svennieke.statues.config.StatuesConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BaseNormal extends BlockHorizontal implements IBucketPickupHandler, ILiquidContainer{

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static float hardness = 0.6F;
	
	protected BaseNormal(Block.Properties builder) {
		super(builder.hardnessAndResistance(hardness));
	    this.setDefaultState((IBlockState)((IBlockState)this.getDefaultState().with(HORIZONTAL_FACING, EnumFacing.NORTH)).with(WATERLOGGED, Boolean.valueOf(false)));
	}
	
	@Override
	protected void fillStateContainer(net.minecraft.state.StateContainer.Builder<Block, IBlockState> builder) {
		builder.add(HORIZONTAL_FACING, WATERLOGGED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState updatePostPlacement(IBlockState stateIn, EnumFacing facing, IBlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public float getBlockHardness(IBlockState blockState, IBlockReader worldIn, BlockPos pos) {
		if(StatuesConfig.COMMON.statueHardness.get() != null) {
			return (float) StatuesConfig.COMMON.statueHardness.get().doubleValue();
		}
		else {
			return 0.6F;
		}
	}

	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, IBlockState state) {
	    if (state.get(WATERLOGGED)) {
	      	worldIn.setBlockState(pos, (IBlockState)state.with(WATERLOGGED, Boolean.valueOf(false)), 3);
	      	return Fluids.WATER;
	    } else {
	    	return Fluids.EMPTY;
	    }
	}

	@SuppressWarnings("deprecation")
	@Override
	public IFluidState getFluidState(IBlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

	@Override
	public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, IBlockState state, Fluid fluidIn) {
		return !state.get(WATERLOGGED) && fluidIn == Fluids.WATER;
	}

	@Override
	public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn) {
		if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
			if (!worldIn.isRemote()) {
				worldIn.setBlockState(pos, (IBlockState)state.with(WATERLOGGED, Boolean.valueOf(true)), 3);
				worldIn.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		    }
		
		    return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(IBlockState state, IBlockReader reader, BlockPos pos) {
		return false;
	}
	/*
	 * @Override public boolean isOpaqueCube(IBlockState state) { return false; }
	 */

	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}

	@Override
	public IBlockState rotate(IBlockState state, Rotation rot) {
		return (IBlockState) state.with(HORIZONTAL_FACING, rot.rotate((EnumFacing) state.get(HORIZONTAL_FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState mirror(IBlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation((EnumFacing) state.get(HORIZONTAL_FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context) {
		return (IBlockState)this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
}