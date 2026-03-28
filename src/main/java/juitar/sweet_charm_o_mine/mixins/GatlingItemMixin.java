package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.PocketItem;
import lykrast.gunswithoutroses.item.GatlingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 拦截 GatlingItem.use() 中的 player.getProjectile() 调用，
 * 让加特林在判断"是否有弹药"时也能检测到 Curios 子弹口袋。
 *
 * GatlingItem 覆写了 use()，在里面独立调用 player.getProjectile()
 * 来判断是否有弹药，该调用不经过 findAmmoAndPlayerShoot，
 * 因此 GunItemMixin 的 @Redirect 无法覆盖到这里。
 */
@Mixin(GatlingItem.class)
public class GatlingItemMixin {

    @Redirect(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack redirectGetProjectileInUse(Player player, ItemStack weapon) {
        // 先走原版逻辑
        ItemStack normalAmmo = player.getProjectile(weapon);
        if (!normalAmmo.isEmpty()) {
            return normalAmmo;
        }

        // 检查 Curios 子弹口袋
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get("bulletpocket");
            if (stacksHandler != null) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack curiosStack = stacksHandler.getStacks().getStackInSlot(i);
                    if (curiosStack.getItem() instanceof PocketItem pocketItem) {
                        if (pocketItem.hasAmmo(curiosStack)) {
                            return curiosStack;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}
