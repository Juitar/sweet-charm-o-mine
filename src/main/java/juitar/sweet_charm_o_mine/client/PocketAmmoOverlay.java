package juitar.sweet_charm_o_mine.client;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.sweet_charm_o_mine.SweetCharm;
import juitar.sweet_charm_o_mine.items.BulletManager;
import juitar.sweet_charm_o_mine.items.PocketItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SweetCharm.MODID, value = Dist.CLIENT)
public class PocketAmmoOverlay {
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/widgets.png");
    private static final long VISIBLE_MS = 2200L;
    private static final long ANIMATION_MS = 240L;
    private static final int HOTBAR_SLOT_TEXTURE_WIDTH = 22;
    private static final int HOTBAR_SLOT_TEXTURE_HEIGHT = 22;
    private static final int HOTBAR_SELECTED_WIDTH = 24;
    private static final int HOTBAR_SELECTED_HEIGHT = 22;
    private static final int BASE_CENTER_SLOT_SIZE = 30;
    private static final int BASE_SIDE_SLOT_SIZE = 23;
    private static final int BASE_EXIT_SLOT_SIZE = 19;
    private static final int BASE_SLOT_SPACING = 29;

    private static long visibleUntil = 0L;
    private static long animationStart = 0L;
    private static int animationDirection = 0;

    public static void show(int direction) {
        long now = System.currentTimeMillis();
        visibleUntil = now + VISIBLE_MS;
        animationStart = now;
        animationDirection = direction < 0 ? -1 : 1;
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (System.currentTimeMillis() > visibleUntil) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        BulletManager.getEquippedPocket(mc.player)
                .ifPresent(pocketStack -> render(event.getGuiGraphics(), mc, pocketStack));
    }

    private static void render(GuiGraphics guiGraphics, Minecraft mc, ItemStack pocketStack) {
        List<PocketItem.AmmoEntry> entries = PocketItem.getDistinctAmmoEntries(pocketStack);
        if (entries.isEmpty()) {
            return;
        }

        ItemStack selected = PocketItem.getSelectedAmmoTemplate(pocketStack);
        int selectedIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (!selected.isEmpty() && ItemStack.isSameItemSameTags(entries.get(i).stack(), selected)) {
                selectedIndex = i;
                break;
            }
        }

        float progress = easeInOutCubic(getAnimationProgress());
        int step = animationDirection == 0 ? 0 : animationDirection;
        OverlayLayout layout = getLayout(mc);

