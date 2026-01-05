package com.iwaliner.ugoblock.object.wireless_redstone_transmitter;

import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.network.WirelessRedstoneProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableAlternateWirelessRedstoneTransmitterItem extends AbstractPortableTransmitterItem {
    public PortableAlternateWirelessRedstoneTransmitterItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    protected void handleSignalSetting(Level level, ItemStack stack, CompoundTag tag) {
        boolean newPoweredState = !isPowered(stack);

        level.getCapability(WirelessRedstoneProvider.WIRELESS_REDSTONE).ifPresent(data -> {
            data.setSignal(getColor1(tag), getColor2(tag), getColor3(tag), newPoweredState);
        });

        setPowered(stack, newPoweredState);
    }

    // @debug, keep this one?
//    public void setSignal(CompoundTag tag,DyeColor color1,DyeColor color2,DyeColor color3,boolean power){
//        String name="signal["+ color1.getName()+","+color2.getName()+","+color3.getName()+"]";
//        if(tag==null){
//            tag=new CompoundTag();
//        }
//        tag.putBoolean(name,power);
//    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
       list.add(Component.translatable("info.ugoblock.portable_wireless_redstone_transmitter").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.portable_wireless_redstone_transmitter2").withStyle(ChatFormatting.GREEN));
        CompoundTag stackTag = stack.getOrCreateTag();
        if(isAllColorNotNull(stackTag)){
            list.add(Utils.getComponentFrequencyColors(getColor1(stackTag),getColor2(stackTag),getColor3(stackTag)));
        }

    }
}
