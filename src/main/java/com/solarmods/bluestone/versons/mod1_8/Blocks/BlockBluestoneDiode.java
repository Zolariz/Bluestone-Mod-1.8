package com.solarmods.bluestone.versons.mod1_8.Blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockBluestoneDiode extends BlockDirectional {
	protected final boolean isRepeaterPowered;
	private static final String __OBFID = "CL_00000226";

	protected BlockBluestoneDiode(boolean powered) {
		super(Material.circuits);
		this.isRepeaterPowered = powered;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
	}

	public boolean isFullCube() {
		return false;
	}

	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) ? super.canPlaceBlockAt(worldIn, pos) : false;
	}

	public boolean canBlockStay(World worldIn, BlockPos pos) {
		return World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
	}

	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!this.isLocked(worldIn, pos, state)) {
			boolean flag = this.shouldBePowered(worldIn, pos, state);

			if (this.isRepeaterPowered && !flag) {
				worldIn.setBlockState(pos, this.getUnpoweredState(state), 2);
			} else if (!this.isRepeaterPowered) {
				worldIn.setBlockState(pos, this.getPoweredState(state), 2);

				if (!flag) {
					worldIn.updateBlockTick(pos, this.getPoweredState(state).getBlock(), this.getTickDelay(state), -1);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return side.getAxis() != EnumFacing.Axis.Y;
	}

	protected boolean isPowered(IBlockState state) {
		return this.isRepeaterPowered;
	}

	public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
		return this.isProvidingWeakPower(worldIn, pos, state, side);
	}

	public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
		return !this.isPowered(state) ? 0
				: (state.getValue(FACING) == side ? this.getActiveSignal(worldIn, pos, state) : 0);
	}

	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		if (this.canBlockStay(worldIn, pos)) {
			this.updateState(worldIn, pos, state);
		} else {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}
		}
	}

	protected void updateState(World worldIn, BlockPos pos, IBlockState state) {
		if (!this.isLocked(worldIn, pos, state)) {
			boolean flag = this.shouldBePowered(worldIn, pos, state);

			if ((this.isRepeaterPowered && !flag || !this.isRepeaterPowered && flag)
					&& !worldIn.isBlockTickPending(pos, this)) {
				byte b0 = -1;

				if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
					b0 = -3;
				} else if (this.isRepeaterPowered) {
					b0 = -2;
				}

				worldIn.updateBlockTick(pos, this, this.getDelay(state), b0);
			}
		}
	}

	public boolean isLocked(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		return false;
	}

	protected boolean shouldBePowered(World worldIn, BlockPos pos, IBlockState state) {
		return this.calculateInputStrength(worldIn, pos, state) > 0;
	}

	protected int calculateInputStrength(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
		BlockPos blockpos1 = pos.offset(enumfacing);
		int i = worldIn.getRedstonePower(blockpos1, enumfacing);

		if (i >= 15) {
			return i;
		} else {
			IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);
			return Math.max(i, iblockstate1.getBlock() == BluestoneBlocks.blockBluestoneWire
					? ((Integer) iblockstate1.getValue(BlockBluestoneWire.POWER)).intValue() : 0);
		}
	}

	protected int getPowerOnSides(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
		EnumFacing enumfacing1 = enumfacing.rotateY();
		EnumFacing enumfacing2 = enumfacing.rotateYCCW();
		return Math.max(this.getPowerOnSide(worldIn, pos.offset(enumfacing1), enumfacing1),
				this.getPowerOnSide(worldIn, pos.offset(enumfacing2), enumfacing2));
	}

	protected int getPowerOnSide(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return this.canPowerSide(block) ? (block == BluestoneBlocks.blockBluestoneWire
				? ((Integer) iblockstate.getValue(BlockBluestoneWire.POWER)).intValue()
				: worldIn.getStrongPower(pos, side)) : 0;
	}

	public boolean canProvidePower() {
		return true;
	}

	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		if (this.shouldBePowered(worldIn, pos, state)) {
			worldIn.scheduleUpdate(pos, this, 1);
		}
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.notifyNeighbors(worldIn, pos, state);
	}

	protected void notifyNeighbors(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
		BlockPos blockpos1 = pos.offset(enumfacing.getOpposite());
		if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos),
				java.util.EnumSet.of(enumfacing.getOpposite())).isCanceled())
			return;
		worldIn.notifyBlockOfStateChange(blockpos1, this);
		worldIn.notifyNeighborsOfStateExcept(blockpos1, this, enumfacing);
	}

	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		if (this.isRepeaterPowered) {
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}
		}

		super.onBlockDestroyedByPlayer(worldIn, pos, state);
	}

	public boolean isOpaqueCube() {
		return false;
	}

	protected boolean canPowerSide(Block blockIn) {
		return blockIn.canProvidePower();
	}

	protected int getActiveSignal(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
		return 15;
	}

	public static boolean isRedstoneRepeaterBlockID(Block blockIn) {
		return BluestoneBlocks.blockBluestoneRepeaterOff.isAssociatedBlock(blockIn)
				|| Blocks.unpowered_comparator.isAssociated(blockIn);
	}

	public boolean isAssociated(Block other) {
		return other == this.getPoweredState(this.getDefaultState()).getBlock()
				|| other == this.getUnpoweredState(this.getDefaultState()).getBlock();
	}

	public boolean isFacingTowardsRepeater(World worldIn, BlockPos pos, IBlockState state) {
		EnumFacing enumfacing = ((EnumFacing) state.getValue(FACING)).getOpposite();
		BlockPos blockpos1 = pos.offset(enumfacing);
		return isRedstoneRepeaterBlockID(worldIn.getBlockState(blockpos1).getBlock())
				? worldIn.getBlockState(blockpos1).getValue(FACING) != enumfacing : false;
	}

	protected int getTickDelay(IBlockState state) {
		return this.getDelay(state);
	}

	protected abstract int getDelay(IBlockState state);

	protected abstract IBlockState getPoweredState(IBlockState unpoweredState);

	protected abstract IBlockState getUnpoweredState(IBlockState poweredState);

	public boolean isAssociatedBlock(Block other) {
		return this.isAssociated(other);
	}

	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}
}