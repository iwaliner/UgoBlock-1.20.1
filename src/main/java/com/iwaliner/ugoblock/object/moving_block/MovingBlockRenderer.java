package com.iwaliner.ugoblock.object.moving_block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.joml.*;

import java.lang.Math;
import java.util.List;


    public class MovingBlockRenderer<T extends MovingBlockEntity, S> extends EntityRenderer<T> {
   /* private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;*/
    private final BlockRenderDispatcher blockRenderer;
    public MovingBlockRenderer(EntityRendererProvider.Context context) {
            super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();

      /*  ModelPart modelpart = context.bakeLayer(ModelLayers.CHEST);
            this.bottom = modelpart.getChild("bottom");
            this.lid = modelpart.getChild("lid");
            this.lock = modelpart.getChild("lock");
            ModelPart modelpart1 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
            this.doubleLeftBottom = modelpart1.getChild("bottom");
            this.doubleLeftLid = modelpart1.getChild("lid");
            this.doubleLeftLock = modelpart1.getChild("lock");
            ModelPart modelpart2 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
            this.doubleRightBottom = modelpart2.getChild("bottom");
            this.doubleRightLid = modelpart2.getChild("lid");
            this.doubleRightLock = modelpart2.getChild("lock");*/
        }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
      return   TextureAtlas.LOCATION_BLOCKS;
    }



    private static <T extends Display> float entityYRot(T p_297849_, float p_297686_) {
        return Mth.rotLerp(p_297686_, p_297849_.yRotO, p_297849_.getYRot());
    }

    private static <T extends Display> float entityXRot(T p_298651_, float p_297691_) {
        return Mth.lerp(p_297691_, p_298651_.xRotO, p_298651_.getXRot());
    }

    /**表示の補完*/
    private Quaternionf calculateOrientationSmooth(MovingBlockEntity movingBlock, float f, Quaternionf quaternionf) {
                if(movingBlock.getAxis()== Direction.Axis.X){
                    quaternionf = quaternionf.rotationYXZ(0F, -0.017453292F * entityYRot(movingBlock, f), 0.0F);
                }else if(movingBlock.getAxis()== Direction.Axis.Y){
                    quaternionf = quaternionf.rotationYXZ(-0.017453292F * entityYRot(movingBlock, f), ((float)Math.PI / 180F) * entityXRot(movingBlock, f), 0.0F);
                }else if(movingBlock.getAxis()== Direction.Axis.Z){
                    quaternionf = quaternionf.rotationYXZ(0F, ((float)Math.PI / 180F) * entityXRot(movingBlock, f), -0.017453292F * entityYRot(movingBlock, f));
                }
        return quaternionf;
    }
    public void render(T movingBlock, float f1, float f2, PoseStack poseStack, MultiBufferSource bufferSource, int i0) {
        Display.RenderState display$renderstate = movingBlock.renderState();
        if (display$renderstate != null) {
                float f = movingBlock.calculateInterpolationProgress(f2);
                this.shadowRadius = display$renderstate.shadowRadius().get(f);
                this.shadowStrength = display$renderstate.shadowStrength().get(f);
                super.render(movingBlock, f1, f2, poseStack, bufferSource, i0);
                Quaternionf rotatedQuaternionf=this.calculateOrientationSmooth((MovingBlockEntity) movingBlock, f2, new Quaternionf());

                poseStack.pushPose();
                poseStack.mulPose(rotatedQuaternionf);
                Transformation transformation = display$renderstate.transformation().get(f);
                poseStack.mulPoseMatrix(transformation.getMatrix());
                poseStack.last().normal().rotate(transformation.getLeftRotation()).rotate(transformation.getRightRotation());
                this.renderInner( (MovingBlockEntity)movingBlock, poseStack, bufferSource,rotatedQuaternionf,i0);
                poseStack.popPose();
        }
    }
    protected int getBlockLightLevel(T p_174216_, BlockPos p_174217_) {
        return p_174216_.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, super.getBlockLightLevel(p_174216_, p_174217_)) : super.getBlockLightLevel(p_174216_, p_174217_);
    }

    private void renderInner(MovingBlockEntity movingBlock, PoseStack poseStack, MultiBufferSource multiBufferSource,Quaternionf rotatedQuaternionf,int i0) {
        List<BlockPos> posList = movingBlock.getPosList();
        if(posList == null || posList.isEmpty()){
            return;
        }
        List<BlockState> stateList = movingBlock.getStateList();
        if(stateList == null || stateList.isEmpty()){
            return;
        }


        Level level = movingBlock.level();
        BlockPos movingBlockPosition = movingBlock.blockPosition();
        BlockState placedState=level.getBlockState(movingBlockPosition);
        i0=15728880;
        BlockPos lightPos=posList.get(0).offset(movingBlockPosition.getX(),movingBlockPosition.getY(),movingBlockPosition.getZ());
        int light=LightTexture.pack(this.getBlockLightLevel((T) movingBlock, lightPos), this.getSkyLightLevel((T) movingBlock, lightPos));
        if(placedState.isAir()){
            movingBlock.setPreBlockLightLevel(i0);
        }
        for (int i = 0; i < posList.size(); i++) {
            BlockPos eachPos = posList.get(i);
            BlockState eachState = stateList.get(i);
            Block block = eachState.getBlock();

            if (!stateList.get(i).isAir()) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees((float) movingBlock.getVisualXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees((float) movingBlock.getVisualYRot()));
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) movingBlock.getVisualZRot()));

                poseStack.translate(eachPos.getX(), eachPos.getY(), eachPos.getZ());
                     /*if (block instanceof ChestBlock abstractchestblock) {

                        boolean flag = level!= null;
                        ChestType chesttype = eachState.hasProperty(ChestBlock.TYPE) ? eachState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
                        boolean flag1 = chesttype != ChestType.SINGLE;
                        LidBlockEntity chestBlockEntity=new ChestBlockEntity(pos,eachState);


                        float f = eachState.getValue(ChestBlock.FACING).toYRot();
                        poseStack.translate(0.5F, 0.5F, 0.5F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
                        poseStack.translate(-0.5F, -0.5F, -0.5F);
                        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborcombineresult;
                        if (flag) {
                            neighborcombineresult = abstractchestblock.combine(eachState, level, pos, true);
                        } else {
                            neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
                        }

                        float f1 = neighborcombineresult.apply(ChestBlock.opennessCombiner(chestBlockEntity)).get(f0);

                        f1 = 1.0F - f1;
                        f1 = 1.0F - f1 * f1 * f1;
                           //  int i = blockDisplay.renderState().glowColorOverride();

                            //      int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(i0);
                            //   int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(-1);
                            Material material = this.getMaterial((BlockEntity) chestBlockEntity, chesttype);
                            VertexConsumer vertexconsumer = material.buffer(multiBufferSource, RenderType::entityCutout);
                            int ii = 0;
                            //  i=0; //色
                            //   int i=15;
                            //    ModCoreUgoBlock.logger.info("色;"+i);
                            //    i*=2;
                        int iii = Minecraft.getInstance().getBlockColors().getColor(eachState, (BlockAndTintGetter)null, pos, 0);
                        float ff = movingBlock.calculateInterpolationProgress(f0);
                            this.shadowRadius = movingBlock.renderState().shadowRadius().get(ff);
                            this.shadowStrength = movingBlock.renderState().shadowStrength().get(ff);
                            if (flag1) {
                                if (chesttype == ChestType.LEFT) {
                                    this.render(poseStack, vertexconsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f1, i0, ii);
                                } else {
                                    this.render(poseStack, vertexconsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f1, i0, ii);
                                }
                            } else {
                                this.render(poseStack, vertexconsumer, this.lid, this.lock, this.bottom, f1, i0, ii);
                            }

                            poseStack.scale(0F, 0F, 0F);



                    }*/
                    /*if (block instanceof ChestBlock || block instanceof EnderChestBlock || (block instanceof BedBlock)) {
                        //  poseStack.pushPose();
                        Direction direction = eachState.getValue(HorizontalDirectionalBlock.FACING);
                        Direction direction1 = Direction.from2DDataValue((direction.get2DDataValue()) % 4);
                        float f = -direction1.toYRot();
                        poseStack.mulPose(Axis.YP.rotationDegrees(f));
                        if (direction == Direction.EAST) {
                            poseStack.translate(-1D, 0D, 0D);
                        } else if (direction == Direction.NORTH) {
                            poseStack.translate(-1D, 0D, -1D);
                        } else if (direction == Direction.WEST) {
                            poseStack.translate(0D, 0D, -1D);
                        }
                    }*/
                    /*if(movingBlock.shouldFixFighting()) {
                        poseStack.translate(0.005F, 0.005F, 0.005F);
                        poseStack.scale(0.99F, 0.99F, 0.99F);

                    }*/
                if (!(block instanceof BedBlock && eachState.getValue(BedBlock.PART) == BedPart.FOOT)) {
                    poseStack.translate(-0.4999D, -0.4999D, -0.4999D);
                    this.blockRenderer.renderSingleBlock(eachState, poseStack, multiBufferSource,light, OverlayTexture.NO_OVERLAY);
                   // this.blockRenderer.renderSingleBlock(eachState, poseStack, multiBufferSource,movingBlock.getPreBlockLightLevel(), OverlayTexture.NO_OVERLAY);
                }
                poseStack.popPose();


            }
        }
        //poseStack.popPose();
        List<BlockPos> basketPosList = movingBlock.getBasketPosList();
        List<BlockPos> basketOriginPosList = movingBlock.getBasketOriginPosList();
        List<BlockState> basketStateList = movingBlock.getBasketStateList();
