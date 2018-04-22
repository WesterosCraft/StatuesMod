package com.svennieke.statues.tileentity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.svennieke.statues.Statues;
import com.svennieke.statues.config.StatuesConfigGen;
import com.svennieke.statues.init.StatuesItems;
import com.svennieke.statues.init.StatuesSounds;
import com.svennieke.statues.items.ItemTea;
import com.svennieke.statues.util.RandomLists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class StatueTileEntity extends TileEntity implements ITickable{
	private int Cooldown;
	private int cooldownMax;
	private boolean statueAble = true;
	private int tier;
	private static FakePlayer fakeStatue = null;
	
	public StatueTileEntity() {
		this.tier = 2;
		this.Cooldown = 0;
		this.cooldownMax = (StatuesConfigGen.general.InteractionTimer * 20);
	}
	
	public int setTier(int theTier) {
		return this.tier = theTier;
	}
	
	public int getTier() {
		return this.tier;
	}
	
	public void holidayCheck(Entity entity, World worldIn, BlockPos pos, boolean isChild) {
		if(tier == 3 || tier == 4)
		{
			LocalDateTime now = LocalDateTime.now();
			if(now.getMonth() == Month.OCTOBER)
			{
				int random = world.rand.nextInt(100);

				if (random < 1)
				{
					if(isChild == true && entity instanceof EntityMob)
					{
						EntityMob mob = (EntityMob)entity;
						mob.isChild();
						mob.setPositionAndUpdate(pos.getX(), pos.getY() + 1, pos.getZ());
						worldIn.spawnEntity(mob);
					}
					else
					{
						entity.setPositionAndUpdate(pos.getX(), pos.getY() + 1, pos.getZ());
						worldIn.spawnEntity(entity);
					}
				}
			}
		}
	}
	
	public void WastelandBehavior(World worldIn, BlockPos pos, EntityPlayer playerIn, @Nullable ItemStack stack1, @Nullable ItemStack stack2, @Nullable ItemStack stack3)
	{		
		if(this.statueAble) 
		{
			int random = world.rand.nextInt(100);
			if(tier == 3 || tier == 4)
			{
				if (random < 100 && stack1 != null && stack1 != ItemStack.EMPTY)
				{
					if(stack1.getItem() instanceof ItemTea && tier == 3)
					{
						worldIn.playSound(null, pos, StatuesSounds.wasteland_tea, SoundCategory.NEUTRAL, 1F, 1F);
					}
					playerIn.dropItem(stack1, true);
				}
				
				if(stack2 != null && stack2 != ItemStack.EMPTY){
					if(random < 50)
					{
						playerIn.dropItem(stack2, true);
					}
				}
				
				if(stack3 != null && stack3 != ItemStack.EMPTY){
					if(random < 10)
					{
						playerIn.dropItem(stack3, true);
					}
				}
			}
            this.statueAble = false;
		}
		else
		{
			if((tier == 2 || tier == 3))
			{
				worldIn.playSound(null, pos, RandomLists.GetRandomWasteland(), SoundCategory.NEUTRAL, 1F, 1F);
			}
		}
	}
	
	public void PlaySound(SoundEvent Mobsound, BlockPos pos, World worldIn) {
		if(tier == 2 || tier == 3)
		{
			worldIn.playSound(null, pos, Mobsound, SoundCategory.NEUTRAL, 1F, 1F);
		}
	}
	
	public void SendInfoMessage(EntityPlayer entity, World worldIn, BlockPos pos) {
			EntityPlayer player = (EntityPlayer)entity;
			
			if (!world.isRemote) {
				int random = world.rand.nextInt(100);
				
				String[] messages = StatuesConfigGen.messages.info_messages;
				
				int idx = new Random().nextInt(messages.length);
				String randommessage = (messages[idx]);
				
				if(Statues.isVeinminerInstalled == true && random < 20)
				{
					randommessage = ("Did you know we have veinminer");
				}
				else
				{
					randommessage = (messages[idx]);
				}

				player.sendMessage(new TextComponentTranslation(randommessage));;
				
			}
	}
	
	public void GiveEffect(BlockPos pos, World worldIn, EntityPlayer entity, Potion effect) {
		if(this.statueAble)
		{
			EntityPlayer player = (EntityPlayer)entity;
			int random = world.rand.nextInt(100);
			if(tier == 3 || tier == 4)
			{
				if(random < 10)
				{
					if (!world.isRemote) {
						if (player.getActivePotionEffect(effect) == null) {
							player.addPotionEffect(new PotionEffect(effect, 20 * 20, 1, true, true));
						}
					}
				}
			}
		}
	}
	
	public void ThrowPotion(BlockPos pos, World worldIn, EntityPlayer entity) {
		if(this.statueAble)
		{
			EntityPlayer player = (EntityPlayer)entity;
			
			int random = world.rand.nextInt(100);
			if(tier == 3 || tier == 4)
			{
				if(random < 10)
				{
					if (!world.isRemote) {
						double d0 = entity.posY + (double)entity.getEyeHeight() - 1.100000023841858D;
			            double d1 = entity.posX + entity.motionX - pos.getX();
			            double d2 = d0 - pos.getY();
			            double d3 = entity.posZ + entity.motionZ - pos.getZ();
			            float f = MathHelper.sqrt(d1 * d1 + d3 * d3);
			            
			            EntityPotion entitypotion = new EntityPotion(world);
			            entitypotion.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 0, 0);
			            entitypotion.setItem(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), RandomLists.getRandomPotionType()));
			            entitypotion.shoot(d1, d2 + (double)(f * 0.2F), d3, 0.25F, 6.0F);
			            this.world.playSound((EntityPlayer)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_WITCH_THROW, SoundCategory.NEUTRAL, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
			            this.world.spawnEntity(entitypotion);
					}
				}
			}
		}
	}
	
	public static FakePlayer getFakePlayer() {
        if (fakeStatue == null) {
        	fakeStatue = FakePlayerFactory.get(DimensionManager.getWorld(0), new GameProfile(new UUID(123, 132), "Shulker Statue"));
        }
        return fakeStatue;
	}
	
	public void ShootBullet(BlockPos pos, World worldIn, EntityPlayer entity, EnumFacing.Axis facing) {
		if(this.statueAble)
		{
			EntityPlayer player = (EntityPlayer)entity;
			FakePlayer fakePlayer = getFakePlayer();
			
			int random = worldIn.rand.nextInt(100);
			if(tier == 3 || tier == 4)
			{
				if(random < 90)
				{
					if (!worldIn.isRemote) {
                        EntityShulkerBullet bullet = new EntityShulkerBullet(worldIn, fakePlayer, player, facing);
                        bullet.setPosition(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ());

                        worldIn.spawnEntity(bullet);
						worldIn.playSound((EntityPlayer)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.NEUTRAL, 1.0F, 0.8F + worldIn.rand.nextFloat() * 0.4F);
					}
				}
			}
		}
	}
	
	public void SpecialInteraction(boolean isCow, boolean isMooshroom, boolean isFlood, Block statue, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		EntityFireworkRocket firework = new EntityFireworkRocket(worldIn, (double)((float)pos.getX() + hitX), (double)((float)pos.getY() + hitY), (double)((float)pos.getZ() + hitZ), stack);
			int random = world.rand.nextInt(100);
			
			if(isCow)
			{
				if(!worldIn.isRemote)
				{
					if (stack.getItem() == Items.BUCKET && !playerIn.capabilities.isCreativeMode)
			        {
						worldIn.playSound(null, pos, SoundEvents.ENTITY_COW_MILK, SoundCategory.NEUTRAL, 1F, 1F);
			            stack.shrink(1);
	
			            if (stack.isEmpty())
			            {
			            	playerIn.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
			            }
			            else if (!playerIn.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET)))
			            {
			            	playerIn.dropItem(new ItemStack(Items.MILK_BUCKET), false);
			            }
			        }
				}
			}
			
			if(isMooshroom)
			{
				if(!worldIn.isRemote)
				{
					if (stack.getItem() == Items.BOWL && !playerIn.capabilities.isCreativeMode)
			        {
						//System.out.println("NO");
						worldIn.playSound(null, pos, SoundEvents.ENTITY_COW_MILK, SoundCategory.NEUTRAL, 1F, 1F);
			            stack.shrink(1);
	
			            if (stack.isEmpty())
			            {
			            	playerIn.setHeldItem(hand, new ItemStack(StatuesItems.soup));
			            }
			            else if (!playerIn.inventory.addItemStackToInventory(new ItemStack(StatuesItems.soup)))
			            {
			            	playerIn.dropItem(new ItemStack(StatuesItems.soup), false);
			            }
			        }
				}
			}
		
			if(isFlood)
			{
				if(!worldIn.isRemote)
					if (stack.getItem() == Items.BUCKET && !playerIn.capabilities.isCreativeMode)
					{
						worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.NEUTRAL, 1F, 1F);
						stack.shrink(1);
						
						ItemStack floodbucket = new ItemStack(Items.WATER_BUCKET); 
						
						if (stack.isEmpty())
			            {
			                playerIn.setHeldItem(hand, floodbucket);
			                floodbucket.setStackDisplayName("The Flood");
			            }
			            else if (!playerIn.inventory.addItemStackToInventory(floodbucket))
			            {
			            	playerIn.dropItem(floodbucket, false);
			            	floodbucket.setStackDisplayName("The Flood");
			            }
					}
				
					if (random < 50){
						 worldIn.spawnEntity(firework);
					}	
			}
		}
	
	public void StatueBehavior(@Nullable ItemStack stack1, @Nullable ItemStack stack2, @Nullable ItemStack stack3, 
			EntityLiving spawnableentity, boolean spawnEntity, boolean isCreeper,Block statue, EntityPlayer playerIn, 
			World worldIn, BlockPos pos) {
		
		if(this.statueAble) 
		{
			int random = world.rand.nextInt(100);
			if(tier == 3 || tier == 4)
			{
				if (random < 100 && stack1 != null && stack1 != ItemStack.EMPTY)
				{
					playerIn.dropItem(stack1, true);
				}
				
				if (random < 10 && spawnEntity)
				{
					if (!worldIn.isRemote)
					{
					spawnableentity.setLocationAndAngles((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, 0.0F, 0.0F);
			        NBTTagCompound tag = new NBTTagCompound();
			        spawnableentity.writeEntityToNBT(tag);
			        
				        if(isCreeper)
				        {
				        tag.setShort("ExplosionRadius", (short)0);
				        tag.setShort("Fuse", (short)0);
				        }
			        
			        spawnableentity.readEntityFromNBT(tag);
			        worldIn.spawnEntity(spawnableentity);
			        
				        if(isCreeper)
				        {
				        spawnableentity.spawnExplosionParticle();
				    	}
			        
					}
				}
				
				if(stack2 != null && stack2 != ItemStack.EMPTY){
					if(random < 50)
					{
						playerIn.dropItem(stack2, true);
					}
				}
				
				if(stack3 != null && stack3 != ItemStack.EMPTY){
					if(random < 10)
					{
						playerIn.dropItem(stack3, true);
					}
				}
			}
            this.statueAble = false;
		}
	}
	
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tier = compound.getInteger("StatueTier");
        Cooldown = compound.getInteger("StatueCooldown");
        cooldownMax = compound.getInteger("StatueMaxCooldown");
        statueAble = compound.getBoolean("statueAble");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("StatueTier", tier);
        compound.setInteger("StatueCooldown", Cooldown);
        compound.setInteger("StatueMaxCooldown", cooldownMax);
        compound.setBoolean("statueAble", statueAble);
        return compound;
    }
    
    @Override
    public void update(){
    	if (this.world.isRemote)
    		return;
    	
    	if (!this.statueAble)
    	{
    		++this.Cooldown;
            
            if(this.Cooldown == this.cooldownMax){
                this.Cooldown = 0;
                this.statueAble = true;
            }
    	}
    }
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }
    
    @Override
	public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
	}
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    	super.onDataPacket(net, pkt);
    	readFromNBT(pkt.getNbtCompound());
    	final IBlockState state = getWorld().getBlockState(getPos());
    	getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }
    
    public int getCooldown() {
		return this.Cooldown;
	}
    
    public int getCooldownMax() {
		return this.cooldownMax;
	}
    
    public boolean isStatueAble() {
		return this.statueAble;
	}
}