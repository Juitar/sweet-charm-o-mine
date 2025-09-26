package juitar.sweet_charm_o_mine.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端专用数据存储类
 */
@OnlyIn(Dist.CLIENT)
public class ClientData {
    
    /**
     * 缩放模式枚举
     */
    public enum ZoomMode {
        OFF(0, 1.0f),      // 关闭
        ZOOM_4X(1, 0.25f), // 4倍缩放
        ZOOM_8X(2, 0.125f); // 8倍缩放
        
        private final int id;
        private final float fovMultiplier;
        
        ZoomMode(int id, float fovMultiplier) {
            this.id = id;
            this.fovMultiplier = fovMultiplier;
        }
        
        public int getId() {
            return id;
        }
        
        public float getFovMultiplier() {
            return fovMultiplier;
        }
        
        public ZoomMode next() {
            switch (this) {
                case OFF: return ZOOM_4X;
                case ZOOM_4X: return ZOOM_8X;
                case ZOOM_8X: return OFF;
                default: return OFF;
            }
        }
    }
    
    /**
     * 当前缩放模式
     */
    private static ZoomMode currentZoomMode = ZoomMode.OFF;
    
    /**
     * 当前是否正在缩放（潜行时）
     */
    private static boolean currentlyZooming = false;
    
    /**
     * 切换缩放模式（Z键）
     * 循环：关闭 -> 4倍 -> 8倍 -> 关闭
     * @return 新的缩放模式
     */
    public static ZoomMode toggleZoomMode() {
        currentZoomMode = currentZoomMode.next();
        // 保存配置
        ClientConfig.save();
        return currentZoomMode;
    }
    
    /**
     * 获取当前缩放模式
     * @return 当前缩放模式
     */
    public static ZoomMode getCurrentZoomMode() {
        return currentZoomMode;
    }
    
    /**
     * 设置缩放模式（用于配置加载）
     * @param mode 缩放模式
     */
    public static void setZoomMode(ZoomMode mode) {
        currentZoomMode = mode;
    }
    
    /**
     * 获取当前是否正在缩放（用于FOV和覆盖层）
     * @return 当前缩放状态
     */
    public static boolean isCurrentlyZooming() {
        return currentlyZooming && currentZoomMode != ZoomMode.OFF;
    }
    
    /**
     * 设置当前缩放状态（由潜行逻辑调用）
     * @param zooming 缩放状态
     */
    public static void setCurrentlyZooming(boolean zooming) {
        // 只有在缩放模式不是OFF时才允许缩放
        if (currentZoomMode != ZoomMode.OFF) {
            currentlyZooming = zooming;
        } else {
            currentlyZooming = false;
        }
    }
    
    /**
     * 获取当前FOV倍数
     * @return FOV倍数
     */
    public static float getCurrentFovMultiplier() {
        if (isCurrentlyZooming()) {
            return currentZoomMode.getFovMultiplier();
        }
        return 1.0f;
    }
    
    // 兼容性方法，保持旧的API
    @Deprecated
    public static boolean toggleSniperZoom() {
        toggleZoomMode();
        return currentZoomMode != ZoomMode.OFF;
    }
    
    @Deprecated
    public static boolean isSniperZoomEnabled() {
        return isCurrentlyZooming();
    }
    
    @Deprecated
    public static void setSniperZoomEnabled(boolean enabled) {
        setCurrentlyZooming(enabled);
    }
    
    @Deprecated
    public static boolean isSniperZoomFeatureEnabled() {
        return currentZoomMode != ZoomMode.OFF;
    }
}
