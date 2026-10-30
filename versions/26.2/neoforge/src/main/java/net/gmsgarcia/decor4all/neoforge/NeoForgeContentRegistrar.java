package net.gmsgarcia.decor4all.neoforge;

import java.util.Set;
import java.util.function.Supplier;
import net.gmsgarcia.decor4all.ContentRegistrar;
import net.gmsgarcia.decor4all.Decor4All;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

final class NeoForgeContentRegistrar implements ContentRegistrar {

    private final DeferredRegister<Block> blocks =
            DeferredRegister.create(Registries.BLOCK, Decor4All.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypes =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Decor4All.MOD_ID);
    private final DeferredRegister<Item> items =
            DeferredRegister.create(Registries.ITEM, Decor4All.MOD_ID);
    private final DeferredRegister<CreativeModeTab> tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Decor4All.MOD_ID);
    private final DeferredRegister<EntityType<?>> entityTypes =
            DeferredRegister.create(Registries.ENTITY_TYPE, Decor4All.MOD_ID);
    private final DeferredRegister<MenuType<?>> menuTypes =
            DeferredRegister.create(Registries.MENU, Decor4All.MOD_ID);
    private final DeferredRegister<RecipeType<?>> recipeTypes =
            DeferredRegister.create(Registries.RECIPE_TYPE, Decor4All.MOD_ID);
    private final DeferredRegister<RecipeSerializer<?>> recipeSerializers =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Decor4All.MOD_ID);

    @Override
    public void block(String path, ResourceKey<Block> key, Supplier<Block> factory) {
        blocks.register(path, factory);
    }

    @Override
    public void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks) {
        blockEntityTypes.register(path, () ->
                new BlockEntityType<>(factory, Set.of(validBlocks.get())));
    }

    @Override
    public void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory) {
        items.register(path, factory);
    }

    @Override
    public void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory) {
        tabs.register(path, factory);
    }

    @Override
    public void menuType(String path, ResourceKey<MenuType<?>> key, Supplier<MenuType<?>> factory) {
        menuTypes.register(path, factory);
    }

    @Override
    public void entityType(String path, ResourceKey<EntityType<?>> key, Supplier<EntityType<?>> factory) {
        entityTypes.register(path, factory);
    }

    @Override
    public void recipeType(String path, ResourceKey<RecipeType<?>> key, Supplier<RecipeType<?>> factory) {
        recipeTypes.register(path, factory);
    }

    @Override
    public void recipeSerializer(String path, ResourceKey<RecipeSerializer<?>> key,
            Supplier<RecipeSerializer<?>> factory) {
        recipeSerializers.register(path, factory);
    }

    void attach(IEventBus eventBus) {
        blocks.register(eventBus);
        blockEntityTypes.register(eventBus);
        items.register(eventBus);
        tabs.register(eventBus);
        menuTypes.register(eventBus);
        entityTypes.register(eventBus);
        recipeTypes.register(eventBus);
        recipeSerializers.register(eventBus);
    }
}
