package com.iwaliner.ugoblock.object.block_imitation_wand;

import com.iwaliner.ugoblock.Utils;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockImitationWandItem extends Item {
    public BlockImitationWandItem(Properties p_41383_) {
        super(p_41383_);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level=context.getLevel();
        BlockPos pos=context.getClickedPos();
        BlockState state=level.getBlockState(pos);
        ItemStack stack=context.getItemInHand();
        BlockEntity blockEntity =level.getBlockEntity(pos);
        Player player=context.getPlayer();

        if(stack.is(Register.block_imitation_wand.get())){
            if(!isMachine(state)){
                setState(state,stack);
                level.playSound(context.getPlayer(),pos, SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                return InteractionResult.SUCCESS;
            }else if(!isNotSavedState(level,stack)&&isMachine(state)&&blockEntity instanceof ImitatableBlockEntity imitatableBlockEntity){
                imitatableBlockEntity.setImitatingState(getState(level,stack));
                level.playSound(context.getPlayer(),pos, SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                if(context.getPlayer()!=null) {
                    stack.hurtAndBreak(1, context.getPlayer(), e -> e.broadcastBreakEvent(context.getHand()));
                }else{
                    stack.setDamageValue(stack.getDamageValue()-1);
                }
                ((ImitatableBlockEntity) blockEntity).markUpdated();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private boolean isMachine(BlockState state){
       return state.is(Register.slide_controller_block.get())||state.is(Register.rotation_controller_block.get())||state.is(Register.wireless_redstone_transmitter_block.get())||state.is(Register.wireless_redstone_receiver_block.get())/*||state.is(Register.basket_maker_block.get())*/;
    }
    private boolean isNotSavedState(Level level,ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("imitatingState")) {
            return NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompound("imitatingState")).isAir();
        }else{
            return true;
        }
    }
    private void setState(BlockState state, ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("imitatingState", NbtUtils.writeBlockState(state));
        stack.setTag(tag);
    }
    public BlockState getState(Level level, ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("imitatingState")) {
            return NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompound("imitatingState"));
        }
        return Blocks.AIR.defaultBlockState();
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("info.ugoblock.block_imitation_wand").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.block_imitation_wand2").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.block_imitation_wand3").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("info.ugoblock.block_imitation_wand4").withStyle(ChatFormatting.GREEN));

    }
}
