package juitar.sweet_charm_o_mine.registry;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.PocketContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SweetCharmContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = 
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, SweetCharm.MODID);

    // 直接创建PocketContainer，不使用PocketMenuFactory
    public static final RegistryObject<MenuType<PocketContainer>> POCKET_CONTAINER = 
        CONTAINERS.register("pocket_container", 
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                return new PocketContainer(windowId, inv, data.readItem());
            }));

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
