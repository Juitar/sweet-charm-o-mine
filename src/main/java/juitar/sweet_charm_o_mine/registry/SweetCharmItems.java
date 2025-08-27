package juitar.sweet_charm_o_mine.registry;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.CurioItem;
import juitar.sweet_charm_o_mine.items.PocketItem;
import juitar.sweet_charm_o_mine.items.PocketType;
import juitar.sweet_charm_o_mine.items.wearable.BulletShellCharmItem;
import juitar.sweet_charm_o_mine.items.wearable.GunpowderItem;
import juitar.sweet_charm_o_mine.items.wearable.PickItem;
import juitar.sweet_charm_o_mine.items.wearable.SniperScopeItem;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class SweetCharmItems {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, SweetCharm.MODID);
    private static final List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();

    // 子弹口袋
    public static final RegistryObject<PocketItem> COPPER_POCKET = initItem(() -> new PocketItem(PocketType.COPPER), "copper_pocket");
    public static final RegistryObject<PocketItem> IRON_POCKET = initItem(() -> new PocketItem(PocketType.IRON), "iron_pocket");
    public static final RegistryObject<PocketItem> GOLD_POCKET = initItem(() -> new PocketItem(PocketType.GOLD), "gold_pocket");
    public static final RegistryObject<PocketItem> DIAMOND_POCKET = initItem(() -> new PocketItem(PocketType.DIAMOND), "diamond_pocket");
    public static final RegistryObject<PocketItem> NETHERITE_POCKET = initItem(() -> new PocketItem(PocketType.NETHERITE), "netherite_pocket");
    // 饰品
    public static final RegistryObject<CurioItem> SNIPER_SCOPE = initItem(() -> new SniperScopeItem(defP().stacksTo(1)), "sniper_scope");
    public static final RegistryObject<CurioItem> DUNLOP_PICK = initItem(() -> new PickItem(defP().stacksTo(1)), "dunlop_pick");
    public static final RegistryObject<CurioItem> GUNPOWDER_BAG = initItem(() -> new GunpowderItem(defP().stacksTo(1)), "gunpowder_bag");
    public static final RegistryObject<CurioItem> MAGAZINE = initItem(() -> new CurioItem(defP().stacksTo(1)), "magazine");
    public static final RegistryObject<CurioItem> TABACCO_PIPE = initItem(() -> new CurioItem(defP().stacksTo(1)), "tobacco_pipe");
    public static final RegistryObject<CurioItem> BULLET_SHELL_CHARM = initItem(() -> new BulletShellCharmItem(defP().stacksTo(1), 0.10), "bullet_shell_charm");
    
    public static Item.Properties defP() {
        return new Item.Properties();
    }
    public static void makeCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            helper.register(ResourceKey.create(Registries.CREATIVE_MODE_TAB, SweetCharm.resource("sweet_charm_o_mine")),
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.sweet_charm_o_mine"))
                            .icon(() -> new ItemStack(COPPER_POCKET.get()))
                            .displayItems((parameters, output) -> orderedItemsCreative.forEach(i -> output.accept(i.get())))
                            .build());
        });
    }
    
    public static <I extends Item> RegistryObject<I> initItem(Supplier<I> item, String name) {
        RegistryObject<I> rego = REG.register(name, item);
        orderedItemsCreative.add(rego);
        return rego;
    }
}