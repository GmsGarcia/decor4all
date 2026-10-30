package net.gmsgarcia.decor4all.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

final class PlacementFacings {

    private PlacementFacings() {
    }

    static Direction horizontal(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            return Direction.fromYRot(player.getYRot());
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                return direction;
            }
        }
        return Direction.NORTH;
    }
}
