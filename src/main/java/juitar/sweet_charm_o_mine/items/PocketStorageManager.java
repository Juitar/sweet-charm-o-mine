package juitar.sweet_charm_o_mine.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Optional;

/**
 * PocketStorageManager - 口袋存储管理器
 * 
 * 主要功能：
 * 1. 全局口袋库存管理 - 管理所有玩家的口袋数据
 * 2. 玩家交互功能 - 打开口袋GUI、切换槽位等
 * 3. 数据同步 - 客户端服务端数据同步
 * 4. 便捷操作 - 提供各种口袋操作的静态方法
 */
public class PocketStorageManager {

    private static final HashMap<Integer, PocketInventory> inventories = new HashMap<>();
    private static int inventoryIndex = 0;

    /**
     * 获取当前选中槽位的物品堆叠
     */
    public static ItemStack getCurrentSlotStack(ItemStack pocketStack) {
        PocketInventory pocketInventory = PocketItem.getInventory(pocketStack);
        // 获取当前选中的槽位索引，默认为0
        int currentSlot = pocketStack.getOrCreateTag().getInt("currentSlot");
        if (currentSlot >= pocketInventory.getSlots()) {
            currentSlot = 0;
            pocketStack.getOrCreateTag().putInt("currentSlot", currentSlot);
        }
        return pocketInventory.getStackInSlot(currentSlot);
    }

    /**
     * 增加口袋槽位索引
     */
    public static boolean increasePocketSlot(Player player) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> optional = CuriosApi.getCuriosHelper()
                .findEquippedCurio(item -> item.getItem() instanceof PocketItem, player);
        
        optional.ifPresent(triple -> {
            ItemStack pocketStack = triple.getRight();
            PocketInventory inventory = PocketItem.getInventory(pocketStack);
            int currentSlot = pocketStack.getOrCreateTag().getInt("currentSlot");
            int newSlot = (currentSlot + 1) % inventory.getSlots();
            pocketStack.getOrCreateTag().putInt("currentSlot", newSlot);
        });
        
