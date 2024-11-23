package com.iwaliner.ugoblock.object;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Objects;

public class MoveableBlockRenderer extends DisplayRenderer.BlockDisplayRenderer {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    public MoveableBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(ModelLayers.CHEST);
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
        this.doubleRightLock = modelpart2.getChild("lock");
    }
    @Override
    public void render(Display.BlockDisplay blockDisplay, float ff1, float ff2, PoseStack poseStack, MultiBufferSource bufferSource, int i0) {
        /*poseStack.scale(2F,2F,2F);
        super.render(blockDisplay, ff1, ff2, poseStack, bufferSource, i0);
        poseStack.popPose();*/


        if (blockDisplay.blockRenderState()!=null&&(blockDisplay.blockRenderState().blockState())!=null&&blockDisplay.blockRenderState().blockState().getBlock() instanceof AbstractChestBlock<?> abstractchestblock) {
            BlockState state=blockDisplay.blockRenderState().blockState();

            Level level=blockDisplay.level();
            boolean flag = level!= null;
            ChestType chesttype = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
            boolean flag1 = chesttype != ChestType.SINGLE;
            LidBlockEntity chestBlockEntity=new ChestBlockEntity(blockDisplay.blockPosition(),state);
            if(state.getBlock()instanceof EnderChestBlock){
                chestBlockEntity=new EnderChestBlockEntity(blockDisplay.blockPosition(),state);
            }
            poseStack.pushPose();
            float f = state.getValue(ChestBlock.FACING).toYRot();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-f));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborcombineresult;
            if (flag) {
                neighborcombineresult = abstractchestblock.combine(state, level, blockDisplay.blockPosition(), true);
            } else {
                neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float f1 = neighborcombineresult.apply(ChestBlock.opennessCombiner(chestBlockEntity)).get(ff1);

            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
         //   int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(i0);
            int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(-1);
            Material material = this.getMaterial((BlockEntity) chestBlockEntity, chesttype);
            VertexConsumer vertexconsumer = material.buffer(bufferSource, RenderType::entityCutout);
            int ii=0;
          //  i=0; //色

            if (flag1) {
                if (chesttype == ChestType.LEFT) {
                    this.render(poseStack, vertexconsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f1, i, ii);
                } else {
                    this.render(poseStack, vertexconsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f1, i, ii);
                }
            } else {
                this.render(poseStack, vertexconsumer, this.lid, this.lock, this.bottom, f1, i, ii);
            }

            poseStack.scale(0F,0F,0F);
            super.render(blockDisplay, ff1, ff2, poseStack, bufferSource, i0);
            poseStack.popPose();

        }else{
            super.render(blockDisplay, ff1, ff2, poseStack, bufferSource, i0);

        }
    }

    @Override
    public void renderInner(Display.BlockDisplay blockDisplay, Display.BlockDisplay.BlockRenderState blockRenderState, PoseStack poseStack, MultiBufferSource bufferSource, int i0, float f0) {
        BlockState state=blockDisplay.blockRenderState().blockState();
        Block block=state.getBlock();
      /*  if (!(block instanceof AbstractChestBlock<?>)) {
            super.renderInner(blockDisplay, blockRenderState, poseStack, bufferSource, i0, f0);
        }else{*/
            super.renderInner(blockDisplay, blockRenderState, poseStack, bufferSource, i0, f0);
      //  }
    }
    private void render(PoseStack p_112370_, VertexConsumer p_112371_, ModelPart p_112372_, ModelPart p_112373_, ModelPart p_112374_, float p_112375_, int p_112376_, int p_112377_) {
        p_112372_.xRot = -(p_112375_ * ((float)Math.PI / 2F));
        p_112373_.xRot = p_112372_.xRot;
        p_112372_.render(p_112370_, p_112371_, p_112376_, p_112377_);
        p_112373_.render(p_112370_, p_112371_, p_112376_, p_112377_);
        p_112374_.render(p_112370_, p_112371_, p_112376_, p_112377_);
    }
    protected Material getMaterial(BlockEntity blockEntity, ChestType chestType) {
        return Sheets.chooseMaterial(blockEntity, chestType, false);
    }
  }


