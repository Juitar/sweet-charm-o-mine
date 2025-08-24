package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    
    public static final KeyMapping TOGGLE_SNIPER_ZOOM = new KeyMapping(
        "key.sweet_charm_o_mine.toggle_sniper_zoom",
        GLFW.GLFW_KEY_Z,
        "key.categories.sweet_charm_o_mine"
    );
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_SNIPER_ZOOM);
    }
    
    @Mod.EventBusSubscriber(modid = SweetCharm.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_SNIPER_ZOOM.consumeClick()) {
                SweetCharm.sniperZoom = !SweetCharm.sniperZoom;
                Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("狙击镜缩放: " + (SweetCharm.sniperZoom ? "开启" : "关闭"))
                );
                SweetCharm.LOG.info("手动切换狙击镜缩放: {}", SweetCharm.sniperZoom);
            }
        }
    }
}