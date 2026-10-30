package net.gmsgarcia.decor4all.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.gmsgarcia.decor4all.content.DecorBlocks.Wood;

final class Woods {

    private Woods() {
    }

    private static final String STRIPPED = "stripped_";

    record WoodMeta(String id, String segment) {

        String logTexture() {
            return "minecraft:block/" + segment;
        }

        String strippedLogTexture() {
            return "minecraft:block/stripped_" + segment;
        }

        String logTopTexture() {
            return "minecraft:block/" + segment + "_top";
        }

        String planksTexture() {
            return "minecraft:block/" + id + "_planks";
        }

        String displayName() {
            StringBuilder out = new StringBuilder(segment.length());
            boolean capitalise = true;
            for (int i = 0; i < segment.length(); i++) {
                char c = segment.charAt(i);
                if (c == '_') {
                    out.append(' ');
                    capitalise = true;
                } else if (capitalise) {
                    out.append(Character.toUpperCase(c));
                    capitalise = false;
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }
    }

    private static final Map<Wood, WoodMeta> BY_WOOD = new LinkedHashMap<>();

    static {
        for (Wood wood : Wood.values()) {
            BY_WOOD.put(wood, new WoodMeta(wood.id(), wood.segment()));
        }
    }

    static WoodMeta of(Wood wood) {
        return BY_WOOD.get(wood);
    }

    static List<WoodMeta> all() {
        return List.copyOf(BY_WOOD.values());
    }

    static WoodMeta match(String path) {
        String candidate = isStripped(path) ? path.substring(STRIPPED.length()) : path;
        WoodMeta best = null;
        for (WoodMeta meta : BY_WOOD.values()) {
            if (candidate.equals(meta.segment())
                    || candidate.startsWith(meta.segment() + "_")) {
                if (best == null || meta.segment().length() > best.segment().length()) {
                    best = meta;
                }
            }
        }
        return best;
    }

    static boolean isStripped(String path) {
        return path.startsWith(STRIPPED);
    }
}
