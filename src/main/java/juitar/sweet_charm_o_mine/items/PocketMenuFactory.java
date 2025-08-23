package juitar.sweet_charm_o_mine.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PocketMenuFactory {
    
    public static AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, ItemStack pocketStack) {
        return new PocketContainer(windowId, playerInventory, pocketStack);
    }
    
    public static AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        // 查找玩家手中的子弹口袋
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        if (mainHand.getItem() instanceof PocketItem) {
            return new PocketContainer(windowId, playerInventory, mainHand);
        } else if (offHand.getItem() instanceof PocketItem) {
            return new PocketContainer(windowId, playerInventory, offHand);
        }
        
        // 如果手中没有，查找背包中的子弹口袋
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.getItem() instanceof PocketItem) {
                return new PocketContainer(windowId, playerInventory, stack);
            }
        }
        
        // 如果都没找到，返回null（这种情况不应该发生）
        return null;
    }
}