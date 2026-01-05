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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VectorCardItem extends Item {
    public VectorCardItem(Properties p_41383_) {
        super(p_41383_);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level=context.getLevel();
        BlockPos pos=context.getClickedPos();
       ItemStack stack=context.getItemInHand();
        if(stack.getItem() instanceof VectorCardItem) {
            CompoundTag tag = stack.getOrCreateTag();
            /**このタグは、tureのときfalseにした上で移動量選択終了処理を行い、falseのときはtrueにしたうえで移動量選択開始処理を行う*/
            if(!tag.contains("select")){
                tag.putBoolean("select",false);
            }
            tag.putBoolean("select",!tag.getBoolean("select"));
            if (tag.getBoolean("select")) {
                if(tag.contains("endPosition")){
                    tag.remove("endPosition");
                }
                /**起点を登録*/
                tag.put("originPosition", NbtUtils.writeBlockPos(pos));
                level.playSound(context.getPlayer(),pos, SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundSource.BLOCKS,1F,1F);
                stack.setTag(tag);
            } else {
                /**終点を登録*/
                tag.put("endPosition", NbtUtils.writeBlockPos(pos));
                level.playSound(context.getPlayer(),pos, SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundSource.BLOCKS,1F,1F);
                stack.setTag(tag);
            }
            Player player=context.getPlayer();
          if(player!=null) {
              if (stack.getCount() > 1) {
                  int size = stack.getCount();

                  ItemStack newStack = new ItemStack(Register.vector_card.get(), size - 1);
                  stack = stack.copyWithCount(1);
                  if (!player.getInventory().add(newStack)) {
                      player.drop(newStack, false);
                  }
              }
              player.setItemInHand(context.getHand(), stack);
          }
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
    public static boolean isDuringSelection(ItemStack stack){
        CompoundTag tag=stack.getTag();
        return tag!=null&& tag.contains("select")&&tag.getBoolean("select");
    }
    public static boolean isSelectionFinished(ItemStack stack){
        CompoundTag tag=stack.getTag();
        return tag!=null&& tag.contains("endPosition")&&tag.contains("originPosition")&& !NbtUtils.readBlockPos(tag.getCompound("endPosition")).equals(Utils.errorPos()) &&!NbtUtils.readBlockPos(tag.getCompound("originPosition")).equals(Utils.errorPos());
    }
    public boolean isFoil(@NotNull ItemStack stack) {
        return isDuringSelection(stack);
    }
    public static BlockPos getTransition(ItemStack stack){
        CompoundTag tag=stack.getTag();
        if(tag!=null&&tag.contains("originPosition")&& tag.contains("endPosition")) {
            BlockPos originPos = NbtUtils.readBlockPos(tag.getCompound("originPosition"));
            BlockPos endPos = NbtUtils.readBlockPos(tag.getCompound("endPosition"));
            return endPos.offset(-originPos.getX(), -originPos.getY(), -originPos.getZ());
        }
        return Utils.errorPos();
    }
    public static void invertTransition(ItemStack stack){
        CompoundTag tag=stack.getTag();
        if(tag!=null&&tag.contains("originPosition")&& tag.contains("endPosition")) {
            BlockPos originPos = NbtUtils.readBlockPos(tag.getCompound("originPosition"));
            BlockPos endPos = NbtUtils.readBlockPos(tag.getCompound("endPosition"));
            tag.put("originPosition", NbtUtils.writeBlockPos(endPos));
            tag.put("endPosition", NbtUtils.writeBlockPos(originPos));
            stack.setTag(tag);
        }
    }
    public static void offsetTransition(ItemStack stack,BlockPos transition){
        CompoundTag tag=stack.getTag();
        if(tag!=null&&tag.contains("originPosition")&& tag.contains("endPosition")) {
            BlockPos originPos = NbtUtils.readBlockPos(tag.getCompound("originPosition"));
            BlockPos endPos = NbtUtils.readBlockPos(tag.getCompound("endPosition"));
            tag.put("originPosition", NbtUtils.writeBlockPos(originPos.offset(transition)));
            tag.put("endPosition", NbtUtils.writeBlockPos(endPos.offset(transition)));
            stack.setTag(tag);
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("info.ugoblock.vector_card").withStyle(ChatFormatting.GREEN));
        if(stack.getTag()!=null) {
            CompoundTag tag=stack.getTag();
            if(tag.contains("originPosition")){
                BlockPos pos=NbtUtils.readBlockPos(tag.getCompound("originPosition"));
                list.add(Component.translatable("info.ugoblock.vector_card_origin_pos").append("[").append(String.valueOf(pos.getX())).append(", ").append(String.valueOf(pos.getY())).append(", ").append(String.valueOf(pos.getZ())).append("]").withStyle(ChatFormatting.GRAY));
            }
            if(tag.contains("endPosition")){
                BlockPos pos=NbtUtils.readBlockPos(tag.getCompound("endPosition"));
                list.add(Component.translatable("info.ugoblock.vector_card_end_pos").append("[").append(String.valueOf(pos.getX())).append(", ").append(String.valueOf(pos.getY())).append(", ").append(String.valueOf(pos.getZ())).append("]").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
