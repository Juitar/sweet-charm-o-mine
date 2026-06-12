package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.network.CyclePocketAmmoPacket;
import juitar.sweet_charm_o_mine.network.OpenPocketPacket;
import juitar.sweet_charm_o_mine.network.SweetCharmNetwork;
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

    public static final KeyMapping OPEN_BULLET_POCKET = new KeyMapping(
        "key.sweet_charm_o_mine.open_bullet_pocket",
        GLFW.GLFW_KEY_B,
        "key.categories.sweet_charm_o_mine"
    );

    public static final KeyMapping PREVIOUS_POCKET_AMMO = new KeyMapping(
        "key.sweet_charm_o_mine.previous_pocket_ammo",
        GLFW.GLFW_KEY_UP,
        "key.categories.sweet_charm_o_mine"
    );

    public static final KeyMapping NEXT_POCKET_AMMO = new KeyMapping(
        "key.sweet_charm_o_mine.next_pocket_ammo",
        GLFW.GLFW_KEY_DOWN,
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

        if (OPEN_BULLET_POCKET.consumeClick()) {
            SweetCharmNetwork.CHANNEL.sendToServer(new OpenPocketPacket());
        }

        if (PREVIOUS_POCKET_AMMO.consumeClick()) {
            cyclePocketAmmo(mc, -1);
        }

        if (NEXT_POCKET_AMMO.consumeClick()) {
            cyclePocketAmmo(mc, 1);
        }
    }

    private static void cyclePocketAmmo(Minecraft mc, int direction) {
        if (mc.player == null) return;

        if (!BulletManager.cycleSelectedAmmo(mc.player, direction).isEmpty()) {
            PocketAmmoOverlay.show(direction);
        }
        SweetCharmNetwork.CHANNEL.sendToServer(new CyclePocketAmmoPacket(direction));
    }
}
