package juitar.sweet_charm_o_mine.items.wearable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import juitar.sweet_charm_o_mine.items.CurioItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class PipeItem extends CurioItem {
    // UUID for attribute modifiers
    public static final UUID HEADSHOT_MULTIPLIER = UUID.fromString("18A59CFD-0018-43DC-9219-04C4361C0980");
    public static final UUID PROJECTILE_MULTIPLIER = UUID.fromString("5E55E2CA-ED42-4870-BFAC-12756ECE6215");
    public static final UUID FIRE_DELAY_REDUCTION = UUID.fromString("263C1828-437B-4F28-AA51-B8E658C92C80");

    // 记录上次位置与静止秒数 - 参考ShellItem的实现
    private static final Map<UUID, Double> lastXMap = new HashMap<>();
    private static final Map<UUID, Double> lastZMap = new HashMap<>();
    private static final Map<UUID, Integer> stillSecondsMap = new HashMap<>();

    public PipeItem(Properties properties) {
        super(properties);
        
        // 10%的爆头倍率提升
        addAttributeModifier(GWRAttributes.sniperMult.get(),
                HEADSHOT_MULTIPLIER,
                "pipe_headshot_boost",
                0.1,
                AttributeModifier.Operation.MULTIPLY_BASE);

        // 5%的弹丸总倍率提升
        addAttributeModifier(GWRAttributes.dmgTotal.get(),
                PROJECTILE_MULTIPLIER,
                "pipe_damage_boost",
                0.05,
                AttributeModifier.Operation.MULTIPLY_BASE);

        // 减5%的开火延迟
        addAttributeModifier(GWRAttributes.fireDelay.get(),
                FIRE_DELAY_REDUCTION,
                "pipe_fire_delay_reduction",
                -0.05,
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
        // 每20tick（1秒）检查一次 - 与ShellItem保持一致
        if (livingEntity.level().getGameTime() % 20 != 0) return;

        UUID id = livingEntity.getUUID();
        double currentX = livingEntity.getX();
        double currentZ = livingEntity.getZ();
        boolean onGround = livingEntity.onGround();

        Double lastX = lastXMap.get(id);
        Double lastZ = lastZMap.get(id);

        if (lastX != null && lastZ != null) {
            double deltaX = Math.abs(currentX - lastX);
            double deltaZ = Math.abs(currentZ - lastZ);

            if (deltaX < 0.01 && deltaZ < 0.01 && onGround) {
                int seconds = stillSecondsMap.getOrDefault(id, 0) + 1;
                stillSecondsMap.put(id, seconds);

                // 静止满2秒，给予隐身和精准度提升
                if (seconds >= 2) {
                    // 给予隐身效果（持续3秒）
                    if (!livingEntity.hasEffect(MobEffects.INVISIBILITY)) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false, true));
                    }
                    
                    // 给予精准度提升效果（持续3秒）
                    if (!livingEntity.hasEffect(GWREffects.accuracyUp.get())) {
                        livingEntity.addEffect(new MobEffectInstance(GWREffects.accuracyUp.get(), 60, 0, false, false, true));
                    }
                }
            } else {
                // 移动时重置计时并移除效果
                stillSecondsMap.put(id, 0);
                if (livingEntity.hasEffect(MobEffects.INVISIBILITY)) {
                    livingEntity.removeEffect(MobEffects.INVISIBILITY);
                }
                if (livingEntity.hasEffect(GWREffects.accuracyUp.get())) {
                    livingEntity.removeEffect(GWREffects.accuracyUp.get());
                }
            }
        }

        lastXMap.put(id, currentX);
        lastZMap.put(id, currentZ);
    }
}