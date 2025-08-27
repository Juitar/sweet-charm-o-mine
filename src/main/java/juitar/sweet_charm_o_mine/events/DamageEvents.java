package juitar.sweet_charm_o_mine.events;

import juitar.sweet_charm_o_mine.items.wearable.BulletShellCharmItem;
import juitar.sweet_charm_o_mine.registry.SweetCharmAttributes;
import juitar.sweet_charm_o_mine.registry.SweetCharmItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import juitar.sweet_charm_o_mine.SweetCharm;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID)
public class DamageEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        
        // 检查是否是GunsWithoutRoses的枪械伤害
        if (source.typeHolder().is(lykrast.gunswithoutroses.registry.GWRDamage.GUN)) {
            // 获取攻击者
            if (source.getEntity() instanceof LivingEntity attacker) {
                // 获取攻击者的子弹穿甲属性值
                AttributeInstance penetrationAttribute = attacker.getAttribute(SweetCharmAttributes.BULLET_ARMOR_PENETRATION.get());
                
                if (penetrationAttribute != null) {
                    double penetrationValue = penetrationAttribute.getValue();
                    
                    if (penetrationValue > 0) {
                        // 获取受害者
                        LivingEntity victim = event.getEntity();
                        
                        // 获取当前已经被护甲减免后的伤害
                        float currentDamage = event.getAmount();
                        
                        // 计算如果没有护甲减免的原始伤害
                        float rawDamage = calculateRawDamage(victim, currentDamage);
                        
                        // 计算考虑穿甲后的新护甲减免
                        float newDamage = calculateDamageWithPenetration(victim, rawDamage, (float)penetrationValue);
                        
                        // 设置新的伤害值
                        event.setAmount(newDamage);
                        
                        // 处理子弹壳护符的连击功能
                        handleBulletShellCharm(attacker, newDamage > currentDamage);
                    }
                }
            }
        }
    }
    
    /**
     * 根据当前伤害反推原始伤害（没有护甲减免时的伤害）
     */
    private static float calculateRawDamage(LivingEntity entity, float currentDamage) {
        float armor = entity.getArmorValue();
        float toughness = (float)entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        
        // 这是一个近似计算，因为从减免后的伤害反推原始伤害比较复杂
        // 我们使用迭代方法来找到合适的原始伤害值
        float rawDamage = currentDamage;
        for (int i = 0; i < 10; i++) {
            float calculatedDamage = calculateDamageAfterArmor(entity, rawDamage);
            if (Math.abs(calculatedDamage - currentDamage) < 0.01f) {
                break;
            }
            rawDamage += (currentDamage - calculatedDamage);
        }
        
        return rawDamage;
    }
    
    /**
     * 计算考虑穿甲效果后的伤害
     */
    private static float calculateDamageWithPenetration(LivingEntity entity, float rawDamage, float penetrationValue) {
        float armor = entity.getArmorValue();
        float toughness = (float)entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        
        // 根据穿甲值减少有效护甲
        float effectiveArmor = armor * (1.0f - penetrationValue);
        
        // 使用减少后的护甲值计算伤害
        float effectiveArmorValue = Math.max(effectiveArmor - rawDamage / (2.0F + toughness / 4.0F), effectiveArmor / 5.0F);
        float armorReductionPercent = Math.min(effectiveArmorValue, 20.0F) / 25.0F;
        
        return rawDamage * (1.0f - armorReductionPercent);
    }
    
    /**
     * 计算护甲减免后的伤害（标准计算）
     */
    private static float calculateDamageAfterArmor(LivingEntity entity, float damage) {
        float armor = entity.getArmorValue();
        float toughness = (float)entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        
        float effectiveArmor = Math.max(armor - damage / (2.0F + toughness / 4.0F), armor / 5.0F);
        float armorReductionPercent = Math.min(effectiveArmor, 20.0F) / 25.0F;
        
        return damage * (1.0f - armorReductionPercent);
    }
    
    /**
     * 处理子弹壳护符的连击功能
     */
    private static void handleBulletShellCharm(LivingEntity attacker, boolean hitSuccessful) {
        if (!(attacker instanceof Player player)) {
            return;
        }
        
        // 查找玩家装备的子弹壳护符
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            var curios = curiosInventory.getCurios();
            
            // 遍历所有装备槽
            for (String slotType : curios.keySet()) {
                ICurioStacksHandler stacksHandler = curios.get(slotType);
                
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    
                    if (stack.getItem() == SweetCharmItems.BULLET_SHELL_CHARM.get()) {
                        if (hitSuccessful) {
                            // 成功击中目标，检查是否处于强化状态
                            if (BulletShellCharmItem.isEnhanced(stack)) {
                                // 消耗强化状态
                                BulletShellCharmItem.consumeEnhancement(stack);
                            } else {
                                // 增加连击计数
                                BulletShellCharmItem.addHit(stack);
                                int hitCount = BulletShellCharmItem.getHitCount(stack);
                                
                                // 检查是否达到强化条件
                                if (BulletShellCharmItem.isEnhanced(stack)) {
                                }
                            }
                        }
                        return; // 找到护符后退出
                    }
                }
            }
        });
    }
}
