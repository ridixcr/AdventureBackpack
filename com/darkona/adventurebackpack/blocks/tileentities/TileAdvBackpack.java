package com.darkona.adventurebackpack.blocks.tileentities;

import com.darkona.adventurebackpack.blocks.BlockSleepingBag;
import com.darkona.adventurebackpack.blocks.ABPBlocks;
import com.darkona.adventurebackpack.common.BackpackAbilities;
import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.common.IAdvBackpack;
import com.darkona.adventurebackpack.common.Utils;
import com.darkona.adventurebackpack.config.BlockInfo;
import com.darkona.adventurebackpack.items.ItemAdvBackpack;
import com.darkona.adventurebackpack.items.ABPItems;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidTank;

public class TileAdvBackpack extends TileEntity implements IAdvBackpack {

	public ItemStack[] inventory;
	public FluidTank leftTank;
	public FluidTank rightTank;
	public boolean needsUpdate = false;
	public boolean equipped = false;
	public boolean sleepingBagDeployed = false;
	private int sbdir;
	private int sbx;
	private int sby;
	private int sbz;
	private int checkTime = 0;
	private String color;
	private String colorName;
	public int lastTime;
	public int luminosity;
	
	public TileAdvBackpack() {
		leftTank = new FluidTank(Constants.tankCapacity);
		rightTank = new FluidTank(Constants.tankCapacity);
		inventory = new ItemStack[Constants.inventorySize];
		setColor("Standard");
		setColorName("Standard");
		luminosity = 0;
		lastTime = 0;
		checkTime = 0;
	}

	public void setColorName(String string) {
		this.colorName = string;
	}

	public String getColorName() {
		return this.colorName;
	};

	public boolean isSBDeployed() {
		return this.sleepingBagDeployed;
	}

	public boolean deploySleepingBag(EntityPlayer player, World world, int x, int y, int z, int meta) {
		Block sleepingBag = ABPBlocks.sleepingbag;
		if (world.setBlock(x, y, z, BlockInfo.SLEEPINGBAG_ID, meta, 3))
		{
			world.playSoundAtEntity(player, Block.soundClothFootstep.getPlaceSound(), 0.5f, 1.0f);
			sbx = x;
			sby = y;
			sbz = z;
			sbdir=meta;
			return sleepingBagDeployed = ((BlockSleepingBag) sleepingBag).placeBlock(world, x,y,z,meta);
		}
		return false;
	}

