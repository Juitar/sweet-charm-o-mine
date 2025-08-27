package juitar.sweet_charm_o_mine.items.wearable;

import java.util.UUID;

import juitar.sweet_charm_o_mine.items.CurioItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


/**
 * 狙击镜饰品，佩戴时提供攻击伤害和攻击速度加成
 */
public class SniperScopeItem extends CurioItem {
    // 为每个属性修饰符创建唯一的UUID
    private static final UUID DMG = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    private static final UUID MULT = UUID.fromString("C8FF0A6D-63AD-466D-8C14-16FC5594863E");
    
    public SniperScopeItem(Properties properties) {
        super(properties);
        

        addAttributeModifier(
            GWRAttributes.sniperMult.get(), 
            MULT, 
            "sniper_scope_headshot_bonus", 
            0.25, 
            AttributeModifier.Operation.ADDITION
        );
        
        addAttributeModifier(
            GWRAttributes.dmgTotal.get(), 
            DMG, 
            "sniper_scope_dmg_bonus", 
            0.1, 
            AttributeModifier.Operation.MULTIPLY_BASE
        );
    }
}