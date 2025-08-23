package juitar.sweet_charm_o_mine.debug;

import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.PocketItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PocketDebugHelper {
    
    public static void debugPocketUse(Player player, ItemStack stack) {
        if (stack.getItem() instanceof PocketItem pocketItem) {
            SweetCharm.LOG.info("=== 子弹口袋调试信息 ===");
            SweetCharm.LOG.info("玩家: {}", player.getName().getString());
            SweetCharm.LOG.info("是否为服务端: {}", !player.level().isClientSide);
            SweetCharm.LOG.info("口袋类型: {}", pocketItem.getPocketType());
            SweetCharm.LOG.info("物品堆叠: {}", stack);
            SweetCharm.LOG.info("物品NBT: {}", stack.getTag());
            SweetCharm.LOG.info("========================");
        }
    }
}