package net.gmsgarcia.decor4all;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

public final class VanillaRegistrar implements ContentRegistrar {

    @Override
    public void block(String path, ResourceKey<Block> key, Supplier<Block> factory) {
        Registry.register(BuiltInRegistries.BLOCK, key, factory.get());
    }

    @Override
    public void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key,
                new BlockEntityType<>(factory, Set.of(validBlocks.get())));
    }

    @Override
    public void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory) {
        Registry.register(BuiltInRegistries.ITEM, key, factory.get());
    }

    @Override
    public void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, factory.get());
    }

    @Override
    public void menuType(String path, ResourceKey<MenuType<?>> key, Supplier<MenuType<?>> factory) {
        Registry.register(BuiltInRegistries.MENU, key, factory.get());
    }

    @Override
    public void entityType(String path, ResourceKey<EntityType<?>> key, Supplier<EntityType<?>> factory) {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, key, factory.get());
    }

    @Override
    public void recipeType(String path, ResourceKey<RecipeType<?>> key, Supplier<RecipeType<?>> factory) {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, key, factory.get());
    }

    @Override
    public void recipeSerializer(String path, ResourceKey<RecipeSerializer<?>> key,
            Supplier<RecipeSerializer<?>> factory) {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, key, factory.get());
    }
}
