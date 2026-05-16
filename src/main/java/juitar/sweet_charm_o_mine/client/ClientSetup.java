package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.client.gui.AmmoChainScreen;
import juitar.sweet_charm_o_mine.client.gui.PocketScreen;
import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    private static boolean configScreenRegistered = false;

    public static void registerConfigScreen() {
        if (configScreenRegistered) {
            return;
        }

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> ClothConfigScreen.build(parent)));
        configScreenRegistered = true;
    }
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerConfigScreen();

        event.enqueueWork(() -> {
            // 注册通用容器的屏幕
            MenuScreens.register(SweetCharmContainers.POCKET_CONTAINER.get(), PocketScreen::new);
            MenuScreens.register(SweetCharmContainers.AMMO_BELT_CONTAINER.get(), AmmoChainScreen::new);
            
            // 加载客户端配置
            ClientConfig.load();
        });
    }
    
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 注册按键绑定到游戏设置中
        event.register(KeyBindings.TOGGLE_SNIPER_ZOOM);
        event.register(KeyBindings.OPEN_BULLET_POCKET);
        event.register(KeyBindings.PREVIOUS_POCKET_AMMO);
        event.register(KeyBindings.NEXT_POCKET_AMMO);
    }
}
