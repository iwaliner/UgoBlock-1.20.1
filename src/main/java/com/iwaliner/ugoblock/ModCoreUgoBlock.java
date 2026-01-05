package com.iwaliner.ugoblock;

import com.iwaliner.ugoblock.network.WirelessRedstoneData;
import com.iwaliner.ugoblock.network.WirelessRedstoneProvider;
import com.iwaliner.ugoblock.object.basket_maker.BasketMakerBlock;
import com.iwaliner.ugoblock.object.block_imitation_wand.BlockImitationWandDecoration;
import com.iwaliner.ugoblock.object.controller.AbstractControllerBlockEntity;
import com.iwaliner.ugoblock.object.controller.RotationControllerBlock;
import com.iwaliner.ugoblock.object.controller.SlideControllerBlock;
import com.iwaliner.ugoblock.object.wireless_redstone_transmitter.PortableAlternateWirelessRedstoneTransmitterItem;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


@Mod(ModCoreUgoBlock.MODID)
public class ModCoreUgoBlock {
     public static final String MODID = "ugoblock";
    public static Logger logger = LogManager.getLogger("ugoblock");
    public ModCoreUgoBlock() {
        IEventBus  modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Register.register(modEventBus);
       MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::CreativeTabEvent);
        modEventBus.addListener(this::ItemDecorationRegisterEvent);
    }
    @SubscribeEvent
    public void CreativeTabEvent(BuildCreativeModeTabContentsEvent event){
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(Register.seat.get());
            event.accept(Register.slide_controller_blockitem.get());
            event.accept(Register.rotation_controller_blockitem.get());
            event.accept(Register.basket_maker_blockitem.get());
            event.accept(Register.wireless_redstone_transmitter_blockitem.get());
            event.accept(Register.wireless_redstone_receiver_blockitem.get());
            event.accept(Register.portable_alternate_wireless_redstone_transmitter.get());
            event.accept(Register.portable_momentary_wireless_redstone_transmitter.get());
            event.accept(Register.block_imitation_wand.get());
           // event.accept(Register.gravitate_piston_blockitem.get());
        }else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(Register.smooth_crying_obsidian_blockitem.get());
            event.accept(Register.ender_infused_smooth_crying_obsidian_blockitem.get());
        }
    }
    @SubscribeEvent
    public void ItemDecorationRegisterEvent(RegisterItemDecorationsEvent event) {
        event.register(Register.block_imitation_wand.get(),new BlockImitationWandDecoration());
    }
    @SubscribeEvent
    public void ItemStackedOnOtherEvent(ItemStackedOnOtherEvent event){
        ItemStack transmitterStack=event.getCarriedItem();
        ItemStack dyeStack=event.getStackedOnItem();
        Player player = event.getPlayer();
        Level level = player.level();
        if(dyeStack.getItem()instanceof DyeItem&& (transmitterStack.is(Register.portable_alternate_wireless_redstone_transmitter.get()) || transmitterStack.is(Register.portable_momentary_wireless_redstone_transmitter.get()) )) {
            DyeColor dyeColor=((DyeItem) dyeStack.getItem()).getDyeColor();
            if (PortableAlternateWirelessRedstoneTransmitterItem.isColor1Null(transmitterStack)) {
                level.playSound(player,player.blockPosition(), SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                PortableAlternateWirelessRedstoneTransmitterItem.setColor1(transmitterStack,dyeColor);
                event.setCanceled(true);
            }else if (PortableAlternateWirelessRedstoneTransmitterItem.isColor2Null(transmitterStack)) {
                level.playSound(player,player.blockPosition(), SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                PortableAlternateWirelessRedstoneTransmitterItem.setColor2(transmitterStack,dyeColor);
                event.setCanceled(true);
            }else if (PortableAlternateWirelessRedstoneTransmitterItem.isColor3Null(transmitterStack)) {
                level.playSound(player,player.blockPosition(), SoundEvents.DYE_USE, SoundSource.BLOCKS,1F,1F);
                PortableAlternateWirelessRedstoneTransmitterItem.setColor3(transmitterStack,dyeColor);
                CompoundTag transmitterStackTag = transmitterStack.getOrCreateTag();
                level.getCapability(WirelessRedstoneProvider.WIRELESS_REDSTONE).ifPresent(data -> {
                    boolean alreadyExist=!data.isSignalNull(PortableAlternateWirelessRedstoneTransmitterItem.getColor1(transmitterStackTag), PortableAlternateWirelessRedstoneTransmitterItem.getColor2(transmitterStackTag), PortableAlternateWirelessRedstoneTransmitterItem.getColor3(transmitterStackTag));
                    if(alreadyExist&& level.isClientSide){
                        event.getPlayer().displayClientMessage(Utils.getComponentFrequencyAlreadyExists(PortableAlternateWirelessRedstoneTransmitterItem.getColor1(transmitterStackTag), PortableAlternateWirelessRedstoneTransmitterItem.getColor2(transmitterStackTag), PortableAlternateWirelessRedstoneTransmitterItem.getColor3(transmitterStackTag)), false);
                    }
                });
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public void CardLeftClickEvent(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockPos pos = event.getPos();
        if (!stack.is(Register.shape_card.get()) || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }

        Level level = event.getLevel();
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag posTag = tag.contains("positionList") ? tag.getCompound("positionList") : new CompoundTag();
        tag.put("positionList", posTag);

       /**このタグは、trueのときfalseにした上で範囲選択終了処理を行い、falseのときはtrueにしたうえで範囲選択開始処理を行う*/
        boolean isSelecting = !tag.getBoolean("select");
        tag.putBoolean("select", isSelecting);

        // get recent positions
        Set<BlockPos> set = new HashSet<>();
        int ii = -1;
        for (int i = 0; i < Utils.maxSize; i++) {
            String key = "location_" + i;
            if (!posTag.contains(key)) {
                ii = i;
                break;
            }
            set.add(NbtUtils.readBlockPos(posTag.getCompound(key)));
        }

        if (ii != -1) {
            if (isSelecting) {
                /**範囲選択の始点を登録*/
                tag.put("edge_A", NbtUtils.writeBlockPos(pos));
            } else {
                /**範囲選択の終点は今クリックした地点なので、始点も呼び出すことで範囲が確定*/
                BlockPos edgeA = NbtUtils.readBlockPos(tag.getCompound("edge_A"));
                Set<BlockPos> newSet = getBlockRemovedSet(set, edgeA, pos);

                CompoundTag newTag = new CompoundTag();
                int index = 0;
                for (BlockPos blockPos : newSet) {
                    newTag.put("location_" + index++, NbtUtils.writeBlockPos(blockPos));
                }
                tag.put("positionList", newTag);
            }
        }

        stack.setTag(tag);
        level.playSound(event.getEntity(), pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1F, 1F);
        
        if (event.getEntity().isCreative()) {
            event.setCanceled(true);
        }
    }

    private static @NotNull Set<BlockPos> getBlockRemovedSet(Set<BlockPos> set, BlockPos edgeA, BlockPos pos) {
        Set<BlockPos> remainingSet = new HashSet<>(set);
        Set<BlockPos> resultSet = new HashSet<>(set);

        if(edgeA.equals(pos)){
            resultSet.remove(pos);
            return resultSet;
        }
        
        int minX = Math.min(edgeA.getX(), pos.getX());
        int maxX = Math.max(edgeA.getX(), pos.getX());
        remainingSet.removeIf(blockPos -> 
            blockPos.getX() < minX || blockPos.getX() > maxX
        );

        int minY = Math.min(edgeA.getY(), pos.getY());
        int maxY = Math.max(edgeA.getY(), pos.getY());
        remainingSet.removeIf(blockPos -> 
            blockPos.getY() < minY || blockPos.getY() > maxY
        );

        int minZ = Math.min(edgeA.getZ(), pos.getZ());
        int maxZ = Math.max(edgeA.getZ(), pos.getZ());
        remainingSet.removeIf(blockPos -> 
            blockPos.getZ() < minZ || blockPos.getZ() > maxZ
        );

        resultSet.removeAll(remainingSet);
        return resultSet;
    }

    @SubscribeEvent
    public void AttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
            if(!event.getObject().getCapability(WirelessRedstoneProvider.WIRELESS_REDSTONE).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "wireless_redstone_properties"), new WirelessRedstoneProvider());
            }
    }
    @SubscribeEvent
    public void RegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WirelessRedstoneData.class);
    }
    @SubscribeEvent
    public void denyOpenGUIEvent(PlayerInteractEvent.RightClickBlock event) {
        if(event.getEntity() == null){
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state=event.getLevel().getBlockState(pos);
        Block block=state.getBlock();
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack=livingEntity.getItemInHand(event.getHand());
        if(stack.is(Register.shape_card.get())||stack.is(Register.vector_card.get())) {
            if(block instanceof BasketMakerBlock||block instanceof RotationControllerBlock||block instanceof SlideControllerBlock){
                if(event.getLevel().getBlockEntity(pos) instanceof AbstractControllerBlockEntity blockEntity){
                    if(stack.is(Register.shape_card.get())&&blockEntity.hasShapeCard()){
                        event.setUseItem(Event.Result.ALLOW);
                        event.setUseBlock(Event.Result.DENY);
                    }else if(stack.is(Register.vector_card.get())&&blockEntity.hasVectorCard()){
                        event.setUseItem(Event.Result.ALLOW);
                        event.setUseBlock(Event.Result.DENY);
                    }
                }
            }else {
                event.setUseItem(Event.Result.ALLOW);
                event.setUseBlock(Event.Result.DENY);
            }
        }
    }
}
