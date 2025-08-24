package juitar.sweet_charm_o_mine.events;

import juitar.sweet_charm_o_mine.commands.SniperTestCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sweet_charm_o_mine")
public class CommandEvents {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SniperTestCommand.register(event.getDispatcher());
    }
}