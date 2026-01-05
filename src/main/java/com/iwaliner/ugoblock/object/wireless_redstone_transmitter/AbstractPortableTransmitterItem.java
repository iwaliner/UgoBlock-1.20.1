package com.iwaliner.ugoblock.object.wireless_redstone_transmitter;

import com.iwaliner.ugoblock.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractPortableTransmitterItem extends Item {
    public AbstractPortableTransmitterItem(Properties properties){
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof AbstractPortableTransmitterItem)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!isAllColorNotNull(stack)) {
            player.displayClientMessage(Component.translatable("info.ugoblock.portable_wireless_redstone_transmitter_color_not_set").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.fail(stack);
        }

        CompoundTag tag = stack.getOrCreateTag();

        handleSignalSetting(level, stack, tag);

        level.playSound(player, player.blockPosition(), SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundSource.BLOCKS, 1F, 1F);
        return InteractionResultHolder.fail(stack);
    }
    protected abstract void handleSignalSetting(Level level, ItemStack stack, CompoundTag tag);

    private static boolean isColorNull(CompoundTag tag, String tagID){
        if(tag == null || tag.isEmpty() || tagID == null || tagID.isEmpty()){
            return true;
        }
        return !tag.contains(tagID);
    }

    public static boolean isColor1Null(ItemStack stack){
        CompoundTag tag= stack.getOrCreateTag();
        return isColor1Null(tag);
    }
    public static boolean isColor2Null(ItemStack stack){
        CompoundTag tag=stack.getOrCreateTag();
        return isColor2Null(tag);
    }
    public static boolean isColor3Null(ItemStack stack){
        CompoundTag tag=stack.getOrCreateTag();
        return isColor3Null(tag);
    }

    public static boolean isColor1Null(CompoundTag tag){
        return isColorNull(tag, "color1");
    }
    public static boolean isColor2Null(CompoundTag tag){
        return isColorNull(tag, "color2");
    }
    public static boolean isColor3Null(CompoundTag tag){
        return isColorNull(tag, "color3");
    }

    // check if none of the tag is null.
    public static boolean isAllColorNotNull(ItemStack stack){
        if(stack == null){
            throw new IllegalArgumentException();
        }
        return isAllColorNotNull(stack.getOrCreateTag());
    }
    public static boolean isAllColorNotNull(CompoundTag tag){
        // tag should not be null.
        if(tag == null){
            throw new IllegalArgumentException();
        }
        return !isColor1Null(tag) && !isColor2Null(tag) && !isColor3Null(tag);
    }

    private static DyeColor getColor(ItemStack stack, String tagID){
        if(stack == null || stack.isEmpty() || tagID == null || tagID.isEmpty()){
            return DyeColor.byId(0);
        }
        return getColor(stack.getOrCreateTag(), tagID);
    }

    private static DyeColor getColor(CompoundTag tag, String tagID){
        // tag should not be null at this point.
        if(tag == null || tag.isEmpty() || tagID == null || tagID.isEmpty()){
            return DyeColor.byId(0);
        }
        if(!tag.contains(tagID)){
            return DyeColor.byId(0);
        }
        return DyeColor.byId(tag.getByte(tagID));
    }

    public static DyeColor getColor1(ItemStack stack){
        return getColor(stack, "color1");
    }
    public static DyeColor getColor2(ItemStack stack){
        return getColor(stack, "color2");
    }
    public static DyeColor getColor3(ItemStack stack){
        return getColor(stack, "color3");
    }

    public static DyeColor getColor1(CompoundTag tag){
        return getColor(tag, "color1");
    }
    public static DyeColor getColor2(CompoundTag tag){
        return getColor(tag, "color2");
    }
    public static DyeColor getColor3(CompoundTag tag){
        return getColor(tag, "color3");
    }

    private static boolean setColor(ItemStack stack, DyeColor color, String tagID){
        if(color == null || tagID.isEmpty()){
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putByte(tagID, (byte) color.getId());
        return true;
    }

    public static void setColor1(ItemStack stack, DyeColor color){
        setColor(stack, color, "color1");
    }
    public static void setColor2(ItemStack stack, DyeColor color){
        setColor(stack, color, "color2");
    }
    public static void setColor3(ItemStack stack, DyeColor color){
        setColor(stack, color, "color3");
    }

    public static void setPowered(ItemStack stack,boolean power){
        if(stack == null){
            return;
        }
        CompoundTag tag = stack.getTag();
        setPowered(tag, power);
    }

    public static void setPowered(CompoundTag tag,boolean power){
        if(tag!=null) {
            tag.putBoolean("signal", power);
        }
    }

    public static boolean isPowered(ItemStack stack){
        return stack.getOrCreateTag().getBoolean("signal");
    }


}
