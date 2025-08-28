package juitar.sweet_charm_o_mine.items.wearable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import juitar.sweet_charm_o_mine.items.CurioItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class ShellItem extends CurioItem{
    final static UUID PROJECTILE = UUID.fromString("0EBF1D6E-2FAF-4021-BE7A-B7417E2EA16C");
    final static UUID DMG_DOWN = UUID.fromString("DA47E53B-8CCF-4A04-97F4-C44939507BFB");

    // 记录上次位置与静止秒数
    private static final Map<UUID, Double> lastXMap = new HashMap<>();
    private static final Map<UUID, Double> lastZMap = new HashMap<>();
    private static final Map<UUID, Integer> stillSecondsMap = new HashMap<>();

    public ShellItem(Properties properties) {
        super(properties);
        
        addAttributeModifier(GWRAttributes.shotgunProjectiles.get(),
         PROJECTILE,
        "Shell_projectile",
        1,
        AttributeModifier.Operation.ADDITION);

        addAttributeModifier(GWRAttributes.dmgTotal.get(),
        DMG_DOWN,
        "Shell_dmg_down",
        -0.1, 
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

                // 静止满3秒且未拥有命中提升，则给予10秒命中提升
                if (seconds >= 3 && !livingEntity.hasEffect(GWREffects.accuracyUp.get())) {
                    livingEntity.addEffect(new MobEffectInstance(GWREffects.accuracyUp.get(), 200, 0, false, false, true));
                    // 重置计时，继续静止需再累计3秒才能再次获得
                    stillSecondsMap.put(id, 0);
                }
            } else {
                stillSecondsMap.put(id, 0);
            }
        }

        lastXMap.put(id, currentX);
        lastZMap.put(id, currentZ);
    }

}