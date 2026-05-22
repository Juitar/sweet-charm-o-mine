package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.AmmoResolver;
import lykrast.gunswithoutroses.item.BurstGunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截 BurstGunItem.use() 中的 player.getProjectile() 预检，
 * 让 burst 类枪械在开始使用前也能检测 Curios 子弹口袋。
 */
@Mixin(BurstGunItem.class)
public class BurstGunItemMixin {

    @Redirect(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack redirectGetProjectileInUse(Player player, ItemStack weapon) {
        return AmmoResolver.resolveAmmoPreview(weapon, player);
    }
}
