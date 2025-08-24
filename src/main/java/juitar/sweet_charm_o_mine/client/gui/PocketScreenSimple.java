package juitar.sweet_charm_o_mine.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.PocketContainer;
import juitar.sweet_charm_o_mine.items.PocketType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PocketScreenSimple extends AbstractContainerScreen<PocketContainer> {
    private static final ResourceLocation COPPER_TEXTURE = SweetCharm.resource("textures/gui/copper_pocket.png");
    private static final ResourceLocation DEFAULT_TEXTURE = SweetCharm.resource("textures/gui/pocket.png");
    
    private final PocketType pocketType;
    private final int rows;
    private final int cols;
    
    public PocketScreenSimple(PocketContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        
        this.pocketType = container.getPocketType();
        this.rows = pocketType.getRows();
        this.cols = pocketType.getColumns();
        
        // 简单的GUI尺寸计算
        this.imageWidth = 176;
        this.imageHeight = 17 + (rows * 18) + 95; // 顶部 + 槽位 + 底部
        
        this.inventoryLabelY = 17 + (rows * 18) + 4;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // 选择纹理
        ResourceLocation texture = (pocketType == PocketType.COPPER) ? COPPER_TEXTURE : DEFAULT_TEXTURE;
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 简单地绘制整个GUI背景
        guiGraphics.blit(texture, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 绘制标题
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        
        // 绘制背包标签
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 4210752, false);
    }
}