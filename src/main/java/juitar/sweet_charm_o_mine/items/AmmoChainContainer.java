package juitar.sweet_charm_o_mine.items;

import juitar.sweet_charm_o_mine.items.wearable.AmmoChainItem;
import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AmmoChainContainer extends AbstractContainerMenu {
    public static final int ROWS = 6;
    public static final int LIST_ROWS = 3;
    public static final int POCKET_AMMO_SLOTS = 6;

    public static final int SELECT_LIST_BUTTON = 120;
    public static final int DELETE_LIST_BUTTON = 140;
    public static final int PAGE_PREVIOUS_BUTTON = 180;
    public static final int PAGE_NEXT_BUTTON = 181;
    public static final int POCKET_PAGE_PREVIOUS_BUTTON = 182;
    public static final int POCKET_PAGE_NEXT_BUTTON = 183;

    private static final int SELECTED_GUN_SLOT = 0;
    private static final int AMMO_SLOT_START = 1;
    private static final int LIST_SLOT_START = AMMO_SLOT_START + ROWS;
    private static final int POCKET_AMMO_SLOT_START = LIST_SLOT_START + LIST_ROWS;

    private final SimpleContainer configInventory = new SimpleContainer(1 + ROWS + LIST_ROWS + POCKET_AMMO_SLOTS);
    private final ItemStack chainStack;
    private final Player player;
    private final int[] shotCounts = new int[ROWS];
    private List<String> configuredKeys = new ArrayList<>();
    private List<ItemStack> pocketAmmoStacks = new ArrayList<>();
    private String selectedGunKey = "";
    private int listPage = 0;
    private int pocketAmmoPage = 0;
    private int syncedSelectedListRow = -1;
    private int syncedConfiguredCount = 0;

    public AmmoChainContainer(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, data.readEnum(InteractionHand.class));
    }

    public AmmoChainContainer(int windowId, Inventory playerInventory, InteractionHand hand) {
        super(SweetCharmContainers.AMMO_BELT_CONTAINER.get(), windowId);
        this.player = playerInventory.player;
        this.chainStack = playerInventory.player.getItemInHand(hand);

        for (int i = 0; i < ROWS; i++) {
            shotCounts[i] = 1;
        }

        this.addSlot(new GhostSlot(configInventory, SELECTED_GUN_SLOT, 74, 42));
        for (int i = 0; i < ROWS; i++) {
            this.addSlot(new GhostSlot(configInventory, AMMO_SLOT_START + i, 102 + i * 22, 42));
        }
        for (int i = 0; i < LIST_ROWS; i++) {
            this.addSlot(new GhostSlot(configInventory, LIST_SLOT_START + i, 14, 34 + i * 24));
        }
        for (int i = 0; i < POCKET_AMMO_SLOTS; i++) {
            this.addSlot(new GhostSlot(configInventory, POCKET_AMMO_SLOT_START + i, 74 + i * 22, 100));
        }

        int inventoryX = 47;
        int inventoryY = 162;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, inventoryX + col * 18, inventoryY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, inventoryX + col * 18, inventoryY + 58));
        }

        for (int i = 0; i < ROWS; i++) {
            final int index = i;
            this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return shotCounts[index];
                }

                @Override
                public void set(int value) {
                    shotCounts[index] = Math.max(1, value);
                }
            });
        }
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return listPage;
            }

            @Override
            public void set(int value) {
                listPage = Math.max(0, value);
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return computeSelectedListRow();
            }

            @Override
            public void set(int value) {
                syncedSelectedListRow = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return configuredKeys.size();
            }

            @Override
            public void set(int value) {
                syncedConfiguredCount = Math.max(0, value);
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pocketAmmoPage;
            }

            @Override
            public void set(int value) {
                pocketAmmoPage = Math.max(0, value);
            }
        });

        refreshConfiguredKeys(false);
        refreshPocketAmmoSlots();
        if (!configuredKeys.isEmpty()) {
            loadGunKey(configuredKeys.get(0), false);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == SELECTED_GUN_SLOT) {
            ItemStack carried = getCarried();
            if (button == 1 && carried.isEmpty()) {
                saveSelectedChain();
                clearEditor();
                refreshConfiguredKeys(false);
                broadcastChanges();
                return;
            }
            if (!carried.isEmpty() && carried.getItem() instanceof GunItem) {
                loadGunKey(AmmoChainItem.getItemKey(carried), true);
                broadcastChanges();
                return;
            }
            return;
        }

        if (slotId >= AMMO_SLOT_START && slotId < AMMO_SLOT_START + ROWS) {
            if (selectedGunKey.isEmpty()) {
                return;
            }

            int row = slotId - AMMO_SLOT_START;
            ItemStack carried = getCarried();
            if (button == 1 && carried.isEmpty()) {
                configInventory.setItem(slotId, ItemStack.EMPTY);
                shotCounts[row] = 1;
                saveSelectedChain();
                refreshConfiguredKeys(true);
                broadcastChanges();
                return;
            }

            if (!carried.isEmpty() && GunItem.BULLETS.test(carried)) {
                ItemStack ammo = carried.copy();
                ammo.setCount(1);
                configInventory.setItem(slotId, ammo);
                saveSelectedChain();
                refreshConfiguredKeys(true);
                broadcastChanges();
                return;
            }
            return;
        }

        if (slotId >= LIST_SLOT_START && slotId < LIST_SLOT_START + LIST_ROWS) {
            String key = getVisibleKey(slotId - LIST_SLOT_START);
            if (!key.isEmpty()) {
                loadGunKey(key, true);
                broadcastChanges();
            }
            return;
        }

        if (slotId >= POCKET_AMMO_SLOT_START && slotId < POCKET_AMMO_SLOT_START + POCKET_AMMO_SLOTS) {
            if (!selectedGunKey.isEmpty()) {
                ItemStack ammo = configInventory.getItem(slotId);
                if (!ammo.isEmpty()) {
                    fillFirstEmptyAmmoSlot(ammo);
                    saveSelectedChain();
                    refreshConfiguredKeys(true);
                    broadcastChanges();
                }
            }
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < ROWS) {
            if (!selectedGunKey.isEmpty()) {
                shotCounts[id] = Math.max(1, shotCounts[id] - 1);
                saveSelectedChain();
                refreshConfiguredKeys(true);
            }
            return true;
        }
        if (id >= 20 && id < 20 + ROWS) {
            if (!selectedGunKey.isEmpty()) {
                int row = id - 20;
                shotCounts[row] = Math.min(64, shotCounts[row] + 1);
                saveSelectedChain();
                refreshConfiguredKeys(true);
            }
            return true;
        }
        if (id >= 40 && id < 40 + ROWS) {
            if (!selectedGunKey.isEmpty()) {
                int row = id - 40;
                configInventory.setItem(AMMO_SLOT_START + row, ItemStack.EMPTY);
                shotCounts[row] = 1;
                saveSelectedChain();
                refreshConfiguredKeys(true);
            }
            return true;
        }
        if (id >= 60 && id < 60 + ROWS) {
            if (!selectedGunKey.isEmpty()) {
                moveRow(id - 60, -1);
                saveSelectedChain();
                refreshConfiguredKeys(true);
            }
            return true;
        }
        if (id >= 80 && id < 80 + ROWS) {
            if (!selectedGunKey.isEmpty()) {
                moveRow(id - 80, 1);
                saveSelectedChain();
                refreshConfiguredKeys(true);
            }
            return true;
        }
        if (id >= SELECT_LIST_BUTTON && id < SELECT_LIST_BUTTON + LIST_ROWS) {
            String key = getVisibleKey(id - SELECT_LIST_BUTTON);
            if (!key.isEmpty()) {
                loadGunKey(key, true);
            }
            return true;
        }
        if (id >= DELETE_LIST_BUTTON && id < DELETE_LIST_BUTTON + LIST_ROWS) {
            deleteVisibleKey(id - DELETE_LIST_BUTTON);
            return true;
        }
        if (id == PAGE_PREVIOUS_BUTTON) {
            if (listPage > 0) {
                saveSelectedChain();
                listPage--;
                refreshConfiguredKeys(false);
            }
            return true;
        }
        if (id == PAGE_NEXT_BUTTON) {
            if ((listPage + 1) * LIST_ROWS < configuredKeys.size()) {
                saveSelectedChain();
                listPage++;
                refreshConfiguredKeys(false);
            }
            return true;
        }
        if (id == POCKET_PAGE_PREVIOUS_BUTTON) {
            if (pocketAmmoPage > 0) {
                pocketAmmoPage--;
                refreshPocketAmmoSlots();
            }
            return true;
        }
        if (id == POCKET_PAGE_NEXT_BUTTON) {
            if ((pocketAmmoPage + 1) * POCKET_AMMO_SLOTS < pocketAmmoStacks.size()) {
                pocketAmmoPage++;
                refreshPocketAmmoSlots();
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !chainStack.isEmpty() && chainStack.getItem() instanceof AmmoChainItem;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            saveSelectedChain();
        }
        super.removed(player);
    }

    public int getShotCount(int row) {
        return shotCounts[row];
    }

    public ItemStack getChainStack() {
        return chainStack;
    }

    public int getListPage() {
        return listPage;
    }

    public int getSelectedListRow() {
        return syncedSelectedListRow;
    }

    public int getConfiguredCount() {
        return syncedConfiguredCount;
    }

    public int getPocketAmmoPage() {
        return pocketAmmoPage;
    }

    public int getPocketAmmoCount() {
        return pocketAmmoStacks.size();
    }

    private int computeSelectedListRow() {
        int index = configuredKeys.indexOf(selectedGunKey);
        if (index < 0) {
            return -1;
        }
        int visibleRow = index - listPage * LIST_ROWS;
        return visibleRow >= 0 && visibleRow < LIST_ROWS ? visibleRow : -1;
    }

    private void loadGunKey(String gunKey, boolean saveCurrent) {
        if (gunKey == null || gunKey.isBlank()) {
            return;
        }

        ItemStack gun = AmmoChainItem.stackFromKey(gunKey);
        if (gun.isEmpty() || !(gun.getItem() instanceof GunItem)) {
            return;
        }

        if (saveCurrent) {
            saveSelectedChain();
        }

        selectedGunKey = gunKey;
        configInventory.setItem(SELECTED_GUN_SLOT, gun);
        loadChainForKey(gunKey);
        refreshConfiguredKeys(true);
    }

    private void loadChainForKey(String gunKey) {
        for (int i = 0; i < ROWS; i++) {
            configInventory.setItem(AMMO_SLOT_START + i, ItemStack.EMPTY);
            shotCounts[i] = 1;
        }

        List<AmmoChainItem.ChainEntry> entries = AmmoChainItem.getEntries(chainStack, gunKey);
        for (int i = 0; i < ROWS && i < entries.size(); i++) {
            configInventory.setItem(AMMO_SLOT_START + i, entries.get(i).ammo.copy());
            shotCounts[i] = entries.get(i).shots;
        }
    }

    private void clearEditor() {
        selectedGunKey = "";
        configInventory.setItem(SELECTED_GUN_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < ROWS; i++) {
            configInventory.setItem(AMMO_SLOT_START + i, ItemStack.EMPTY);
            shotCounts[i] = 1;
        }
    }

    private void saveSelectedChain() {
        if (selectedGunKey.isEmpty()) {
            return;
        }

        ItemStack gun = AmmoChainItem.stackFromKey(selectedGunKey);
        if (gun.isEmpty() || !(gun.getItem() instanceof GunItem)) {
            return;
        }

        List<ItemStack> ammoStacks = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            ammoStacks.add(configInventory.getItem(AMMO_SLOT_START + i));
        }

        boolean keepEmpty = configuredKeys.contains(selectedGunKey);
        AmmoChainItem.saveChain(chainStack, gun, ammoStacks, shotCounts, keepEmpty);
    }

    private void deleteVisibleKey(int visibleRow) {
        String key = getVisibleKey(visibleRow);
        if (key.isEmpty()) {
            return;
        }

        int oldIndex = listPage * LIST_ROWS + visibleRow;
        AmmoChainItem.deleteChain(chainStack, key);
        refreshConfiguredKeys(false);

        if (key.equals(selectedGunKey)) {
            if (configuredKeys.isEmpty()) {
                clearEditor();
                refreshConfiguredKeys(false);
                return;
            }

            int nextIndex = Math.min(oldIndex, configuredKeys.size() - 1);
            listPage = nextIndex / LIST_ROWS;
            loadGunKey(configuredKeys.get(nextIndex), false);
        }
    }

    private void moveRow(int row, int direction) {
        int target = row + direction;
        if (row < 0 || row >= ROWS || target < 0 || target >= ROWS) {
            return;
        }

        ItemStack current = configInventory.getItem(AMMO_SLOT_START + row);
        ItemStack other = configInventory.getItem(AMMO_SLOT_START + target);
        configInventory.setItem(AMMO_SLOT_START + row, other);
        configInventory.setItem(AMMO_SLOT_START + target, current);

        int currentCount = shotCounts[row];
        shotCounts[row] = shotCounts[target];
        shotCounts[target] = currentCount;
    }

    private void refreshConfiguredKeys(boolean followSelected) {
        configuredKeys = AmmoChainItem.getConfiguredGunKeys(chainStack);

        if (followSelected) {
            int index = configuredKeys.indexOf(selectedGunKey);
            if (index >= 0) {
                listPage = index / LIST_ROWS;
            }
        }

        int maxPage = configuredKeys.isEmpty() ? 0 : (configuredKeys.size() - 1) / LIST_ROWS;
        listPage = Math.max(0, Math.min(listPage, maxPage));
        syncedSelectedListRow = computeSelectedListRow();
        syncedConfiguredCount = configuredKeys.size();
        syncListSlots();
        refreshPocketAmmoSlots();
    }

    private void syncListSlots() {
        for (int i = 0; i < LIST_ROWS; i++) {
            String key = getVisibleKey(i);
            configInventory.setItem(LIST_SLOT_START + i, key.isEmpty() ? ItemStack.EMPTY : AmmoChainItem.stackFromKey(key));
        }
    }

    private String getVisibleKey(int visibleRow) {
        int index = listPage * LIST_ROWS + visibleRow;
        return index >= 0 && index < configuredKeys.size() ? configuredKeys.get(index) : "";
    }

    private void fillFirstEmptyAmmoSlot(ItemStack ammoStack) {
        for (int i = 0; i < ROWS; i++) {
            int slot = AMMO_SLOT_START + i;
            if (configInventory.getItem(slot).isEmpty()) {
                ItemStack ammo = ammoStack.copy();
                ammo.setCount(1);
                configInventory.setItem(slot, ammo);
                return;
            }
        }
    }

    private void refreshPocketAmmoSlots() {
        pocketAmmoStacks = new ArrayList<>();
        for (int i = 0; i < POCKET_AMMO_SLOTS; i++) {
            configInventory.setItem(POCKET_AMMO_SLOT_START + i, ItemStack.EMPTY);
        }

        List<PocketItem.AmmoEntry> entries = BulletManager.getDistinctAmmoEntries(player);
        for (PocketItem.AmmoEntry entry : entries) {
            pocketAmmoStacks.add(entry.stack().copy());
        }

        int maxPage = pocketAmmoStacks.isEmpty() ? 0 : (pocketAmmoStacks.size() - 1) / POCKET_AMMO_SLOTS;
        pocketAmmoPage = Math.max(0, Math.min(pocketAmmoPage, maxPage));

        int start = pocketAmmoPage * POCKET_AMMO_SLOTS;
        for (int i = 0; i < POCKET_AMMO_SLOTS && start + i < pocketAmmoStacks.size(); i++) {
            configInventory.setItem(POCKET_AMMO_SLOT_START + i, pocketAmmoStacks.get(start + i).copy());
        }
    }

    private static class GhostSlot extends Slot {
        public GhostSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