//        if(basketPosList.isEmpty() || basketOriginPosList.isEmpty() || basketStateList.isEmpty()){
//            return;
//        }

        for (int j = 0; j < basketPosList.size(); j++) {
            BlockPos eachBasketPos = basketPosList.get(j);
            BlockPos eachBasketOriginPos = basketOriginPosList.get(j);
            BlockPos eachBasketOffset=eachBasketPos.offset(-eachBasketOriginPos.getX(),-eachBasketOriginPos.getY(),-eachBasketOriginPos.getZ());
            BlockState eachBasketState = basketStateList.get(j);
            Block eachBasketBlock = eachBasketState.getBlock();
            poseStack.pushPose();
            poseStack.translate(eachBasketOriginPos.getX(), eachBasketOriginPos.getY(),eachBasketOriginPos.getZ());
            if (!(eachBasketBlock instanceof BedBlock && eachBasketState.getValue(BedBlock.PART) == BedPart.FOOT)) {
               int lightLevel = eachBasketBlock.getLightEmission(eachBasketState, level, eachBasketPos);
                int skyLightLevel = this.getSkyLightLevel((T) movingBlock,eachBasketPos);
                   if (movingBlock.getAxis() == Direction.Axis.X) {
                       poseStack.mulPose(new Quaternionf(-rotatedQuaternionf.x, 0F, 0F, rotatedQuaternionf.w));

                   } else if (movingBlock.getAxis() == Direction.Axis.Y) {
                       poseStack.mulPose(new Quaternionf(0F, -rotatedQuaternionf.y, 0F, rotatedQuaternionf.w));

                   } else if (movingBlock.getAxis() == Direction.Axis.Z) {
                       poseStack.mulPose(new Quaternionf(0F, 0F, -rotatedQuaternionf.z, rotatedQuaternionf.w));
                   }
                poseStack.translate(-0.4999D, -0.4999D, -0.4999D);
                poseStack.translate(eachBasketOffset.getX(), eachBasketOffset.getY(), eachBasketOffset.getZ());
                this.blockRenderer.renderSingleBlock(eachBasketState, poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
    }
    protected Material getMaterial(BlockEntity blockEntity, ChestType chestType) {
        return Sheets.chooseMaterial(blockEntity, chestType, false);
    }
    }

