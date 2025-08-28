package juitar.sweet_charm_o_mine.client;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID, value = Dist.CLIENT)
public class SniperOverlay {
    
    private static final ResourceLocation SNIPER_SCOPE_TEXTURE = 
        new ResourceLocation(SweetCharm.MODID, "textures/gui/sniper_scope.png");
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ClientData.isCurrentlyZooming()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) return;
        
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 使用最强的渲染覆盖设置
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 设置极高的Z层级，确保完全覆盖所有UI
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 5000); // 极高的Z坐标
        
        // 渲染狙击镜蒙版（只填充纹理周围的区域为黑色，保留中心透明区域）
        renderSniperScopeMask(guiGraphics, screenWidth, screenHeight);
        
        guiGraphics.pose().popPose();
        
        // 恢复渲染状态
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    
    /**
     * 渲染狙击镜蒙版
     * 只填充纹理周围的区域为黑色，保留中心透明区域
     */
    private static void renderSniperScopeMask(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        int scopeSize = 256; // 狙击镜纹理固定大小
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int scopeX = centerX - scopeSize / 2;
        int scopeY = centerY - scopeSize / 2;
        
        try {
            // 填充纹理周围的区域为黑色，保留中心区域不填充
            // 上方黑色区域 - 从屏幕顶部到狙击镜上边缘
            guiGraphics.fill(0, 0, screenWidth, Math.max(0, scopeY), 0xFF000000);
            
            // 下方黑色区域 - 从狙击镜下边缘到屏幕底部
            guiGraphics.fill(0, scopeY + scopeSize, screenWidth, screenHeight, 0xFF000000);
            
            // 左侧黑色区域 - 从屏幕左边到狙击镜左边缘
            guiGraphics.fill(0, Math.max(0, scopeY), Math.max(0, scopeX), scopeY + scopeSize, 0xFF000000);
            
            // 右侧黑色区域 - 从狙击镜右边缘到屏幕右边
            guiGraphics.fill(scopeX + scopeSize, Math.max(0, scopeY), screenWidth, scopeY + scopeSize, 0xFF000000);
            
            
            // 在中心区域渲染狙击镜纹理
            guiGraphics.blit(SNIPER_SCOPE_TEXTURE, scopeX, scopeY, 0, 0, scopeSize, scopeSize, scopeSize, scopeSize);

            
        } catch (Exception e) {
            SweetCharm.LOG.warn("狙击镜纹理加载失败，使用备用渲染: {}", e.getMessage());
            // 如果纹理加载失败，使用备用渲染方式
            renderFallbackScope(guiGraphics, screenWidth, screenHeight);
        }
    }
    
    /**
     * 备用狙击镜渲染（当纹理文件不存在时）
     */
    private static void renderFallbackScope(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int scopeRadius = 120; // 固定狙击镜半径
        
        // 填充整个屏幕为黑色
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);
        
        // 绘制白色圆形边框
        drawCircleOutline(guiGraphics, centerX, centerY, scopeRadius, 0xFFFFFFFF, 3);
        
        // 绘制十字准星
        int crosshairSize = scopeRadius / 3;
        int lineWidth = 2;
        
        // 水平线
        guiGraphics.fill(centerX - crosshairSize, centerY - lineWidth/2, 
                        centerX + crosshairSize, centerY + lineWidth/2, 0xFFFFFFFF);
        // 垂直线
        guiGraphics.fill(centerX - lineWidth/2, centerY - crosshairSize, 
                        centerX + lineWidth/2, centerY + crosshairSize, 0xFFFFFFFF);
    }
    
    /**
     * 绘制圆形轮廓
     */
    private static void drawCircleOutline(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color, int thickness) {
        for (int i = 0; i < thickness; i++) {
            drawCircle(guiGraphics, centerX, centerY, radius - i, color);
        }
    }
    
    /**
     * 绘制圆形（使用像素点）
     */
    private static void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        
        while (y >= x) {
            // 绘制8个对称点
            drawPixel(guiGraphics, centerX + x, centerY + y, color);
            drawPixel(guiGraphics, centerX - x, centerY + y, color);
            drawPixel(guiGraphics, centerX + x, centerY - y, color);
            drawPixel(guiGraphics, centerX - x, centerY - y, color);
            drawPixel(guiGraphics, centerX + y, centerY + x, color);
            drawPixel(guiGraphics, centerX - y, centerY + x, color);
            drawPixel(guiGraphics, centerX + y, centerY - x, color);
            drawPixel(guiGraphics, centerX - y, centerY - x, color);
            
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
        }
    }
    
    /**
     * 绘制单个像素点
     */
    private static void drawPixel(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + 1, y + 1, color);
    }
}