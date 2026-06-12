package juitar.sweet_charm_o_mine.network;

import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SweetCharmNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SweetCharm.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(OpenPocketPacket.class, nextId())
                .encoder(OpenPocketPacket::encode)
                .decoder(OpenPocketPacket::decode)
                .consumerMainThread(OpenPocketPacket::handle)
                .add();

        CHANNEL.messageBuilder(CyclePocketAmmoPacket.class, nextId())
                .encoder(CyclePocketAmmoPacket::encode)
                .decoder(CyclePocketAmmoPacket::decode)
                .consumerMainThread(CyclePocketAmmoPacket::handle)
                .add();

        CHANNEL.messageBuilder(SwitchPocketPacket.class, nextId())
                .encoder(SwitchPocketPacket::encode)
                .decoder(SwitchPocketPacket::decode)
                .consumerMainThread(SwitchPocketPacket::handle)
                .add();
    }

    private static int nextId() {
        return packetId++;
    }
}
