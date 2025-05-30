package com.iwaliner.ugoblock.object.wireless_redstone_receiver;

import com.iwaliner.ugoblock.object.wireless_redstone_transmitter.WirelessRedstoneTransmitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WirelessRedstoneReceiverRenderer implements BlockEntityRenderer<WirelessRedstoneReceiverBlockEntity> {
    private final BlockRenderDispatcher blockRenderDispatcher;
    public WirelessRedstoneReceiverRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderDispatcher=context.getBlockRenderDispatcher();
    }
    public void render(WirelessRedstoneReceiverBlockEntity blockEntity, float f1, PoseStack poseStack, MultiBufferSource bufferSource, int i1, int i2) {
      BlockState imitatingState=blockEntity.getImitatingState();
        if(!imitatingState.isAir()){
            poseStack.pushPose();
            poseStack.scale(1.005F, 1.005F, 1.005F);
            poseStack.translate(-0.0025F,-0.0025F,-0.0025F);
            this.blockRenderDispatcher.renderSingleBlock(imitatingState,poseStack,bufferSource,i1, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }


}