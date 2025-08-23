package juitar.sweet_charm_o_mine.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BulletManager {
    
    /**
     * 检查玩家是否佩戴了子弹口袋
     */
    public static boolean hasEquippedPocket(Player player) {
        return getEquippedPocket(player).isPresent();
    }
    
    /**
     * 获取玩家佩戴的子弹口袋
     */
    public static Optional<ItemStack> getEquippedPocket(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get("bulletpocket");
            if (stacksHandler != null) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (stack.getItem() instanceof PocketItem) {
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }).filter(stack -> !stack.isEmpty());
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
        Optional<ItemStack> pocketOpt = getEquippedPocket(player);
        if (pocketOpt.isEmpty()) return false;
        
        ItemStack pocket = pocketOpt.get();
        List<ItemStack> items = getPocketItems(pocket);
        int remaining = amount;
        
        // 第一遍：计算可用数量
        int available = 0;
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameTags(stack, bulletType)) {
                available += stack.getCount();
            }
        }
        
        if (available < amount) return false;
        
        // 第二遍：实际消耗
        List<ItemStack> updatedItems = new ArrayList<>();
        for (ItemStack stack : items) {
            if (remaining > 0 && ItemStack.isSameItemSameTags(stack, bulletType)) {
                int toConsume = Math.min(remaining, stack.getCount());
                stack.shrink(toConsume);
                remaining -= toConsume;
            }
            
            if (!stack.isEmpty()) {
                updatedItems.add(stack);
            }
        }
        
        // 保存更新后的物品列表
        savePocketItems(pocket, updatedItems);
        return remaining == 0;
    }
    
    /**
     * 尝试向子弹口袋中添加子弹
     */
    public static int addBullets(Player player, ItemStack bulletStack) {
        Optional<ItemStack> pocketOpt = getEquippedPocket(player);
        if (pocketOpt.isEmpty()) return 0;
        
        ItemStack pocket = pocketOpt.get();
        if (!(pocket.getItem() instanceof PocketItem pocketItem)) return 0;
        
        List<ItemStack> items = getPocketItems(pocket);
        ItemStack toAdd = bulletStack.copy();
        int originalCount = toAdd.getCount();
        
        // 尝试添加到现有堆叠
        boolean added = false;
        for (ItemStack existing : items) {
            if (!toAdd.isEmpty() && ItemStack.isSameItemSameTags(existing, toAdd)) {
                int maxStack = Math.min(existing.getMaxStackSize(), 64);
                int canAdd = maxStack - existing.getCount();
                if (canAdd > 0) {
                    int adding = Math.min(canAdd, toAdd.getCount());
                    existing.grow(adding);
                    toAdd.shrink(adding);
                    added = true;
                }
            }
        }
        
        // 如果还有剩余，添加为新物品
        if (!toAdd.isEmpty() && items.size() < pocketItem.getSlotCount()) {
            items.add(toAdd);
            toAdd = ItemStack.EMPTY;
            added = true;
        }
        
        // 如果有任何更改，保存物品列表
        if (added) {
            savePocketItems(pocket, items);
        }
        
        return originalCount - (toAdd.isEmpty() ? 0 : toAdd.getCount());
    }
    
    /**
     * 获取子弹口袋中指定类型子弹的数量
     */
    public static int getBulletCount(Player player, ItemStack bulletType) {
        Optional<ItemStack> pocketOpt = getEquippedPocket(player);
        if (pocketOpt.isEmpty()) return 0;
        
        ItemStack pocket = pocketOpt.get();
        List<ItemStack> items = getPocketItems(pocket);
        int count = 0;
        
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameTags(stack, bulletType)) {
                count += stack.getCount();
            }
        }
        
        return count;
    }
}
