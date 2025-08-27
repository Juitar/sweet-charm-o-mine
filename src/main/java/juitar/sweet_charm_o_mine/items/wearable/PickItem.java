package juitar.sweet_charm_o_mine.items.wearable;

import java.util.UUID;

import juitar.sweet_charm_o_mine.items.CurioItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class PickItem extends CurioItem{
    public static final UUID DMG = UUID.fromString("08FF1DAD-5C52-42D2-A27B-7B2E458491A1");
    public static final UUID FIREUP = UUID.fromString("CFA93FDD-4FC0-417B-A18E-6BF33E7E437D");
    public static final UUID BULLET = UUID.fromString("BAD979FA-5957-490C-8D10-76EA6F5339DA");
    
    // 用于跟踪玩家位置的Map
    private static final java.util.Map<UUID, Double> lastXMap = new java.util.HashMap<>();
    private static final java.util.Map<UUID, Double> lastZMap = new java.util.HashMap<>();
     public PickItem(Properties properties) {
        super(properties);

        addAttributeModifier(
            GWRAttributes.fireDelay.get(),
            FIREUP,
            "pick_fire_up",
            -0.25,
           AttributeModifier.Operation.MULTIPLY_BASE);
                addAttributeModifier(
            GWRAttributes.dmgTotal.get(),
            DMG,
            "pick_dmg_down",
            -0.25,
           AttributeModifier.Operation.MULTIPLY_BASE);
     }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity living = slotContext.entity();
        if (living != null && !living.level().isClientSide) {
            this.equipmentTick(living);
        }
    }
        protected void equipmentTick(LivingEntity livingEntity) {
            // 每20tick（1秒）检查一次
            if (livingEntity.level().getGameTime() % 20 == 0) {
                UUID entityId = livingEntity.getUUID();
                double currentX = livingEntity.getX();
                double currentZ = livingEntity.getZ();
                boolean onGround = livingEntity.onGround();
                
                // 获取上次记录的位置
                Double lastX = lastXMap.get(entityId);
                Double lastZ = lastZMap.get(entityId);
                
                if (lastX != null && lastZ != null) {
                    // 计算位置变化
                    double deltaX = Math.abs(currentX - lastX);
                    double deltaZ = Math.abs(currentZ - lastZ);
                    
                    // 检查玩家是否静止（位置变化很小且在地面上）
                    if (deltaX < 0.01 && deltaZ < 0.01 && onGround) {

                        livingEntity.addEffect(new MobEffectInstance(GWREffects.ammoUp.get(), 40, 0, false, false, true));
                    }
                }
                
                // 更新位置记录
                lastXMap.put(entityId, currentX);
                lastZMap.put(entityId, currentZ);
            }
        }
}