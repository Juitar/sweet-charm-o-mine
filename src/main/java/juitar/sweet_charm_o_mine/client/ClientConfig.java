package juitar.sweet_charm_o_mine.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import juitar.sweet_charm_o_mine.SweetCharm;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 客户端配置管理类
 * 处理缩放模式的持久化存储
 */
@OnlyIn(Dist.CLIENT)
public class ClientConfig {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "sweet_charm_o_mine_client.json";
    
    /**
     * 配置数据类
     */
    public static class ConfigData {
        public int zoomMode = 0; // 默认关闭 (OFF = 0, ZOOM_4X = 1, ZOOM_8X = 2)
        
        public ConfigData() {}
        
        public ConfigData(int zoomMode) {
            this.zoomMode = zoomMode;
        }
    }
    
    private static ConfigData configData = new ConfigData();
    
    /**
     * 获取配置文件路径
     */
    private static File getConfigFile() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File configDir = new File(gameDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, CONFIG_FILE_NAME);
    }
    
    /**
     * 加载配置
     */
    public static void load() {
        File configFile = getConfigFile();
        
        if (!configFile.exists()) {
            // 配置文件不存在，使用默认配置并保存
            save();
            SweetCharm.LOG.info("创建新的客户端配置文件: {}", configFile.getAbsolutePath());
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            ConfigData loadedData = GSON.fromJson(reader, ConfigData.class);
            if (loadedData != null) {
                configData = loadedData;
                
                // 应用加载的配置到ClientData
                ClientData.ZoomMode mode = ClientData.ZoomMode.values()[Math.max(0, Math.min(configData.zoomMode, ClientData.ZoomMode.values().length - 1))];
                ClientData.setZoomMode(mode);
                
                SweetCharm.LOG.info("加载客户端配置: 缩放模式 = {}", mode);
            }
        } catch (IOException e) {
            SweetCharm.LOG.error("加载客户端配置失败", e);
        } catch (Exception e) {
            SweetCharm.LOG.error("解析客户端配置失败，使用默认配置", e);
            configData = new ConfigData();
            save();
        }
    }
    
    /**
     * 保存配置
     */
    public static void save() {
        // 从ClientData获取当前配置
        configData.zoomMode = ClientData.getCurrentZoomMode().getId();
        
        File configFile = getConfigFile();
        
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(configData, writer);
            SweetCharm.LOG.debug("保存客户端配置: 缩放模式 = {}", ClientData.getCurrentZoomMode());
        } catch (IOException e) {
            SweetCharm.LOG.error("保存客户端配置失败", e);
        }
    }
    
    /**
     * 获取当前配置数据
     */
    public static ConfigData getConfigData() {
        return configData;
    }
}