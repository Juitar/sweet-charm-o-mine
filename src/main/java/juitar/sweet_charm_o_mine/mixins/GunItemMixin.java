package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.AmmoResolver;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin to modify GunItem's findAmmoAndPlayerShoot method to check Curios slots for bullet pockets
 */
@Mixin(GunItem.class)
public class GunItemMixin {

    @Redirect(method = "findAmmoAndPlayerShoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack redirectGetProjectile(Player player, ItemStack weapon) {
        return AmmoResolver.resolveAmmoForShot(weapon, player);
    }
}
