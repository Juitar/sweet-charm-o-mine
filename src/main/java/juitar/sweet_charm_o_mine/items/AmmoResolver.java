package juitar.sweet_charm_o_mine.items;

import juitar.sweet_charm_o_mine.items.wearable.AmmoChainItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AmmoResolver {
    private AmmoResolver() {
    }

    public static ItemStack resolveAmmoForShot(ItemStack gunStack, Player player) {
        ItemStack chainAmmo = AmmoChainItem.resolveAmmo(gunStack, player, true);
        if (!chainAmmo.isEmpty()) {
            return chainAmmo;
        }

        ItemStack pocketAmmo = resolvePocketAmmo(player);
        if (!pocketAmmo.isEmpty()) {
            return pocketAmmo;
        }

        return player.getProjectile(gunStack);
    }

    public static ItemStack resolveAmmoPreview(ItemStack gunStack, Player player) {
        ItemStack chainAmmo = AmmoChainItem.resolveAmmo(gunStack, player, false);
        if (!chainAmmo.isEmpty()) {
            return chainAmmo;
        }

        ItemStack pocketAmmo = resolvePocketAmmo(player, false);
        if (!pocketAmmo.isEmpty()) {
            return pocketAmmo;
        }

        return player.getProjectile(gunStack);
    }

    private static ItemStack resolvePocketAmmo(Player player) {
        return resolvePocketAmmo(player, true);
    }

    private static ItemStack resolvePocketAmmo(Player player, boolean storeResolved) {
        ItemStack selected = storeResolved
                ? BulletManager.normalizeSelectedAmmo(player)
                : BulletManager.getSelectedAmmoTemplate(player);
        if (!selected.isEmpty()) {
            for (ItemStack pocketStack : BulletManager.getEquippedPockets(player)) {
                boolean hasSelectedAmmo = storeResolved
                        ? PocketItem.prepareResolvedAmmo(pocketStack, selected)
                        : PocketItem.prepareSpecificAmmo(pocketStack, selected, false);
                if (hasSelectedAmmo) {
                    return pocketStack;
                }
            }
        }

        for (ItemStack pocketStack : BulletManager.getEquippedPockets(player)) {
            if (pocketStack.getItem() instanceof PocketItem
                    && !PocketItem.resolveSelectedAmmo(pocketStack, storeResolved).isEmpty()) {
                return pocketStack;
            }
        }
        return ItemStack.EMPTY;
    }
}
