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

        ItemStack pocketAmmo = BulletManager.getEquippedPocket(player)
                .filter(stack -> stack.getItem() instanceof PocketItem)
                .filter(stack -> !PocketItem.resolveSelectedAmmo(stack, false).isEmpty())
                .orElse(ItemStack.EMPTY);
        if (!pocketAmmo.isEmpty()) {
            return pocketAmmo;
        }

        return player.getProjectile(gunStack);
    }

    private static ItemStack resolvePocketAmmo(Player player) {
        return BulletManager.getEquippedPocket(player)
                .filter(stack -> stack.getItem() instanceof PocketItem)
                .filter(stack -> !PocketItem.resolveSelectedAmmo(stack, true).isEmpty())
                .orElse(ItemStack.EMPTY);
    }
}
