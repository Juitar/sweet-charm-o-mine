package juitar.sweet_charm_o_mine.items;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * PocketInventory - 基于QuiverInventory的口袋库存系统
 * 
 * 主要功能：
 * 1. 动态大小管理 - 支持可配置的行数和列数
 * 2. 物品验证 - 通过标签系统验证可存储的物品类型
 * 3. NBT序列化/反序列化 - 完整的数据持久化支持
 * 4. 堆叠逻辑 - 智能的物品堆叠处理
 * 5. 远程/本地模式 - 区分客户端和服务端操作
 * 6. 动态调整大小 - 运行时调整库存容量
 */
public class PocketInventory extends ItemStackHandler {

    private final boolean remote;
    public int rows;
    public int columns;
    private final NonNullList<ItemStack> stacks;

    /**
     * 构造函数
     * @param remote 是否为远程模式（客户端为true，服务端为false）
     * @param rows 行数
     * @param columns 列数
     */
    public PocketInventory(boolean remote, int rows, int columns) {
        this.remote = remote;
        this.rows = rows;
        this.columns = columns;
        stacks = NonNullList.withSize(this.rows * this.columns, ItemStack.EMPTY);
    }

    /**
     * 默认构造函数 - 创建3x3的库存
     */
    public PocketInventory() {
        this(false, 3, 3);
    }

    /**
     * 检查两个物品是否可以堆叠
     */
    private static boolean canStack(ItemStack stack1, ItemStack stack2) {
        return stack1.isEmpty() || stack2.isEmpty() || 
               (stack1.getItem() == stack2.getItem() && 
                stack1.getDamageValue() == stack2.getDamageValue() && 
                ItemStack.isSameItemSameTags(stack1, stack2));
    }

    @Override
    public int getSlots() {
        return this.rows * this.columns;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.stacks.get(slot);
    }

    /**
     * 尝试插入物品到任何可用的槽位
     */
    @Nonnull
    public ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate) {
        for (int x = 0; x < getSlots(); x++) {
            if (getStackInSlot(x).is(stack.getItem()) && getStackInSlot(x).getCount() < 64 || getStackInSlot(x).isEmpty()) {
                return insertItem(x, stack, simulate);
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack current = this.stacks.get(slot);
        if (!stack.isEmpty() && this.isItemValid(slot, stack) && canStack(current, stack)) {
            int amount = Math.min(stack.getCount(), 64 - current.getCount());
            if (!simulate) {
                ItemStack newStack = stack.copy();
                newStack.setCount(current.getCount() + amount);
                this.stacks.set(slot, newStack);
            }
            ItemStack result = stack.copy();
            result.shrink(amount);
            return result;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = this.stacks.get(slot);
        int count = Math.min(amount, stack.getCount());
        ItemStack result = stack.copy();
        if (!simulate) {
            stack.shrink(count);
            if (!this.remote && result.getItem() instanceof PocketItem && result.getOrCreateTag().contains("invIndex")) {
                int index = result.getOrCreateTag().getInt("invIndex");
                boolean contains = false;
                for (ItemStack stack1 : this.stacks) {
                    if (stack1.getItem() instanceof PocketItem && stack1.getOrCreateTag().contains("invIndex")
                            && stack1.getOrCreateTag().getInt("invIndex") == index) {
                        contains = true;
                        break;
                    }
                }
            }
        }
        result.setCount(count);
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        // 这里需要根据你的项目添加适当的标签检查
        // 例如：return stack.is(TagInit.POCKET_ITEMS);
        
        // 暂时允许所有物品，你可以根据需要修改
        return true;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        ItemStack oldStack = this.stacks.get(slot);
        this.stacks.set(slot, stack);
    }

    /**
     * 动态调整库存大小
     */
    public void adjustSize(int rows, int columns) {
        if (this.rows == rows && this.columns == columns)
            return;
        this.rows = rows;
        this.columns = columns;
        while (this.stacks.size() < this.rows * this.columns)
            this.stacks.add(ItemStack.EMPTY);
    }

    /**
     * 获取所有物品堆叠
     */
    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    /**
     * 序列化为NBT
     */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("rows", this.rows);
        compound.putInt("columns", this.columns);
        compound.putInt("stacks", this.stacks.size());
        for (int slot = 0; slot < this.stacks.size(); slot++) {
            compound.put("stack" + slot, this.stacks.get(slot).save(new CompoundTag()));
        }
        return compound;
    }

    /**
     * 从NBT反序列化
     */
    @Override
    public void deserializeNBT(CompoundTag compound) {
        if (compound == null) return;
        this.rows = compound.contains("rows") ? compound.getInt("rows") : compound.getInt("slots") / 9; // Do this for compatibility with older versions
        this.columns = compound.contains("columns") ? compound.getInt("columns") : compound.getInt("slots") / 9; // Do this for compatibility with older versions
        this.stacks.clear();
        int size = compound.contains("stacks") ? compound.getInt("stacks") : this.rows * this.columns; // Do this for compatibility with older versions

        for (int slot = 0; slot < size; slot++) {
            this.stacks.set(slot, ItemStack.of(compound.getCompound("stack" + slot)));
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
    }

    /**
     * 检查库存是否为空
     */
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清空库存
     */
    public void clear() {
        for (int i = 0; i < this.stacks.size(); i++) {
            this.stacks.set(i, ItemStack.EMPTY);
        }
    }

    /**
     * 获取第一个空槽位的索引
     */
    public int getFirstEmptySlot() {
        for (int i = 0; i < this.stacks.size(); i++) {
            if (this.stacks.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}