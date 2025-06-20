package com.iwaliner.ugoblock.object.seat;

import com.iwaliner.ugoblock.register.Register;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SeatEntityRenderer<T extends Entity, S> extends EntityRenderer<T>
{
    public static final ResourceLocation SEAT_TEXTURE = new ResourceLocation("ugoblock:textures/item/seat.png");

    public SeatEntityRenderer(EntityRendererProvider.Context p_174008_)
    {
        super(p_174008_);
    }

    @Override
    public void render(T entity, float p_114486_, float p_114487_, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn)
    {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack itemStack = player.getMainHandItem().is(Register.seat_blockitem.get()) ? player.getMainHandItem() : player.getOffhandItem();
        if (itemStack.getItem() != Register.seat_blockitem.get()) return;
        itemStack = itemStack.copyWithCount(1);
        poseStack.pushPose();
        poseStack.translate(0, 0.5, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-player.getYRot()));
        poseStack.scale(0.618f, 0.618f, 0.618f);
        Minecraft.getInstance().getItemRenderer().render(itemStack, ItemDisplayContext.NONE, true, poseStack, bufferSource, packedLightIn, OverlayTexture.NO_OVERLAY, Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, packedLightIn));
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_)
    {
        return SEAT_TEXTURE;
    }
}
