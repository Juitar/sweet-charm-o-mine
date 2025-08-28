package juitar.sweet_charm_o_mine.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "sweet_charm_o_mine", value = Dist.CLIENT)
public class KeyBindings {
    
    public static final KeyMapping TOGGLE_SNIPER_ZOOM = new KeyMapping(
        "key.sweet_charm_o_mine.toggle_sniper_zoom",
        GLFW.GLFW_KEY_Z,
        "key.categories.sweet_charm_o_mine"
    );
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player == null || mc.screen != null) {
            return;
        }
        
        if (TOGGLE_SNIPER_ZOOM.consumeClick()) {
            ClientData.ZoomMode newMode = ClientData.toggleZoomMode();
            
            // 根据新模式发送不同的消息
            String messageKey;
            switch (newMode) {
                case ZOOM_4X:
                    messageKey = "message.sweet_charm_o_mine.sniper_zoom_4x";
                    break;
                case ZOOM_8X:
                    messageKey = "message.sweet_charm_o_mine.sniper_zoom_8x";
                    break;
                case OFF:
                default:
                    messageKey = "message.sweet_charm_o_mine.sniper_zoom_off";
                    break;
            }
            
            mc.player.displayClientMessage(Component.translatable(messageKey), true);
        }
    }
}