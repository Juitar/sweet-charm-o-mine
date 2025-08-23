package juitar.sweet_charm_o_mine.registry;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.PocketItem;
import juitar.sweet_charm_o_mine.items.PocketType;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import java.util.function.Supplier;

public class SweetCharmItems {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, SweetCharm.MODID);
    private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();


    static {

    }

    public static void makeCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            helper.register(ResourceKey.create(Registries.CREATIVE_MODE_TAB, SweetCharm.resource("sweet_charm_o_mine")),
                    CreativeModeTab.builder().title(Component.translatable("itemGroup.sweet_charm_o_mine")).icon(() -> new ItemStack(GWRItems.bulletBag.get()))
                            .displayItems((parameters, output) -> orderedItemsCreative.forEach(i -> output.accept(i.get()))).build());
        });
    }
    	public static <I extends Item> RegistryObject<I> initItem(Supplier<I> item, String name) {
		RegistryObject<I> rego = REG.register(name, item);
		orderedItemsCreative.add(rego);
		return rego;
	}
}
