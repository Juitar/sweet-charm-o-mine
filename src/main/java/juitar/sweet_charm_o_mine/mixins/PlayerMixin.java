package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.PocketItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Predicate;

/**
 * Mixin to modify Player's getProjectile method to check Curios slots for bullet pockets
 */
@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void getProjectileFromCurios(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        
        // 检查武器是否是ProjectileWeaponItem
        if (!(weapon.getItem() instanceof ProjectileWeaponItem projectileWeapon)) {
            return;
        }

        // 获取武器支持的弹药类型
        Predicate<ItemStack> supportedProjectiles = projectileWeapon.getSupportedHeldProjectiles();
        
        // 首先检查Curios槽位中的子弹口袋
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get("bulletpocket");
            if (stacksHandler != null) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack curiosStack = stacksHandler.getStacks().getStackInSlot(i);
                    if (curiosStack.getItem() instanceof PocketItem pocketItem) {
                        // 检查子弹口袋中是否有合适的弹药
                        CompoundTag tag = curiosStack.getTag();
                        if (tag != null && tag.contains("Inventory")) {
                            ListTag inventoryList = tag.getList("Inventory", Tag.TAG_COMPOUND);
                            
                            for (int j = 0; j < inventoryList.size(); j++) {
                                CompoundTag slotTag = inventoryList.getCompound(j);
                                ItemStack bulletStack = ItemStack.of(slotTag);
                                
                                if (!bulletStack.isEmpty() && supportedProjectiles.test(bulletStack)) {
                                    // 返回子弹口袋本身，让它的consume方法被调用
                                    cir.setReturnValue(curiosStack);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
