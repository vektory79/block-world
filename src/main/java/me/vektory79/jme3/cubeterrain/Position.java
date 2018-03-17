package me.vektory79.jme3.cubeterrain;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Position {
    private int x;
    private int y;
    private int z;

    private Position(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public final Position mask(final int mask) {
        return Position.get(
                x & mask,
                y & mask,
                z & mask
        );
    }

    public final Position rightShift(final int count) {
        return Position.get(
                x >> count,
                y >> count,
                z >> count
        );
    }

    public static Position get(final int x, final int y, final int z) {
        return new Position(x, y, z);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position that = (Position) o;
        return x == that.x &&
                y == that.y &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
