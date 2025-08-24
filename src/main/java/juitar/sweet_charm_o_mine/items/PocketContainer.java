package juitar.sweet_charm_o_mine.items;

import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PocketContainer extends AbstractContainerMenu {
    private final PocketInventory pocketInventory;
    private final ItemStack pocketStack;

    public PocketContainer(int windowId, Inventory playerInventory, ItemStack pocketStack) {
        super(SweetCharmContainers.POCKET_CONTAINER.get(), windowId);
        this.pocketStack = pocketStack;
        
        PocketType type = ((PocketItem) pocketStack.getItem()).getPocketType();
        int expectedSize = type.getRows() * type.getColumns();
        this.pocketInventory = new PocketInventory(pocketStack, expectedSize);

        // 添加子弹口袋槽位 - 调整Y坐标偏移来匹配纹理
        int rows = type.getRows();
        int cols = type.getColumns();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                this.addSlot(new Slot(pocketInventory, row * cols + col, 
                    8 + col * 18, 20 + row * 18) { // 从18改为20，向下偏移2像素
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return GunItem.BULLETS.test(stack) && !(stack.getItem() instanceof PocketItem);
                    }
                });
            }
        }

        // 添加玩家背包槽位
        int yOffset = 18 + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 
                    8 + col * 18, yOffset + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        // 防止子弹口袋被放入背包槽位（当GUI打开时）
                        return !(stack.getItem() instanceof PocketItem && ItemStack.isSameItemSameTags(stack, pocketStack));
                    }
                });
            }
        }

        // 添加玩家快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, yOffset + 58) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // 防止子弹口袋被放入快捷栏槽位（当GUI打开时）
                    return !(stack.getItem() instanceof PocketItem && ItemStack.isSameItemSameTags(stack, pocketStack));
                }
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            int pocketSlots = pocketInventory.getContainerSize();
            
            // 防止子弹袋被移动
            if (itemstack1.getItem() instanceof PocketItem) {
                return ItemStack.EMPTY;
            }
            
            if (index < pocketSlots) {
                // 从口袋移动到背包
                if (!this.moveItemStackTo(itemstack1, pocketSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从背包移动到口袋
                if (!this.moveItemStackTo(itemstack1, 0, pocketSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // 关键：检查子弹口袋是否仍然存在且有效
        return !pocketStack.isEmpty();
    }

    public PocketType getPocketType() {
        if (pocketStack.getItem() instanceof PocketItem pocketItem) {
            return pocketItem.getPocketType();
        }
        return PocketType.COPPER;
    }

    public ItemStack getPocketStack() {
        return pocketStack;
    }

    // 内部类：基于GunsWithoutRoses的BulletBagContainer实现
    private static class PocketInventory extends SimpleContainer {
        private static final String TAG = "Inventory";
        private final ItemStack stack;

        public PocketInventory(ItemStack stack, int expectedSize) {
            super(expectedSize);
            this.stack = stack;

            // 从NBT加载物品
            ListTag lst = (!stack.isEmpty() && stack.hasTag() && stack.getOrCreateTag().contains(TAG)) 
                ? stack.getOrCreateTag().getList(TAG, Tag.TAG_COMPOUND) 
                : new ListTag();
            
            for (int i = 0; i < expectedSize && i < lst.size(); i++) {
                setItem(i, ItemStack.of(lst.getCompound(i)));
            }
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return GunItem.BULLETS.test(stack) && !(stack.getItem() instanceof PocketItem);
        }

        @Override
        public boolean stillValid(Player player) {
            // 关键：检查子弹口袋ItemStack是否仍然有效
            return !stack.isEmpty();
        }

        @Override
        public void setChanged() {
            super.setChanged();
            // 保存到NBT
            ListTag list = new ListTag();
            for (int i = 0; i < getContainerSize(); i++) {
                list.add(getItem(i).save(new CompoundTag()));
            }
            stack.getOrCreateTag().put(TAG, list);
        }
    }
}
