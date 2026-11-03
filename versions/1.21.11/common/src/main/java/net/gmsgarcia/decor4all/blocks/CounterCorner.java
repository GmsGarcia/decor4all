package net.gmsgarcia.decor4all.blocks;

import net.minecraft.util.StringRepresentable;

public enum CounterCorner implements StringRepresentable {

    NONE("none"),
    LEFT("left"),
    RIGHT("right"),
    BOTH("both");

    private final String name;

    CounterCorner(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
