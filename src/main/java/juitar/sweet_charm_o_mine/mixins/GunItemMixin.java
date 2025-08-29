package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.PocketItem;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Mixin to modify GunItem's findAmmoAndPlayerShoot method to check Curios slots for bullet pockets
 */
@Mixin(GunItem.class)
public class GunItemMixin {

    @Redirect(method = "findAmmoAndPlayerShoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack redirectGetProjectile(Player player, ItemStack weapon) {
        // 首先尝试获取正常的弹药
        ItemStack normalAmmo = player.getProjectile(weapon);
        
        // 如果找到了正常弹药，直接返回
        if (!normalAmmo.isEmpty()) {
            return normalAmmo;
        }
        
        // 如果没有找到正常弹药，检查Curios槽位
        return findAmmoInCurios(weapon, player);
    }

    /**
     * 在Curios槽位中查找合适的弹药
     */
    private ItemStack findAmmoInCurios(ItemStack gun, Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get("bulletpocket");
            if (stacksHandler != null) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack curiosStack = stacksHandler.getStacks().getStackInSlot(i);
                    if (curiosStack.getItem() instanceof PocketItem pocketItem) {
                        // 检查子弹口袋中是否有弹药
                        if (pocketItem.hasAmmo(curiosStack)) {
                            // 返回子弹口袋本身，让它的consume方法被调用
                            return curiosStack;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}