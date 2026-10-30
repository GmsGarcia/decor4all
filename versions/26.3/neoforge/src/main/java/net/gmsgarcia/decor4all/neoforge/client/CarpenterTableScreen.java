package net.gmsgarcia.decor4all.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.List;
import net.gmsgarcia.decor4all.menu.CarpenterTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CarpenterTableScreen extends AbstractContainerScreen<CarpenterTableMenu> {

    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/stonecutter.png");

    private static final Identifier SCROLLER =
            Identifier.withDefaultNamespace("container/stonecutter/scroller");

    private static final Identifier SCROLLER_DISABLED =
            Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");

    private static final Identifier RECIPE =
            Identifier.withDefaultNamespace("container/stonecutter/recipe");

    private static final Identifier RECIPE_HIGHLIGHTED =
            Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");

    private static final Identifier RECIPE_SELECTED =
            Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");

    private static final int COLUMNS = 4;
    private static final int ROWS = 3;

    private static final int PER_PAGE = COLUMNS * ROWS;

    private static final int CELL_WIDTH = 16;
    private static final int CELL_HEIGHT = 18;

    private static final int CELL_FRAME_INSET = 1;
    private static final int CELL_ITEM_INSET = 2;

    private static final int GRID_X = 52;
    private static final int GRID_Y = 14;

    private static final int SCROLLBAR_X = 119;
    private static final int SCROLLBAR_TOP = 15;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;

    private static final int SCROLLBAR_TRACK = 54;

    private static final int SCROLLBAR_HIT_TOP = 9;

    private static final int SCROLLBAR_DRAG_TOP = 14;

    private static final int SCROLLBAR_TRAVEL = 41;

    private float scroll;

    private boolean dragging;

    private int lastResultCount = -1;

    public CarpenterTableScreen(CarpenterTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
        this.titleLabelY = 5;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        syncScrollToList();

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scrollbarSprite(),
                scrollbarX(), scrollbarY(), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        if (isHovering(SCROLLBAR_X, SCROLLBAR_TOP, SCROLLBAR_WIDTH, SCROLLBAR_TRACK, mouseX, mouseY)) {
            graphics.requestCursor(isScrollBarActive()
                    ? (this.dragging ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND)
                    : CursorTypes.NOT_ALLOWED);
        }

        List<ItemStack> results = results();
        int first = firstVisible();
        int originX = leftPos + GRID_X;
        int originY = topPos + GRID_Y;
        int selected = this.menu.getSelectedIndex();

        for (int cell = 0; cell < PER_PAGE; cell++) {
            int index = first + cell;
            if (index >= results.size()) {
                break;
            }
            int x = originX + cell % COLUMNS * CELL_WIDTH;
            int y = originY + cell / COLUMNS * CELL_HEIGHT;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, cellSprite(index, selected, cellAt(mouseX, mouseY)),
                    x, y + CELL_FRAME_INSET, CELL_WIDTH, CELL_HEIGHT);
            graphics.item(results.get(index), x, y + CELL_ITEM_INSET);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        int cell = cellAt(mouseX, mouseY);
        List<ItemStack> results = results();
        int index = firstVisible() + cell;
        if (cell >= 0 && index < results.size()) {
            graphics.setTooltipForNextFrame(this.font, results.get(index), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        if (isOverScrollbar(event.x(), event.y())) {
            this.dragging = true;
        }

        int cell = cellAt(event.x(), event.y());
        List<ItemStack> results = results();
        int index = firstVisible() + cell;
        if (cell >= 0 && index < results.size()) {
            this.minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.dragging && isScrollBarActive()) {
            scrollToPointer(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        int rows = offscreenRows();
        if (rows <= 0) {
            return false;
        }
        this.scroll = Mth.clamp(this.scroll - (float) scrollY / rows, 0.0F, 1.0F);
        return true;
    }

    @Override
    public void removed() {
        CarpenterTableClientRecipes.forget(this.menu.containerId);
        super.removed();
    }

    private List<ItemStack> results() {
        return CarpenterTableClientRecipes.forMenu(this.menu.containerId);
    }

    private int offscreenRows() {
        int rows = (results().size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - ROWS);
    }

    private boolean isScrollBarActive() {
        return results().size() > PER_PAGE;
    }

    private int firstVisible() {
        return Mth.clamp((int) (this.scroll * offscreenRows() + 0.5F), 0, offscreenRows()) * COLUMNS;
    }

    private void syncScrollToList() {
        int size = results().size();
        if (size != this.lastResultCount) {
            this.lastResultCount = size;
            this.scroll = 0.0F;
        }
    }

    private int cellAt(double mouseX, double mouseY) {
        if (!isHovering(GRID_X, GRID_Y + CELL_ITEM_INSET, COLUMNS * CELL_WIDTH, ROWS * CELL_HEIGHT,
                mouseX, mouseY)) {
            return -1;
        }
        int column = Mth.clamp((int) (mouseX - leftPos - GRID_X) / CELL_WIDTH, 0, COLUMNS - 1);
        int row = Mth.clamp((int) (mouseY - topPos - GRID_Y - CELL_ITEM_INSET) / CELL_HEIGHT, 0, ROWS - 1);
        return row * COLUMNS + column;
    }

    private Identifier cellSprite(int index, int selected, int hovered) {
        if (index == selected) {
            return RECIPE_SELECTED;
        }
        if (hovered >= 0 && index == firstVisible() + hovered) {
            return RECIPE_HIGHLIGHTED;
        }
        return RECIPE;
    }

    private Identifier scrollbarSprite() {
        return isScrollBarActive() ? SCROLLER : SCROLLER_DISABLED;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return isHovering(SCROLLBAR_X, SCROLLBAR_HIT_TOP, SCROLLBAR_WIDTH, SCROLLBAR_TRACK, mouseX, mouseY);
    }

    private int scrollbarX() {
        return leftPos + SCROLLBAR_X;
    }

    private int scrollbarY() {
        return topPos + SCROLLBAR_TOP + (int) (SCROLLBAR_TRAVEL * this.scroll);
    }

    private void scrollToPointer(double mouseY) {
        int trackTop = topPos + SCROLLBAR_DRAG_TOP;
        this.scroll = Mth.clamp(((float) mouseY - trackTop - SCROLLBAR_HEIGHT / 2.0F)
                / (SCROLLBAR_TRACK - SCROLLBAR_HEIGHT), 0.0F, 1.0F);
    }
}
