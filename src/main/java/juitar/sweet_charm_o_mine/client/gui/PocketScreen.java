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
    private static final ResourceLocation TEXTURE = SweetCharm.resource("textures/gui/pocket.png");
    
    // GUI元素的尺寸常量
    private static final int TOP_HEIGHT = 17;      // 顶部边框高度
    private static final int BOTTOM_HEIGHT = 95;   // 底部边框高度(包含玩家物品栏)
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
    
    public PocketScreen(PocketContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        
        // 获取口袋类型和尺寸
        this.pocketType = container.getPocketType();
        this.rows = pocketType.getRows();
        this.cols = pocketType.getColumns();
        
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
        
        // 1. 绘制顶部边框
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, TOP_HEIGHT);
        
        // 2. 绘制中间部分(根据行数拉伸)
        int middleHeight = rows * SLOT_SIZE;
        for (int row = 0; row < rows; row++) {
            guiGraphics.blit(TEXTURE, x, y + TOP_HEIGHT + (row * SLOT_SIZE), 
                    0, 17, this.imageWidth, SLOT_SIZE);
        }
        
        // 3. 绘制底部边框(包含玩家物品栏)
        guiGraphics.blit(TEXTURE, x, y + TOP_HEIGHT + middleHeight, 
                0, 71, this.imageWidth, BOTTOM_HEIGHT);
        
        // 4. 绘制材质类型图标
        int materialIconX = x + this.imageWidth - MATERIAL_ICON_SIZE - 7;
        int materialIconY = y + 6;
        int materialIndex = Math.min(pocketType.ordinal(), MATERIAL_ICON_UV.length - 1);
        guiGraphics.blit(TEXTURE, materialIconX, materialIconY, 
                MATERIAL_ICON_UV[materialIndex][0], MATERIAL_ICON_UV[materialIndex][1], 
                MATERIAL_ICON_SIZE, MATERIAL_ICON_SIZE);
        
        // 5. 绘制所有物品槽
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int slotX = x + 7 + col * SLOT_SIZE;
                int slotY = y + TOP_HEIGHT + row * SLOT_SIZE;
                guiGraphics.blit(TEXTURE, slotX, slotY, 176, 0, SLOT_SIZE, SLOT_SIZE);
            }
        }
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
        
        // 绘制容量信息
        String capacityText = rows + "x" + cols;
        int capacityWidth = this.font.width(capacityText);
        guiGraphics.drawString(this.font, capacityText, this.imageWidth - capacityWidth - 30, 6, 4210752, false);
    }
}
