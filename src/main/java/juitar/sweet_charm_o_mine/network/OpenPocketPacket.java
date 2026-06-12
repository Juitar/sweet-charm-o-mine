package juitar.sweet_charm_o_mine.network;

import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.PocketContainer;
import juitar.sweet_charm_o_mine.items.PocketItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenPocketPacket {
    public static void encode(OpenPocketPacket packet, FriendlyByteBuf buf) {
    }

    public static OpenPocketPacket decode(FriendlyByteBuf buf) {
        return new OpenPocketPacket();
    }

    public static void handle(OpenPocketPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            BulletManager.getActivePocket(player).ifPresent(pocketStack -> openPocket(player, pocketStack));
        });
        context.setPacketHandled(true);
    }

    public static void openPocket(ServerPlayer player, ItemStack pocketStack) {
        boolean hasSwitcher = BulletManager.getManageablePocketCount(player, pocketStack) > 1;
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (windowId, playerInventory, p) -> new PocketContainer(windowId, playerInventory, pocketStack, hasSwitcher),
                pocketStack.getHoverName()
        ), buf -> {
            buf.writeItem(PocketItem.createMenuDataStack(pocketStack));
            buf.writeBoolean(hasSwitcher);
        });
    }
}
