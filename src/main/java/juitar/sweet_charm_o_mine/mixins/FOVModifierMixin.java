package juitar.sweet_charm_o_mine.mixins;

import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class FOVModifierMixin {
    
    @Inject(method = "getFov(Lnet/minecraft/client/Camera;FZ)D", at = @At("RETURN"), cancellable = true)
    private void modifyFOV(Camera camera, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (SweetCharm.sniperZoom) {
            // 将FOV缩放到原来的1/4，实现更明显的缩放效果
            double originalFov = cir.getReturnValue();
            double zoomedFov = originalFov * 0.25;
            cir.setReturnValue(zoomedFov);
        }
    }
}
