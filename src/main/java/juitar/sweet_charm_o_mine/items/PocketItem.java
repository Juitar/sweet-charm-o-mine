package juitar.sweet_charm_o_mine.items;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class PocketItem extends Item implements ICurioItem, IBullet {
    private final PocketType pocketType;

    public PocketItem(PocketType pocketType) {
        super(new Properties().stacksTo(1).fireResistant());
        this.pocketType = pocketType;
    }

    public PocketType getPocketType() {
        return pocketType;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false; // 禁用右键装备，让右键触发use方法
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        // 检查玩家是否正在使用这个特定子弹口袋的GUI
        if (slotContext.entity() instanceof Player player) {
            if (player.containerMenu instanceof PocketContainer pocketContainer) {
                // 检查是否是同一个子弹口袋实例
                ItemStack containerStack = pocketContainer.getPocketStack();
                if (containerStack == stack || ItemStack.isSameItemSameTags(containerStack, stack)) {
                    return false; // 如果正在使用这个子弹口袋的GUI，则不允许卸下
                }
            }
        }
        return true; // 默认允许卸下
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        
        if (!level.isClientSide) {
            try {
                // 使用NetworkHooks打开GUI界面，传递ItemStack数据
                NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                    (windowId, playerInventory, p) -> new PocketContainer(windowId, playerInventory, stack),
                    Component.translatable("container.sweet_charm_o_mine.pocket")
                ), buf -> buf.writeItem(stack)); // 关键：传递ItemStack数据到客户端
                
                juitar.sweet_charm_o_mine.SweetCharm.LOG.info("成功打开GUI界面");
            } catch (Exception e) {
                juitar.sweet_charm_o_mine.SweetCharm.LOG.error("打开GUI时发生错误: ", e);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        // 如果玩家正在使用这个子弹口袋的GUI，则不允许丢弃
        if (player.containerMenu instanceof PocketContainer) {
            return false;
        }
        return super.onDroppedByPlayer(item, player);
    }

    public int getSlotCount() {
        return pocketType.getRows() * pocketType.getColumns();
    }

    // 获取子弹口袋的物品列表（用于IBullet接口）
    private java.util.List<ItemStack> getInventoryItems(ItemStack stack) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Inventory")) {
            ListTag inventoryList = tag.getList("Inventory", 10); // 10 = CompoundTag type
            
            for (int i = 0; i < inventoryList.size(); i++) {
                CompoundTag slotTag = inventoryList.getCompound(i);
                ItemStack slotStack = ItemStack.of(slotTag);
                if (!slotStack.isEmpty()) {
                    items.add(slotStack);
                }
            }
        }
        return items;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.size", pocketType.getRows(), pocketType.getColumns()).withStyle(style -> style.withColor(0x999999)));
        
        if (pocketType.getFireProof()) {
            tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.fireproof").withStyle(style -> style.withColor(0xFFAA00)));
        }
        
        // 获取物品列表并计算统计信息
        java.util.List<ItemStack> items = getInventoryItems(stack);
        int usedSlots = items.size();
        int totalItems = 0;
        
        for (ItemStack slotStack : items) {
            totalItems += slotStack.getCount();
        }
        tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.pocket.ammo_count", totalItems).withStyle(style -> style.withColor(0x999999)));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return super.initCapabilities(stack, nbt);
    }

    // IBullet接口实现 - 自动供弹功能
    @Override
    public boolean hasAmmo(ItemStack stack) {
        java.util.List<ItemStack> items = getInventoryItems(stack);
        for (ItemStack bulletStack : items) {
            if (GunItem.BULLETS.test(bulletStack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找匹配的弹药（用于Player.getProjectile()）
     */
    public ItemStack findAmmo(ItemStack pocketStack, java.util.function.Predicate<ItemStack> supportedProjectiles) {
        java.util.List<ItemStack> items = getInventoryItems(pocketStack);
        for (ItemStack bulletStack : items) {
            if (supportedProjectiles.test(bulletStack)) {
                return bulletStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        // 子弹口袋本身不创建弹丸，而是委托给实际的子弹
        return null;
    }

    @Override
    public boolean hasDelegate(ItemStack stack, Player player) {
        return true; // 子弹口袋总是使用委托机制
    }

    @Override
    public ItemStack getDelegate(ItemStack stack, Player player) {
        java.util.List<ItemStack> items = getInventoryItems(stack);
        for (ItemStack bulletStack : items) {
            if (GunItem.BULLETS.test(bulletStack)) {
                return bulletStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void consume(ItemStack stack, Player player) {
        // 获取子弹口袋的NBT数据
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Inventory")) {
            ListTag inventoryList = tag.getList("Inventory", 10); // 10 = CompoundTag type
            
            // 查找第一个可用的子弹
            for (int i = 0; i < inventoryList.size(); i++) {
                CompoundTag slotTag = inventoryList.getCompound(i);
                ItemStack bulletStack = ItemStack.of(slotTag);
                
                if (!bulletStack.isEmpty() && GunItem.BULLETS.test(bulletStack)) {
                    // 减少子弹数量
                    bulletStack.shrink(1);
                    
                    // 如果子弹被完全消耗，从列表中移除
                    if (bulletStack.isEmpty()) {
                        inventoryList.remove(i);
                    } else {
                        // 更新子弹数据
                        inventoryList.set(i, bulletStack.save(new CompoundTag()));
                    }
                    
                    // 保存更新后的列表
                    tag.put("Inventory", inventoryList);
                    break;
                }
            }
        }
    }
}