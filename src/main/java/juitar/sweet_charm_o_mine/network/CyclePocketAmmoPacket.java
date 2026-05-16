package juitar.sweet_charm_o_mine.network;

import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.PocketItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CyclePocketAmmoPacket {
    private final int direction;

    public CyclePocketAmmoPacket(int direction) {
        this.direction = direction < 0 ? -1 : 1;
    }

    public static void encode(CyclePocketAmmoPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.direction);
    }

    public static CyclePocketAmmoPacket decode(FriendlyByteBuf buf) {
        return new CyclePocketAmmoPacket(buf.readInt());
    }

    public static void handle(CyclePocketAmmoPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            BulletManager.getEquippedPocket(player).ifPresent(pocketStack -> {
                ItemStack selected = PocketItem.cycleSelectedAmmo(pocketStack, packet.direction);
                if (!selected.isEmpty()) {
                    player.containerMenu.broadcastChanges();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
