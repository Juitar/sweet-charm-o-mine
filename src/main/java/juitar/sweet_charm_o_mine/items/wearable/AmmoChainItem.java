package juitar.sweet_charm_o_mine.items.wearable;

import juitar.sweet_charm_o_mine.items.AmmoChainContainer;
import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.CurioItem;
import juitar.sweet_charm_o_mine.items.PocketItem;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AmmoChainItem extends CurioItem {
    private static final String TAG_CHAINS = "Chains";
    private static final String TAG_STATE = "State";
    private static final String TAG_AMMO = "Ammo";
    private static final String TAG_SHOTS = "Shots";
    private static final String TAG_INDEX = "Index";
    private static final String TAG_PROGRESS = "Progress";

    public AmmoChainItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (windowId, playerInventory, p) -> new AmmoChainContainer(windowId, playerInventory, hand),
                    Component.translatable("container.sweet_charm_o_mine.ammo_belt")
            ), buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static ItemStack resolveAmmo(ItemStack gunStack, Player player, boolean advance) {
        if (!(gunStack.getItem() instanceof GunItem)) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> chainOpt = getEquippedChain(player);
        if (chainOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack chainStack = chainOpt.get();
        String gunKey = getItemKey(gunStack);
        List<ChainEntry> entries = getEntries(chainStack, gunKey);
        if (entries.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ChainState state = getState(chainStack, gunKey, entries.size());
        for (int checked = 0; checked < entries.size(); checked++) {
            int index = Math.floorMod(state.index + checked, entries.size());
            ChainEntry entry = entries.get(index);
            ItemStack resolved = findAvailableAmmo(entry.ammo, player, advance);
            if (!resolved.isEmpty()) {
                if (advance) {
                    advanceState(chainStack, gunKey, index, state.progress, entry.shots, entries.size());
                }
                return resolved;
            }
        }

        return ItemStack.EMPTY;
    }

    public static List<ChainEntry> getEntries(ItemStack chainStack, String gunKey) {
        List<ChainEntry> entries = new ArrayList<>();
        CompoundTag tag = chainStack.getTag();
        if (tag == null || !tag.contains(TAG_CHAINS)) {
            return entries;
        }

        CompoundTag chains = tag.getCompound(TAG_CHAINS);
        if (!chains.contains(gunKey)) {
            return entries;
        }

        ListTag list = chains.getList(gunKey, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            ItemStack ammo = ItemStack.of(entryTag.getCompound(TAG_AMMO));
            if (!ammo.isEmpty() && GunItem.BULLETS.test(ammo)) {
                ammo.setCount(1);
                entries.add(new ChainEntry(ammo, Math.max(1, entryTag.getInt(TAG_SHOTS))));
            }
        }
        return entries;
    }

    public static List<String> getConfiguredGunKeys(ItemStack chainStack) {
        List<String> keys = new ArrayList<>();
        CompoundTag tag = chainStack.getTag();
        if (tag == null || !tag.contains(TAG_CHAINS)) {
            return keys;
        }

        CompoundTag chains = tag.getCompound(TAG_CHAINS);
        for (String key : chains.getAllKeys()) {
            ItemStack gun = stackFromKey(key);
            if (!gun.isEmpty() && gun.getItem() instanceof GunItem) {
                keys.add(key);
            }
        }
        Collections.sort(keys);
        return keys;
    }

    public static void deleteChain(ItemStack chainStack, String gunKey) {
        if (gunKey == null || gunKey.isBlank()) {
            return;
        }

        CompoundTag tag = chainStack.getOrCreateTag();
        CompoundTag chains = tag.getCompound(TAG_CHAINS);
        chains.remove(gunKey);
        if (chains.isEmpty()) {
            tag.remove(TAG_CHAINS);
        } else {
            tag.put(TAG_CHAINS, chains);
        }
        resetState(chainStack, gunKey);
    }

    public static void saveChain(ItemStack chainStack, ItemStack gunStack, List<ItemStack> ammoStacks, int[] shotCounts) {
        saveChain(chainStack, gunStack, ammoStacks, shotCounts, false);
    }

    public static void saveChain(ItemStack chainStack, ItemStack gunStack, List<ItemStack> ammoStacks, int[] shotCounts, boolean keepEmpty) {
        if (!(gunStack.getItem() instanceof GunItem)) {
            return;
        }

        String gunKey = getItemKey(gunStack);
        CompoundTag tag = chainStack.getOrCreateTag();
        CompoundTag chains = tag.getCompound(TAG_CHAINS);
        ListTag list = new ListTag();

        for (int i = 0; i < ammoStacks.size(); i++) {
            ItemStack ammo = ammoStacks.get(i);
            if (ammo.isEmpty() || !GunItem.BULLETS.test(ammo)) {
                continue;
            }
            ItemStack storedAmmo = ammo.copy();
            storedAmmo.setCount(1);

            CompoundTag entryTag = new CompoundTag();
            entryTag.put(TAG_AMMO, storedAmmo.save(new CompoundTag()));
            entryTag.putInt(TAG_SHOTS, Math.max(1, shotCounts[i]));
            list.add(entryTag);
        }

        if (list.isEmpty() && !keepEmpty) {
            deleteChain(chainStack, gunKey);
            return;
        }

        if (list.isEmpty()) {
            chains.put(gunKey, list);
        } else {
            chains.put(gunKey, list);
        }
        tag.put(TAG_CHAINS, chains);
        resetState(chainStack, gunKey);
    }

    public static Optional<ItemStack> getEquippedChain(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof AmmoChainItem)
                .map(result -> result.stack());
    }

    public static String getItemKey(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    public static ItemStack stackFromKey(String key) {
        try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
            return item == null ? ItemStack.EMPTY : new ItemStack(item);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack findAvailableAmmo(ItemStack ammoTemplate, Player player, boolean preparePocket) {
        Optional<ItemStack> pocketOpt = BulletManager.getEquippedPocket(player);
        if (pocketOpt.isPresent()) {
            ItemStack pocketStack = pocketOpt.get();
            boolean hasPocketAmmo = preparePocket
                    ? PocketItem.prepareResolvedAmmo(pocketStack, ammoTemplate)
                    : PocketItem.prepareSpecificAmmo(pocketStack, ammoTemplate, false);
            if (hasPocketAmmo) {
                return pocketStack;
            }
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && GunItem.BULLETS.test(stack) && ItemStack.isSameItemSameTags(stack, ammoTemplate)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static ChainState getState(ItemStack chainStack, String gunKey, int entryCount) {
        CompoundTag stateRoot = chainStack.getOrCreateTag().getCompound(TAG_STATE);
        CompoundTag state = stateRoot.getCompound(gunKey);
        int index = Math.floorMod(state.getInt(TAG_INDEX), entryCount);
        int progress = Math.max(0, state.getInt(TAG_PROGRESS));
        return new ChainState(index, progress);
    }

    private static void advanceState(ItemStack chainStack, String gunKey, int usedIndex, int currentProgress, int shots, int entryCount) {
        int nextProgress = currentProgress + 1;
        int nextIndex = usedIndex;
        if (nextProgress >= shots) {
            nextProgress = 0;
            nextIndex = Math.floorMod(usedIndex + 1, entryCount);
        }

        CompoundTag tag = chainStack.getOrCreateTag();
        CompoundTag stateRoot = tag.getCompound(TAG_STATE);
        CompoundTag state = new CompoundTag();
        state.putInt(TAG_INDEX, nextIndex);
        state.putInt(TAG_PROGRESS, nextProgress);
        stateRoot.put(gunKey, state);
        tag.put(TAG_STATE, stateRoot);
    }

    private static void resetState(ItemStack chainStack, String gunKey) {
        CompoundTag tag = chainStack.getOrCreateTag();
        CompoundTag stateRoot = tag.getCompound(TAG_STATE);
        stateRoot.remove(gunKey);
        tag.put(TAG_STATE, stateRoot);
    }

    private static class ChainState {
        private final int index;
        private final int progress;

        private ChainState(int index, int progress) {
            this.index = index;
            this.progress = progress;
        }
    }

    public static class ChainEntry {
        public final ItemStack ammo;
        public final int shots;

        public ChainEntry(ItemStack ammo, int shots) {
            this.ammo = ammo;
            this.shots = shots;
        }
    }
}
