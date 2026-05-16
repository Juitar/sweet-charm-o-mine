package juitar.sweet_charm_o_mine.client.gui;

import juitar.sweet_charm_o_mine.items.AmmoChainContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AmmoChainScreen extends AbstractContainerScreen<AmmoChainContainer> {
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
    private static final int LIST_SLOT_X = 14;
    private static final int LIST_SLOT_Y = 34;
    private static final int LIST_ROW_HEIGHT = 24;
    private static final int SELECTED_GUN_X = 74;
    private static final int SELECTED_GUN_Y = 42;
    private static final int CHAIN_SLOT_X = 102;
    private static final int CHAIN_SLOT_Y = 42;
    private static final int CHAIN_COLUMN_STEP = 22;
    private static final int POCKET_AMMO_X = 74;
    private static final int POCKET_AMMO_Y = 100;
    private static final int PLAYER_INVENTORY_X = 47;
    private static final int PLAYER_INVENTORY_Y = 162;
    private static final int VANILLA_INVENTORY_U = 7;
    private static final int VANILLA_INVENTORY_V = 83;
    private static final int VANILLA_INVENTORY_WIDTH = 162;
    private static final int VANILLA_INVENTORY_HEIGHT = 76;
    private static final int VANILLA_SLOT_U = 7;
    private static final int VANILLA_SLOT_V = 83;
    private static final int VANILLA_SLOT_SIZE = 18;

    public AmmoChainScreen(AmmoChainContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 248;
        this.inventoryLabelX = PLAYER_INVENTORY_X;
        this.inventoryLabelY = 150;
    }

    @Override
    protected void init() {
        super.init();

        addMenuButton(this.leftPos + 12, this.topPos + 112, 16, 12, Component.literal("<"), AmmoChainContainer.PAGE_PREVIOUS_BUTTON);
        addMenuButton(this.leftPos + 32, this.topPos + 112, 16, 12, Component.literal(">"), AmmoChainContainer.PAGE_NEXT_BUTTON);
        addMenuButton(this.leftPos + 206, this.topPos + 103, 16, 12, Component.literal("<"), AmmoChainContainer.POCKET_PAGE_PREVIOUS_BUTTON);
        addMenuButton(this.leftPos + 224, this.topPos + 103, 16, 12, Component.literal(">"), AmmoChainContainer.POCKET_PAGE_NEXT_BUTTON);

        for (int row = 0; row < AmmoChainContainer.ROWS; row++) {
            int x = this.leftPos + CHAIN_SLOT_X + row * CHAIN_COLUMN_STEP;
            int y = this.topPos + CHAIN_SLOT_Y;
            addMenuButton(x - 1, y + 34, 10, 12, Component.literal("-"), row);
            addMenuButton(x + 9, y + 34, 10, 12, Component.literal("+"), 20 + row);
        }
    }

    private void addMenuButton(int x, int y, int width, int height, Component label, int id) {
        this.addRenderableWidget(Button.builder(label, button -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            }
        }).bounds(x, y, width, height).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int row = 0; row < AmmoChainContainer.LIST_ROWS; row++) {
            int x = this.leftPos + 10;
            int y = this.topPos + LIST_SLOT_Y + row * LIST_ROW_HEIGHT - 1;
            if (mouseX >= x && mouseX < x + 40 && mouseY >= y && mouseY < y + LIST_ROW_HEIGHT) {
                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    int id = button == 1 ? AmmoChainContainer.DELETE_LIST_BUTTON + row : AmmoChainContainer.SELECT_LIST_BUTTON + row;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight, true);
        drawPanel(guiGraphics, x + 8, y + 18, 44, 116, false);
        drawPanel(guiGraphics, x + 66, y + 18, 182, 116, false);
        drawPlayerInventory(guiGraphics, x + PLAYER_INVENTORY_X - 1, y + PLAYER_INVENTORY_Y - 1);

        int selectedRow = this.menu.getSelectedListRow();
        for (int row = 0; row < AmmoChainContainer.LIST_ROWS; row++) {
            int rowY = y + LIST_SLOT_Y + row * LIST_ROW_HEIGHT;
            drawRow(guiGraphics, x + 10, rowY - 1, 40, 19, row == selectedRow);
            if (row == selectedRow) {
                guiGraphics.fill(x + 11, rowY, x + 34, rowY + 18, 0x33FFFFFF);
            }
            drawSlot(guiGraphics, x + LIST_SLOT_X, rowY);
        }

        drawRow(guiGraphics, x + 70, y + SELECTED_GUN_Y - 1, 28, 19, true);
        drawSlot(guiGraphics, x + SELECTED_GUN_X, y + SELECTED_GUN_Y);
        for (int row = 0; row < AmmoChainContainer.ROWS; row++) {
            int slotX = x + CHAIN_SLOT_X + row * CHAIN_COLUMN_STEP;
            drawRow(guiGraphics, slotX - 3, y + CHAIN_SLOT_Y - 1, 23, 19, false);
            drawSlot(guiGraphics, slotX, y + CHAIN_SLOT_Y);
        }
        for (int i = 0; i < AmmoChainContainer.POCKET_AMMO_SLOTS; i++) {
            drawSlot(guiGraphics, x + POCKET_AMMO_X + i * CHAIN_COLUMN_STEP, y + POCKET_AMMO_Y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("label.sweet_charm_o_mine.ammo_belt.configs"), 12, 20, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("label.sweet_charm_o_mine.ammo_belt.gun"), 72, 30, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("label.sweet_charm_o_mine.ammo_belt.belt"), 102, 30, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("label.sweet_charm_o_mine.ammo_belt.pocket"), 74, 90, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        int pages = Math.max(1, (this.menu.getConfiguredCount() + AmmoChainContainer.LIST_ROWS - 1) / AmmoChainContainer.LIST_ROWS);
        String pageText = (this.menu.getListPage() + 1) + "/" + pages;
        guiGraphics.drawCenteredString(this.font, pageText, 30, 100, 0xFFFFFF);

        int pocketPages = Math.max(1, (this.menu.getPocketAmmoCount() + AmmoChainContainer.POCKET_AMMO_SLOTS - 1) / AmmoChainContainer.POCKET_AMMO_SLOTS);
        String pocketPageText = (this.menu.getPocketAmmoPage() + 1) + "/" + pocketPages;
        guiGraphics.drawCenteredString(this.font, pocketPageText, 224, 91, 0xFFFFFF);

        for (int row = 0; row < AmmoChainContainer.ROWS; row++) {
            guiGraphics.drawCenteredString(this.font, Integer.toString(this.menu.getShotCount(row)), CHAIN_SLOT_X + row * CHAIN_COLUMN_STEP + 8, 67, 0xFFFFFF);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean outer) {
        int border = outer ? 0xFF5A5A5A : 0xFF7A7A7A;
        int topLeft = outer ? 0xFFE0E0E0 : 0xFFD0D0D0;
        int bottomRight = outer ? 0xFF8B8B8B : 0xFF777777;
        int fill = outer ? 0xFFC6C6C6 : 0xFFB8B8B8;

        guiGraphics.fill(x, y, x + width, y + height, border);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, topLeft);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, topLeft);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, bottomRight);
        guiGraphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, bottomRight);
    }

    private void drawPlayerInventory(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(
                INVENTORY_TEXTURE,
                x,
                y,
                VANILLA_INVENTORY_U,
                VANILLA_INVENTORY_V,
                VANILLA_INVENTORY_WIDTH,
                VANILLA_INVENTORY_HEIGHT
        );
    }

    private void drawRow(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean selected) {
        int fill = selected ? 0xFFE2D28A : 0xFFA9A9A9;
        int edge = selected ? 0xFF8E7A2A : 0xFF858585;
        guiGraphics.fill(x, y, x + width, y + height, edge);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(INVENTORY_TEXTURE, x - 1, y - 1, VANILLA_SLOT_U, VANILLA_SLOT_V, VANILLA_SLOT_SIZE, VANILLA_SLOT_SIZE);
    }
}
