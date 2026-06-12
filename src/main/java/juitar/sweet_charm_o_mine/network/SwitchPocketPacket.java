package juitar.sweet_charm_o_mine.network;

import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.PocketContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchPocketPacket {
    private final int direction;

    public SwitchPocketPacket(int direction) {
        this.direction = direction < 0 ? -1 : 1;
    }

    public static void encode(SwitchPocketPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.direction);
    }

    public static SwitchPocketPacket decode(FriendlyByteBuf buf) {
        return new SwitchPocketPacket(buf.readInt());
    }

    public static void handle(SwitchPocketPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof PocketContainer container)) {
                return;
            }

            BulletManager.getAdjacentPocket(player, container.getPocketStack(), packet.direction)
                    .ifPresent(pocketStack -> OpenPocketPacket.openPocket(player, pocketStack));
        });
        context.setPacketHandled(true);
    }
}
