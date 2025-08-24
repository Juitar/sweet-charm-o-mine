package juitar.sweet_charm_o_mine;

import juitar.sweet_charm_o_mine.registry.SweetCharmItems;
import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SweetCharm.MODID)
public class SweetCharm {
    public static final String MODID = "sweet_charm_o_mine";
    public static final Logger LOG = LogManager.getLogger();
    
    // 狙击镜缩放状态
    public static boolean sniperZoom = false;
    
    public SweetCharm() {
        IEventBus EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        
        // 注册物品和容器
        SweetCharmItems.REG.register(EVENT_BUS);
        SweetCharmContainers.register(EVENT_BUS);
        EVENT_BUS.addListener(SweetCharmItems::makeCreativeTab);

    }
    
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}