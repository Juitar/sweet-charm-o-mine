package juitar.sweet_charm_o_mine.registry;

import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SweetCharmAttributes {
    public static final DeferredRegister<Attribute> REG = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, SweetCharm.MODID);
    
    // 子弹穿甲属性 - 百分比值，0.0表示不忽略护甲，1.0表示完全忽略护甲
    public static final RegistryObject<Attribute> BULLET_ARMOR_PENETRATION = REG.register("bullet_armor_penetration",
            () -> new RangedAttribute("attribute.sweet_charm_o_mine.bullet_armor_penetration", .0, 0.0, 1.0)
                    .setSyncable(true));
    
    /**
     * 将属性添加到玩家实体
     */
    public static void playerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, BULLET_ARMOR_PENETRATION.get());
    }
}
