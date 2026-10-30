package net.gmsgarcia.decor4all.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.blockentity.DrawerBlockEntity;
import net.gmsgarcia.decor4all.blockentity.CabinetBlockEntity;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.blockentity.SmallStoolBlockEntity;
import net.gmsgarcia.decor4all.registry.DecorTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class DecorBlocks {

    private DecorBlocks() {
    }

    public enum Wood {
        OAK("oak", "oak_log", WoodType.OAK),
        BIRCH("birch", "birch_log", WoodType.BIRCH),
        SPRUCE("spruce", "spruce_log", WoodType.SPRUCE),
        DARK_OAK("dark_oak", "dark_oak_log", WoodType.DARK_OAK),
        ACACIA("acacia", "acacia_log", WoodType.ACACIA),
        JUNGLE("jungle", "jungle_log", WoodType.JUNGLE),
        CRIMSON("crimson", "crimson_stem", WoodType.CRIMSON),
        WARPED("warped", "warped_stem", WoodType.WARPED),
        CHERRY("cherry", "cherry_log", WoodType.CHERRY),
        MANGROVE("mangrove", "mangrove_log", WoodType.MANGROVE),
        PALE_OAK("pale_oak", "pale_oak_log", WoodType.PALE_OAK);

        private final String id;
        private final String segment;
        private final WoodType woodType;

        Wood(String id, String segment, WoodType woodType) {
            this.id = id;
            this.segment = segment;
            this.woodType = woodType;
        }

        public String id() {
            return this.id;
        }

        public String segment() {
            return this.segment;
        }

        public WoodType woodType() {
            return this.woodType;
        }
    }

    public static final ResourceKey<CreativeModeTab> SEATS_TAB = tabKey("seats");
    public static final ResourceKey<CreativeModeTab> TABLES_TAB = tabKey("tables");
    public static final ResourceKey<CreativeModeTab> FENCES_TAB = tabKey("fences");
    public static final ResourceKey<CreativeModeTab> COUNTER_TAB = tabKey("counter");

    public static final ResourceKey<CreativeModeTab> CARPENTER_TABLE_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("minecraft", "functional_blocks"));

    public record Entry(String path, ResourceKey<Block> key, BlockSpec spec,
                        ResourceKey<CreativeModeTab> tab, List<TagKey<Block>> tags) {

        public ResourceKey<Item> itemKey() {
            return ResourceKey.create(Registries.ITEM, id(path));
        }
    }

    public static final Entry CARPENTER_TABLE_ENTRY = new Entry("carpenter_table", BlockSpec.key("carpenter_table"),
            BlockFamilies.CARPENTER_TABLE, null, List.of());

    private static final List<Wood> ALL_WOODS = List.of(
            Wood.OAK, Wood.BIRCH, Wood.SPRUCE, Wood.DARK_OAK,
            Wood.ACACIA, Wood.JUNGLE, Wood.CRIMSON, Wood.WARPED,
            Wood.CHERRY, Wood.MANGROVE, Wood.PALE_OAK);

    public static final List<Entry> ALL = buildAll();

    static {
        int expected = 121 + 45 + 33;
        if (ALL.size() != expected) {
            throw new IllegalStateException("block catalogue has " + ALL.size()
                    + " entries, expected " + expected);
        }
    }

    public static final List<Entry> DISPLAY = buildDisplay();

    static {
        if (DISPLAY.size() != ALL.size()
                || !pathsOf(ALL).equals(pathsOf(DISPLAY))) {
            throw new IllegalStateException("display order has " + DISPLAY.size()
                    + " entries and is not a permutation of the "
                    + ALL.size() + " registered blocks");
        }
    }

    private static Set<String> pathsOf(List<Entry> entries) {
        Set<String> paths = new TreeSet<>();
        for (Entry entry : entries) {
            paths.add(entry.path());
        }
        return paths;
    }

    private static List<Entry> buildAll() {
        List<Entry> out = new ArrayList<>(199);

        out.add(CARPENTER_TABLE_ENTRY);

        addFamilies(out, ALL_WOODS);

        return List.copyOf(out);
    }

    private static List<Entry> buildDisplay() {
        List<Entry> out = new ArrayList<>(166);

        out.add(CARPENTER_TABLE_ENTRY);

        addFamilies(out, ALL_WOODS);

        return List.copyOf(out);
    }

    private static void addFamilies(List<Entry> out, List<Wood> woods) {
        for (Wood wood : woods) {
            out.add(bench(wood, ""));
        }
        for (Wood wood : woods) {
            out.add(bench(wood, "_2"));
        }
        for (Wood wood : woods) {
            out.add(bench(wood, "_3"));
        }

        for (Wood wood : woods) {
            out.add(stool(wood, false));
        }
        for (Wood wood : woods) {
            out.add(stool(wood, true));
        }

        for (Wood wood : woods) {
            out.add(chair(wood, false, false));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, false, true));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, true, false));
        }
        for (Wood wood : woods) {
            out.add(chair(wood, true, true));
        }

        for (Wood wood : woods) {
            out.add(table(wood, false));
        }
        for (Wood wood : woods) {
            out.add(table(wood, true));
        }

        for (Wood wood : woods) {
            out.add(fence(wood, false));
        }
        for (Wood wood : woods) {
            out.add(fence(wood, true));
        }

        for (Wood wood : woods) {
            out.add(gate(wood, false));
        }
        for (Wood wood : woods) {
            out.add(gate(wood, true));
        }

        for (Wood wood : woods) {
            out.add(counter(wood));
            out.add(drawerCounter(wood));
            out.add(cabinetCounter(wood));
        }
    }

    private static Entry counter(Wood wood) {
        String path = wood.segment() + "_counter";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.COUNTER, COUNTER_TAB, List.of());
    }

    private static Entry drawerCounter(Wood wood) {
        String path = wood.segment() + "_drawer_counter";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.DRAWER_COUNTER, COUNTER_TAB, List.of());
    }

    private static Entry cabinetCounter(Wood wood) {
        String path = wood.segment() + "_cabinet_counter";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.CABINET_COUNTER, COUNTER_TAB, List.of());
    }

    private static Entry bench(Wood wood, String suffix) {
        String path = wood.segment() + "_bench" + suffix;
        BlockSpec spec = switch (suffix) {
            case "" -> BlockFamilies.LOG_BENCH;
            case "_2" -> BlockFamilies.LOG_BENCH_2;
            case "_3" -> BlockFamilies.HIGH_BENCH;
            default -> throw new IllegalArgumentException(suffix);
        };
        TagKey<Block> tag = suffix.equals("_3")
                ? DecorTags.Blocks.HIGH_BENCHES
                : DecorTags.Blocks.BENCHES;
        return new Entry(path, BlockSpec.key(path), spec, SEATS_TAB, List.of(tag));
    }

    private static Entry stool(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_small_stool";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.SMALL_STOOL, SEATS_TAB,
                List.of(DecorTags.Blocks.SMALL_STOOLS));
    }

    private static Entry chair(Wood wood, boolean stripped, boolean arms) {
        String path = prefix(wood, stripped) + "_chair" + (arms ? "_2" : "");
        BlockSpec spec = arms ? BlockFamilies.ARMCHAIR : BlockFamilies.CHAIR;
        return new Entry(path, BlockSpec.key(path), spec, SEATS_TAB,
                List.of(DecorTags.Blocks.CHAIRS));
    }

    private static Entry table(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_table";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.LOG_TABLE, TABLES_TAB,
                List.of(DecorTags.Blocks.TABLES));
    }

    private static Entry fence(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_fence";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.LOG_FENCE, FENCES_TAB, List.of());
    }

    private static Entry gate(Wood wood, boolean stripped) {
        String path = prefix(wood, stripped) + "_fence_gate";
        return new Entry(path, BlockSpec.key(path), BlockFamilies.logFenceGate(wood.woodType()),
                FENCES_TAB, List.of());
    }

    private static String prefix(Wood wood, boolean stripped) {
        return stripped ? "stripped_" + wood.segment() : wood.segment();
    }

    public record BlockEntityEntry(String path, ResourceKey<BlockEntityType<?>> key,
                                   BlockEntityType.BlockEntitySupplier<?> factory,
                                   Predicate<Entry> accepts) {
    }

    public static final BlockEntityEntry LOG_BENCH = new BlockEntityEntry(
            "log_bench",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_bench")),
            LogBenchBlockEntity::new,
            entry -> entry.path().endsWith("_bench"));

    public static final BlockEntityEntry LOG_SMALL_STOOL = new BlockEntityEntry(
            "log_small_stool",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("log_small_stool")),
            SmallStoolBlockEntity::new,
            entry -> entry.path().endsWith("_small_stool"));

    public static final BlockEntityEntry DRAWER_COUNTER = new BlockEntityEntry(
            "drawer_counter",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("drawer_counter")),
            DrawerBlockEntity::new,
            entry -> entry.path().endsWith("_drawer_counter"));

    public static final BlockEntityEntry CABINET_COUNTER = new BlockEntityEntry(
            "cabinet_counter",
            ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("cabinet_counter")),
            CabinetBlockEntity::new,
            entry -> entry.path().endsWith("_cabinet_counter"));

    public static final List<BlockEntityEntry> BLOCK_ENTITIES = List.of(LOG_BENCH, LOG_SMALL_STOOL, DRAWER_COUNTER, CABINET_COUNTER);

    public record TabEntry(ResourceKey<CreativeModeTab> key, String path, String iconPath) {
    }

    public static final List<TabEntry> TABS = List.of(
            new TabEntry(SEATS_TAB, "seats", "stripped_oak_log_chair"),
            new TabEntry(TABLES_TAB, "tables", "oak_log_table"),
            new TabEntry(FENCES_TAB, "fences", "oak_log_fence"),
            new TabEntry(COUNTER_TAB, "counter", "oak_log_counter"));

    public static CreativeModeTab buildTab(TabEntry tab, Function<String, Block> lookup) {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.decor4all." + tab.path()))
                .icon(() -> new ItemStack(lookup.apply(tab.iconPath())))
                .displayItems((parameters, output) -> {
                    for (Entry entry : DISPLAY) {
                        if (tab.key().equals(entry.tab())) {
                            output.accept(lookup.apply(entry.path()));
                        }
                    }
                })
                .build();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Decor4All.MOD_ID, path);
    }

    private static ResourceKey<CreativeModeTab> tabKey(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, id(path));
    }
}
