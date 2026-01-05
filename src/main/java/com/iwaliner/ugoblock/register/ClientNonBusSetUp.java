package com.iwaliner.ugoblock.register;

import com.iwaliner.ugoblock.ModCoreUgoBlock;
import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.object.controller.VectorCardItem;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sun.jna.platform.win32.WinNT;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;

@Mod.EventBusSubscriber(modid = ModCoreUgoBlock.MODID, value = Dist.CLIENT)
public class ClientNonBusSetUp {

    // @debug, only render one box for performance
    private static final double[] box = {
        // front
        0.05, 0.05, 0.0, 0.95, 0.95, 0.0,
        // back
        0.05, 0.05, 1.0, 0.95, 0.95, 1.0,
        // top
        0.05, 1.0, 0.05, 0.95, 1.0, 0.95,
        // bottom
        0.05, 0.0, 0.05, 0.95, 0.0, 0.95,
        // right
        1.0, 0.05, 0.05, 1.0, 0.95, 0.95,
        // left
        0.0, 0.05, 0.05, 0.0, 0.95, 0.95};


    private static final double two_third_PI = 2.09439510239319549231;// PI*2/3
    private static final double one_third_PI = 1.04719755119659774615;// PI/3


    private static final ResourceLocation GREEN_BLOCK_TEXTURE = new ResourceLocation("ugoblock", "textures/gui/green_block_surface.png");
    private static final ResourceLocation YELLOW_BLOCK_TEXTURE = new ResourceLocation("ugoblock", "textures/gui/yellow_block_surface.png");

