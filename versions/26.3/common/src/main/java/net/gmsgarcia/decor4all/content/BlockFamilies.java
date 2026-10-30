package net.gmsgarcia.decor4all.content;

import net.gmsgarcia.decor4all.blocks.ChairBlock;
import net.gmsgarcia.decor4all.blocks.HighBenchBlock;
import net.gmsgarcia.decor4all.blocks.LogBench2Block;
import net.gmsgarcia.decor4all.blocks.LogBenchBlock;
import net.gmsgarcia.decor4all.blocks.LogFenceBlock;
import net.gmsgarcia.decor4all.blocks.LogFenceGateBlock;
import net.gmsgarcia.decor4all.blocks.LogTableBlock;
import net.gmsgarcia.decor4all.blocks.SmallStoolBlock;
import net.gmsgarcia.decor4all.blocks.CarpenterTableBlock;
import net.gmsgarcia.decor4all.blocks.CabinetCounterBlock;
import net.gmsgarcia.decor4all.blocks.CounterBlock;
import net.gmsgarcia.decor4all.blocks.DrawerCounterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public final class BlockFamilies {

    private BlockFamilies() {
    }

    public static final IntegerProperty WOOL_COLOR = IntegerProperty.create("wool_color", 0, 16);

    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public static final BlockSpec LOG_BENCH = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBenchBlock::new);

    public static final BlockSpec LOG_BENCH_2 = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogBench2Block::new);

    public static final BlockSpec HIGH_BENCH = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            HighBenchBlock::new);

    public static final BlockSpec SMALL_STOOL = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            SmallStoolBlock::new);

    public static final BlockSpec CHAIR = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            properties -> new ChairBlock(false, properties));

    public static final BlockSpec ARMCHAIR = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            properties -> new ChairBlock(true, properties));

    public static final BlockSpec LOG_TABLE = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogTableBlock::new);

    public static final BlockSpec LOG_FENCE = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            LogFenceBlock::new);

    public static final BlockSpec CARPENTER_TABLE = new BlockSpec(MapColor.METAL, SoundType.WOOD, 3.5F, 3.5F,
            CarpenterTableBlock::new);

    public static final BlockSpec COUNTER = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            CounterBlock::new);

    public static final BlockSpec DRAWER_COUNTER = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            DrawerCounterBlock::new);

    public static final BlockSpec CABINET_COUNTER = new BlockSpec(MapColor.WOOD, SoundType.WOOD, 2.0F, 3.0F,
            CabinetCounterBlock::new);

    public static BlockSpec logFenceGate(WoodType woodType) {
        return new BlockSpec(MapColor.WOOD, woodType.soundType(), 2.0F, 3.0F,
                properties -> new LogFenceGateBlock(woodType, properties));
    }
}
