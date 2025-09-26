package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件处理类
 * 处理配置的自动保存
 */
@Mod.EventBusSubscriber(modid = SweetCharm.MODID, value = Dist.CLIENT)
public class ClientEvents {
    
    /**
     * 玩家断开连接时保存配置
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientConfig.save();
        SweetCharm.LOG.debug("玩家退出，保存客户端配置");
    }
    
    /**
     * 游戏关闭时保存配置
     */
    public static void onGameShutdown() {
        ClientConfig.save();
        SweetCharm.LOG.debug("游戏关闭，保存客户端配置");
    }
}