package com.iwaliner.ugoblock.object.controller;

import com.iwaliner.ugoblock.ModCoreUgoBlock;
import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.object.block_imitation_wand.ImitatableBlockEntity;
import com.iwaliner.ugoblock.object.moving_block.MovingBlockEntity;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class RotationControllerBlockEntity extends AbstractControllerBlockEntity implements ImitatableBlockEntity {
    private int degreeAngle=-30;
    private int duration=3*20;
    private int visualDegree;
    protected NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private CompoundTag basketPosList;
    private CompoundTag basketOriginPosList;
    private boolean preRedstoneSignal;
    private boolean ignore;
    /**ContainerDataを1つにまとめるとGUI内のボタンを推した時に連動しちゃったから分けてる*/
    protected final ContainerData degreeAngleDataAccess = new ContainerData() {
        public int get(int i) {
            if (i == 0) {
                return RotationControllerBlockEntity.this.getDegreeAngleForMenu();
            }
            return 0;
        }
        public void set(int i, int j) {
            if (i == 0&&!isMoving&&(getBlockState().getBlock() instanceof RotationControllerBlock && !getBlockState().getValue(RotationControllerBlock.POWERED))) {
                if(RotationControllerBlockEntity.this.degreeAngle>=0&&j<0){
                    setTurnDirection(false);
                }else if(RotationControllerBlockEntity.this.degreeAngle<0&&j>=0){
                    setTurnDirection(true);
                }
                RotationControllerBlockEntity.this.degreeAngle =  j;
            }
        }
        public int getCount() {
            return 1;
        }
    };
    protected final ContainerData durationDataAccess = new ContainerData() {
        public int get(int i) {
            if (i == 0) {
                return Mth.floor(RotationControllerBlockEntity.this.getDuration() / 20f);
            }
            return 0;
        }
        public void set(int i, int j) {
            if (i == 0&&!isMoving) {
                RotationControllerBlockEntity.this.duration = j * 20;
            }
        }
        public int getCount() {
            return 1;
        }
    };
    public RotationControllerBlockEntity(BlockPos p_155077_, BlockState p_155078_) {
        super(Register.RotationController.get(), p_155077_, p_155078_);
    }
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.ugoblock.rotation_controller");
    }
    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new RotationControllerMenu(i,inventory,this,degreeAngleDataAccess,durationDataAccess,collisionShapeDataAccess);
    }
    @Override
    public int getContainerSize() {
        return 1;
    }
    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    @Override
    public ItemStack getItem(int slot) {
        return  items.get(slot);
    }
    public ItemStack removeItem(int ii, int jj) {
        return ContainerHelper.removeItem(this.items, ii, jj);
    }
    public ItemStack removeItemNoUpdate(int p_58387_) {
        return ContainerHelper.takeItem(this.items, p_58387_);
    }
    public void setItem(int slot, ItemStack stack) {
            ItemStack itemstack = this.items.get(slot);
            boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, stack);
            this.items.set(slot, stack);
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            if (slot == 0 && !flag) {
                this.setChanged();
            }
    }
    public boolean stillValid(Player p_70300_1_) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return p_70300_1_.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }
    @Override
    public void clearContent() {
        this.items.clear();
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.degreeAngle=tag.getInt("degreeAngle");
        if(tag.contains("duration")) {
            this.duration = tag.getInt("duration");
        }else{
            this.duration = 5*20;
        }
        this.visualDegree=tag.getInt("visualDegree");
        basketPosList =tag.getCompound("basketPosList");
        basketOriginPosList =tag.getCompound("basketOriginPosList");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        preRedstoneSignal=tag.getBoolean("preRedstoneSignal");
        ContainerHelper.loadAllItems(tag, this.items);
        ignore  =tag.getBoolean("ignore");
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("degreeAngle",degreeAngle);
        tag.putInt("duration",duration);
        tag.putInt("visualDegree",visualDegree);
        if(basketPosList !=null) {
            tag.put("basketPosList", basketPosList);
        }
        if(basketOriginPosList !=null) {
            tag.put("basketOriginPosList", basketOriginPosList);
        }
        tag.putBoolean("preRedstoneSignal",preRedstoneSignal);
        tag.putBoolean("ignore",ignore);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public void setBasketPosList(CompoundTag tag){
        basketPosList =tag;
    }
    public void setBasketOriginPosList(CompoundTag tag){
        basketOriginPosList =tag;
    }

    public List<BlockPos> getBasketPosList(){
        if(basketPosList==null){
            basketPosList=new CompoundTag();
        }
       List<BlockPos> posList=new ArrayList<>();
        for(int i=0; i< basketPosList.size();i++){
            if (basketPosList.contains("location_" + String.valueOf(i))) {
                posList.add(NbtUtils.readBlockPos(basketPosList.getCompound("location_" + String.valueOf(i))));
            }
        }
        return posList;
    }
    public List<BlockPos> getBasketOriginPosList(){
        if(basketOriginPosList==null){
            basketOriginPosList=new CompoundTag();
        }
        List<BlockPos> posList=new ArrayList<>();
        for(int i=0; i< basketOriginPosList.size();i++){
            if (basketOriginPosList.contains("location_" + String.valueOf(i))) {
                posList.add(NbtUtils.readBlockPos(basketOriginPosList.getCompound("location_" + String.valueOf(i))));
            }
        }
        return posList;
    }
    public int getDegreeAngleForMenu(){
        return degreeAngle;
    }
    public int getDegreeAngle(){
        if(degreeAngle>=180){
            return 180;
        }else if(degreeAngle<=-180){
            return -180;
        }else{
            return degreeAngle;
        }
    }
    public boolean isLoop(){
        return degreeAngle==-181||degreeAngle==181;
    }
    public void setDegreeAngle(int degree){
        degreeAngle=degree;
    }
    public void setDuration(int d){
        duration=d;
    }
    public int getDuration() {
        return duration;
    }
    public boolean isPreRedstoneSignalON() {
        return preRedstoneSignal;
    }
    public void setPreRedstoneSignal(boolean preRedstoneSignal) {
        this.preRedstoneSignal = preRedstoneSignal;
    }
    public boolean hasCards(){
        return getItem(0).getItem()==Register.shape_card.get()&&getItem(0).getTag()!=null&&getItem(0).getTag().contains("positionList");
    }
    private void setTurnDirection(boolean isCounterClockwise) {
        if (getBlockState().getBlock() instanceof RotationControllerBlock) {
            level.setBlock(getBlockPos(), getBlockState().setValue(RotationControllerBlock.COUNTER_CLOCKWISE, isCounterClockwise), 3);
        }
    }
    public int getVisualDegree(){
        return visualDegree;
    }
    public void setVisualDegree(int degree){
        visualDegree=degree;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RotationControllerBlockEntity blockEntity) {
        if(state.getBlock() instanceof RotationControllerBlock rotationControllerBlock) {
                if (blockEntity.isMoving() && !state.getValue(RotationControllerBlock.MOVING)) {
                    level.setBlock(pos, state.setValue(RotationControllerBlock.MOVING, true), 2);
                } else if (!blockEntity.isMoving() && state.getValue(RotationControllerBlock.MOVING)) {
                    level.setBlock(pos, state.setValue(RotationControllerBlock.MOVING, false), 2);
                }
            if (blockEntity.getMoveTick() > 0) {
                if (blockEntity.isMoving()) {
                  if (blockEntity.getTickCount() > blockEntity.getMoveTick()+2) {
                        if(!blockEntity.isLoop()) {
                            blockEntity.setMoving(false);
                            blockEntity.setTickCount(0);
                        }
                    }
                    blockEntity.increaseTickCount(1);
                }
            }
   }

        boolean signal=level.hasNeighborSignal(pos);
         if(blockEntity.preRedstoneSignal!=signal){
            blockEntity.setPreRedstoneSignal(signal);
        }
       }
    @Override
    public boolean canPlaceItem(int i, ItemStack stack) {
            return stack.getItem()==Register.shape_card.get();
    }
    public void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
    public boolean hasShapeCard(){
        return !getItem(0).isEmpty()&&getItem(0).is(Register.shape_card.get());
    }
    public boolean hasVectorCard(){
        return true;
    }
    public int getDegreeAngleAndLoop(){
        return degreeAngle;
    }
}
