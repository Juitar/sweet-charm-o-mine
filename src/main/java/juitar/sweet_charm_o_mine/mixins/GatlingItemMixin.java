package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.AmmoResolver;
import lykrast.gunswithoutroses.item.GatlingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        return AmmoResolver.resolveAmmoPreview(weapon, player);
    }
}
