package com.shynieke.statues.tiles;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.shynieke.statues.blocks.statues.PlayerStatueBlock;
import com.shynieke.statues.init.StatueRegistry;
import com.shynieke.statues.init.StatueTiles;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class PlayerTile extends TileEntity implements INameable, ITickableTileEntity {
    private static PlayerProfileCache profileCache;
    private static MinecraftSessionService sessionService;

    private GameProfile playerProfile;
    private boolean isSlim = false;
    private boolean comparatorApplied;
    private boolean OnlineChecking;
    private int checkerCooldown;

    public PlayerTile() {
        super(StatueTiles.PLAYER);
        this.comparatorApplied = false;
        this.checkerCooldown = 0;
        this.OnlineChecking = false;
    }

    public static void setProfileCache(PlayerProfileCache profileCacheIn) {
        profileCache = profileCacheIn;
    }

    public static void setSessionService(MinecraftSessionService sessionServiceIn) {
        sessionService = sessionServiceIn;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        if (compound.contains("PlayerProfile", 10)) {
            this.setPlayerProfile(NBTUtil.readGameProfile(compound.getCompound("PlayerProfile")));
        }

        comparatorApplied = compound.getBoolean("comparatorApplied");
        OnlineChecking = compound.getBoolean("OnlineChecking");
        checkerCooldown = compound.getInt("checkerCooldown");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (this.playerProfile != null) {
            CompoundNBT compoundnbt = new CompoundNBT();
            NBTUtil.writeGameProfile(compoundnbt, this.playerProfile);
            compound.put("PlayerProfile", compoundnbt);
        }
        compound.putBoolean("comparatorApplied", comparatorApplied);
        compound.putBoolean("OnlineChecking", OnlineChecking);
        compound.putInt("checkerCooldown", checkerCooldown);
        return compound;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT compoundNBT = pkt.getNbtCompound();
        handleUpdateTag(getBlockState(), compoundNBT);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 4, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public boolean hasCustomName() {
        return this.playerProfile != null && !this.playerProfile.getName().isEmpty();
    }

    @Nullable
    public GameProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public boolean isSlim() {
        return this.isSlim;
    }

    public void setPlayerProfile(@Nullable GameProfile profile) {
        if (this.world != null && this.world.isRemote() && profile != null) {
            Minecraft.getInstance().getSkinManager().loadProfileTextures(profile, (textureType, textureLocation, profileTexture) -> {
                if (textureType.equals(MinecraftProfileTexture.Type.SKIN)) {
                    String metadata = profileTexture.getMetadata("model");
                    this.isSlim = metadata != null && metadata.equals("slim");
                }
            }, true);
        }
        this.playerProfile = profile;
        this.updatePlayerProfile();
    }

    private void updatePlayerProfile() {
        this.playerProfile = updateGameProfile(this.playerProfile);
        this.markDirty();
    }

    @Nullable
    public static GameProfile updateGameProfile(@Nullable GameProfile input) {
        if (input != null && !StringUtils.isNullOrEmpty(input.getName())) {
            if (input.isComplete() && input.getProperties().containsKey("textures")) {
                return input;
            } else if (profileCache != null && sessionService != null) {
                GameProfile gameprofile = profileCache.getGameProfileForUsername(input.getName());
                if (gameprofile == null) {
                    return input;
                } else {
                    Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), (Property)null);
                    if (property == null) {
                        gameprofile = sessionService.fillProfileProperties(gameprofile, true);
                    }

                    return gameprofile;
                }
            } else {
                return input;
            }
        } else {
            return input;
        }
    }

    @Override
    public void tick() {
        if(this.world != null && !this.world.isRemote) {
            BlockState state = getBlockState();
            if(state.getBlock() == StatueRegistry.PLAYER_STATUE.get() && comparatorApplied) {
                if(!OnlineChecking) {
                    ++this.checkerCooldown;
                    markDirty();
                    if(this.checkerCooldown == 0)
                        this.checkerCooldown = 200;

                    if(this.checkerCooldown >= 200) {
                        this.checkerCooldown = 0;
                        setOnlineChecking(true);
                    }
                } else {
                    updateOnline();
                    setOnlineChecking(false);
                }
            }
        }
    }

    public void updateOnline() {
        BlockState state = getBlockState();
        boolean isStateOnline = state.get(PlayerStatueBlock.ONLINE);
        boolean checkAnswer = world.getPlayerByUuid(this.playerProfile.getId()) != null;
        if(isStateOnline != checkAnswer) {
            BlockState newState = state.with(PlayerStatueBlock.ONLINE, checkAnswer);
            world.setBlockState(getPos(), newState);
            world.notifyBlockUpdate(getPos(), state, newState, 3);
        }
    }


    public void setComparatorApplied(boolean comparatorApplied) {
        this.comparatorApplied = comparatorApplied;
        if(!comparatorApplied) {
            BlockState state = getBlockState();
            BlockState newState = state.with(PlayerStatueBlock.ONLINE, false);
            world.setBlockState(getPos(), newState);
            world.notifyBlockUpdate(getPos(), state, newState, 3);
        }
        this.markDirty();
    }

    public boolean getComparatorApplied() {
        return comparatorApplied;
    }

    public int getCooldown() {
        return this.checkerCooldown;
    }

    public void setOnlineChecking(boolean onlineChecking) {
        this.OnlineChecking = onlineChecking;
        this.markDirty();
    }

    @Override
    public ITextComponent getName() {
        return this.hasCustomName() ? new StringTextComponent(this.playerProfile != null ? playerProfile.getName() : "") : new TranslationTextComponent("statue.player");
    }

    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}