package net.gmsgarcia.decor4all.menu;

import java.util.ArrayList;
import java.util.List;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.gmsgarcia.decor4all.recipe.CarpenterTableRecipe;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class CarpenterTableMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;

    public static final int RESULT_SLOT = 1;

    private static final int PLAYER_SLOT_START = 2;

    private static final int PLAYER_SLOT_END = 38;

    private static final int PLAYER_MAIN_END = PLAYER_SLOT_START + 27;

    private long lastTakeTime = Long.MIN_VALUE;

    private final ContainerLevelAccess access;

    private final SimpleContainer input;

    private final ResultContainer output;

    final Slot inputSlot;

    final Slot resultSlot;

    private List<CarpenterTableRecipe> visibleRecipes = List.of();

    private int selectedIndex = -1;

    private final DataSlot selectedRecipeSlot = new DataSlot() {
        @Override
        public int get() {
            return selectedIndex;
        }

        @Override
        public void set(int value) {
            selectedIndex = value;
        }
    };

    private ItemStack lastInputItem = ItemStack.EMPTY;

    private ServerPlayer viewer;

    public CarpenterTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public CarpenterTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(Decor4All.carpenterTableMenuType(), containerId);
        this.access = access;

        this.input = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                CarpenterTableMenu.this.slotsChanged(this);
            }
        };

        this.output = new ResultContainer();

        this.inputSlot = addSlot(new Slot(this.input, 0, 20, 33));
        this.resultSlot = addSlot(new ResultSlot(this.output, 143, 33));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlot(selectedRecipeSlot);
    }

    public void setViewer(ServerPlayer player) {
        this.viewer = player;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Decor4All.block(DecorBlocks.CARPENTER_TABLE_ENTRY));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (isValidSelection(id)) {
            selectedIndex = id;
            populateResult();
        }
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != this.input) {
            return;
        }
        ItemStack stack = this.inputSlot.getItem();
        if (stack.getItem() == lastInputItem.getItem()) {
            return;
        }
        lastInputItem = stack.copy();
        refreshRecipes();
        syncRecipeList();
    }

    private void refreshRecipes() {
        visibleRecipes = List.of();
        selectedIndex = -1;
        this.resultSlot.set(ItemStack.EMPTY);

        Level level = level();
        if (level == null || !(level.recipeAccess() instanceof RecipeManager manager)) {
            return;
        }

        ItemStack stack = this.inputSlot.getItem();
        if (stack.isEmpty()) {
            return;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(stack);
        List<CarpenterTableRecipe> matches = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof CarpenterTableRecipe recipe && recipe.matches(recipeInput, level)) {
                matches.add(recipe);
            }
        }
        visibleRecipes = List.copyOf(matches);
    }

    private void populateResult() {
        Level level = level();
        if (isValidSelection(selectedIndex) && level != null) {
            CarpenterTableRecipe recipe = visibleRecipes.get(selectedIndex);
            this.resultSlot.set(recipe.craft(this.inputSlot.getItem(), level.registryAccess()));
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }
        broadcastFullState();
    }

    private boolean isValidSelection(int index) {
        return index >= 0 && index < visibleRecipes.size();
    }

    private Level level() {
        return access.evaluate((level, pos) -> level, (Level) null);
    }

    private void syncRecipeList() {
        if (viewer == null || !viewer.connection.isAcceptingMessages()) {
            return;
        }
        viewer.connection.send(new ClientboundCustomPayloadPacket(
                new CarpenterTableRecipesPayload(containerId, resultsForClient())));
    }

    private List<ItemStack> resultsForClient() {
        Level level = level();
        if (level == null) {
            return List.of();
        }
        ItemStack input = this.inputSlot.getItem();
        List<ItemStack> stacks = new ArrayList<>(visibleRecipes.size());
        for (CarpenterTableRecipe recipe : visibleRecipes) {
            stacks.add(recipe.craft(input, level.registryAccess()));
        }
        return stacks;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != output;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_SLOT) {
            stack.onCraftedBy(player, stack.getCount());
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(original, stack);
        } else if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (level() == null) {
            return ItemStack.EMPTY;
        } else if (isIngredient(stack)) {
            if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_SLOT_START && index < PLAYER_MAIN_END) {
            if (!moveItemStackTo(stack, PLAYER_MAIN_END, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_MAIN_END && index < PLAYER_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_MAIN_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }
        slot.setChanged();
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        broadcastFullState();
        return original;
    }

    private boolean isIngredient(ItemStack stack) {
        Level level = level();
        if (level == null || stack.isEmpty() || !(level.recipeAccess() instanceof RecipeManager manager)) {
            return false;
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(stack);
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof CarpenterTableRecipe recipe && recipe.matches(recipeInput, level)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.output.clearContent();
        this.viewer = null;
        access.execute((level, pos) -> clearContainer(player, this.input));
    }

    private class ResultSlot extends Slot {

        ResultSlot(ResultContainer container, int x, int y) {
            super(container, 1, x, y);
        }

        @Override
        public boolean isFake() {
            return true;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            return getItem().copyWithCount(amount);
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
        }

        @Override
        public void onTake(Player player, ItemStack taken) {
            ItemStack input = inputSlot.remove(1);
            if (!input.isEmpty()) {
                populateResult();
            }

            taken.onCraftedBy(player, taken.getCount());

            access.execute((level, pos) -> {
                long time = level.getGameTime();
                if (lastTakeTime != time) {
                    level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    lastTakeTime = time;
                }
            });
        }
    }
}
