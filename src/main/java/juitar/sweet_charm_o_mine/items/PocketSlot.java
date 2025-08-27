package juitar.sweet_charm_o_mine.items;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import lykrast.gunswithoutroses.item.GunItem;
/**
 * 自定义槽位，防止子弹袋被放入自己内部
 */
public class PocketSlot extends SlotItemHandler {
    private final ItemStack pocketStack;

    public PocketSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, ItemStack pocketStack) {
        super(itemHandler, index, xPosition, yPosition);
        this.pocketStack = pocketStack;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 防止子弹袋被放入自己内部
        if (stack.getItem() instanceof PocketItem) {
            return false;
        }
        
        // 防止放入当前正在使用的子弹袋
        if (ItemStack.isSameItemSameTags(stack, pocketStack)) {
            return false;
        }
        
        // 只允许放入子弹物品
        if (!GunItem.BULLETS.test(stack)) {
            return false;
        }       
        return super.mayPlace(stack);
    }
}