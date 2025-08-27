package juitar.sweet_charm_o_mine.items;


import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CurioItem extends Item implements ICurioItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    
    public CurioItem(Properties properties) {
        super(properties);
        this.defaultModifiers = HashMultimap.create();
    }


    public boolean isEquippedBy(@Nullable LivingEntity entity) {
        return entity != null && CuriosApi.getCuriosHelper().findFirstCurio(entity, this).isPresent();
    }
    
    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_CHAIN, 1.0f, 1.0f);
    }
    
   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) { 
      String translationKey = "tooltip.sweet_charm_o_mine." + getTooltipItemName() + ".desc";
      tooltip.add(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));
   }


    protected String getTooltipItemName() {
        return BuiltInRegistries.ITEM.getKey(this).getPath();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true; 
    }


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        return defaultModifiers;
    }
    
    /**
     * 添加属性修饰符
     * @param attribute 要修改的属性
     * @param uuid 修饰符UUID
     * @param name 修饰符名称
     * @param amount 修饰值
     * @param operation 操作类型
     */
    protected void addAttributeModifier(Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
        defaultModifiers.put(attribute, modifier);
    }


}
