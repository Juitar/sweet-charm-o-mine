package juitar.sweet_charm_o_mine.items.wearable;

import java.util.UUID;

import juitar.sweet_charm_o_mine.items.CurioItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class GunpowderItem extends CurioItem {
    final static UUID DMG = UUID.fromString("04AB92F7-102F-4FB0-BF4B-8C441C522836");
    final static UUID FireDown = UUID.fromString("1DED1238-8A90-4529-8613-CFCDE159C242");
        public GunpowderItem(Properties properties) {
        super(properties);
        
        addAttributeModifier(GWRAttributes.dmgBase.get(),
        DMG,
        "gunpowder_dmg_bonus", 
        3,
        AttributeModifier.Operation.ADDITION);

                addAttributeModifier(GWRAttributes.fireDelay.get(),
        DMG,
        "gunpowder_firedown", 
        0.3,
        AttributeModifier.Operation.MULTIPLY_BASE);


    }
    

}
