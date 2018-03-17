package me.vektory79.jme3.cubeterrain;

import com.jme3.shader.BufferObject;
import com.jme3.util.BufferUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BlockTypeDescriptorsBuffer extends BufferObject {
    private static final int OPTIONS_SHIFT = 0;
    private static final int TEXTURE_SIZE_SHIFT = OPTIONS_SHIFT + Integer.BYTES;
    private static final int TEXTURE_X_SHIFT = TEXTURE_SIZE_SHIFT + Integer.BYTES;
    private static final int TEXTURE_Y_SHIFT = TEXTURE_X_SHIFT + Integer.BYTES;
    private static final int FULL_STRIDE = TEXTURE_Y_SHIFT + Integer.BYTES;

    @NotNull
    private final ByteBuffer data;

    public BlockTypeDescriptorsBuffer(final int binding) {
        super(binding, BufferType.ShaderStorageBufferObject);
        data = BufferUtils.createByteBuffer(6 * FULL_STRIDE);
        write(Type.AIR);
        write(Type.STONE);
        write(Type.DIRT);
        write(Type.SAND);
        write(Type.SNOW);
        write(Type.WATER);
    }


    // TODO: seems strange a bit. May be it's better to split data computation and accessor to it?
    @Override
    @NotNull
    @Contract(pure = true)
    public ByteBuffer computeData(final int maxSize) {
        return data;
    }

    private void write(@NotNull Type info) {
        int address = info.ordinal() * FULL_STRIDE;
        data.putInt(address + OPTIONS_SHIFT, info.getOptions());
        data.putInt(address + TEXTURE_SIZE_SHIFT, info.getTextureSize());
        data.putInt(address + TEXTURE_X_SHIFT, info.getTextureX());
        data.putInt(address + TEXTURE_Y_SHIFT, info.getTextureY());
    }

    public enum Type {
        AIR((short)1, (short)0, 128, 0, 0),
        STONE((short)1, (short)1, 128, 0, 128),
        DIRT((short)1, (short)1, 128, 0, 256),
        SAND((short)1, (short)1, 128, 0, 384),
        SNOW((short)1, (short)1, 128, 0, 512),
        WATER((short)32, (short)0, 128, 128, 0);

        private final int options;
        private final int textureSize;
        private final int textureX;
        private final int textureY;

        Type(short variants, short options, int textureSize, int textureX, int textureY) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.options = ((int) variants << 16) | (int) options;
            this.textureSize = textureSize;
        }

        /**
         * The per bit packaged options of block type.
         *
         * <pre>
         *   15  14  13  12  11  10  9   8    7   6   5   4   3   2   1   0
         * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         * |   |   |   |   |   |   |   |   ||   |   |   |   |   |   |   |   |
         * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         *                                                   \__ __/  |   |
         *                                                      V     |   +- Transparent (0) or opaque (1)
         *                                                      |     +----- Animated (1). 'Variants' field is treated as count of animation frames.
         *                                                      +----------- Texture mapping type:
         *                                                                   00 - All edges have same texture
         *                                                                   01 - All but top edges have same texture
         *                                                                   10 - All but top and bottom edges have same texture
         *                                                                   11 - All edges have different textures
         *
         *   31  30  29  28  27  26  25  24   23  22  21  20  19  18  17  16
         * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         * |   |   |   |   |   |   |   |   ||   |   |   |   |   |   |   |   |
         * +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         *  \_______________________________ ______________________________/
         *                                  V
         *                                  |
         *                                  +------------------------------- Variants of texture
         * </pre>
         */
        @Contract(pure = true)
        public int getOptions() {
            return options;
        }

        public boolean isTransparent() {
            return (options & 0b1) == 0;
        }

        @Contract(pure = true)
        public int getTextureSize() {
            return textureSize;
        }

        @Contract(pure = true)
        public int getTextureX() {
            return textureX;
        }

        @Contract(pure = true)
        public int getTextureY() {
            return textureY;
        }
    }
}
