package net.gmsgarcia.decor4all;

import java.util.function.Supplier;
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

public interface ContentRegistrar {

    void block(String path, ResourceKey<Block> key, Supplier<Block> factory);

    void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks);

    void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory);

    void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory);

    void menuType(String path, ResourceKey<MenuType<?>> key, Supplier<MenuType<?>> factory);

    void entityType(String path, ResourceKey<EntityType<?>> key, Supplier<EntityType<?>> factory);

    void recipeType(String path, ResourceKey<RecipeType<?>> key, Supplier<RecipeType<?>> factory);

    void recipeSerializer(String path, ResourceKey<RecipeSerializer<?>> key, Supplier<RecipeSerializer<?>> factory);
}
