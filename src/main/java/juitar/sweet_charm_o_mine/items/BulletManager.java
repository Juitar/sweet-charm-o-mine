package juitar.sweet_charm_o_mine.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BulletManager {
    private static final String DEDICATED_POCKET_SLOT = "bulletpocket";
    
    /**
     * 检查玩家是否佩戴了子弹口袋
     */
    public static boolean hasEquippedPocket(Player player) {
        return !getEquippedPockets(player).isEmpty();
    }
    
    /**
     * 获取玩家优先使用的子弹口袋
     */
    public static Optional<ItemStack> getEquippedPocket(Player player) {
        List<ItemStack> pockets = getEquippedPockets(player);
        return pockets.isEmpty() ? Optional.empty() : Optional.of(pockets.get(0));
    }

    /**
     * 获取玩家佩戴的所有子弹口袋。专用bulletpocket槽优先，其余Curios槽作为同一弹药池继续查询。
     */
    public static List<ItemStack> getEquippedPockets(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            List<ItemStack> pockets = new ArrayList<>();
            var curios = handler.getCurios();
            addPockets(pockets, curios.get(DEDICATED_POCKET_SLOT));

            List<String> slotIds = new ArrayList<>(curios.keySet());
            slotIds.remove(DEDICATED_POCKET_SLOT);
            Collections.sort(slotIds);
            for (String slotId : slotIds) {
                addPockets(pockets, curios.get(slotId));
            }

            return pockets;
        }).orElseGet(ArrayList::new);
    }

    private static void addPockets(List<ItemStack> pockets, ICurioStacksHandler stacksHandler) {
        if (stacksHandler == null) {
            return;
        }

        for (int i = 0; i < stacksHandler.getSlots(); i++) {
            ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
            if (stack.getItem() instanceof PocketItem) {
                pockets.add(stack);
            }
        }
    }

    public static List<PocketItem.AmmoEntry> getDistinctAmmoEntries(Player player) {
        List<PocketItem.AmmoEntry> entries = new ArrayList<>();
        for (ItemStack pocket : getEquippedPockets(player)) {
            for (PocketItem.AmmoEntry pocketEntry : PocketItem.getDistinctAmmoEntries(pocket)) {
                PocketItem.AmmoEntry existing = null;
                for (PocketItem.AmmoEntry entry : entries) {
                    if (ItemStack.isSameItemSameTags(entry.stack(), pocketEntry.stack())) {
                        existing = entry;
                        break;
                    }
                }

                if (existing == null) {
                    entries.add(new PocketItem.AmmoEntry(pocketEntry.stack().copy(), pocketEntry.count()));
                } else {
                    existing.addCount(pocketEntry.count());
                }
            }
        }
        return entries;
    }

    public static ItemStack getSelectedAmmoTemplate(Player player) {
        List<ItemStack> pockets = getEquippedPockets(player);
        for (ItemStack pocket : pockets) {
            ItemStack selected = PocketItem.getSelectedAmmoTemplate(pocket);
            if (!selected.isEmpty() && getBulletCount(player, selected) > 0) {
                return selected;
            }
        }
        return ItemStack.EMPTY;
    }

    public static Optional<ItemStack> getActivePocket(Player player) {
        List<ItemStack> pockets = getEquippedPockets(player);
        ItemStack selected = getSelectedAmmoTemplate(player);
        if (!selected.isEmpty()) {
            for (ItemStack pocket : pockets) {
                if (PocketItem.getAmmoCount(pocket, selected) > 0) {
                    return Optional.of(pocket);
                }
            }
        }

        return pockets.isEmpty() ? Optional.empty() : Optional.of(pockets.get(0));
    }

    public static Optional<ItemStack> getAdjacentPocket(Player player, ItemStack currentPocket, int direction) {
        List<ItemStack> pockets = getManageablePockets(player, currentPocket);
        if (pockets.isEmpty()) {
            return Optional.empty();
        }
        if (pockets.size() == 1 || currentPocket.isEmpty()) {
            return Optional.of(pockets.get(0));
        }

        int currentIndex = -1;
        for (int i = 0; i < pockets.size(); i++) {
            if (pockets.get(i) == currentPocket) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = currentIndex < 0
                ? 0
                : Math.floorMod(currentIndex + (direction < 0 ? -1 : 1), pockets.size());
        return Optional.of(pockets.get(nextIndex));
    }

    public static int getManageablePocketCount(Player player, ItemStack currentPocket) {
        return getManageablePockets(player, currentPocket).size();
    }

    private static List<ItemStack> getManageablePockets(Player player, ItemStack currentPocket) {
        List<ItemStack> pockets = new ArrayList<>(getEquippedPockets(player));
        addManageablePocket(pockets, player.getMainHandItem());
        addManageablePocket(pockets, player.getOffhandItem());
        addManageablePocket(pockets, currentPocket);
        return pockets;
    }

    private static void addManageablePocket(List<ItemStack> pockets, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof PocketItem && !containsPocketReference(pockets, stack)) {
            pockets.add(stack);
        }
    }

    private static boolean containsPocketReference(List<ItemStack> pockets, ItemStack target) {
        for (ItemStack pocket : pockets) {
            if (pocket == target) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack cycleSelectedAmmo(Player player, int direction) {
        List<PocketItem.AmmoEntry> entries = getDistinctAmmoEntries(player);
        if (entries.isEmpty()) {
            for (ItemStack pocket : getEquippedPockets(player)) {
                PocketItem.setSelectedAmmoTemplate(pocket, ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        ItemStack selected = getSelectedAmmoTemplate(player);
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
        setSelectedAmmoTemplate(getEquippedPockets(player), next);
        return next;
    }

    private static void setSelectedAmmoTemplate(List<ItemStack> pockets, ItemStack ammoStack) {
        for (ItemStack pocket : pockets) {
            PocketItem.setSelectedAmmoTemplate(pocket, ammoStack);
        }
    }

    public static ItemStack normalizeSelectedAmmo(Player player) {
        List<ItemStack> pockets = getEquippedPockets(player);
        ItemStack selected = getSelectedAmmoTemplate(player);
        if (!selected.isEmpty()) {
            setSelectedAmmoTemplate(pockets, selected);
        }
        return selected;
    }
    
    /**
     * 获取子弹口袋中的所有物品
     */
    private static List<ItemStack> getPocketItems(ItemStack pocket) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag tag = pocket.getTag();
        if (tag != null && tag.contains("Inventory")) {
            ListTag inventoryList = tag.getList("Inventory", Tag.TAG_COMPOUND);
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
    
    /**
     * 保存子弹口袋中的物品
     */
    private static void savePocketItems(ItemStack pocket, List<ItemStack> items) {
        CompoundTag tag = pocket.getOrCreateTag();
        ListTag inventoryList = new ListTag();
        
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                item.save(slotTag);
                inventoryList.add(slotTag);
            }
        }
        
        tag.put("Inventory", inventoryList);
    }
    
    /**
     * 尝试从子弹口袋中消耗指定数量的子弹
     */
    public static boolean consumeBullets(Player player, ItemStack bulletType, int amount) {
        int remaining = amount;
        
        // 第一遍：计算可用数量
        int available = 0;
        for (ItemStack pocket : getEquippedPockets(player)) {
            for (ItemStack stack : getPocketItems(pocket)) {
                if (ItemStack.isSameItemSameTags(stack, bulletType)) {
                    available += stack.getCount();
                }
            }
        }
        
        if (available < amount) return false;
        
        // 第二遍：实际消耗
        for (ItemStack pocket : getEquippedPockets(player)) {
            List<ItemStack> updatedItems = new ArrayList<>();
            boolean changed = false;
            for (ItemStack stack : getPocketItems(pocket)) {
                if (remaining > 0 && ItemStack.isSameItemSameTags(stack, bulletType)) {
                    int toConsume = Math.min(remaining, stack.getCount());
                    stack.shrink(toConsume);
                    remaining -= toConsume;
                    changed = true;
                }

                if (!stack.isEmpty()) {
                    updatedItems.add(stack);
                }
            }

            if (changed) {
                savePocketItems(pocket, updatedItems);
            }

            if (remaining == 0) {
                break;
            }
        }
        
        return remaining == 0;
    }
    
    /**
     * 尝试向子弹口袋中添加子弹
     */
    public static int addBullets(Player player, ItemStack bulletStack) {
        ItemStack toAdd = bulletStack.copy();
        int originalCount = toAdd.getCount();
        
        // 先补已有堆叠，再使用空槽，顺序与供弹优先级一致。
        for (ItemStack pocket : getEquippedPockets(player)) {
            if (toAdd.isEmpty()) break;

            List<ItemStack> items = getPocketItems(pocket);
            boolean added = false;
            for (ItemStack existing : items) {
                if (!toAdd.isEmpty() && ItemStack.isSameItemSameTags(existing, toAdd)) {
                    int maxStack = existing.getMaxStackSize();
                    int canAdd = maxStack - existing.getCount();
                    if (canAdd > 0) {
                        int adding = Math.min(canAdd, toAdd.getCount());
                        existing.grow(adding);
                        toAdd.shrink(adding);
                        added = true;
                    }
                }
            }

            if (added) {
                savePocketItems(pocket, items);
            }
        }

        for (ItemStack pocket : getEquippedPockets(player)) {
            if (toAdd.isEmpty()) break;
            if (!(pocket.getItem() instanceof PocketItem pocketItem)) continue;

            List<ItemStack> items = getPocketItems(pocket);
            if (items.size() < pocketItem.getSlotCount()) {
                ItemStack split = toAdd.copy();
                split.setCount(Math.min(toAdd.getCount(), toAdd.getMaxStackSize()));
                items.add(split);
                toAdd.shrink(split.getCount());
                savePocketItems(pocket, items);
            }
        }
        
        return originalCount - (toAdd.isEmpty() ? 0 : toAdd.getCount());
    }
    
    /**
     * 获取子弹口袋中指定类型子弹的数量
     */
    public static int getBulletCount(Player player, ItemStack bulletType) {
        int count = 0;
        
        for (ItemStack pocket : getEquippedPockets(player)) {
            for (ItemStack stack : getPocketItems(pocket)) {
                if (ItemStack.isSameItemSameTags(stack, bulletType)) {
                    count += stack.getCount();
                }
            }
        }
        
        return count;
    }
}
