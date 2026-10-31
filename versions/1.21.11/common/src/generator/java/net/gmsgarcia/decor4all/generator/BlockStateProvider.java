package net.gmsgarcia.decor4all.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.generator.Catalogue.BlockFacts;
import net.gmsgarcia.decor4all.generator.Catalogue.Family;
import net.gmsgarcia.decor4all.generator.Catalogue.ModelRef;
import net.minecraft.world.item.DyeColor;

final class BlockStateProvider {

    private BlockStateProvider() {
    }

    private record Corner(String sideA, String sideB, int rotation) {
    }

    private static final List<Corner> CORNERS = List.of(
            new Corner("north", "west", 0),
            new Corner("north", "east", 90),
            new Corner("south", "east", 180),
            new Corner("south", "west", 270));

    static Map<String, Object> blockstate(BlockFacts facts) {
        return switch (facts.family()) {
            case CARPENTER_TABLE -> singleModel(facts, "block/" + facts.path());
            case HIGH_BENCH -> singleModel(facts, "block/" + facts.path(), 90);
            case CHAIR, ARMCHAIR -> singleModel(facts, "block/" + facts.path());
            case BENCH -> bench(facts, 0);
            case BENCH_2 -> bench(facts, 90);
            case SMALL_STOOL -> stool(facts);
            case TABLE -> table(facts);
            case FENCE -> fence(facts);
            case FENCE_GATE -> gate(facts);
            case COUNTER -> counter(facts);
            case DRAWER_COUNTER -> counter(facts);
            case CABINET_COUNTER -> counter(facts);
        };
    }

    private static Map<String, Object> singleModel(BlockFacts facts, String model) {
        return singleModel(facts, model, 0);
    }

    private static Map<String, Object> singleModel(BlockFacts facts, String model, int offset) {
        Map<String, Object> variants = new LinkedHashMap<>();
        for (String facing : Catalogue.DIRECTIONS) {
            variants.put("facing=" + facing,
                    ModelRef.rotated(model,
                            (Catalogue.rotationFor(facing) + offset) % 360).apply());
        }
        return Json.obj("variants", variants);
    }

