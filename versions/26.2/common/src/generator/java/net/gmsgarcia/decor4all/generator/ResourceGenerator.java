package net.gmsgarcia.decor4all.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.generator.Catalogue.BlockFacts;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class ResourceGenerator {

    private ResourceGenerator() {
    }

    private static final Path OUTPUT = Path.of("src", "main", "generated", "resources");

    private static final String ASSET = "assets/" + Decor4All.MOD_ID;
    private static final String DATA = "data/" + Decor4All.MOD_ID;

    public static void main(String[] args) throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        IntrusiveHolderAccess.reopen();

        List<BlockFacts> all = Catalogue.all();
        System.out.println("catalogue: " + all.size() + " blocks");

        Map<String, Object> files = build(all);
        assertCoversRegistry(all, files);
        assertReferencesResolve(files);

        Path root = OUTPUT;
        deleteRecursively(root);
        write(root, files);
        System.out.println("wrote " + files.size() + " files to " + root.toAbsolutePath());
    }

    private static Map<String, Object> build(List<BlockFacts> all) {
        Map<String, Object> files = new LinkedHashMap<>();

        for (BlockFacts facts : all) {
            Catalogue.assertProperties(facts);

            Map<String, Object> blockstate = BlockStateProvider.blockstate(facts);
            BlockStateProvider.assertInvariants(facts, blockstate);

            files.put(ASSET + "/blockstates/" + facts.path() + ".json", blockstate);
            files.put(DATA + "/loot_table/blocks/" + facts.path() + ".json",
                    DataProvider.lootTable(facts));
            for (Map.Entry<String, Object> entry : ModelProvider.filesFor(facts).entrySet()) {
                files.put(ASSET + "/" + entry.getKey(), entry.getValue());
            }
        }

        ModelProvider.sharedModels()
                .forEach((path, document) -> files.put(ASSET + "/" + path, document));

        files.putAll(DataProvider.tags(all));
        files.putAll(DataProvider.recipes(all));
        files.putAll(DataProvider.advancements());
        files.put(ASSET + "/lang/en_us.json", DataProvider.lang(all));
        return files;
    }

    private static void assertCoversRegistry(List<BlockFacts> all, Map<String, Object> files) {
        List<String> missing = new ArrayList<>();
        for (BlockFacts facts : all) {
            String id = facts.path();
            for (String path : List.of(
                        ASSET + "/blockstates/" + id + ".json",
                        ASSET + "/items/" + id + ".json",
                        ASSET + "/models/item/" + id + ".json",
                        DATA + "/loot_table/blocks/" + id + ".json",
                        DATA + "/recipe/" + id + ".json")) {
                if (!files.containsKey(path)) {
                    missing.add(id + " -> " + path);
                }
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("catalogue blocks missing generated files: " + missing);
        }
    }

    private static final Path HAND = Path.of("..", "..", "..", "shared");

    private static void assertReferencesResolve(Map<String, Object> files) {
        String modelsPrefix = ASSET + "/models/";
        Set<String> generatedModels = new TreeSet<>();
        for (String path : files.keySet()) {
            if (path.startsWith(modelsPrefix) && path.endsWith(".json")) {
                generatedModels.add(path.substring(modelsPrefix.length()));
            }
        }

        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, Object> entry : files.entrySet()) {
            String from = entry.getKey();
            if (!from.startsWith(ASSET + "/")) {
                continue;
            }
            collectReferences(entry.getValue(), from, missing, generatedModels);
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("generated resources reference "
                    + missing.size() + " file(s) that do not exist. Derived files are in "
                    + OUTPUT + ", hand-authored ones in " + HAND + " -- copy the source art "
                    + "across and re-run. Missing: " + missing);
        }
    }

    private static void collectReferences(Object node, String from, List<String> missing,
            Set<String> generatedModels) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                Object value = e.getValue();
                switch (key) {
                    case "parent" -> checkModelRef(value, from, missing, generatedModels);
                    case "model" -> {
                        if (value instanceof String s) {
                            checkModelRef(s, from, missing, generatedModels);
                        } else if (value instanceof Map<?, ?> inner) {
                            collectReferences(inner, from, missing, generatedModels);
                        }
                    }
                    case "textures" -> {
                        if (value instanceof Map<?, ?> textures) {
                            for (Object t : textures.values()) {
                                if (t instanceof String s && !s.startsWith("#")) {
                                    checkTextureRef(s, from, missing);
                                }
                            }
                        }
                    }
                    default -> collectReferences(value, from, missing, generatedModels);
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object element : list) {
                collectReferences(element, from, missing, generatedModels);
            }
        }
    }

    private static void checkModelRef(Object value, String from, List<String> missing,
            Set<String> generatedModels) {
        if (!(value instanceof String ref) || ref.startsWith("#") || isVanilla(ref)) {
            return;
        }
        String rel = stripNamespace(ref) + ".json";
        if (generatedModels.contains(rel)) {
            return;
        }
        if (!Files.isRegularFile(HAND.resolve(ASSET + "/models").resolve(rel))) {
            missing.add(from + " -> model " + ref);
        }
    }

    private static void checkTextureRef(String ref, String from, List<String> missing) {
        if (isVanilla(ref)) {
            return;
        }
        if (!Files.isRegularFile(HAND.resolve(ASSET + "/textures").resolve(stripNamespace(ref) + ".png"))) {
            missing.add(from + " -> texture " + ref);
        }
    }

    private static boolean isVanilla(String ref) {
        return ref.startsWith("minecraft:") || ref.indexOf(':') < 0;
    }

    private static String stripNamespace(String ref) {
        int colon = ref.indexOf(':');
        return colon < 0 ? ref : ref.substring(colon + 1);
    }

    private static void write(Path root, Map<String, Object> files) throws IOException {
        Set<String> sorted = new TreeSet<>(files.keySet());
        for (String path : sorted) {
            Path target = root.resolve(path);
            Files.createDirectories(target.getParent());
            Files.writeString(target, Json.write(files.get(path)), StandardCharsets.UTF_8);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.forEach(paths::add);
        }
        for (int i = paths.size() - 1; i >= 0; i--) {
            Files.deleteIfExists(paths.get(i));
        }
    }
}
