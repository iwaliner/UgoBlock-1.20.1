package com.iwaliner.ugoblock.object.controller;

import com.iwaliner.ugoblock.ModCoreUgoBlock;
import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlideControllerBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty MOVING = BooleanProperty.create("moving");
    public SlideControllerBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(MOVING,false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        p_49915_.add(POWERED,FACING,MOVING);
    }
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof SlideControllerBlockEntity) {
            player.openMenu((MenuProvider)blockentity);
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack stack=player.getItemInHand(hand);
        if(level.getBlockEntity(pos) instanceof SlideControllerBlockEntity blockEntity) {
            if (stack.getItem() == Register.shape_card.get()&&blockEntity.getItem(0).isEmpty()&&!player.isSuppressingBounce()) {
                blockEntity.setItem(0,stack);
                player.setItemInHand(hand,ItemStack.EMPTY);
                player.level().playSound(player,pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS,1F,1F);
                return InteractionResult.SUCCESS;
            }else if (stack.getItem() == Register.vector_card.get()&&stack.getTag()!=null&&blockEntity.getItem(1).isEmpty()) {
               blockEntity.setItem(1,stack);

                player.setItemInHand(hand,ItemStack.EMPTY);
                player.level().playSound(player,pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS,1F,1F);
                return InteractionResult.SUCCESS;
            }else if(stack.is(Register.block_imitation_wand.get())){
                return InteractionResult.PASS;
            }else if(!state.getValue(MOVING)){
                if(!player.isSuppressingBounce()) {
                    if (level.isClientSide) {
                        return InteractionResult.SUCCESS;
                    }
                    this.openContainer(level, pos, player);
                    return InteractionResult.CONSUME;
                }else if(!blockEntity.getImitatingState().isAir()){
                    blockEntity.setImitatingState(Blocks.AIR.defaultBlockState());
                    level.playSound(player,pos, SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        player.displayClientMessage(Component.translatable("info.ugoblock.slide_controller_denyed_opening_gui").withStyle(ChatFormatting.YELLOW), true);
        return InteractionResult.PASS;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean b) {
        boolean flag = state.getValue(POWERED);
        if (level.hasNeighborSignal(pos)&&level.getBlockEntity(pos) instanceof SlideControllerBlockEntity blockEntity) {
                 if(!blockEntity.isMoving()&&blockEntity.hasCards()) { /**動いている最中は赤石入力の変化を無視する*/
                       int start=blockEntity.getStartTime();
                      int duration=blockEntity.getDuration();
                       boolean makingEntitySuccess=false;
                       if(!blockEntity.getTransition().equals(Utils.errorPos())&&duration>0) {
                           BlockPos startPos=pos.relative(state.getValue(FACING));
                           List<BlockPos> posList=blockEntity.getPositionList();
                           if(posList!=null) {

                                /**↓移動の変位。座標ではないことに注意。*/
                               BlockPos transitionPos = blockEntity.getTransition();
                               /**ブロックをエンティティ化*/
                              makingEntitySuccess= Utils.makeMoveableBlock(level,pos, startPos, start, duration,null,0,blockEntity.getPositionList(),0,false, transitionPos,0);
                              if(makingEntitySuccess) {
                                  blockEntity.setMoving(true);
                                  level.setBlock(pos, state.cycle(POWERED), 2);



                                  if(blockEntity.hasCards()) {
                                      if(posList!=null) {
                                          for (BlockPos eachPos : posList) {
                                              if(!blockEntity.getBlockPos().equals(eachPos)) {
                                                  destroyOldBlock(level, eachPos);
                                              }
                                          }
                                          for (BlockPos eachPos : posList) {
                                              if(!blockEntity.getBlockPos().equals(eachPos)) {
                                                  updateDestroyedPos(level, eachPos);
                                              }
                                          }
                                          blockEntity.setNotFirstTime(true);
                                      }
                                  }




                              }
                           }

                       }


                   }
            }
    }


   /* public static void makeMoveableBlock(Level level,BlockPos controllerPos,BlockPos startPos,int start,int duration,BlockPos transitionPos){
        if(level.getBlockEntity(controllerPos) instanceof SlideControllerBlockEntity slideControllerBlockEntity&&slideControllerBlockEntity.getItem(0).getItem()==Register.shape_card.get()&&slideControllerBlockEntity.getItem(0).getTag().contains("positionList")) {
            CompoundTag entityTag=new CompoundTag();
            List<BlockPos> posList=slideControllerBlockEntity.getPositionList();
           *//* if(posList.contains(controllerPos)) {
                posList.remove(controllerPos);
            }*//*
            CompoundTag posTag=new CompoundTag();
            CompoundTag stateTag=new CompoundTag();
            CompoundTag blockEntityTag=new CompoundTag();
            for(int i=0; i<posList.size();i++){
                BlockPos eachPos=posList.get(i);
                BlockState eachState=level.getBlockState(eachPos);
                BlockEntity eachBlockEntity = level.getBlockEntity(eachPos);
                  if (eachBlockEntity != null&&i!=posList.indexOf(controllerPos)&&!(eachBlockEntity instanceof PistonMovingBlockEntity)) {
                        if (eachBlockEntity instanceof SlideControllerBlockEntity slideControllerBlockEntity2 && !slideControllerBlockEntity2.getPositionList().isEmpty() && !slideControllerBlockEntity2.getEndPos().equals(Utils.errorPos())) {

                            List<BlockPos> newPos = new ArrayList<>();
                            for (int ii = 0; ii < ((SlideControllerBlockEntity) eachBlockEntity).getPositionList().size(); ii++) {
                                newPos.add(slideControllerBlockEntity2.getPositionList().get(ii).offset(transitionPos.getX(), transitionPos.getY(), transitionPos.getZ()));
                            }
                            slideControllerBlockEntity2.setPositionList(newPos);
                            BlockPos newEndPos = slideControllerBlockEntity2.getEndPos().offset(transitionPos.getX(), transitionPos.getY(), transitionPos.getZ());
                            slideControllerBlockEntity2.setEndPos(newEndPos);

                        }else if (eachBlockEntity instanceof RotationControllerBlockEntity rotationControllerBlockEntity && !rotationControllerBlockEntity.getPositionList().isEmpty() ) {

                            List<BlockPos> newPos = new ArrayList<>();
                            for (int ii = 0; ii < ((RotationControllerBlockEntity) eachBlockEntity).getPositionList().size(); ii++) {
                                newPos.add(rotationControllerBlockEntity.getPositionList().get(ii).offset(transitionPos.getX(), transitionPos.getY(), transitionPos.getZ()));
                            }
                            rotationControllerBlockEntity.setPositionList(newPos);
                        }
                        blockEntityTag.put("blockEntity_" + String.valueOf(i),eachBlockEntity.saveWithFullMetadata());
                  }else if (eachBlockEntity instanceof PistonMovingBlockEntity pistonMovingBlockEntity) {
                      eachState=pistonMovingBlockEntity.getMovedState();
                      blockEntityTag.put("blockEntity_" + String.valueOf(i),new CompoundTag());
                  }else if (eachState.getBlock() instanceof PressurePlateBlock||eachState.getBlock() instanceof ButtonBlock||eachState.getBlock() instanceof TripWireBlock) {
                      eachState=eachState.setValue(ButtonBlock.POWERED,false);
                      blockEntityTag.put("blockEntity_" + String.valueOf(i),new CompoundTag());
                  }else{
                        blockEntityTag.put("blockEntity_" + String.valueOf(i),new CompoundTag());
                    }
                    if(eachState.is(Register.TAG_DISABLE_MOVING)||i==posList.indexOf(controllerPos)){
                        eachState=Blocks.AIR.defaultBlockState();
                    }
                   eachState= eachState.hasProperty(BlockStateProperties.WATERLOGGED) ? eachState.setValue(BlockStateProperties.WATERLOGGED, false) : level.getFluidState(eachPos).isEmpty() ? eachState : Blocks.AIR.defaultBlockState();
                    posTag.put("location_" + String.valueOf(i), NbtUtils.writeBlockPos(new BlockPos(posList.get(i).getX() - startPos.getX(), posList.get(i).getY() - startPos.getY(), posList.get(i).getZ() - startPos.getZ())));
                    stateTag.put("state_" + String.valueOf(i), NbtUtils.writeBlockState(eachState));

            }
            entityTag.put("positionList",posTag);
            entityTag.put("stateList",stateTag);
            entityTag.put("blockEntityList",blockEntityTag);

            MovingBlockEntity moveableBlock = new MovingBlockEntity(level, startPos, level.getBlockState(controllerPos), start + 1, duration, transitionPos,entityTag);

                if (!level.isClientSide) {
                    level.addFreshEntity(moveableBlock);
                }

        }

    }*/
    public static void destroyOldBlock(Level level,BlockPos pos){
        BlockState state=level.getBlockState(pos);
        if(!state.is(Register.TAG_DISABLE_MOVING)) {
            level.removeBlockEntity(pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 82);
        }
    }
    public static void updateDestroyedPos(Level level,BlockPos pos){
        BlockState state=level.getBlockState(pos);
        if(!state.is(Register.TAG_DISABLE_MOVING)) {
            level.removeBlockEntity(pos);
            level.updateNeighborsAt(pos,state.getBlock());
        }
    }
    public RenderShape getRenderShape(BlockState p_49090_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SlideControllerBlockEntity(pos,state);
    }
    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152160_, BlockState p_152161_, BlockEntityType<T> p_152162_) {
        return createTickerHelper(p_152162_, Register.SlideController.get(), SlideControllerBlockEntity::tick);
    }
    public BlockState getStateForPlacement(BlockPlaceContext p_55087_) {
        return this.defaultBlockState().setValue(FACING, p_55087_.getNearestLookingDirection().getOpposite());
    }
    public BlockState rotate(BlockState p_55115_, Rotation p_55116_) {
        return p_55115_.setValue(FACING, p_55116_.rotate(p_55115_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_55112_, Mirror p_55113_) {
        return p_55112_.rotate(p_55113_.getRotation(p_55112_.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        if(livingEntity instanceof Player&&state.getBlock() instanceof SlideControllerBlock){
            ItemStack endLocationCard=new ItemStack(Register.vector_card.get());
            CompoundTag tag=new CompoundTag();
            tag.put("end_location", NbtUtils.writeBlockPos(Utils.errorPos()));
            //tag.put("start_location", NbtUtils.writeBlockPos(pos.relative(state.getValue(FACING))));
            endLocationCard.setTag(tag);
            ItemEntity itemEntity1=new ItemEntity(level,livingEntity.getX(),livingEntity.getY()+1D,livingEntity.getZ(),new ItemStack(Register.shape_card.get()));
            ItemEntity itemEntity2=new ItemEntity(level,livingEntity.getX(),livingEntity.getY()+1D,livingEntity.getZ(),endLocationCard);
            if(!level.isClientSide){
                level.addFreshEntity(itemEntity1);
                level.addFreshEntity(itemEntity2);
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<Component> list, TooltipFlag p_49819_) {
        list.add(Component.translatable("info.ugoblock.slide_controller").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.slide_controller_scroll").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.block_imitatable").withStyle(ChatFormatting.GREEN));
    }
}