    private static Map<String, Object> bench(BlockFacts facts, int offset) {
        List<Object> parts = new ArrayList<>();
        String base = "block/" + facts.path();

        for (String facing : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(base, (Catalogue.rotationFor(facing) + offset) % 360),
                    Map.of("facing", facing)));
        }

        return Json.obj("multipart", parts);
    }

    private static Map<String, Object> stool(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        String base = "block/" + facts.path();

        for (String facing : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(base, Catalogue.rotationFor(facing)),
                    Map.of("facing", facing)));
        }

        for (int i = 0; i < DyeColor.values().length; i++) {
            String model = "block/repetitive_models/small_stool_carpet/"
                    + DyeColor.byId(i).getSerializedName();
            for (String facing : Catalogue.DIRECTIONS) {
                parts.add(part(
                        ModelRef.rotated(model, Catalogue.rotationFor(facing)),
                        ordered("wool_color", i + 1, "facing", facing)));
            }
        }
        return Json.obj("multipart", parts);
    }

    private static Map<String, Object> table(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        parts.add(part(ModelRef.of("block/" + facts.path() + "_top")));

        String leg = "block/" + facts.path() + "_leg";
        for (int mask = 0; mask < 16; mask++) {
            Map<String, Object> when = new LinkedHashMap<>();
            for (int d = 0; d < Catalogue.DIRECTIONS.size(); d++) {
                when.put(Catalogue.DIRECTIONS.get(d), Boolean.toString((mask & (1 << d)) != 0));
            }
            for (Corner corner : CORNERS) {
                if (Boolean.parseBoolean(when.get(corner.sideA()).toString())
                        || Boolean.parseBoolean(when.get(corner.sideB()).toString())) {
                    continue;
                }
                parts.add(part(ModelRef.locked(leg, corner.rotation()), when));
            }
        }
        return Json.obj("multipart", parts);
    }

    private static Map<String, Object> fence(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        parts.add(part(ModelRef.of(facts.ownModelPath("_post"))));

        String side = facts.ownModelPath("_side");
        for (String direction : Catalogue.DIRECTIONS) {
            parts.add(part(ModelRef.rotated(side, Catalogue.rotationFor(direction)),
                    Map.of(direction, "true")));
        }
        return Json.obj("multipart", parts);
    }

    private static Map<String, Object> gate(BlockFacts facts) {
        Map<String, Object> variants = new LinkedHashMap<>();
        for (String facing : Catalogue.DIRECTIONS) {
            int y = (Catalogue.rotationFor(facing) + 180) % 360;
            for (String inWall : new String[] {"false", "true"}) {
                for (String open : new String[] {"false", "true"}) {
                    String model = facts.ownModelPath(
                            (inWall.equals("true") ? "_wall" : "")
                                    + (open.equals("true") ? "_open" : ""));
                    variants.put("facing=" + facing + ",in_wall=" + inWall + ",open=" + open,
                            ModelRef.locked(model, y).apply());
                }
            }
        }
        return Json.obj("variants", variants);
    }

    private static Map<String, Object> counter(BlockFacts facts) {
        List<Object> parts = new ArrayList<>();
        boolean corners = facts.family() == Family.COUNTER;
        String base = "block/" + facts.path();
        String fillerLeft = "block/" + facts.path() + "_filler_left";
        String fillerRight = "block/" + facts.path() + "_filler_right";

        for (String facing : Catalogue.DIRECTIONS) {
            int rot = Catalogue.rotationFor(facing);
            if (!corners) {
                parts.add(part(ModelRef.rotated(base, rot), Map.of("facing", facing)));
                parts.add(part(ModelRef.rotated(fillerLeft, rot), ordered("facing", facing, "left", "true")));
                parts.add(part(ModelRef.rotated(fillerRight, rot), ordered("facing", facing, "right", "true")));
                continue;
            }

            parts.add(part(ModelRef.rotated(base, rot), ordered("facing", facing, "front", "false")));
            parts.add(part(ModelRef.rotated("block/" + facts.path() + "_front", rot),
                    ordered("facing", facing, "front", "true")));
            for (String front : new String[] {"false", "true"}) {
                String variant = front.equals("true") ? "_front" : "";
                parts.add(part(ModelRef.rotated(fillerLeft + variant, rot),
                        ordered("facing", facing, "front", front, "left", "true")));
                parts.add(part(ModelRef.rotated(fillerRight + variant, rot),
                        ordered("facing", facing, "front", front, "right", "true")));
            }
            parts.add(part(ModelRef.rotated(fillerLeft + "_front", rot),
                    ordered("facing", facing, "front", "true", "right", "true", "left", "false")));
            parts.add(part(ModelRef.rotated(fillerRight + "_front", rot),
                    ordered("facing", facing, "front", "true", "left", "true", "right", "false")));
        }

        return Json.obj("multipart", parts);
    }

    private static Map<String, Object> part(ModelRef model) {
        return Json.obj("apply", model.apply());
    }

    private static Map<String, Object> part(ModelRef model, Map<String, Object> when) {
        return Json.obj("apply", model.apply(), "when", when);
    }

    private static Map<String, Object> ordered(Object... keyValuePairs) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            out.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return out;
    }

    static void assertInvariants(BlockFacts facts, Map<String, Object> document) {
        switch (facts.family()) {
            case BENCH, BENCH_2 -> assertBench(facts, document);
            case FENCE_GATE -> assertGate(facts, document);
            case TABLE -> assertTable(facts, document);
            case FENCE -> assertFence(facts, document);
            case SMALL_STOOL -> assertStool(facts, document);
            case COUNTER, DRAWER_COUNTER, CABINET_COUNTER -> assertCounter(facts, document);
            case CARPENTER_TABLE, HIGH_BENCH, CHAIR, ARMCHAIR -> assertSingleModel(facts, document);
        }
    }

    private static void assertCounter(BlockFacts facts, Map<String, Object> document) {
        boolean corners = facts.family() == Family.COUNTER;
        int expected = corners ? 32 : 12;
        List<Object> parts = parts(document);
        if (parts.size() != expected) {
            throw new IllegalStateException(facts.path() + ": expected " + expected
                    + " multipart entries, got " + parts.size());
        }
        if (corners) {
            assertCounterCorners(facts, parts);
        }
    }

    private static void assertCounterCorners(BlockFacts facts, List<Object> parts) {
        for (String facing : Catalogue.DIRECTIONS) {
            for (String joined : new String[] {"left", "right"}) {
                String closed = joined.equals("left") ? "right" : "left";
                String model = Decor4All.MOD_ID + ":block/" + facts.path()
                        + "_filler_" + closed + "_front";
                boolean declared = parts.stream().anyMatch(part -> {
                    Map<String, Object> clause = when(part);
                    return facing.equals(clause.get("facing"))
                            && "true".equals(clause.get("front"))
                            && "true".equals(clause.get(joined))
                            && "false".equals(clause.get(closed))
                            && model.equals(apply(part).get("model"));
                });
                if (!declared) {
                    throw new IllegalStateException(facts.path() + ": facing=" + facing
                            + " with front and " + joined + " must close " + closed
                            + " with " + model);
                }
            }
        }
    }

    private static void assertSingleModel(BlockFacts facts, Map<String, Object> document) {
        Object variants = document.get("variants");
        if (!(variants instanceof Map<?, ?> map)) {
            throw new IllegalStateException(facts.path() + ": expected a variants document");
        }
        if (map.size() != Catalogue.DIRECTIONS.size()) {
            throw new IllegalStateException(facts.path() + ": expected 4 variants, got "
                    + map.size());
        }
        for (Object key : map.keySet()) {
            String facing = ((String) key).substring("facing=".length());
            @SuppressWarnings("unchecked")
            int actual = y((Map<String, Object>) map.get(key));
            int expected = expectedRotation(facts.family(), facing);
            if (actual != expected) {
                throw new IllegalStateException(facts.path() + ": facing=" + facing + " rotates "
                        + actual + " but must be " + expected);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> parts(Map<String, Object> document) {
        Object multipart = document.get("multipart");
        if (multipart instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new IllegalStateException("expected a multipart document, got " + document.keySet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> apply(Object part) {
        return (Map<String, Object>) ((Map<String, Object>) part).get("apply");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> when(Object part) {
        Object w = ((Map<String, Object>) part).get("when");
        return w == null ? Map.of() : (Map<String, Object>) w;
    }

    private static int y(Map<String, Object> apply) {
        Object y = apply.get("y");
        return y == null ? 0 : (int) y;
    }

    private static void assertBench(BlockFacts facts, Map<String, Object> document) {
        java.util.Map<String, Integer> base = new java.util.HashMap<>();

        for (Object part : parts(document)) {
            Map<String, Object> clause = when(part);
            String facing = (String) clause.get("facing");
            if (facing == null) {
                throw new IllegalStateException(facts.path() + ": bench part with no facing");
            }
            base.put(facing, y(apply(part)));
        }

        for (var entry : base.entrySet()) {
            int expected = expectedRotation(facts.family(), entry.getKey());
            if (entry.getValue() != expected) {
                throw new IllegalStateException(facts.path() + ": facing=" + entry.getKey()
                        + " rotates " + entry.getValue() + " but must be " + expected);
            }
        }
    }

    private static final java.util.Map<Family, Integer> ASSERTED_OFFSETS = Map.of(
            Family.BENCH, 0,
            Family.BENCH_2, 90,
            Family.HIGH_BENCH, 90,
            Family.CARPENTER_TABLE, 0,
            Family.CHAIR, 0,
            Family.ARMCHAIR, 0,
            Family.SMALL_STOOL, 0,
            Family.TABLE, 0,
            Family.FENCE, 0,
            Family.FENCE_GATE, 180);

    private static int expectedRotation(Family family, String facing) {
        return (Catalogue.rotationFor(facing) + ASSERTED_OFFSETS.get(family)) % 360;
    }

    private static void assertGate(BlockFacts facts, Map<String, Object> document) {
        Object variants = document.get("variants");
        if (!(variants instanceof Map<?, ?> map)) {
            throw new IllegalStateException(facts.path() + ": expected a variants document");
        }
        if (map.size() != Catalogue.DIRECTIONS.size() * 2 * 2) {
            throw new IllegalStateException(facts.path() + ": expected 16 gate variants, got "
                    + map.size());
        }
        for (Object key : map.keySet()) {
            String[] fields = ((String) key).split(",");
            String facing = fields[0].substring("facing=".length());
            @SuppressWarnings("unchecked")
            int actual = y((Map<String, Object>) map.get(key));
            if (actual != expectedRotation(facts.family(), facing)) {
                throw new IllegalStateException(facts.path() + ": " + key + " rotates " + actual
                        + " but must be " + expectedRotation(facts.family(), facing)
                        + "; vanilla's own oak_fence_gate uses that offset");
            }
        }
    }

    private static void assertTable(BlockFacts facts, Map<String, Object> document) {
        Map<String, List<Integer>> legsByMask = new java.util.LinkedHashMap<>();
        for (Object part : parts(document)) {
            Map<String, Object> apply = apply(part);
            if (!((String) apply.get("model")).endsWith("_leg")) {
                continue;
            }
            if (!Boolean.TRUE.equals(apply.get("uvlock"))) {
                throw new IllegalStateException(facts.path() + ": legs must be uvlocked");
            }
            Map<String, Object> clause = when(part);
            if (clause.size() != Catalogue.DIRECTIONS.size()) {
                throw new IllegalStateException(facts.path()
                        + ": a leg part must constrain all four sides, got " + clause);
            }
            legsByMask.computeIfAbsent(maskKey(clause), k -> new ArrayList<>()).add(y(apply));
        }

        int totalLegs = 0;
        for (int mask = 0; mask < 16; mask++) {
            Map<String, Object> clause = maskClause(mask);
            List<Integer> expected = new ArrayList<>();
            for (Corner corner : CORNERS) {
                if (Boolean.parseBoolean((String) clause.get(corner.sideA()))
                        || Boolean.parseBoolean((String) clause.get(corner.sideB()))) {
                    continue;
                }
                expected.add(corner.rotation());
            }
            List<Integer> actual =
                    legsByMask.getOrDefault(maskKey(clause), List.of());
            Collections.sort(expected);
            List<Integer> sortedActual = new ArrayList<>(actual);
            Collections.sort(sortedActual);
            if (!expected.equals(sortedActual)) {
                throw new IllegalStateException(facts.path() + ": mask " + clause
                        + " should have legs at rotations " + expected + " but has " + sortedActual);
            }
            totalLegs += expected.size();
        }
        if (totalLegs != 16) {
            throw new IllegalStateException(facts.path() + ": the leg rule should produce 16 legs"
                    + " across all masks, got " + totalLegs);
        }
    }

    private static String maskKey(Map<String, Object> clause) {
        return clause.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElseThrow();
    }

    private static Map<String, Object> maskClause(int mask) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int d = 0; d < Catalogue.DIRECTIONS.size(); d++) {
            out.put(Catalogue.DIRECTIONS.get(d), Boolean.toString((mask & (1 << d)) != 0));
        }
        return out;
    }

    private static void assertFence(BlockFacts facts, Map<String, Object> document) {
        List<Object> parts = parts(document);
        if (parts.size() != Catalogue.DIRECTIONS.size() + 1) {
            throw new IllegalStateException(facts.path() + ": expected 5 parts, got "
                    + parts.size());
        }
        if (!when(parts.get(0)).isEmpty()) {
            throw new IllegalStateException(facts.path() + ": the post part must be unconditional");
        }
        for (int i = 1; i < parts.size(); i++) {
            if (when(parts.get(i)).size() != 1) {
                throw new IllegalStateException(facts.path()
                        + ": each side part must constrain exactly one side");
            }
        }
    }

    private static void assertStool(BlockFacts facts, Map<String, Object> document) {
        int expected = Catalogue.DIRECTIONS.size() * (1 + DyeColor.values().length);
        List<Object> parts = parts(document);
        if (parts.size() != expected) {
            throw new IllegalStateException(facts.path() + ": expected " + expected
                    + " parts, got " + parts.size());
        }
        for (Object part : parts) {
            if (apply(part).containsKey("uvlock")) {
                throw new IllegalStateException(facts.path()
                        + ": a stool part must not be uvlocked; a stray uvlock on the"
                        + " orange carpet changes nothing in game");
            }
        }
    }
}
