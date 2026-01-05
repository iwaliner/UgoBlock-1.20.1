package com.iwaliner.ugoblock.object.wireless_redstone_transmitter;

import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.network.WirelessRedstoneProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableMomentaryWirelessRedstoneTransmitterItem extends AbstractPortableTransmitterItem {
    public PortableMomentaryWirelessRedstoneTransmitterItem(Properties p_41383_) {
        super(p_41383_);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i0, boolean b0) {
        super.inventoryTick(stack, level, entity, i0, b0);
        CompoundTag tag=stack.getTag();
        if(stack.getItem() instanceof PortableMomentaryWirelessRedstoneTransmitterItem){
            if(tag==null){
                stack.setTag(new CompoundTag());
            }
            int coolTime= tag.contains("coolTime")? tag.getInt("coolTime") : 0;
            if(coolTime>0){
                if(coolTime==1){
                    if (isAllColorNotNull(tag)) {
                        level.getCapability(WirelessRedstoneProvider.WIRELESS_REDSTONE).ifPresent(data -> {
                            data.setSignal(getColor1(tag), getColor2(tag), getColor3(tag), false);
                        });
                        setPowered(stack, false);
                    }
                }
                tag.putInt("coolTime",coolTime-1);
                stack.setTag(tag);
            }
        }
    }
    @Override
    protected void handleSignalSetting(Level level, ItemStack stack, CompoundTag tag) {
        level.getCapability(WirelessRedstoneProvider.WIRELESS_REDSTONE).ifPresent(data -> {
            data.setSignal(getColor1(tag), getColor2(tag), getColor3(tag), true);
        });

        setPowered(stack, true);
        tag.putInt("coolTime", 4);
        stack.setTag(tag);
    }

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
