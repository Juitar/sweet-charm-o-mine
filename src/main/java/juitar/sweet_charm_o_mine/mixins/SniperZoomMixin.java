package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.wearable.SniperScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class SniperZoomMixin {
    
    // 创建狙击枪标签的TagKey - 修复ResourceLocation格式
    private static final TagKey<Item> SNIPER_TAG = TagKey.create(Registries.ITEM, 
        new ResourceLocation("gunswithoutroses", "gun/sniper"));
    
    @Inject(method = "isShiftKeyDown", at = @At("HEAD"))
    private void checkSneakZoom(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide()) return;
        
        ItemStack stack = player.getMainHandItem();
        Minecraft mc = Minecraft.getInstance();
        
        
        // 检查玩家是否装备了狙击镜饰品
        boolean hasSniperScope = CuriosApi.getCuriosHelper().findFirstCurio(player, item -> item.getItem() instanceof SniperScopeItem).isPresent();
        
        // 检查手持物品是否带有狙击枪标签、处于第一人称视角，且玩家装备了狙击镜
        if (stack.is(SNIPER_TAG) && mc.options.getCameraType().isFirstPerson() && hasSniperScope) {
            boolean sneaking = player.isCrouching();
            
            
            if (sneaking && sneaking != SweetCharm.sniperZoom) {
                // 开启缩放时播放望远镜使用音效
                player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
                SweetCharm.sniperZoom = sneaking;
            } else if (!sneaking && sneaking != SweetCharm.sniperZoom) {
                // 关闭缩放时播放望远镜停止使用音效
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
                SweetCharm.sniperZoom = sneaking;
            }
        } else {
            // 如果不满足条件，关闭缩放
            if (SweetCharm.sniperZoom) {
                SweetCharm.sniperZoom = false;
            }
        }
    }
}
