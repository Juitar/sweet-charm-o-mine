package juitar.sweet_charm_o_mine;

import juitar.sweet_charm_o_mine.registry.SweetCharmAttributes;
import juitar.sweet_charm_o_mine.registry.SweetCharmItems;
import juitar.sweet_charm_o_mine.registry.SweetCharmContainers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SweetCharm.MODID)
public class SweetCharm {
    public static final String MODID = "sweet_charm_o_mine";
    public static final Logger LOG = LogManager.getLogger();
    
    public SweetCharm() {
        IEventBus EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        
        // 注册物品和容器
        SweetCharmItems.REG.register(EVENT_BUS);
        SweetCharmContainers.register(EVENT_BUS);
        SweetCharmAttributes.REG.register(EVENT_BUS);
        EVENT_BUS.addListener(SweetCharmItems::makeCreativeTab);
        EVENT_BUS.addListener(SweetCharmAttributes::playerAttributes);
        
        // 添加游戏关闭时的配置保存钩子
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Class<?> clientEventsClass = Class.forName("juitar.sweet_charm_o_mine.client.ClientEvents");
                    clientEventsClass.getMethod("onGameShutdown").invoke(null);
                } catch (Exception e) {
                    LOG.debug("无法调用客户端关闭钩子: {}", e.getMessage());
                }
            }));
        });
    }
    
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}