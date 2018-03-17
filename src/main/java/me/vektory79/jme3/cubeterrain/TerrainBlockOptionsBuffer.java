package me.vektory79.jme3.cubeterrain;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The buffer of terrain blocks parameters, packet to the structure of bits.
 *
 * <pre>
 *   15  14  13  12  11  10  9   8    7   6   5   4   3   2   1   0
 * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 * |   |   |   |   |   |   |   |   ||   |   |   |   |   |   |   |   |
 * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 *                      \____ ____/  \__ __/  |   |   |   |   |   |
 *                           V          V     |   |   |   |   |   +- Left   (-x) face is visible.
 *                           |          |     |   |   |   |   +----- Right  (+x) face is visible.
 *                           |          |     |   |   |   +--------- Bottom (-y) face is visible.
 *                           |          |     |   |   +------------- Top    (+y) face is visible.
 *                           |          |     |   +----------------- Back   (-z) face is visible.
 *                           |          |     +--------------------- Front  (+z) face is visible.
 *                           |          +--------------------------- Rotation of the block. Rotation clockwise along the front-back axis.
 *                           |                                       00 (0) - 0º
 *                           |                                       01 (1) - 90º
 *                           |                                       10 (2) - 180º
 *                           |                                       11 (3) - 270º
 *                           +-------------------------------------- Block front face direction (direction to which
 *                                                                   the front face of the block is oriented).
 *                                                                   000 (0) - Front
 *                                                                   001 (1) - Back
 *                                                                   010 (2) - Left
 *                                                                   011 (3) - Right
 *                                                                   100 (4) - Top
 *                                                                   101 (5) - Bottom
 *
 *   31  30  29  28  27  26  25  24   23  22  21  20  19  18  17  16
 * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 * |   |   |   |   |   |   |   |   ||   |   |   |   |   |   |   |   |
 * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 * </pre>
 */
public class TerrainBlockOptionsBuffer extends AbstractTerrainBlockBuffer {
    private static final int ROTATE_FIELD_SHIFT = 6;
    private static final int ROTATE_FIELD_MASK = 0b11;

    private static final int ORIENTATION_FIELD_SHIFT = 8;
    private static final int ORIENTATION_FIELD_MASK = 0b111;

    public TerrainBlockOptionsBuffer(@NotNull final TerrainChunksMesh terrainChunksMesh) {
        super(terrainChunksMesh, Type.TexCoord8);
    }

    public final void setVisibleFace(final int blockAddress, @NotNull final Face face, final boolean visible) {
        int value = getValue(blockAddress);
        value = setVisibilityBit(face, visible, value);
        setValue(blockAddress, value);
    }

    public final void setVisibleFace(final int chunkId, @NotNull final Position position, @NotNull final Face face,
                                     final boolean visible) {
        int blockAddress = getBlockAddress(chunkId, position);
        setVisibleFace(blockAddress, face, visible);
    }

    public final void setVisibleFace(@NotNull final Position position, @NotNull final Face face,
                                     final boolean visible) {
        int chunkID = getChunkID(position);
        setVisibleFace(chunkID, position.mask(BLOCK_DIMENSION_MASK), face, visible);
    }

    public final boolean isVisibleFace(final int blockAddress, @NotNull final Face face) {
        return extractFaceVisibility(getValue(blockAddress), face);
    }

    public final boolean isVisibleFace(final int chunkId, @NotNull final Position position, @NotNull final Face face) {
        return extractFaceVisibility(getValue(chunkId, position), face);
    }

    public final boolean isVisibleFace(@NotNull final Position position, @NotNull final Face face) {
        return extractFaceVisibility(getValue(position), face);
    }

    public final void setRotate(final int blockAddress, @NotNull final Rotate rotate) {
        int value = getValue(blockAddress);
        // Clear field bits
        value &= ~(ROTATE_FIELD_MASK << ROTATE_FIELD_SHIFT);
        // Set new field value bits
        value |= rotate.ordinal() << ROTATE_FIELD_SHIFT;
        setValue(blockAddress, value);
    }

    public final void setRotate(final int chunkId, @NotNull final Position position, @NotNull final Rotate rotate) {
        int blockAddress = getBlockAddress(chunkId, position);
        setRotate(blockAddress, rotate);
    }

    public final void setRotate(@NotNull final Position position, @NotNull final Rotate rotate) {
        int chunkID = getChunkID(position);
        setRotate(chunkID, position.mask(BLOCK_DIMENSION_MASK), rotate);
    }

    @NotNull
    public final Rotate getRotate(final int blockAddress) {
        return extractRotate(getValue(blockAddress));
    }

    @NotNull
    public final Rotate getRotate(final int chunkId, @NotNull final Position position) {
        return extractRotate(getValue(chunkId, position));
    }

    @NotNull
    public final Rotate getRotate(final @NotNull Position position) {
        return extractRotate(getValue(position));
    }

    public final void setFrontDirection(final int blockAddress, @NotNull final Face orientation) {
        int value = getValue(blockAddress);
        // Clear field bits
        value &= ~(ORIENTATION_FIELD_MASK << ORIENTATION_FIELD_SHIFT);
        // Set new field value bits
        value |= orientation.getFrontDirection() << ORIENTATION_FIELD_SHIFT;
        setValue(blockAddress, value);
    }

    public final void setFrontDirection(final int chunkId, @NotNull final Position position, @NotNull final Face orientation) {
        int blockAddress = getBlockAddress(chunkId, position);
        setFrontDirection(blockAddress, orientation);
    }

    public final void setFrontDirection(@NotNull final Position position, @NotNull final Face orientation) {
        int chunkID = getChunkID(position);
        setFrontDirection(chunkID, position.mask(BLOCK_DIMENSION_MASK), orientation);
    }

    @NotNull
    public final Face getFrontDirection(final int blockAddress) {
        return extractFrontDirection(getValue(blockAddress));
    }

    @NotNull
    public final Face getFrontDirection(final int chunkId, @NotNull final Position position) {
        return extractFrontDirection(getValue(chunkId, position));
    }

    @NotNull
    public final Face getFrontDirection(final @NotNull Position position) {
        return extractFrontDirection(getValue(position));
    }

    @Contract(pure = true)
    private static boolean extractFaceVisibility(final int value, @NotNull final Face face) {
        return ((value >>> face.getVisibilityBitShift()) & 1) == 1;
    }

    @Contract(pure = true)
    private static Rotate extractRotate(int value) {
        return Rotate.values()[(value >>> ROTATE_FIELD_SHIFT) & ROTATE_FIELD_MASK];
    }

    @Contract(pure = true)
    private static Face extractFrontDirection(int value) {
        return Face.values()[(value >>> ORIENTATION_FIELD_SHIFT) & ORIENTATION_FIELD_MASK];
    }

    @Contract(pure = true)
    private static int setVisibilityBit(@NotNull final Face face, final boolean visible, final int value) {
        int faceBit = 1 << face.getVisibilityBitShift();
        return visible ? value | faceBit : value & ~faceBit;
    }

    public enum Face {
        FRONT(5),
        BACK(4),
        LEFT(0),
        RIGHT(1),
        TOP(3),
        BOTTOM(2);

        private final int visibilityBitShift;

        Face(final int visibilityBitShift) {
            this.visibilityBitShift = visibilityBitShift;
        }

        @Contract(pure = true)
        public int getVisibilityBitShift() {
            return visibilityBitShift;
        }

        @Contract(pure = true)
        public int getFrontDirection() {
            return ordinal();
        }
    }

    public enum Rotate {
        R0, R90, R180, R270
    }
}
