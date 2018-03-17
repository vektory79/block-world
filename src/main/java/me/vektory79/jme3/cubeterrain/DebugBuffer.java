package me.vektory79.jme3.cubeterrain;

import com.jme3.shader.BufferObject;
import com.jme3.util.BufferUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class DebugBuffer extends BufferObject {
    @NotNull
    private final ByteBuffer data;

    public DebugBuffer(final int binding, final int chunks) {
        super(binding, BufferType.ShaderStorageBufferObject);
        data = BufferUtils.createByteBuffer(chunks * TerrainBlockTypesBuffer.CHUNK_SIZE * Integer.BYTES);
        while (data.hasRemaining()) {
            data.putInt(0);
        }
        data.rewind();
    }

    public int getIntAt(int index) {
        return data.getInt(index * Integer.BYTES);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public ByteBuffer computeData(final int maxSize) {
        return data;
    }
}
