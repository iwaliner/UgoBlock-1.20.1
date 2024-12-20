package com.iwaliner.ugoblock.object;

import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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

import java.util.ArrayList;
import java.util.List;

public class RotationControllerBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty MOVING = BooleanProperty.create("moving");
    public static final BooleanProperty COUNTER_CLOCKWISE = BooleanProperty.create("counter_clockwise");
    public RotationControllerBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(MOVING,false).setValue(COUNTER_CLOCKWISE,true));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        p_49915_.add(POWERED,FACING,MOVING,COUNTER_CLOCKWISE);
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
        if(level.getBlockEntity(pos) instanceof RotationControllerBlockEntity blockEntity) {
            if (stack.getItem() == Register.shape_card.get()&&blockEntity.getItem(0).isEmpty()) {
                blockEntity.setItem(0,stack);
                blockEntity.setPositionList(ShapeCardItem.getPositionList(stack.getTag()));
                player.setItemInHand(hand,ItemStack.EMPTY);
            }else if (stack.getItem() == Register.end_location_card.get()&&stack.getTag()!=null&&blockEntity.getItem(1).isEmpty()) {
               blockEntity.setItem(1,stack);
                blockEntity.setEndPos(EndLocationCardItem.getEndPos(stack.getTag()));
                player.setItemInHand(hand,ItemStack.EMPTY);
            }else if(!level.isClientSide){
                this.openContainer(level, pos, player);
                return InteractionResult.CONSUME;
            }
        }


        return InteractionResult.SUCCESS;
    }
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {

        if(level.getBlockEntity(pos) instanceof RotationControllerBlockEntity blockEntity&&blockEntity.hasCards()) {
            List<BlockPos> posList=blockEntity.getPositionList();
            if(posList!=null) {
                for (BlockPos eachPos : posList) {
                    if(!blockEntity.getBlockPos().equals(eachPos)) {
                        destroyOldBlock(level, eachPos); /**neighborChangedでエンティティ化したら、その2tickあとにここでエンティティ化済みのブロックを除去する。時間をあけるのは一瞬何も表示されなくなる(=一瞬消えることでちらつく)のを軽減するため。*/
                    }
                }
                blockEntity.setNotFirstTime(true);
            }
        }
    }
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean b) {
        boolean flag = state.getValue(POWERED);

            if (flag != level.hasNeighborSignal(pos)&&level.getBlockEntity(pos) instanceof RotationControllerBlockEntity blockEntity) {
                   if(!blockEntity.isMoving()&&blockEntity.hasCards()) { /**動いている最中は赤石入力の変化を無視する*/
                      int start=blockEntity.getStartTime();
                      int duration=blockEntity.getDuration();
                       if(blockEntity.getEndPos()!=null&&duration>0) {
                           BlockPos startPos=pos.relative(state.getValue(FACING));
                           BlockPos endPos=blockEntity.getEndPos();
                           List<BlockPos> posList=blockEntity.getPositionList();
                           if(posList!=null) {
                                blockEntity.setMoving(true);
                                /**↓移動の変位。座標ではないことに注意。*/
                               BlockPos transitionPos = new BlockPos(startPos.getX() - endPos.getX(), startPos.getY() - endPos.getY(), startPos.getZ() - endPos.getZ());
                               for (BlockPos eachPos : posList) {
                                   if(!blockEntity.getBlockPos().equals(eachPos)) {
                                       /**ブロックをエンティティ化*/
                                       makeMoveableBlock(level, startPos,eachPos, start, duration, transitionPos);
                                   }
                               }
                           }
                       }
                           level.setBlock(pos, state.cycle(POWERED), 2);
                           level.scheduleTick(pos, this, 2);

                   }
            }



    }
    /*public static void makeMoveableBlock(Level level,BlockPos startPos,int start,int duration,BlockPos transitionPos){
        BlockEntity blockEntity=level.getBlockEntity(startPos);
        BlockState state=level.getBlockState(startPos);
        MoveableBlockEntity moveableBlock;
        if(!state.isAir()&&!state.is(Register.TAG_DISABLE_MOVING)) {
            if (blockEntity != null) {
                moveableBlock = new MoveableBlockEntity(level, startPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start * 20, duration * 20, transitionPos, blockEntity);
                if(blockEntity instanceof SlideControllerBlockEntity slideControllerBlockEntity&&!slideControllerBlockEntity.getPositionList().isEmpty()&&!slideControllerBlockEntity.getEndPos().equals(ShapeCardItem.errorPos())){

                    List<BlockPos> newPos=new ArrayList<>();
                    for(int i=0;i< ((SlideControllerBlockEntity) blockEntity).getPositionList().size();i++){
                        newPos.add(slideControllerBlockEntity.getPositionList().get(i).offset(transitionPos.getX(),transitionPos.getY(),transitionPos.getZ()));
                    }
                    slideControllerBlockEntity.setPositionList(newPos);
                    BlockPos newEndPos=slideControllerBlockEntity.getEndPos().offset(transitionPos.getX(),transitionPos.getY(),transitionPos.getZ());
                    slideControllerBlockEntity.setEndPos(newEndPos);

                    moveableBlock = new MoveableBlockEntity(level, startPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start * 20+1, duration * 20, transitionPos, slideControllerBlockEntity);

                }
            } else {
                moveableBlock = new MoveableBlockEntity(level, startPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start * 20+1, duration * 20, transitionPos, null);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(moveableBlock);
            }
        }

    }*/
    public static void makeMoveableBlock(Level level,BlockPos startPos,BlockPos visualPos,int start,int duration,BlockPos transitionPos){
        BlockEntity blockEntity=level.getBlockEntity(visualPos);
        BlockState state=level.getBlockState(visualPos);
        MovingBlockEntity moveableBlock;
        MovingBlockEntity.trigonometricFunctionType type= MovingBlockEntity.trigonometricFunctionType.Y_COUNTERCLOCKWISE;
        if(!state.isAir()&&!state.is(Register.TAG_DISABLE_MOVING)) {
            if (blockEntity != null) {
                moveableBlock = new MovingBlockEntity(level, startPos,visualPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start, duration, transitionPos, blockEntity,type);
                if(blockEntity instanceof RotationControllerBlockEntity slideControllerBlockEntity&&!slideControllerBlockEntity.getPositionList().isEmpty()&&!slideControllerBlockEntity.getEndPos().equals(ShapeCardItem.errorPos())){

                    List<BlockPos> newPos=new ArrayList<>();
                    for(int i=0;i< ((RotationControllerBlockEntity) blockEntity).getPositionList().size();i++){
                        newPos.add(slideControllerBlockEntity.getPositionList().get(i).offset(transitionPos.getX(),transitionPos.getY(),transitionPos.getZ()));
                    }
                    slideControllerBlockEntity.setPositionList(newPos);
                    BlockPos newEndPos=slideControllerBlockEntity.getEndPos().offset(transitionPos.getX(),transitionPos.getY(),transitionPos.getZ());
                    slideControllerBlockEntity.setEndPos(newEndPos);

                    moveableBlock = new MovingBlockEntity(level, startPos,visualPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start+1, duration, transitionPos, slideControllerBlockEntity,type);

                }
            } else {
                moveableBlock = new MovingBlockEntity(level, startPos,visualPos, state.hasProperty(BlockStateProperties.WATERLOGGED)? state.setValue(BlockStateProperties.WATERLOGGED,false) : level.getFluidState(startPos).isEmpty()? state : Blocks.AIR.defaultBlockState(), start+1, duration, transitionPos, null,type);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(moveableBlock);
            }
        }

    }
    public static void destroyOldBlock(Level level,BlockPos pos){
        BlockState state=level.getBlockState(pos);
        if(!state.is(Register.TAG_DISABLE_MOVING)) {
            level.removeBlockEntity(pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 82);
        }
    }

    public RenderShape getRenderShape(BlockState p_49090_) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RotationControllerBlockEntity(pos,state);
    }
    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152160_, BlockState p_152161_, BlockEntityType<T> p_152162_) {
        return createTickerHelper(p_152162_, Register.RotationController.get(), RotationControllerBlockEntity::tick);
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
        if(livingEntity instanceof Player&&state.getBlock() instanceof RotationControllerBlock){
            ItemStack endLocationCard=new ItemStack(Register.end_location_card.get());
            CompoundTag tag=new CompoundTag();
            tag.put("end_location", NbtUtils.writeBlockPos(ShapeCardItem.errorPos()));
            tag.put("start_location", NbtUtils.writeBlockPos(pos.relative(state.getValue(FACING))));
            endLocationCard.setTag(tag);
            ItemEntity itemEntity1=new ItemEntity(level,livingEntity.getX(),livingEntity.getY()+1D,livingEntity.getZ(),new ItemStack(Register.shape_card.get()));
            ItemEntity itemEntity2=new ItemEntity(level,livingEntity.getX(),livingEntity.getY()+1D,livingEntity.getZ(),endLocationCard);
            if(!level.isClientSide){
                level.addFreshEntity(itemEntity1);
                level.addFreshEntity(itemEntity2);
            }
        }
    }


}
