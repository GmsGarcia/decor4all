package net.gmsgarcia.decor4all.generator;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import net.minecraft.core.registries.BuiltInRegistries;

final class IntrusiveHolderAccess {

    private IntrusiveHolderAccess() {
    }

    private static final String PENDING_HOLDERS = "unregisteredIntrusiveHolders";
    private static final String FROZEN = "frozen";

    static final String FIELD_MISSING_HINT =
            "Could not find the private fields this generator patches. The mappings "
                    + "for the block registry changed; look for '" + PENDING_HOLDERS + "' "
                    + "and '" + FROZEN + "' on MappedRegistry and update this class. "
                    + "Nothing was written.";

    static void reopen() {
        Object registry = BuiltInRegistries.BLOCK;
        try {
            Field frozen = find(registry.getClass(), FROZEN);
            Field pending = find(registry.getClass(), PENDING_HOLDERS);
            frozen.setAccessible(true);
            pending.setAccessible(true);
            frozen.setBoolean(registry, false);
            pending.set(registry, new IdentityHashMap<>());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException(FIELD_MISSING_HINT, e);
        }
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name + " on " + type.getName() + " or any supertype");
    }
}
