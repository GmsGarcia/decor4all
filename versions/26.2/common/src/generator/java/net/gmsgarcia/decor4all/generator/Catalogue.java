package net.gmsgarcia.decor4all.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.gmsgarcia.decor4all.generator.Woods.WoodMeta;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

final class Catalogue {

    private Catalogue() {
    }

    enum Family {
        CARPENTER_TABLE("carpenter_table"),
        BENCH("_bench"),
        BENCH_2("_bench_2"),
        HIGH_BENCH("_bench_3"),
        SMALL_STOOL("_small_stool"),
        CHAIR("_chair"),
        ARMCHAIR("_chair_2"),
        TABLE("_table"),
        FENCE("_fence"),
        FENCE_GATE("_fence_gate"),
        COUNTER("_counter"),
        DRAWER_COUNTER("_drawer_counter"),
        CABINET_COUNTER("_cabinet_counter");

        private final String suffix;

        Family(String suffix) {
            this.suffix = suffix;
        }

        String suffix() {
            return suffix;
        }

        Set<String> modelProperties() {
            return switch (this) {
                case CARPENTER_TABLE, HIGH_BENCH, CHAIR, ARMCHAIR -> Set.of("facing");
                case BENCH, BENCH_2 -> Set.of("facing");
                case SMALL_STOOL -> Set.of("facing", "wool_color");
                case TABLE, FENCE -> Set.of("north", "east", "south", "west");
                case FENCE_GATE -> Set.of("facing", "in_wall", "open");
                case COUNTER -> Set.of("facing", "left", "right", "front");
                case DRAWER_COUNTER, CABINET_COUNTER -> Set.of("facing", "left", "right");
            };
        }
    }

    static final Set<String> IGNORABLE = Set.of("waterlogged", "occupied", "powered");

    record BlockFacts(DecorBlocks.Entry entry, Family family, WoodMeta wood,
                      boolean stripped, Block block) {

        String path() {
            return entry.path();
        }

        String ownTexture() {
            return stripped ? wood.strippedLogTexture() : wood.logTexture();
        }

        String ownTopTexture() {
            return stripped ? wood.strippedLogTexture() + "_top" : wood.logTopTexture();
        }

        String otherTexture() {
            return stripped ? wood.logTexture() : wood.strippedLogTexture();
        }

        String otherTopTexture() {
            return stripped ? wood.logTopTexture() : wood.strippedLogTexture() + "_top";
        }

        String planksTexture() {
            return wood.planksTexture();
        }

        String translationKey() {
            return "block.decor4all." + path();
        }

        String modelDir() {
            return switch (family) {
                case FENCE -> stripped ? "stripped_fences" : "fences";
                case FENCE_GATE -> stripped ? "stripped_fence_gates" : "fence_gates";
                default -> "";
            };
        }

        String ownModelPath(String suffix) {
            return "block/" + modelDir() + "/" + path() + suffix;
        }
    }

    static List<BlockFacts> all() {
        List<BlockFacts> out = new ArrayList<>(DecorBlocks.ALL.size());
        for (DecorBlocks.Entry entry : DecorBlocks.ALL) {
            out.add(new BlockFacts(
                    entry,
                    familyOf(entry.path()),
                    Woods.match(entry.path()),
                    Woods.isStripped(entry.path()),
                    entry.spec().create(entry.key())));
        }
        return out;
    }

    private static Family familyOf(String path) {
        String tail = path.startsWith("stripped_") ? path.substring("stripped_".length()) : path;
        Family best = null;
        for (Family family : Family.values()) {
            if (tail.endsWith(family.suffix())
                    && (best == null || family.suffix().length() > best.suffix().length())) {
                best = family;
            }
        }
        if (best == null) {
            throw new IllegalStateException("no family matches catalogue id " + path);
        }
        return best;
    }

    static List<Property<?>> declaredProperties(Block block) {
        List<Property<?>> out = new ArrayList<>();
        block.defaultBlockState().getProperties().forEach(out::add);
        return out;
    }

    static void assertProperties(BlockFacts facts) {
        Set<String> declared = new LinkedHashSet<>();
        for (Property<?> property : declaredProperties(facts.block())) {
            declared.add(property.getName());
        }

        Set<String> expected = facts.family().modelProperties();
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(declared);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(facts.path() + " (" + facts.family()
                    + ") blockstate needs properties the block does not declare: " + missing);
        }

        Set<String> unmodelled = new LinkedHashSet<>(declared);
        unmodelled.removeAll(expected);
        unmodelled.removeAll(IGNORABLE);
        if (!unmodelled.isEmpty()) {
            throw new IllegalStateException(facts.path() + " (" + facts.family()
                    + ") declares properties no model uses and that are not ignorable: " + unmodelled
                    + ". Add them to " + Family.class.getSimpleName() + ".modelProperties()"
                    + " or to IGNORABLE, do not let the blockstate ignore them.");
        }
    }

    static final List<String> DIRECTIONS = List.of("north", "east", "south", "west");

    static int rotationFor(String facing) {
        return switch (facing) {
            case "north" -> 0;
            case "east" -> 90;
            case "south" -> 180;
            case "west" -> 270;
            default -> throw new IllegalArgumentException(facing);
        };
    }

    record ModelRef(String model, int y, boolean uvlock) {

        static ModelRef of(String model) {
            return new ModelRef(model, 0, false);
        }

        static ModelRef rotated(String model, int y) {
            return new ModelRef(model, y, false);
        }

        static ModelRef locked(String model, int y) {
            return new ModelRef(model, y, true);
        }

        Map<String, Object> apply() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("model", Decor4All.MOD_ID + ":" + model);
            if (y != 0) {
                out.put("y", y);
            }
            if (uvlock) {
                out.put("uvlock", true);
            }
            return out;
        }
    }
}
