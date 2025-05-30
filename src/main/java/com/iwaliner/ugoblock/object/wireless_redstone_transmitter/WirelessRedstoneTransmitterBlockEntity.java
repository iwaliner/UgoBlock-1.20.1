package com.iwaliner.ugoblock.object.wireless_redstone_transmitter;

import com.iwaliner.ugoblock.ModCoreUgoBlock;
import com.iwaliner.ugoblock.network.WirelessRedstoneProvider;
import com.iwaliner.ugoblock.object.block_imitation_wand.ImitatableBlockEntity;
import com.iwaliner.ugoblock.object.wireless_redstone_receiver.WirelessRedstoneReceiverBlock;
import com.iwaliner.ugoblock.object.wireless_redstone_receiver.WirelessRedstoneReceiverBlockEntity;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WirelessRedstoneTransmitterBlockEntity extends BlockEntity implements ImitatableBlockEntity {
    private DyeColor color1;
    private DyeColor color2;
    private DyeColor color3;
    protected BlockState imitatingState;
    private boolean disregardRedstoneInputChanging;
    public WirelessRedstoneTransmitterBlockEntity(BlockPos p_155077_, BlockState p_155078_) {
        super(Register.WirelessRedstoneTransmitterBlockEntity.get(), p_155077_, p_155078_);
    }
    public void load(CompoundTag tag) {
        super.load(tag);
        if(!tag.contains("color1")){
            this.color1=DyeColor.byId(0);
        }else{
            this.color1=DyeColor.byId(tag.getByte("color1"));
        }
        if(!tag.contains("color2")){
            this.color2=DyeColor.byId(0);
        }else{
            this.color2=DyeColor.byId(tag.getByte("color2"));
        }
        if(!tag.contains("color3")){
            this.color3=DyeColor.byId(0);
        }else{
            this.color3=DyeColor.byId(tag.getByte("color3"));
        }
        if (tag.contains("imitatingState")) {
            HolderGetter<Block> holdergetter = (HolderGetter<Block>)(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup());
            this.imitatingState = NbtUtils.readBlockState(holdergetter, tag.getCompound("imitatingState"));
        }
        if (tag.contains("disregardRedstoneInputChanging")) {
            this.disregardRedstoneInputChanging=tag.getBoolean("disregardRedstoneInputChanging");
        }
    }
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("color1", color1==null? 0 : (byte) color1.getId());
        tag.putByte("color2", color2==null? 0 : (byte) color2.getId());
        tag.putByte("color3", color3==null? 0 : (byte) color3.getId());
        if(imitatingState!=null) {
            tag.put("imitatingState", NbtUtils.writeBlockState(imitatingState));
        }
        tag.putBoolean("disregardRedstoneInputChanging",disregardRedstoneInputChanging);
    }
    public void setColor1(DyeColor color1) {
        this.color1 = color1;
    }
    public void setColor2(DyeColor color2) {
        this.color2 = color2;
    }
    public void setColor3(DyeColor color3) {
        this.color3 = color3;
    }
    public DyeColor getColor1() {
        return color1==null? DyeColor.byId(0):color1;
    }
    public DyeColor getColor2() {
        return color2 == null ? DyeColor.byId(0) : color2;
    }
    public DyeColor getColor3() {
        return color3==null? DyeColor.byId(0):color3;
    }
    public BlockState getImitatingState() {
        return imitatingState==null? Blocks.AIR.defaultBlockState() : imitatingState;
    }
    public void setImitatingState(BlockState imitatingState) {
        this.imitatingState = imitatingState;
    }
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag= super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }
    public void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
    }
