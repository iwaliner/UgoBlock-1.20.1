package com.iwaliner.ugoblock.object.controller;

import com.iwaliner.ugoblock.ModCoreUgoBlock;
import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.object.block_imitation_wand.ImitatableBlockEntity;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public class SlideControllerBlockEntity extends AbstractControllerBlockEntity implements ImitatableBlockEntity {
    private int speedx10=10;
    private boolean oneway;
    protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    /**ContainerDataを1つにまとめるとGUI内のボタンを推した時に連動しちゃったから分けてる*/
    protected final ContainerData startTickDataAccess = new ContainerData() {
        public int get(int i) {
            switch(i) {
                case 0:
                    return SlideControllerBlockEntity.this.getStartTime();

                default:
                    return 0;
            }
        }
        public void set(int i, int j) {
           if(!isMoving){
                    SlideControllerBlockEntity.this.startTime = j;
            }
        }
        public int getCount() {
            return 1;
        }
    };
    protected final ContainerData speedDataAccess = new ContainerData() {
        public int get(int i) {
            switch(i) {
                case 0:
                    return getSpeedx10();
                default:
                    return 0;
            }
        }
        public void set(int i, int j) {
            if(!isMoving){
                    SlideControllerBlockEntity.this.speedx10=j;
            }
        }
        public int getCount() {
            return 1;
        }
    };
    protected final ContainerData onewayDataAccess = new ContainerData() {
        @Override
        public int get(int i) {
            if(isOneway()){
                return 1;
            }else{
                return 0;
            }
        }
        public void set(int i, int j) {
            setOneway(j == 1);
        }
        public int getCount() {
            return 1;
        }
    };
    public SlideControllerBlockEntity( BlockPos p_155077_, BlockState p_155078_) {
        super(Register.SlideController.get(), p_155077_, p_155078_);
    }
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.ugoblock.slide_controller");
    }
    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new SlideControllerMenu(i,inventory,this,startTickDataAccess,speedDataAccess,onewayDataAccess,collisionShapeDataAccess);
    }

    @Override
    public int getContainerSize() {
        return 2;
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
        this.oneway=tag.getBoolean("oneway");
        if(tag.contains("speedx10")) {
            this.speedx10 = tag.getInt("speedx10");
        }else{
            this.speedx10 = 10;
        }
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("oneway",oneway);
        tag.putInt("speedx10",speedx10);
        ContainerHelper.saveAllItems(tag, this.items);
    }
    public BlockPos getStartPos(){
        if(level.getBlockState(getBlockPos()).getBlock()instanceof SlideControllerBlock){
            return getBlockPos().relative(level.getBlockState(getBlockPos()).getValue(BlockStateProperties.FACING));
        }
        return getBlockPos();
    }
   public BlockPos getTransition() {
       if(getItem(1).getItem()==Register.vector_card.get()&&getItem(1).getTag()!=null){
           return VectorCardItem.getTransition(getItem(1));
       }
       return Utils.errorPos();
   }
    public int getSpeedx10(){
        return  speedx10;
    }
    public void setSpeedx10(int speed){
        speedx10=speed;
    }
    public int getDuration() {
        double distance=this.getDistance();
        double d=(distance/((double) speedx10/10D))*20D;
        return Math.round((float) d);
    }
    public double getDistance(){
        BlockPos startPos=this.getStartPos();
        BlockPos transition=getTransition();
        return Mth.sqrt((float)Mth.square(transition.getX())+(float)Mth.square(transition.getY())+(float)Mth.square(transition.getZ()));
    }
    public boolean hasCards(){
        return getItem(0).getItem()==Register.shape_card.get()&&getItem(1).getItem()==Register.vector_card.get()&&getItem(0).getTag()!=null&&getItem(1).getTag()!=null&&getItem(0).getTag().contains("positionList")&&VectorCardItem.isSelectionFinished(getItem(1));
    }
    public static void tick(Level level, BlockPos pos, BlockState state, SlideControllerBlockEntity blockEntity) {
        if(state.getBlock() instanceof SlideControllerBlock) {
            if(blockEntity.isMoving()&&!state.getValue(SlideControllerBlock.MOVING)){
                level.setBlock(pos,state.setValue(SlideControllerBlock.MOVING,true),2);
            }else if(!blockEntity.isMoving()&&state.getValue(SlideControllerBlock.MOVING)){
                level.setBlock(pos,state.setValue(SlideControllerBlock.MOVING,false),2);
            }
            if (blockEntity.getMoveTick() > 0) {
                if (blockEntity.isMoving()) {
                    if (blockEntity.getTickCount() > blockEntity.getMoveTick()+2) {
                        blockEntity.setMoving(false);
                        blockEntity.setTickCount(0);
                    } else if (blockEntity.getTickCount() == blockEntity.getMoveTick() && blockEntity.hasCards()&&!blockEntity.isOneway()) {
                        BlockPos startPos = blockEntity.getStartPos();
                        List<BlockPos> posList = blockEntity.getPositionList();
                        if (blockEntity.isNotFirstTime()) {
                            List<BlockPos> posList0 = blockEntity.getPositionList();
                            BlockPos transition=blockEntity.getTransition();
                            for (int i = 0; i < posList0.size(); i++) {
                                posList.set(i, new BlockPos(posList0.get(i).getX() + (transition.getX()), posList0.get(i).getY() + (transition.getY()), posList0.get(i).getZ() + (transition.getZ())));
                            }
                        }
                        blockEntity.setPositionList(posList);
                        VectorCardItem.invertTransition(blockEntity.getItem(1));
                    }
                    blockEntity.increaseTickCount(1);
                }
            }
        }
       }
    @Override
    public boolean canPlaceItem(int i, ItemStack stack) {
        if(i==1){
            return stack.getItem()==Register.vector_card.get();
        }else{
            return stack.getItem()==Register.shape_card.get();
        }
    }
    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }
    public boolean isOneway() {
        return oneway;
    }
    public void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
    public boolean hasShapeCard(){
        return !getItem(0).isEmpty()&&getItem(0).is(Register.shape_card.get());
    }
    public boolean hasVectorCard(){
        return !getItem(1).isEmpty()&&getItem(1).is(Register.vector_card.get());
    }
}
