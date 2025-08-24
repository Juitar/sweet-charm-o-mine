package juitar.sweet_charm_o_mine.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SniperTestCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("snipertest")
            .executes(SniperTestCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        boolean zoomState = SweetCharm.sniperZoom;
        context.getSource().sendSuccess(() -> 
            Component.literal("狙击镜缩放状态: " + (zoomState ? "开启" : "关闭")), false);
        
        // 手动切换状态进行测试
        SweetCharm.sniperZoom = !SweetCharm.sniperZoom;
        context.getSource().sendSuccess(() -> 
            Component.literal("已切换到: " + (SweetCharm.sniperZoom ? "开启" : "关闭")), false);
        
        return 1;
    }
}