        for (int finalOffset = -2; finalOffset <= 2; finalOffset++) {
            int initialOffset = finalOffset - step;
            float position = lerp(initialOffset, finalOffset, progress);
            if (Math.abs(position) > 1.65F) {
                continue;
            }

            PocketItem.AmmoEntry entry = entries.get(Math.floorMod(selectedIndex - finalOffset, entries.size()));
            int slotSize = Math.round(slotSizeForPosition(position, layout));
            float y = layout.centerY + position * layout.slotSpacing;
            float selectedAlpha = selectedAlpha(finalOffset, step, progress);

            renderVanillaHotbarSlot(guiGraphics, layout.centerX, y, slotSize, layout.exitSlotSize,
                    alphaForPosition(position), selectedAlpha);
            renderAmmoItem(guiGraphics, entry.stack(), layout.centerX, y, slotSize, layout.centerSlotSize);

            if (selectedAlpha > 0.55F) {
                drawCount(guiGraphics, mc.font, Integer.toString(entry.count()), layout.centerX + slotSize / 2 - 3,
                        Math.round(y + slotSize / 2 - 9));
            }
        }
    }

    private static float getAnimationProgress() {
        if (animationDirection == 0) {
            return 1.0F;
        }
        long elapsed = System.currentTimeMillis() - animationStart;
        return Math.min(1.0F, Math.max(0.0F, elapsed / (float) ANIMATION_MS));
    }

    private static float easeInOutCubic(float progress) {
        return progress < 0.5F
                ? 4.0F * progress * progress * progress
                : 1.0F - (float) Math.pow(-2.0F * progress + 2.0F, 3.0F) / 2.0F;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    private static float slotSizeForPosition(float position, OverlayLayout layout) {
        float distance = Math.min(1.0F, Math.abs(position));
        return lerp(layout.centerSlotSize, layout.sideSlotSize, distance);
    }

    private static float alphaForPosition(float position) {
        float distance = Math.abs(position);
        if (distance <= 1.0F) {
            return lerp(1.0F, 0.78F, distance);
        }
        return Math.max(0.0F, lerp(0.78F, 0.0F, Math.min(1.0F, distance - 1.0F)));
    }

    private static float selectedAlpha(int finalOffset, int step, float progress) {
        if (step == 0) {
            return finalOffset == 0 ? 1.0F : 0.0F;
        }
        if (finalOffset == 0) {
            return progress;
        }
        if (finalOffset == step) {
            return 1.0F - progress;
        }
        return 0.0F;
    }

    private static void renderVanillaHotbarSlot(GuiGraphics guiGraphics, int centerX, float centerY, int size,
            int exitSlotSize, float alpha, float selectedAlpha) {
        renderTexturedWidget(guiGraphics, centerX, centerY, size, size, HOTBAR_SLOT_TEXTURE_WIDTH,
                HOTBAR_SLOT_TEXTURE_HEIGHT, 0, 0, alpha);
        if (selectedAlpha > 0.0F) {
            renderTexturedWidget(guiGraphics, centerX, centerY, Math.max(size + 4, exitSlotSize), HOTBAR_SELECTED_WIDTH,
                    HOTBAR_SELECTED_HEIGHT, 0, 22, selectedAlpha);
        }
    }

    private static void renderTexturedWidget(GuiGraphics guiGraphics, int centerX, float centerY, int targetWidth,
            int targetHeight, int textureWidth, int textureHeight, int u, int v, float alpha) {
        if (alpha <= 0.0F) {
            return;
        }

        int x = centerX - targetWidth / 2;
        int y = Math.round(centerY - targetHeight / 2.0F);
        float scaleX = targetWidth / (float) textureWidth;
        float scaleY = targetHeight / (float) textureHeight;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scaleX, scaleY, 1.0F);
        guiGraphics.blit(WIDGETS_TEXTURE, 0, 0, u, v, textureWidth, textureHeight);
        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void renderTexturedWidget(GuiGraphics guiGraphics, int centerX, float centerY, int size,
            int textureWidth, int textureHeight, int u, int v, float alpha) {
        if (alpha <= 0.0F) {
            return;
        }

        int x = centerX - size / 2;
        int y = Math.round(centerY - size / 2);
        float scale = size / (float) textureWidth;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.blit(WIDGETS_TEXTURE, 0, 0, u, v, textureWidth, textureHeight);
        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void renderAmmoItem(GuiGraphics guiGraphics, ItemStack stack, int centerX, float centerY,
            int slotSize, int centerSlotSize) {
        int itemX = centerX - 8;
        int itemY = Math.round(centerY - 8);
        float scale = slotSize / (float) centerSlotSize * 1.05F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(itemX + 8, itemY + 8, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.renderItem(stack, -8, -8);
        guiGraphics.pose().popPose();
    }

    private static void drawCount(GuiGraphics guiGraphics, Font font, String text, int right, int y) {
        int x = right - font.width(text);
        guiGraphics.drawString(font, text, x + 1, y + 1, 0xFF000000, false);
        guiGraphics.drawString(font, text, x, y, 0xFFFFFFFF, false);
    }

    private static OverlayLayout getLayout(Minecraft mc) {
        ClientConfig.ConfigData config = ClientConfig.getConfigData();
        float scale = Math.max(0.75F, Math.min(1.5F, config.ammoOverlayScale));
        int centerSlotSize = Math.round(BASE_CENTER_SLOT_SIZE * scale);
        int sideSlotSize = Math.round(BASE_SIDE_SLOT_SIZE * scale);
        int exitSlotSize = Math.round(BASE_EXIT_SLOT_SIZE * scale);
        int slotSpacing = Math.round(BASE_SLOT_SPACING * scale);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        String position = config.ammoOverlayPosition == null ? "right_lower" : config.ammoOverlayPosition;
        int centerX;
        int centerY;

        switch (position) {
            case "left_center":
                centerX = 8 + centerSlotSize / 2;
                centerY = screenHeight / 2;
                break;
            case "left_lower":
                centerX = 8 + centerSlotSize / 2;
                centerY = screenHeight - 86;
                break;
            case "right_center":
                centerX = screenWidth - 8 - centerSlotSize / 2;
                centerY = screenHeight / 2;
                break;
            case "right_lower":
            default:
                centerX = screenWidth - 66;
                centerY = screenHeight - 86;
                break;
        }

        centerX = Math.max(centerSlotSize / 2 + 4, Math.min(screenWidth - centerSlotSize / 2 - 4, centerX));
        centerY = Math.max(slotSpacing + centerSlotSize / 2 + 4,
                Math.min(screenHeight - slotSpacing - centerSlotSize / 2 - 4, centerY));
        return new OverlayLayout(centerX, centerY, centerSlotSize, sideSlotSize, exitSlotSize, slotSpacing);
    }

    private static class OverlayLayout {
        private final int centerX;
        private final int centerY;
        private final int centerSlotSize;
        private final int sideSlotSize;
        private final int exitSlotSize;
        private final int slotSpacing;

        private OverlayLayout(int centerX, int centerY, int centerSlotSize, int sideSlotSize, int exitSlotSize,
                int slotSpacing) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerSlotSize = centerSlotSize;
            this.sideSlotSize = sideSlotSize;
            this.exitSlotSize = exitSlotSize;
            this.slotSpacing = slotSpacing;
        }
    }
}
