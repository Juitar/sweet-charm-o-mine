package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.client.ClientData;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class FOVModifierMixin {
    
    @Inject(method = "getFov(Lnet/minecraft/client/Camera;FZ)D", at = @At("RETURN"), cancellable = true)
    private void modifyFOV(Camera camera, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (ClientData.isCurrentlyZooming()) {
            // 根据当前缩放模式应用不同的FOV倍数
            double originalFov = cir.getReturnValue();
            float fovMultiplier = ClientData.getCurrentFovMultiplier();
            cir.setReturnValue(originalFov * fovMultiplier);
        }
    }
}