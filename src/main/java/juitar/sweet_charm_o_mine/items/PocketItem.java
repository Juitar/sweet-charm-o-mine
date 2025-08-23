package juitar.sweet_charm_o_mine.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * PocketItem - 基于QuiverItem的口袋物品系统
 * 
 * 主要功能：
 * 1. Curios饰品槽位集成 - 可装备在特定槽位
 * 2. 动态库存管理 - 支持不同类型的口袋大小
 * 3. GUI界面交互 - 右键打开库存界面
 * 4. 物品快速存储 - 右键点击快速存入物品
 * 5. 工具提示预览 - Shift显示库存内容
 * 6. NBT数据同步 - 客户端服务端数据同步
 */
public class PocketItem extends Item implements ICurioItem {

    public PocketType type;
    private PocketInventory inventory;

    public PocketItem(PocketType type) {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(type.getDefaultColumns() * type.getDefaultRows()));
        this.type = type;
    }

    /**
     * 获取物品的库存实例
     */
    public static PocketInventory getInventory(ItemStack itemStack) {
        // 从NBT获取或创建新的库存
        CompoundTag tag = itemStack.getOrCreateTag();
        PocketItem pocketItem = (PocketItem) itemStack.getItem();
        
        if (!tag.contains("inventory")) {
            // 创建新库存
            PocketInventory newInventory = new PocketInventory(false, 
                pocketItem.type.getDefaultRows(), 
                pocketItem.type.getDefaultColumns());
            tag.put("inventory", newInventory.serializeNBT());
            return newInventory;
        } else {
            // 从NBT加载库存
            PocketInventory inventory = new PocketInventory(false, 
                pocketItem.type.getDefaultRows(), 
                pocketItem.type.getDefaultColumns());
            inventory.deserializeNBT(tag.getCompound("inventory"));
            return inventory;
        }
    }

    /**
     * 保存库存到NBT
     */
    public static void saveInventory(ItemStack itemStack, PocketInventory inventory) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put("inventory", inventory.serializeNBT());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        
        PocketInventory inventory = getInventory(stack);
        int usedSlots = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                usedSlots++;
            }
        }
        
        tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.capacity", 
            usedSlots, inventory.getSlots()).withStyle(ChatFormatting.GRAY));
        
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.contents").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.shift", 
                Component.translatable("tooltip.sweet_charm_o_mine.shift").withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        
        if (!playerIn.isShiftKeyDown()) {
            // 正常右键 - 打开GUI
            if (!worldIn.isClientSide() && stack.getItem() instanceof PocketItem) {
                int bagSlot = handIn == InteractionHand.MAIN_HAND ? playerIn.getInventory().selected : -1;
                PocketInventory inventory = getInventory(stack);
                
                NetworkHooks.openScreen((ServerPlayer) playerIn, 
                    new ContainerProvider(stack.getDisplayName(), bagSlot, inventory), 
                    buffer -> {
                        buffer.writeInt(bagSlot);
                        buffer.writeInt(inventory.rows);
                        buffer.writeInt(inventory.columns);
                        buffer.writeNbt(inventory.serializeNBT());
                    });
            }
        } else {
            // Shift+右键 - 可以添加特殊功能，比如快速整理
            if (!worldIn.isClientSide()) {
                // 这里可以添加快速整理功能
                organizeInventory(stack);
                playerIn.displayClientMessage(
                    Component.translatable("message.sweet_charm_o_mine.pocket.organized"), 
                    true);
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide);
    }

    /**
     * 整理库存 - 将相同物品堆叠在一起
     */
    public void organizeInventory(ItemStack pocketStack) {
        PocketInventory inventory = getInventory(pocketStack);
        
        // 简单的整理逻辑：将相同物品尽可能堆叠
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack current = inventory.getStackInSlot(i);
            if (current.isEmpty()) continue;
            
            for (int j = i + 1; j < inventory.getSlots(); j++) {
                ItemStack other = inventory.getStackInSlot(j);
                if (other.isEmpty()) continue;
                
                if (current.getItem() == other.getItem() && 
                    ItemStack.isSameItemSameTags(current, other)) {
                    
                    int transferAmount = Math.min(other.getCount(), 
                        current.getMaxStackSize() - current.getCount());
                    
                    if (transferAmount > 0) {
                        current.grow(transferAmount);
                        other.shrink(transferAmount);
                        
                        if (other.isEmpty()) {
                            inventory.setStackInSlot(j, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        
        saveInventory(pocketStack, inventory);
    }

    @Override
    public boolean isFireResistant() {
        return type.getFireProof();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            PocketInventory inventory = getInventory(itemStack);
            if (inventory != null) {
                // 这里需要创建一个自定义的TooltipComponent来显示库存内容
                // 暂时返回空，你可以后续实现PocketTooltip类
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains("inventory")) {
            // 客户端接收服务端数据
            CompoundTag tag = stack.getOrCreateTag();
            tag.put("inventory", nbt.getCompound("inventory"));
        }
        super.readShareTag(stack, nbt);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) {
            tag = new CompoundTag();
        }
        
        // 同步库存数据到客户端
        CompoundTag stackTag = stack.getOrCreateTag();
        if (stackTag.contains("inventory")) {
            tag.put("inventory", stackTag.getCompound("inventory"));
        }
        
        return tag;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return type.getDefaultColumns() * type.getDefaultRows();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pocketStack, ItemStack inputItem, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY && !inputItem.isEmpty()) {
            // 右键点击将物品存入口袋
            PocketInventory inventory = getInventory(pocketStack);
            
            // 尝试插入物品
            ItemStack remaining = inventory.insertItem(inputItem, false);
            inputItem.setCount(remaining.getCount());
            
            // 保存库存变化
            saveInventory(pocketStack, inventory);
            
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false; // 口袋不会因为耐久度耗尽而损坏
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        ICurioItem.super.curioTick(slotContext, stack);
        
        // 这里可以添加装备时的特殊效果
        // 比如自动收集掉落物品等功能
    }

    /**
     * 容器提供者 - 用于打开GUI界面
     */
    public static class ContainerProvider implements MenuProvider {
        private final Component displayName;
        private final int bagSlot;
        private final PocketInventory inventory;

        public ContainerProvider(Component displayName, int bagSlot, PocketInventory inventory) {
            this.displayName = displayName;
            this.bagSlot = bagSlot;
            this.inventory = inventory;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return this.displayName;
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
            // 这里需要创建PocketContainer类来处理GUI逻辑
            // 暂时返回null，你需要实现PocketContainer
            return null; // new PocketContainer(id, playerInv, this.bagSlot, this.inventory, this.inventory.rows, this.inventory.columns);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // 定期保存库存数据，确保数据同步
        if (entity instanceof Player player && !level.isClientSide()) {
            // 每20tick（1秒）检查一次数据同步
            if (level.getGameTime() % 20 == 0) {
                PocketInventory inventory = getInventory(stack);
                saveInventory(stack, inventory);
            }
        }
    }

    /**
     * 检查物品是否可以存储在口袋中
     */
    public boolean canStoreItem(ItemStack stack) {
        // 这里可以添加物品过滤逻辑
        // 比如某些口袋只能存储特定类型的物品
        return !stack.isEmpty() && stack.getItem() != this;
    }
}