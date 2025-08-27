package juitar.sweet_charm_o_mine.items.wearable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import juitar.sweet_charm_o_mine.items.CurioItem;
import juitar.sweet_charm_o_mine.registry.SweetCharmAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class BulletShellCharmItem extends CurioItem {
    private final double basePenetrationValue;
    private static final UUID ARMOR_PENETRATION_UUID = UUID.fromString("d3fd87d3-4251-4c7a-b49d-9b9187cf2283");
    private static final String HIT_COUNT_TAG = "HitCount";
    private static final String ENHANCED_TAG = "Enhanced";
    private static final int HITS_REQUIRED = 5;
    private static final double ENHANCED_PENETRATION = 0.15; // 额外15%穿甲

    public BulletShellCharmItem(Properties properties, double basePenetrationValue) {
        super(properties);
        this.basePenetrationValue = basePenetrationValue;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        
        // 基础穿甲值
        double totalPenetration = basePenetrationValue;
        
        // 检查是否处于强化状态
        if (isEnhanced(stack)) {
            totalPenetration += ENHANCED_PENETRATION;
        }
        
        // 添加子弹穿甲属性
        attributes.put(SweetCharmAttributes.BULLET_ARMOR_PENETRATION.get(), 
                new AttributeModifier(ARMOR_PENETRATION_UUID, 
                        "Bullet shell charm penetration", 
                        totalPenetration, 
                        AttributeModifier.Operation.ADDITION));
        
        return attributes;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        int hitCount = getHitCount(stack);
        boolean enhanced = isEnhanced(stack);
        
        // 基础穿甲描述
        double displayPenetration = basePenetrationValue;
        if (enhanced) {
            displayPenetration += ENHANCED_PENETRATION;
            tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.bullet_shell.enhanced", 
                            (int)(displayPenetration * 100))
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.bullet_shell.base", 
                            (int)(displayPenetration * 100))
                    .withStyle(ChatFormatting.GRAY));
        }
        
        // 连击进度
        tooltip.add(Component.translatable("tooltip.sweet_charm_o_mine.bullet_shell.progress", 
                        hitCount, HITS_REQUIRED)
                .withStyle(ChatFormatting.YELLOW));
        
    }
    
    /**
     * 获取当前连击次数
     */
    public static int getHitCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(HIT_COUNT_TAG) : 0;
    }
    
    /**
     * 设置连击次数
     */
    public static void setHitCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(HIT_COUNT_TAG, count);
    }
    
    /**
     * 增加连击次数
     */
    public static void addHit(ItemStack stack) {
        int currentCount = getHitCount(stack);
        currentCount++;
        
        if (currentCount >= HITS_REQUIRED) {
            // 达到5次，激活强化状态
            setEnhanced(stack, true);
            setHitCount(stack, 0); // 重置计数
        } else {
            setHitCount(stack, currentCount);
        }
    }
    
    /**
     * 检查是否处于强化状态
     */
    public static boolean isEnhanced(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ENHANCED_TAG);
    }
    
    /**
     * 设置强化状态
     */
    public static void setEnhanced(ItemStack stack, boolean enhanced) {
        stack.getOrCreateTag().putBoolean(ENHANCED_TAG, enhanced);
    }
    
    /**
     * 消耗强化状态（射击后调用）
     */
    public static void consumeEnhancement(ItemStack stack) {
        setEnhanced(stack, false);
    }
}