    private static void renderFace(int sel, VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float nx, float ny, float nz, float r, float g, float b, float a,
                                 float minU, float maxU, float minV, float maxV) {

        switch(sel){
            case 0:
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;
            case 1:
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;
            case 2:
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;
            case 3:
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;
            case 4:
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;
            case 5:
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, nx, ny, nz).endVertex();
                break;

        }
    }

    public static void renderShape(PoseStack poseStack, MultiBufferSource buffer, double d1, double d2, double d3, boolean isYellow, boolean[] faceVisibility) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(isYellow ? YELLOW_BLOCK_TEXTURE : GREEN_BLOCK_TEXTURE));

        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        float minU = 0f;
        float maxU = 1f;
        float minV = 0f;
        float maxV = 1f;

        float r = isYellow ? 1f : 0f;
        float g = 1f;
        float b = 0f;
        float a = 0.95f;

        float offsetDistance = 0.005f;


        for (int i = 0; i < 6; i++) {
            if (!faceVisibility[i]) continue;
            
            int faceOffset = i * 6;
            float x1 = (float)(box[faceOffset] + d1);
            float y1 = (float)(box[faceOffset + 1] + d2);
            float z1 = (float)(box[faceOffset + 2] + d3);
            float x2 = (float)(box[faceOffset + 3] + d1);
            float y2 = (float)(box[faceOffset + 4] + d2);
            float z2 = (float)(box[faceOffset + 5] + d3);

            float nx = 0, ny = 0, nz = 0;
            switch (i) {
                case 0: // front
                    nz = -1;
                    z1 -= offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    z1 += offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;

                case 1: // back
                    nz = 1;
                    z1 += offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    z1 -= offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;

                case 2: // top
                    ny = 1;
                    y1 += offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    y1 -= offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;

                case 3: // bottom
                    ny = -1;
                    y1 -= offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);

                    y1 += offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;

                case 4: // right
                    nx = 1;
                    x1 += offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);

                    x1 -= offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;

                case 5: // left
                    nx = -1;
                    x1 -= offsetDistance;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    x1 += offsetDistance * 2;
                    renderFace(i,vertexConsumer, matrix4f, matrix3f, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, minU, maxU, minV, maxV);
                    break;
            }
        }
    }

    public static void renderYellowOutline(PoseStack poseStack, MultiBufferSource buffer, Entity entity, double x, double y, double z, BlockPos blockPos, boolean[] faceVisibility) {
        renderShape(poseStack, buffer, blockPos.getX() - x, blockPos.getY() - y, blockPos.getZ() - z, true, faceVisibility);
    }

    public static void renderGreenOutline(PoseStack poseStack, MultiBufferSource buffer, Entity entity, double x, double y, double z, BlockPos blockPos) {
        boolean[] faceVisibility = {true, true, true, true, true, true};
        renderShape(poseStack, buffer, blockPos.getX() - x, blockPos.getY() - y, blockPos.getZ() - z, false,  faceVisibility);
    }


    @SubscribeEvent
    public static void RenderLevelEvent(RenderLevelStageEvent event) throws Throwable {
        RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.AFTER_PARTICLES;
        Camera camera = event.getCamera();
        Entity cameraEntity = camera.getEntity();
        Vec3 cameraPos = camera.getPosition();
        if (event.getStage() == stage && cameraEntity instanceof Player player) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            Item vectorCard = Register.vector_card.get();
            Item shapeCard = Register.shape_card.get();

            if (mainStack.is(vectorCard) || offStack.is(vectorCard)) {
                ItemStack stack = mainStack.is(vectorCard) ? mainStack : offStack;
                CompoundTag tag = stack.getTag();
                if (tag == null) {
                    return;
                }
                BlockPos startLocation;
                if (!tag.contains("originPosition")) {
                    return;
                }
                startLocation = NbtUtils.readBlockPos(tag.getCompound("originPosition"));

                if (tag.contains("endPosition")) {
                    VertexConsumer lineBuffer = multiBufferSource.getBuffer(RenderType.lines());
                    BlockPos endLocation = NbtUtils.readBlockPos(tag.getCompound("endPosition"));
                    double startLocationX = startLocation.getX();
                    double startLocationY = startLocation.getY();
                    double startLocationZ = startLocation.getZ();
                    double dx = startLocationX - cameraPos.x + 0.5D;
                    double dy = startLocationY - cameraPos.y + 0.5D;
                    double dz = startLocationZ - cameraPos.z + 0.5D;
                    double transitionX = endLocation.getX() - startLocationX;
                    double transitionY = endLocation.getY() - startLocationY;
                    double transitionZ = endLocation.getZ() - startLocationZ;

                    double length = 0.5D;
                    double angleXZ = Math.atan(transitionZ / transitionX);
                    double arrowPointXZ_A_x = transitionX >= 0 ? transitionX - length * Math.sin(one_third_PI - angleXZ) : transitionX + length * Math.sin(one_third_PI - angleXZ);
                    double arrowPointXZ_A_z = transitionX >= 0 ? transitionZ - length * Math.cos(one_third_PI - angleXZ) : transitionZ + length * Math.cos(one_third_PI - angleXZ);
                    double arrowPointXZ_B_x = transitionX >= 0 ? transitionX - length * Math.sin(two_third_PI - angleXZ) : transitionX + length * Math.sin(two_third_PI - angleXZ);
                    double arrowPointXZ_B_z = transitionX >= 0 ? transitionZ - length * Math.cos(two_third_PI - angleXZ) : transitionZ + length * Math.cos(two_third_PI - angleXZ);

                    double angleXY = Math.atan(transitionY / transitionX);
                    double arrowPointXY_A_x = transitionX >= 0 ? transitionX - length * Math.sin(one_third_PI - angleXY) : transitionX + length * Math.sin(one_third_PI - angleXY);
                    double arrowPointXY_A_y = transitionX >= 0 ? transitionY - length * Math.cos(one_third_PI - angleXY) : transitionY + length * Math.cos(one_third_PI - angleXY);
                    double arrowPointXY_B_x = transitionX >= 0 ? transitionX - length * Math.sin(two_third_PI - angleXY) : transitionX + length * Math.sin(two_third_PI - angleXY);
                    double arrowPointXY_B_y = transitionX >= 0 ? transitionY - length * Math.cos(two_third_PI - angleXY) : transitionY + length * Math.cos(two_third_PI - angleXY);

                    double angleZY = Math.atan(transitionY / transitionZ);
                    double arrowPointZY_A_z = transitionZ >= 0 ? transitionZ - length * Math.sin(one_third_PI - angleZY) : transitionZ + length * Math.sin(one_third_PI - angleZY);
                    double arrowPointZY_A_y = transitionZ >= 0 ? transitionY - length * Math.cos(one_third_PI - angleZY) : transitionY + length * Math.cos(one_third_PI - angleZY);
                    double arrowPointZY_B_z = transitionZ >= 0 ? transitionZ - length * Math.sin(two_third_PI - angleZY) : transitionZ + length * Math.sin(two_third_PI - angleZY);
                    double arrowPointZY_B_y = transitionZ >= 0 ? transitionY - length * Math.cos(two_third_PI - angleZY) : transitionY + length * Math.cos(two_third_PI - angleZY);

                    float[] endColor = {0F, 1F, 0F, 1F};
                    float[] originColor = {1F, 1F, 1F, 1F};
                    for (int j = 0; j < 6; j++) {
                        float fx1 = 0F;
                        float fy1 = 0F;
                        float fz1 = 0F;
                        float b = 1F;
                        switch (j) {
                            case 0 -> fx1 = b;
                            case 1 -> fx1 = -b;
                            case 2 -> fy1 = b;
                            case 3 -> fy1 = -b;
                            case 4 -> fz1 = b;
                            case 5 -> fz1 = -b;
                        }
                        for (int i = 0; i < 6; i++) {
                            double dx1 = 0D;
                            double dy1 = 0D;
                            double dz1 = 0D;

                            switch (i) {
                                case 0 -> dx1 += 0.52D;
                                case 1 -> dx1 -= 0.52D;
                                case 2 -> dy1 += 0.52D;
                                case 3 -> dy1 -= 0.52D;
                                case 4 -> dz1 += 0.52D;
                                case 5 -> dz1 -= 0.52D;
                            }

                            double dx_total = dx + dx1, dy_total = dy + dy1, dz_total = dz + dz1;

                            poseStack.pushPose();
                            lineBuffer.vertex(poseStack.last().pose(), (float) (0 + dx_total), (float) (0 + dy_total), (float) (0 + dz_total)).color(originColor[0], originColor[1], originColor[2], originColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                            lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                            switch (i) {
                                case 0:
                                case 1:
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (arrowPointZY_A_y + dy_total), (float) (arrowPointZY_A_z + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (arrowPointZY_B_y + dy_total), (float) (arrowPointZY_B_z + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    break;
                                case 2:
                                case 3:
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (arrowPointXZ_A_x + dx_total), (float) (transitionY + dy_total), (float) (arrowPointXZ_A_z + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (arrowPointXZ_B_x + dx_total), (float) (transitionY + dy_total), (float) (arrowPointXZ_B_z + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    break;
                                case 4:
                                case 5:
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (arrowPointXY_A_x + dx_total), (float) (arrowPointXY_A_y + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (arrowPointXY_B_x + dx_total), (float) (arrowPointXY_B_y + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    lineBuffer.vertex(poseStack.last().pose(), (float) (transitionX + dx_total), (float) (transitionY + dy_total), (float) (transitionZ + dz_total)).color(endColor[0], endColor[1], endColor[2], endColor[3]).normal(poseStack.last().normal(), fx1, fy1, fz1).endVertex();
                                    break;

                            }
                            poseStack.popPose();

                        }
                    }
                }

                poseStack.pushPose();
                ClientNonBusSetUp.renderGreenOutline(poseStack, multiBufferSource, cameraEntity, cameraPos.x, cameraPos.y, cameraPos.z, startLocation);
                poseStack.popPose();


            } else if (mainStack.is(shapeCard) || offStack.is(shapeCard)) {
                CompoundTag tag = (mainStack.is(shapeCard) ? mainStack : offStack).getTag();
                if (tag == null) {
                    return;
                }
                HitResult hitResult = Minecraft.getInstance().hitResult;
                if (!(hitResult instanceof BlockHitResult)) {
                    return;
                }
                BlockPos hitPos = ((BlockHitResult) Objects.requireNonNull(hitResult)).getBlockPos();
                int range = 32;

                if (!tag.contains("positionList")) {
                    tag.put("positionList", new CompoundTag());
                    return;
                }
                CompoundTag posTag = tag.getCompound("positionList");
                List<BlockPos> blockPosList = new ArrayList<>();
                Map<BlockPos, boolean[]> faceVisibilityMap = new HashMap<>();

                int tagLen = posTag.size();
                if (tagLen <= 0) {
                    return;
                }

                Set<BlockPos> allPositions = new HashSet<>();
                for (int i = 0; i < tagLen; i++) {
                    BlockPos pos = NbtUtils.readBlockPos(posTag.getCompound("location_" + i));
                    if (pos.equals(Utils.errorPos())) {
                        continue;
                    }

                    int dx = Math.abs(pos.getX() - hitPos.getX());
                    int dy = Math.abs(pos.getY() - hitPos.getY());
                    int dz = Math.abs(pos.getZ() - hitPos.getZ());

                    if (dx <= range && dy <= range && dz <= range) {
                        allPositions.add(pos);
                    }
                }

                for (BlockPos pos : allPositions) {
                    boolean[] faceVisibility = new boolean[6];
                    BlockPos[] adjacentPositions = new BlockPos[]{pos.north(), pos.south(), pos.above(), pos.below(), pos.east(), pos.west()};

                    for (int i = 0; i < 6; i++) {
                        BlockPos adjPos = adjacentPositions[i];
                        faceVisibility[i] = !allPositions.contains(adjPos); // && !cameraEntity.level().getBlockState(adjPos).canOcclude();
                    }

                    boolean hasVisibleFace = false;
                    for (boolean visible : faceVisibility) {
                        if (visible) {
                            hasVisibleFace = true;
                            break;
                        }
                    }

                    if (hasVisibleFace) {
                        blockPosList.add(pos);
                        faceVisibilityMap.put(pos, faceVisibility);
                    }
                }

                if (blockPosList.isEmpty()) {
                    return;
                }

                poseStack.pushPose();
                for (BlockPos pos : blockPosList) {
                    renderYellowOutline(poseStack, multiBufferSource, cameraEntity, cameraPos.x, cameraPos.y, cameraPos.z, pos, faceVisibilityMap.get(pos));
                }
                poseStack.popPose();

            }
        }
    }

    @SubscribeEvent
    public static void RenderGUIEvent(RenderGuiOverlayEvent event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().options.hideGui) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Window window = event.getWindow();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        int centerWidth = width / 2;
        int centerHeight = height / 2;
        Font font = Minecraft.getInstance().font;
        PoseStack poseStack = guiGraphics.pose();
        MultiBufferSource multiBufferSource = event.getGuiGraphics().bufferSource();
        if (stack.is(Register.vector_card.get()) && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                return;
            }
            BlockPos startLocation = NbtUtils.readBlockPos(tag.getCompound("originPosition"));
            ClientNonBusSetUp.renderGreenOutline(poseStack, multiBufferSource, player, player.position().x, player.position().y, player.position().z, startLocation);
            if ((!VectorCardItem.isSelectionFinished(stack) && !VectorCardItem.isDuringSelection(stack))) {
                guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.vector_card_select_origin_pos").getString(), centerWidth, height - 75, 7208704);
            } else if (VectorCardItem.isDuringSelection(stack)) {
                guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.vector_card_select_end_pos").getString(), centerWidth, height - 75, 7208704);
            } else if (VectorCardItem.isSelectionFinished(stack)) {
                guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.vector_card_select_finish").getString(), centerWidth, height - 75, 7208704);
            }
        } else if (stack.is(Register.shape_card.get())) {
            guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.shape_card_text").getString(), centerWidth, height - 88, 16776960);
            guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.shape_card_text2").getString(), centerWidth, height - 78, 16776960);
            guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.reset_setting_text").getString(), centerWidth, height - 68, 16776960);
        } else if (stack.is(Register.basket_maker_blockitem.get())) {
            guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.basket_maker_text").getString(), centerWidth, height - 85, 58584);
            guiGraphics.drawCenteredString(font, Component.translatable("info.ugoblock.basket_maker_text2").getString(), centerWidth, height - 75, 58584);
        } else if (stack.is(Register.slide_controller_blockitem.get())) {
            Utils.displayImage(guiGraphics, "slide_controller_display", centerWidth - 200, centerHeight - 90, 0.8F);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.shape_card_display_text"), centerWidth - 130, centerHeight - 55, 0.8F, 16766976);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.vector_card_display_text"), centerWidth - 135, centerHeight + 30, 0.8F, 4123655);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obserber_display_text1"), centerWidth - 160, centerHeight + 78, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obserber_display_text2"), centerWidth - 160, centerHeight + 90, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.seat_display_text"), centerWidth - 160, centerHeight + 119, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.oneway_display_text"), centerWidth - 192, centerHeight + 145, 0.8F, 16766976);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text1"), centerWidth - 80, centerHeight - 78, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text2"), centerWidth - 80, centerHeight - 68, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text1"), centerWidth - 80, centerHeight + 3, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text2"), centerWidth - 80, centerHeight + 13, 0.8F, 16777215);
        } else if (stack.is(Register.rotation_controller_blockitem.get())) {
            Utils.displayImage(guiGraphics, "rotation_controller_display", centerWidth - 200, centerHeight - 90, 0.8F);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.shape_card_display_text"), centerWidth - 130, centerHeight - 55, 0.8F, 16766976);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.rotation_direction_display_text1"), centerWidth - 130, centerHeight - 35, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.rotation_direction_display_text2"), centerWidth - 130, centerHeight - 25, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.basket_maker_display_text"), centerWidth - 197, centerHeight + 70, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obserber_display_text1"), centerWidth - 160, centerHeight + 93, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obserber_display_text2"), centerWidth - 160, centerHeight + 103, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.seat_display_text"), centerWidth - 160, centerHeight + 134, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text1"), centerWidth - 80, centerHeight - 78, 0.8F, 16777215);
            Utils.displayString(guiGraphics, Component.translatable("info.ugoblock.obtain_card_display_text2"), centerWidth - 80, centerHeight - 68, 0.8F, 16777215);
        }


    }
}