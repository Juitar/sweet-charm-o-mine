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
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_SELECTED_AMMO = "SelectedAmmo";
    private static final String TAG_RESOLVED_AMMO = "ResolvedAmmo";

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
                    (windowId, playerInventory, p) -> new PocketContainer(windowId, playerInventory, stack,
                            BulletManager.getManageablePocketCount(player, stack) > 1),
                    stack.getHoverName()
                ), buf -> {
                    buf.writeItem(createMenuDataStack(stack));
                    buf.writeBoolean(BulletManager.getManageablePocketCount(player, stack) > 1);
                });
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

    public static ItemStack createMenuDataStack(ItemStack stack) {
        ItemStack menuStack = new ItemStack(stack.getItem());
        menuStack.setCount(1);
        return menuStack;
    }

    // 获取子弹口袋的物品列表（用于IBullet接口）
    private static java.util.List<ItemStack> getInventoryItems(ItemStack stack) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_INVENTORY)) {
            ListTag inventoryList = tag.getList(TAG_INVENTORY, 10); // 10 = CompoundTag type
            
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

    public static java.util.List<AmmoEntry> getDistinctAmmoEntries(ItemStack pocketStack) {
        java.util.List<AmmoEntry> entries = new java.util.ArrayList<>();
        for (ItemStack bulletStack : getInventoryItems(pocketStack)) {
            if (!GunItem.BULLETS.test(bulletStack)) {
                continue;
            }

            AmmoEntry existing = null;
            for (AmmoEntry entry : entries) {
                if (ItemStack.isSameItemSameTags(entry.stack(), bulletStack)) {
                    existing = entry;
                    break;
                }
            }

            if (existing == null) {
                ItemStack displayStack = bulletStack.copy();
                displayStack.setCount(1);
                entries.add(new AmmoEntry(displayStack, bulletStack.getCount()));
            } else {
                existing.addCount(bulletStack.getCount());
            }
        }
        return entries;
    }

    public static ItemStack getSelectedAmmoTemplate(ItemStack pocketStack) {
        CompoundTag tag = pocketStack.getTag();
        if (tag == null || !tag.contains(TAG_SELECTED_AMMO)) {
            return ItemStack.EMPTY;
        }
        ItemStack selected = ItemStack.of(tag.getCompound(TAG_SELECTED_AMMO));
        selected.setCount(1);
        return selected;
    }

    public static void setSelectedAmmoTemplate(ItemStack pocketStack, ItemStack ammoStack) {
        if (ammoStack.isEmpty()) {
            clearStoredAmmo(pocketStack, TAG_SELECTED_AMMO);
            clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
            return;
        }

        setStoredAmmo(pocketStack, TAG_SELECTED_AMMO, ammoStack);
        clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
    }

    public static int getAmmoCount(ItemStack pocketStack, ItemStack ammoTemplate) {
        int count = 0;
        for (ItemStack bulletStack : getInventoryItems(pocketStack)) {
            if (GunItem.BULLETS.test(bulletStack) && ItemStack.isSameItemSameTags(bulletStack, ammoTemplate)) {
                count += bulletStack.getCount();
            }
        }
        return count;
    }

    public static ItemStack cycleSelectedAmmo(ItemStack pocketStack, int direction) {
        java.util.List<AmmoEntry> entries = getDistinctAmmoEntries(pocketStack);
        if (entries.isEmpty()) {
            clearStoredAmmo(pocketStack, TAG_SELECTED_AMMO);
            clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
            return ItemStack.EMPTY;
        }

        ItemStack selected = getSelectedAmmoTemplate(pocketStack);
        int currentIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (!selected.isEmpty() && ItemStack.isSameItemSameTags(entries.get(i).stack(), selected)) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = currentIndex < 0
                ? 0
                : Math.floorMod(currentIndex + (direction < 0 ? -1 : 1), entries.size());
        ItemStack next = entries.get(nextIndex).stack().copy();
        setStoredAmmo(pocketStack, TAG_SELECTED_AMMO, next);
        clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
        return next;
    }

    public static ItemStack resolveSelectedAmmo(ItemStack pocketStack, boolean storeResolved) {
        ItemStack selected = getSelectedAmmoTemplate(pocketStack);
        ItemStack resolved = ItemStack.EMPTY;

        if (!selected.isEmpty()) {
            resolved = findMatchingAmmo(pocketStack, selected);
        }

        if (resolved.isEmpty()) {
            for (AmmoEntry entry : getDistinctAmmoEntries(pocketStack)) {
                resolved = findMatchingAmmo(pocketStack, entry.stack());
                if (!resolved.isEmpty()) {
                    setStoredAmmo(pocketStack, TAG_SELECTED_AMMO, resolved);
                    break;
                }
            }
        }

        if (storeResolved) {
            if (resolved.isEmpty()) {
                clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
            } else {
                setStoredAmmo(pocketStack, TAG_RESOLVED_AMMO, resolved);
            }
        }

        return resolved;
    }

    public static boolean prepareSpecificAmmo(ItemStack pocketStack, ItemStack ammoTemplate, boolean storeResolved) {
        ItemStack resolved = findMatchingAmmo(pocketStack, ammoTemplate);
        if (resolved.isEmpty()) {
            return false;
        }
        if (storeResolved) {
            setStoredAmmo(pocketStack, TAG_SELECTED_AMMO, resolved);
            setStoredAmmo(pocketStack, TAG_RESOLVED_AMMO, resolved);
        }
        return true;
    }

    public static boolean prepareResolvedAmmo(ItemStack pocketStack, ItemStack ammoTemplate) {
        ItemStack resolved = findMatchingAmmo(pocketStack, ammoTemplate);
        if (resolved.isEmpty()) {
            clearStoredAmmo(pocketStack, TAG_RESOLVED_AMMO);
            return false;
        }
        setStoredAmmo(pocketStack, TAG_RESOLVED_AMMO, resolved);
        return true;
    }

    private static ItemStack getResolvedAmmoTemplate(ItemStack pocketStack) {
        CompoundTag tag = pocketStack.getTag();
        if (tag == null || !tag.contains(TAG_RESOLVED_AMMO)) {
            return ItemStack.EMPTY;
        }
        ItemStack resolved = ItemStack.of(tag.getCompound(TAG_RESOLVED_AMMO));
        resolved.setCount(1);
        return resolved;
    }

    private static ItemStack findMatchingAmmo(ItemStack pocketStack, ItemStack ammoTemplate) {
        if (ammoTemplate.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack bulletStack : getInventoryItems(pocketStack)) {
            if (GunItem.BULLETS.test(bulletStack) && ItemStack.isSameItemSameTags(bulletStack, ammoTemplate)) {
                ItemStack result = bulletStack.copy();
                result.setCount(1);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void setStoredAmmo(ItemStack pocketStack, String key, ItemStack ammoStack) {
        ItemStack stored = ammoStack.copy();
        stored.setCount(1);
        pocketStack.getOrCreateTag().put(key, stored.save(new CompoundTag()));
    }

    private static void clearStoredAmmo(ItemStack pocketStack, String key) {
        CompoundTag tag = pocketStack.getTag();
        if (tag != null) {
            tag.remove(key);
        }
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
        return !resolveSelectedAmmo(stack, false).isEmpty();
    }

    /**
     * 查找匹配的弹药（用于Player.getProjectile()）
     */
    public ItemStack findAmmo(ItemStack pocketStack, java.util.function.Predicate<ItemStack> supportedProjectiles) {
        ItemStack selected = resolveSelectedAmmo(pocketStack, false);
        if (!selected.isEmpty() && supportedProjectiles.test(selected)) {
            return selected;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        ItemStack selected = resolveSelectedAmmo(stack, false);
        if (!selected.isEmpty() && selected.getItem() instanceof IBullet bullet) {
            return bullet.createProjectile(world, selected, shooter);
        }
        return null;
    }

    @Override
    public boolean hasDelegate(ItemStack stack, Player player) {
        return true; // 子弹口袋总是使用委托机制
    }

    @Override
    public ItemStack getDelegate(ItemStack stack, Player player) {
        ItemStack resolved = getResolvedAmmoTemplate(stack);
        if (!resolved.isEmpty()) {
            ItemStack matching = findMatchingAmmo(stack, resolved);
            if (!matching.isEmpty()) {
                return matching;
            }
        }
        return resolveSelectedAmmo(stack, false);
    }

    @Override
    public void consume(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        ItemStack targetAmmo = getResolvedAmmoTemplate(stack);
        if (targetAmmo.isEmpty()) {
            targetAmmo = resolveSelectedAmmo(stack, false);
        }

        if (!targetAmmo.isEmpty() && tag.contains(TAG_INVENTORY)) {
            ListTag inventoryList = tag.getList(TAG_INVENTORY, 10); // 10 = CompoundTag type

            for (int i = 0; i < inventoryList.size(); i++) {
                CompoundTag slotTag = inventoryList.getCompound(i);
                ItemStack bulletStack = ItemStack.of(slotTag);

                if (!bulletStack.isEmpty()
                        && GunItem.BULLETS.test(bulletStack)
                        && ItemStack.isSameItemSameTags(bulletStack, targetAmmo)) {
                    bulletStack.shrink(1);

                    if (bulletStack.isEmpty()) {
                        inventoryList.remove(i);
                    } else {
                        inventoryList.set(i, bulletStack.save(new CompoundTag()));
                    }
                    tag.put(TAG_INVENTORY, inventoryList);
                    break;
                }
            }
        }
        clearStoredAmmo(stack, TAG_RESOLVED_AMMO);
    }

    public static class AmmoEntry {
        private final ItemStack stack;
        private int count;

        public AmmoEntry(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }

        public ItemStack stack() {
            return stack;
        }

        public int count() {
            return count;
        }

        public void addCount(int amount) {
            this.count += amount;
        }
    }
}
