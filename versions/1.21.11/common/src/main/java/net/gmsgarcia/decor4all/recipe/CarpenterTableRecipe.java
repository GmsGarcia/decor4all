package net.gmsgarcia.decor4all.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gmsgarcia.decor4all.Decor4All;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class CarpenterTableRecipe extends SingleItemRecipe {

    public static final RecipeType<CarpenterTableRecipe> TYPE =
            new RecipeType<CarpenterTableRecipe>() { };

    public static final MapCodec<CarpenterTableRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(CarpenterTableRecipe::input),
                    ItemStack.CODEC.fieldOf("result").forGetter(CarpenterTableRecipe::result)
            ).apply(instance, (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)));

    public static final StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CarpenterTableRecipe::input,
                    ItemStack.STREAM_CODEC, CarpenterTableRecipe::result,
                    (ingredient, result) -> new CarpenterTableRecipe("", ingredient, result)
            );

    public static final RecipeSerializer<CarpenterTableRecipe> SERIALIZER = new RecipeSerializer<>() {
            @Override
            public MapCodec<CarpenterTableRecipe> codec() {
                return MAP_CODEC;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, CarpenterTableRecipe> streamCodec() {
                return STREAM_CODEC;
            }
        };

    public CarpenterTableRecipe(String group, Ingredient ingredient, ItemStack result) {
        super(group, ingredient, result);
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return TYPE;
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public ItemStack craft(ItemStack input, HolderLookup.Provider registries) {
        return assemble(new SingleRecipeInput(input), registries);
    }
}