	public boolean removeSleepingBag(World world) {
		if (sleepingBagDeployed)
		{
			if (world.getBlockId(sbx, sby, sbz) == ABPBlocks.sleepingbag.blockID)
			{
				world.setBlock(sbx, sby, sbz, 0);
				world.removeBlockTileEntity(sbx, sby, sbz);
				// TODO play a sound here
				this.sleepingBagDeployed = false;
				saveChanges();
				return true;
			}
		}else{
			this.sleepingBagDeployed = false;
			saveChanges();
		}
		return false;
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		// super.onDataPacket(net, pkt);
		readFromNBT(pkt.data);

	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = writeToNBT();
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void updateEntity() {
		if (!colorName.isEmpty())
			BackpackAbilities.instance.executeAbility(null, this.worldObj, this);
		if(checkTime == 0){
			int lastLumen = luminosity;
			int left = (leftTank.getFluid() != null) ? leftTank.getFluid().getFluid().getLuminosity() : 0;
			int right = (rightTank.getFluid() != null) ? rightTank.getFluid().getFluid().getLuminosity() : 0;
			luminosity = Math.max(left, right);
			if (luminosity != lastLumen)
			{
				int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
				worldObj.setBlock(xCoord, yCoord, zCoord, ABPBlocks.advbackpack.blockID, meta, 3);
				worldObj.setLightValue(EnumSkyBlock.Block, xCoord, yCoord, zCoord, luminosity);
			}
			if(worldObj.getBlockId(sbx, sby, sbz) != ABPBlocks.sleepingbag.blockID)
			{
				sleepingBagDeployed = false;
			}
			checkTime = 40;
			saveChanges();
		}else{
			checkTime--;
		}
		super.updateEntity();
	}

	public boolean equip(World world, EntityPlayer player, int x, int y, int z) {
		ItemStack stacky = new ItemStack(ABPItems.advBackpack, 1);
		stacky.stackTagCompound = this.writeToNBT();
		removeSleepingBag(world);

		if (player.inventory.armorInventory[2] == null)
		{
			player.inventory.armorInventory[2] = stacky;
			return true;

		} else if (player.inventory.addItemStackToInventory(stacky))
		{
			return true;
		} else
		{
			return drop(world,player, x, y, z);
		}
	}

	public boolean drop(World world, EntityPlayer player, int x, int y, int z) {
		removeSleepingBag(world);
		if(player.capabilities.isCreativeMode) return true;
		ItemStack stacky = new ItemStack(ABPItems.advBackpack, 1);
		stacky.stackTagCompound = this.writeToNBT();

		float spawnX = x + world.rand.nextFloat();
		float spawnY = y + world.rand.nextFloat();
		float spawnZ = z + world.rand.nextFloat();

		EntityItem droppedItem = new EntityItem(world, spawnX, spawnY, spawnZ, stacky);

		float mult = 0.05F;

		droppedItem.motionX = (-0.5F + world.rand.nextFloat()) * mult;
		droppedItem.motionY = (4 + world.rand.nextFloat()) * mult;
		droppedItem.motionZ = (-0.5F + world.rand.nextFloat()) * mult;

		return world.spawnEntityInWorld(droppedItem);
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int i, int count) {
		ItemStack itemstack = getStackInSlot(i);

		if (itemstack != null)
		{
			if (itemstack.stackSize <= count)
			{
				setInventorySlotContents(i, null);
			} else
			{
				itemstack = itemstack.splitStack(count);
				onInventoryChanged();
			}
		}

		return itemstack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {

		inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}

		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return "Adventure Backpack";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {
		for (int i = 0; i < inventory.length; i++)
		{
			if (i == 6 && inventory[i] != null)
			{
				updateTankSlots(getLeftTank(), i);
			}

			if (i == 8 && inventory[i] != null)
			{
				updateTankSlots(getRightTank(), i);
			}
		}
		saveChanges();
		super.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
	}

	@Override
	public void openChest() {

	}

	@Override
	public void closeChest() {

		saveChanges();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return (!(itemstack.getItem() instanceof ItemAdvBackpack) && itemstack.getItem().itemID != ABPBlocks.advbackpack.blockID);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		sleepingBagDeployed = compound.getBoolean("sleepingbag");
		sbx = compound.getInteger("sbx");
		sby = compound.getInteger("sby");
		sbz = compound.getInteger("sbz");
		sbdir = compound.getInteger("sbdir");
		luminosity = compound.getInteger("lumen");
		loadFromNBT(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound tankLeft = new NBTTagCompound();
		NBTTagCompound tankRight = new NBTTagCompound();

		NBTTagList items = new NBTTagList();
		for (int i = 0; i < inventory.length; i++)
		{
			ItemStack stack = inventory[i];
			if (stack != null)
			{
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte) i);
				stack.writeToNBT(item);
				items.appendTag(item);
			}
		}
		compound.setTag("ABPItems", items);
		compound.setBoolean("sleepingbag", sleepingBagDeployed);
		compound.setInteger("sbx", sbx);
		compound.setInteger("sby", sby);
		compound.setInteger("sbz", sbz);
		compound.setInteger("lumen", luminosity);
		compound.setInteger("sbdir", sbdir);
		
		compound.setString("color", color);
		compound.setString("colorName", colorName);
		compound.setCompoundTag("rightTank", rightTank.writeToNBT(tankRight));
		compound.setCompoundTag("leftTank", leftTank.writeToNBT(tankLeft));
		compound.setInteger("lastTime", lastTime);

	}

	public void loadFromNBT(NBTTagCompound compound) {
		if (compound != null)
		{
			NBTTagList items = compound.getTagList("ABPItems");
			for (int i = 0; i < items.tagCount(); i++)
			{
				NBTTagCompound item = (NBTTagCompound) items.tagAt(i);
				byte slot = item.getByte("Slot");
				if (slot >= 0 && slot < inventory.length)
				{
					inventory[slot] = ItemStack.loadItemStackFromNBT(item);
				}
			}
			leftTank.readFromNBT(compound.getCompoundTag("leftTank"));
			rightTank.readFromNBT(compound.getCompoundTag("rightTank"));
			color = compound.getString("color");
			colorName = compound.getString("colorName");
			lastTime = compound.getInteger("lastTime");

		}
	}

	@Override
	public NBTTagCompound writeToNBT() {

		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return compound;

	}

	@Override
	public void setRightTank(FluidTank rightTank) {
		this.rightTank = rightTank;

	}

	@Override
	public void setLeftTank(FluidTank leftTank) {
		this.leftTank = leftTank;
	}

	@Override
	public boolean updateTankSlots(FluidTank tank, int slotIn) {

		int slotOut = slotIn + 1;
		ItemStack stackIn = getStackInSlot(slotIn);
		ItemStack stackOut = getStackInSlot(slotOut);

		if (tank.getFluid() != null)
		{
			for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData())
			{
				if (data.fluid.isFluidEqual(tank.getFluid()))
				{

					if (stackIn.isItemEqual(data.emptyContainer) && tank.drain(data.fluid.amount, false).amount >= data.fluid.amount)
					{

						if (stackOut != null && stackOut.isItemEqual(data.filledContainer) && stackOut.stackSize < stackOut.getMaxStackSize())
						{
							ItemStack newCont = FluidContainerRegistry.fillFluidContainer(data.fluid, stackIn);
							newCont.stackSize = stackOut.stackSize + 1;
							setInventorySlotContentsSafe(slotOut, newCont);
							decrStackSizeSafe(slotIn, 1);
							tank.drain(data.fluid.amount, true);
							saveChanges();

						} else if (stackOut == null)
						{
							ItemStack newCont = FluidContainerRegistry.fillFluidContainer(data.fluid, stackIn);
							newCont.stackSize = 1;
							setInventorySlotContentsSafe(slotOut, newCont);
							decrStackSizeSafe(slotIn, 1);
							tank.drain(data.fluid.amount, true);
							saveChanges();

						}
					} else if (stackIn.isItemEqual(data.filledContainer) && tank.fill(data.fluid, false) >= data.fluid.amount)
					{

						if (stackOut != null && stackOut.isItemEqual(data.emptyContainer) && stackOut.stackSize < stackOut.getMaxStackSize())
						{
							if (Utils.shouldGiveEmpty(data.emptyContainer))
							{
								setInventorySlotContentsSafe(slotOut, new ItemStack(data.emptyContainer.getItem(), stackOut.stackSize + 1));
							}
							decrStackSizeSafe(slotIn, 1);
							tank.fill(data.fluid, true);
							saveChanges();

						} else if (stackOut == null)
						{
							if (Utils.shouldGiveEmpty(data.emptyContainer))
							{
								setInventorySlotContentsSafe(slotOut, new ItemStack(data.emptyContainer.getItem(), 1));
							}
							decrStackSizeSafe(slotIn, 1);
							tank.fill(data.fluid, true);
							saveChanges();
						}
					}
				}
			}
		} else if (tank.getFluid() == null)
		{
			for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData())
			{
				if (stackIn.isItemEqual(data.filledContainer) && tank.fill(data.fluid, false) >= data.fluid.amount)
				{

					if (stackOut != null && stackOut.isItemEqual(data.emptyContainer) && stackOut.stackSize < stackOut.getMaxStackSize())
					{
						if (Utils.shouldGiveEmpty(data.emptyContainer))
						{
							setInventorySlotContentsSafe(slotOut, new ItemStack(data.emptyContainer.getItem(), stackOut.stackSize + 1));
						}
						decrStackSizeSafe(slotIn, 1);
						tank.fill(data.fluid, true);
						saveChanges();

					} else if (stackOut == null)
					{
						if (Utils.shouldGiveEmpty(data.emptyContainer))
						{
							setInventorySlotContentsSafe(slotOut, new ItemStack(data.emptyContainer.getItem(), 1));
						}
						decrStackSizeSafe(slotIn, 1);
						tank.fill(data.fluid, true);
						saveChanges();
					}
				}
			}
		}

		return false;
	}

	public void setInventorySlotContentsSafe(int slot, ItemStack itemstack) {
		inventory[slot] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	private ItemStack decrStackSizeSafe(int slot, int amount) {
		ItemStack stack = getStackInSlot(slot);

		if (stack != null)
		{
			if (stack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			} else
			{
				stack = stack.splitStack(amount);
			}
		}
		return stack;
	}

	private void saveChanges() {
		writeToNBT();
	}

	@Override
	public FluidTank getLeftTank() {
		return leftTank;
	}

	@Override
	public FluidTank getRightTank() {
		return rightTank;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
