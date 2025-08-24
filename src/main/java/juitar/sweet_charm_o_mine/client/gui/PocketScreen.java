package juitar.sweet_charm_o_mine.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.PocketContainer;
import juitar.sweet_charm_o_mine.items.PocketItem;
import juitar.sweet_charm_o_mine.items.PocketType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PocketScreen extends AbstractContainerScreen<PocketContainer> {
    // 不同材质的纹理文件
    private static final ResourceLocation[] TEXTURES = {
        SweetCharm.resource("textures/gui/copper_pocket.png"),    // 铜
        SweetCharm.resource("textures/gui/iron_pocket.png"),           // 铁 
        SweetCharm.resource("textures/gui/gold_pocket.png"),           // 金 
        SweetCharm.resource("textures/gui/diamond_pocket.png"),           // 钻石 
        SweetCharm.resource("textures/gui/netherite_pocket.png")            // 下界合金 
    };
    
    // GUI元素的尺寸常量
    private static final int TOP_HEIGHT = 17;      // 顶部边框高度
    private static final int BOTTOM_HEIGHT = 95;   // 底部边框高度
    private static final int SLOT_SIZE = 18;       // 槽位大小
    private static final int MATERIAL_ICON_SIZE = 20; // 材质图标大小
    
    // 材质类型图标的纹理坐标
    private static final int[][] MATERIAL_ICON_UV = {
        {176, 18},  // 铜
        {176, 38},  // 铁
        {176, 58},  // 金
        {176, 78},  // 钻石
        {176, 98}   // 下界合金
    };
    
    private final PocketType pocketType;
    private final int rows;
    private final int cols;
    private final ResourceLocation currentTexture;
    
    public PocketScreen(PocketContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        
        // 获取口袋类型和尺寸
        this.pocketType = container.getPocketType();
        this.rows = pocketType.getRows();
        this.cols = pocketType.getColumns();
        
        // 选择对应的纹理文件
        int textureIndex = Math.min(pocketType.ordinal(), TEXTURES.length - 1);
        this.currentTexture = TEXTURES[textureIndex];
        
        // 计算GUI尺寸
        this.imageWidth = 176;
        this.imageHeight = TOP_HEIGHT + (rows * SLOT_SIZE) + BOTTOM_HEIGHT;
        
        // 调整玩家物品栏标签位置
        this.inventoryLabelY = TOP_HEIGHT + (rows * SLOT_SIZE) + 4;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 简化渲染 - 直接绘制完整的GUI背景
        // 不分割纹理，直接使用完整的GUI区域
        guiGraphics.blit(currentTexture, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // 绘制材质类型提示
        int materialIconX = (this.width + this.imageWidth) / 2 - MATERIAL_ICON_SIZE - 7;
        int materialIconY = (this.height - this.imageHeight) / 2 + 6;
        if (mouseX >= materialIconX && mouseX < materialIconX + MATERIAL_ICON_SIZE &&
            mouseY >= materialIconY && mouseY < materialIconY + MATERIAL_ICON_SIZE) {
            
            Component materialName = Component.translatable("item.sweet_charm_o_mine." + pocketType.name().toLowerCase() + "_pocket");
            guiGraphics.renderTooltip(this.font, materialName, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 绘制标题
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        
        // 绘制背包标签
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 4210752, false);
        
    }
}