        return optional.isPresent();
    }

    /**
     * 减少口袋槽位索引
     */
    public static boolean decreasePocketSlot(Player player) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> optional = CuriosApi.getCuriosHelper()
                .findEquippedCurio(item -> item.getItem() instanceof PocketItem, player);
        
        optional.ifPresent(triple -> {
            ItemStack pocketStack = triple.getRight();
            PocketInventory inventory = PocketItem.getInventory(pocketStack);
            int currentSlot = pocketStack.getOrCreateTag().getInt("currentSlot");
            int newSlot = currentSlot - 1;
            if (newSlot < 0) {
                newSlot = inventory.getSlots() - 1;
            }
            pocketStack.getOrCreateTag().putInt("currentSlot", newSlot);
        });
        
        return optional.isPresent();
    }

    /**
     * 切换到下一个槽位
     */
    public static void nextSlot(ItemStack pocketStack, int direction) {
        PocketInventory inventory = PocketItem.getInventory(pocketStack);
        int pocketSize = inventory.getSlots();
        int currentSlot = pocketStack.getOrCreateTag().getInt("currentSlot");
        int newSlot = currentSlot + direction;
        
        if (newSlot >= pocketSize) {
            newSlot = 0;
        }
        if (newSlot < 0) {
            newSlot = pocketSize - 1;
        }
        
        pocketStack.getOrCreateTag().putInt("currentSlot", newSlot);
    }

    /**
     * 打开装备的口袋GUI
     */
    public static boolean openPocket(Player playerIn) {
        Level worldIn = playerIn.level();

        // 查找装备的口袋物品
        ItemStack pocketStack = CuriosApi.getCuriosHelper()
                .findEquippedCurio(item -> item.getItem() instanceof PocketItem, playerIn)
                .map(triple -> triple.getRight())
                .orElse(ItemStack.EMPTY);

        if (pocketStack.isEmpty()) {
            return false;
        }

        if (!playerIn.isShiftKeyDown()) {
            // 正常打开GUI
            if (!worldIn.isClientSide() && pocketStack.getItem() instanceof PocketItem) {
                int bagSlot = 0; // 饰品槽位
                PocketInventory inventory = PocketItem.getInventory(pocketStack);
                
                NetworkHooks.openScreen((ServerPlayer) playerIn, 
                    new PocketItem.ContainerProvider(pocketStack.getDisplayName(), bagSlot, inventory), 
                    buffer -> {
                        buffer.writeInt(bagSlot);
                        buffer.writeInt(inventory.rows);
                        buffer.writeInt(inventory.columns);
                        buffer.writeNbt(inventory.serializeNBT());
                    });
                return true;
            }
        } else {
            // Shift+使用 - 快速整理或其他功能
            if (!worldIn.isClientSide()) {
                // 这里可以添加快速整理功能
                organizeEquippedPocket(playerIn);
                return true;
            }
        }
        
        return false;
    }

    /**
     * 整理装备的口袋
     */
    public static void organizeEquippedPocket(Player player) {
        CuriosApi.getCuriosHelper()
                .findEquippedCurio(item -> item.getItem() instanceof PocketItem, player)
                .ifPresent(triple -> {
                    ItemStack pocketStack = triple.getRight();
                    if (pocketStack.getItem() instanceof PocketItem pocketItem) {
                        pocketItem.organizeInventory(pocketStack);
                    }
                });
    }

    /**
     * 获取玩家装备的口袋物品
     */
    public static ItemStack getEquippedPocket(Player player) {
        return CuriosApi.getCuriosHelper()
                .findEquippedCurio(item -> item.getItem() instanceof PocketItem, player)
                .map(triple -> triple.getRight())
                .orElse(ItemStack.EMPTY);
    }

    /**
     * 检查玩家是否装备了口袋
     */
    public static boolean hasEquippedPocket(Player player) {
        return !getEquippedPocket(player).isEmpty();
    }

    /**
     * 尝试将物品存储到装备的口袋中
     */
    public static ItemStack tryStoreInEquippedPocket(Player player, ItemStack stack) {
        ItemStack pocketStack = getEquippedPocket(player);
        if (pocketStack.isEmpty()) {
            return stack;
        }

        PocketInventory inventory = PocketItem.getInventory(pocketStack);
        ItemStack remaining = inventory.insertItem(stack, false);
        
        // 保存库存变化
        PocketItem.saveInventory(pocketStack, inventory);
        
        return remaining;
    }

    /**
     * 从装备的口袋中提取物品
     */
    public static ItemStack extractFromEquippedPocket(Player player, int slot, int amount) {
        ItemStack pocketStack = getEquippedPocket(player);
        if (pocketStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PocketInventory inventory = PocketItem.getInventory(pocketStack);
        ItemStack extracted = inventory.extractItem(slot, amount, false);
        
        // 保存库存变化
        PocketItem.saveInventory(pocketStack, inventory);
        
        return extracted;
    }

    /**
     * 获取装备口袋的库存
     */
    public static PocketInventory getEquippedPocketInventory(Player player) {
        ItemStack pocketStack = getEquippedPocket(player);
        if (pocketStack.isEmpty()) {
            return null;
        }
        return PocketItem.getInventory(pocketStack);
    }

    /**
     * 玩家登录事件处理
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide()) {
            // 这里可以添加玩家登录时的数据同步逻辑
            Player player = event.getEntity();
            
            // 检查装备的口袋并确保数据完整性
            ItemStack pocketStack = getEquippedPocket(player);
            if (!pocketStack.isEmpty()) {
                // 确保口袋数据正确初始化
                PocketInventory inventory = PocketItem.getInventory(pocketStack);
                PocketItem.saveInventory(pocketStack, inventory);
            }
        }
    }

    /**
     * 玩家退出事件处理
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide()) {
            // 这里可以添加玩家退出时的数据保存逻辑
            Player player = event.getEntity();
            
            // 保存装备口袋的数据
            ItemStack pocketStack = getEquippedPocket(player);
            if (!pocketStack.isEmpty()) {
                PocketInventory inventory = PocketItem.getInventory(pocketStack);
                PocketItem.saveInventory(pocketStack, inventory);
            }
        }
    }

    /**
     * 清理缓存数据
     */
    public static void clearCache() {
        inventories.clear();
    }

    /**
     * 获取缓存的库存数量
     */
    public static int getCachedInventoryCount() {
        return inventories.size();
    }
}