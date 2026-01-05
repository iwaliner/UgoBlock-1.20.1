package com.iwaliner.ugoblock.object.controller;

import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ShapeCardItem extends Item {
    public ShapeCardItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        List<BlockPos> list = new ArrayList<>();
        Player player = context.getPlayer();
        int size = stack.getCount();
        boolean flag = false;
        if (stack.getItem() instanceof ShapeCardItem && player != null) {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("positionList")) {
                tag.put("positionList", new CompoundTag());
            }
            CompoundTag posTag = tag.getCompound("positionList");
            CompoundTag backup = posTag.copy();
            /**このタグは、tureのときfalseにした上で範囲選択終了処理を行い、falseのときはtrueにしたうえで範囲選択開始処理を行う*/

            HashSet<BlockPos> existingPositions = new HashSet<>();

            // Travel through current positions and first index only once
            int firstAvailableIndex = -1;
            for (int i = 0; i < Utils.maxSize; i++) {
                String key = "location_" + i;
                if (posTag.contains(key)) {
                    BlockPos existingPos = NbtUtils.readBlockPos(posTag.getCompound(key));
                    existingPositions.add(existingPos);
                } else if (firstAvailableIndex == -1) {
                    firstAvailableIndex = i;
                }
            }

            if (firstAvailableIndex != -1) {
                if (!tag.contains("select")) {
                    tag.putBoolean("select", false);
                }
                boolean isSelecting = !tag.getBoolean("select");
                tag.putBoolean("select", isSelecting);

                if (isSelecting) {
                    tag.put("edge_A", NbtUtils.writeBlockPos(pos));
                } else {
                    BlockPos edgeA = NbtUtils.readBlockPos(tag.getCompound("edge_A"));

                    // pre-calculate range
                    int xDiff = Math.abs(edgeA.getX() - pos.getX());
                    int yDiff = Math.abs(edgeA.getY() - pos.getY());
                    int zDiff = Math.abs(edgeA.getZ() - pos.getZ());
                    int xDir = edgeA.getX() - pos.getX() >= 0 ? 1 : -1;
                    int yDir = edgeA.getY() - pos.getY() >= 0 ? 1 : -1;
                    int zDir = edgeA.getZ() - pos.getZ() >= 0 ? 1 : -1;

                    // use single index counter
                    int newPosIndex = firstAvailableIndex;

                    // add new index
                    for (int i = 0; i <= xDiff && newPosIndex < Utils.maxSize; i++) {
                        for (int j = 0; j <= yDiff && newPosIndex < Utils.maxSize; j++) {
                            for (int k = 0; k <= zDiff && newPosIndex < Utils.maxSize; k++) {
                                BlockPos newPos = pos.offset(i * xDir, j * yDir, k * zDir);

                                if (!existingPositions.contains(newPos)) {
                                    if (player.isSuppressingBounce() || !level.getBlockState(newPos).isAir()) {
                                        posTag.put("location_" + newPosIndex, NbtUtils.writeBlockPos(newPos));
                                        existingPositions.add(newPos);
                                        newPosIndex++;
                                    }
                                }
                            }
                        }
                    }
                    /**始点をリセット*/
                    tag.put("edge_A", NbtUtils.writeBlockPos(Utils.errorPos()));
                }
                flag = true;
            }

            if (posTag.size() >= Utils.maxSize) {
                if (context.getPlayer() != null && !level.isClientSide) {
                    context.getPlayer().displayClientMessage(Component.translatable("info.ugoblock.reachedPositionSelectLimit", Utils.maxSize - 1).withStyle(ChatFormatting.RED), false);
                }
                CompoundTag tag1 = new CompoundTag();
                tag1.put("edge_A", NbtUtils.writeBlockPos(Utils.errorPos()));
                tag1.put("positionList", backup);
                stack.setTag(tag1);
            } else {
                stack.setTag(tag);
            }
            if (stack.getCount() > 1) {
                ItemStack newStack = new ItemStack(Register.shape_card.get(), size - 1);
                stack = stack.copyWithCount(1);
                if (!player.getInventory().add(newStack)) {
                    player.drop(newStack, false);
                }
            }
            player.setItemInHand(context.getHand(), stack);
            if (level.getBlockEntity(pos) instanceof AbstractControllerBlockEntity blockEntity && flag) {
                player.level().playSound(player, pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1F, 1F);
            } else {
                level.playSound(context.getPlayer(), pos, SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundSource.BLOCKS, 1F, 1F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public boolean isFoil(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("select");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("info.ugoblock.shape_card").withStyle(ChatFormatting.GREEN));
        if (stack.getTag() != null) {
            int size = Utils.getPositionList(stack.getTag()).size();
            for (int i = 0; size > 5 ? i < 5 : i < size; i++) {
                BlockPos pos = NbtUtils.readBlockPos(stack.getTag().getCompound("positionList").getCompound("location_" + String.valueOf(i)));
                if (!pos.equals(Utils.errorPos())) {
                    list.add(Component.translatable("info.ugoblock.shape_card_location").append("[").append(String.valueOf(pos.getX())).append(", ").append(String.valueOf(pos.getY())).append(", ").append(String.valueOf(pos.getZ())).append("]").withStyle(ChatFormatting.GRAY));
                }
            }
            if (size > 5) {
                list.add(Component.translatable("info.ugoblock.shape_card_location_other", size).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
