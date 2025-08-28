package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.wearable.SniperScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 狙击镜缩放管理器
 * 处理狙击镜的缩放逻辑和状态管理
 */
@OnlyIn(Dist.CLIENT)
public class SniperZoomManager {
    
    // 狙击枪标签
    private static final TagKey<Item> SNIPER_TAG = TagKey.create(Registries.ITEM, 
        new ResourceLocation("gunswithoutroses", "gun/sniper"));
    
    /**
     * 检查玩家是否可以使用狙击镜缩放
     * @param player 玩家
     * @return 是否可以使用狙击镜
     */
    public static boolean canUseSniperZoom(LocalPlayer player) {
        if (player == null) return false;
        
        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = player.getMainHandItem();
        
        // 检查是否装备了狙击镜饰品
        boolean hasSniperScope = CuriosApi.getCuriosHelper()
            .findFirstCurio(player, item -> item.getItem() instanceof SniperScopeItem)
            .isPresent();
        
        // 检查条件：持有狙击枪 + 第一人称视角 + 装备狙击镜
        return stack.is(SNIPER_TAG) && 
               mc.options.getCameraType().isFirstPerson() && 
               hasSniperScope;
    }
    
    /**
     * 手动切换狙击镜缩放状态（按Z键触发）
     * @param player 玩家
     * @return 是否成功切换
     */
    public static boolean toggleSniperZoom(LocalPlayer player) {
        if (!canUseSniperZoom(player)) {
            // 如果不能使用狙击镜，强制关闭缩放
            if (ClientData.isSniperZoomEnabled()) {
                ClientData.setSniperZoomEnabled(false);
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
                SweetCharm.LOG.info("狙击镜缩放已关闭 - 不满足使用条件");
            }
            return false;
        }
        
        // 切换状态
        boolean newState = ClientData.toggleSniperZoom();
        
        // 播放音效
        if (newState) {
            player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        } else {
            player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
        }
        
        SweetCharm.LOG.info("手动切换狙击镜缩放: {}", newState);
        return true;
    }
    
    /**
     * 自动处理潜行时的狙击镜缩放（原有的潜行逻辑）
     * @param player 玩家
     */
    public static void handleSneakZoom(LocalPlayer player) {
        if (!canUseSniperZoom(player)) {
            // 如果不满足条件，关闭缩放
            if (ClientData.isSniperZoomEnabled()) {
                ClientData.setSniperZoomEnabled(false);
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            }
            return;
        }
        
        boolean sneaking = player.isCrouching();
        boolean currentZoomState = ClientData.isSniperZoomEnabled();
        
        // 只有在潜行状态改变时才处理
        if (sneaking != currentZoomState) {
            ClientData.setSniperZoomEnabled(sneaking);
            
            if (sneaking) {
                player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            } else {
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            }
        }
    }
    
    /**
     * 强制关闭狙击镜缩放
     * @param player 玩家
     */
    public static void forceDisableZoom(LocalPlayer player) {
        if (ClientData.isSniperZoomEnabled()) {
            ClientData.setSniperZoomEnabled(false);
            if (player != null) {
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
            }
        }
    }
}