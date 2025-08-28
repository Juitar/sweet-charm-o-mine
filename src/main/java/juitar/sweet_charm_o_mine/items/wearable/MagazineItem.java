package juitar.sweet_charm_o_mine.items.wearable;

import juitar.sweet_charm_o_mine.items.CurioItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagazineItem extends CurioItem implements ICurioItem {
    
    // 连击计数器
    private static final Map<UUID, Integer> shotStreaks = new HashMap<>();
    private static final Map<UUID, Long> lastShotTime = new HashMap<>();
    
    // 效果UUID
    private static final UUID RELOAD_SPEED_UUID = UUID.fromString("8b5c9d2e-4f3a-1b6c-9e8d-7a2b3c4d5e6f");
    private static final UUID AMMO_CAPACITY_UUID = UUID.fromString("9c6d0e3f-5a4b-2c7d-0f9e-8b3c4d5e6f7a");
    
    public MagazineItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        

        attributes.put(GWRAttributes.chanceUseAmmo.get(), 
            new AttributeModifier(RELOAD_SPEED_UUID, "Magazine reload speed", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        
        attributes.put(GWRAttributes.knockback.get(), 
            new AttributeModifier(AMMO_CAPACITY_UUID, "Magazine ammo capacity", 1.0, AttributeModifier.Operation.ADDITION));
        
        return attributes;
    }
    
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && !player.level().isClientSide) {
            UUID playerId = player.getUUID();
            // 检查是否超过5秒未射击，重置连击
            long currentTime = System.currentTimeMillis();
            Long lastShot = lastShotTime.get(playerId);
            if (lastShot != null && currentTime - lastShot > 5000) {
                shotStreaks.remove(playerId);
                lastShotTime.remove(playerId);
            }
        }
    }
    
    // 射击时调用的方法
    public static void onPlayerShoot(LivingEntity shooter) {
        if (!(shooter instanceof Player player) || player.level().isClientSide) {
            return;
        }
        
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        
        // 更新连击计数
        int currentStreak = shotStreaks.getOrDefault(playerId, 0) + 1;
        shotStreaks.put(playerId, currentStreak);
        lastShotTime.put(playerId, currentTime);
        
        // 连击奖励
        if (currentStreak == 5) {
            // 5发连击：射速提升效果
            player.addEffect(new MobEffectInstance(GWREffects.fireUp.get(), 200, 0, false, false,true));
        } else if (currentStreak == 10) {
            // 10发连击：弹药补充效果
            player.addEffect(new MobEffectInstance(GWREffects.ammoUp.get(), 300, 0, false, false,true));
        }
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            UUID playerId = player.getUUID();
            // 卸下时清除连击记录
            shotStreaks.remove(playerId);
            lastShotTime.remove(playerId);
        }
    }
}