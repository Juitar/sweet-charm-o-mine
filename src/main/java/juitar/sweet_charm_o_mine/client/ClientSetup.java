package juitar.sweet_charm_o_mine.client;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.client.gui.PocketScreen;
import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册通用容器的屏幕
            MenuScreens.register(SweetCharmContainers.POCKET_CONTAINER.get(), PocketScreen::new);
        });
    }
}
