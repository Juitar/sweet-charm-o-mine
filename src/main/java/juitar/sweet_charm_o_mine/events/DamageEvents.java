package juitar.sweet_charm_o_mine.events;

import juitar.sweet_charm_o_mine.items.wearable.BulletShellCharmItem;
import juitar.sweet_charm_o_mine.items.wearable.SniperScopeItem;
import juitar.sweet_charm_o_mine.items.wearable.GunpowderItem;
import juitar.sweet_charm_o_mine.items.wearable.MagazineItem;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerLevel;
import java.util.List;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID)
public class DamageEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        
        // 检查是否是GunsWithoutRoses的枪械伤害
        if (source.typeHolder().is(lykrast.gunswithoutroses.registry.GWRDamage.GUN)) {
            // 获取攻击者
            if (source.getEntity() instanceof LivingEntity attacker) {
                // 检查是否有火药袋（GunpowderItem）
                boolean hasgunpowder = CuriosApi.getCuriosHelper().findFirstCurio(attacker, item -> item.getItem() instanceof GunpowderItem).isPresent();
                
                // 爆炸：佩戴火药袋且20%几率，在受害者位置触发可控制的爆炸（仅服务端）
                if (hasgunpowder && !attacker.level().isClientSide && attacker.getRandom().nextFloat() < 0.2f) {
                    LivingEntity victimEntity = event.getEntity();
                    // 使用自定义爆炸方法，可以精确控制范围和伤害
                    createCustomExplosion(attacker, victimEntity, 4.0f, event.getAmount()*0.8f);
                }
                
                // 弹匣连击系统：只有佩戴弹匣时才触发
                boolean hasMagazine = CuriosApi.getCuriosHelper().findFirstCurio(attacker, item -> item.getItem() instanceof MagazineItem).isPresent();
                if (hasMagazine) {
                    MagazineItem.onPlayerShoot(attacker);
                }
                
                
                
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
     * 创建自定义爆炸效果，可以精确控制范围和伤害
     * @param attacker 攻击者
     * @param victim 受害者（爆炸中心）
     * @param explosionRadius 爆炸范围
     * @param baseDamage 基础伤害
     */
    private static void createCustomExplosion(LivingEntity attacker, LivingEntity victim, float explosionRadius, float baseDamage) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        Vec3 explosionCenter = victim.position();
        
        // 播放爆炸声音
        serverLevel.playSound(null, explosionCenter.x, explosionCenter.y, explosionCenter.z, 
                             SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 1.0F);
        
        // 生成爆炸粒子效果
        for (int i = 0; i < 50; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (serverLevel.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * explosionRadius * 2;
            
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, 
                                    explosionCenter.x + offsetX, 
                                    explosionCenter.y + offsetY, 
                                    explosionCenter.z + offsetZ, 
                                    1, 0, 0, 0, 0);
        }
        
        
        // 查找爆炸范围内的所有生物
        AABB explosionArea = new AABB(
            explosionCenter.x - explosionRadius, explosionCenter.y - explosionRadius, explosionCenter.z - explosionRadius,
            explosionCenter.x + explosionRadius, explosionCenter.y + explosionRadius, explosionCenter.z + explosionRadius
        );
        
        List<LivingEntity> entitiesInRange = serverLevel.getEntitiesOfClass(LivingEntity.class, explosionArea);
        
        for (LivingEntity entity : entitiesInRange) {
            if (entity == attacker) continue; // 攻击者不受伤害
            
            double distance = entity.position().distanceTo(explosionCenter);
            if (distance <= explosionRadius) {
                // 根据距离计算伤害衰减
                float damageMultiplier = 1.0f - (float)(distance / explosionRadius);
                damageMultiplier = Math.max(0.1f, damageMultiplier); // 最小伤害倍数为0.1
                
                float finalDamage = baseDamage * damageMultiplier;
                
                // 创建爆炸伤害源
                DamageSource explosionDamage = entity.damageSources().explosion(attacker, attacker);
                
                // 对实体造成伤害
                entity.hurt(explosionDamage, finalDamage);
                
                // 添加击退效果
                Vec3 knockbackDirection = entity.position().subtract(explosionCenter).normalize();
                double knockbackStrength = (1.0 - distance / explosionRadius) * 0.8; // 最大击退强度0.8
                
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                    knockbackDirection.x * knockbackStrength,
                    Math.max(0.2, knockbackDirection.y * knockbackStrength), // 确保有向上的击退
                    knockbackDirection.z * knockbackStrength
                ));
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