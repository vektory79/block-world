package me.vektory79.jme3.cubeterrain;

import com.jme3.shader.BufferObject;
import com.jme3.util.BufferUtils;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.stack.array.TIntArrayStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Builder for chunk information SSBO.
 * <p>
 * <p>This is descriptors of the chunk positions. In shader's program actual descriptor
 * is calculated from vertex index by shifting right by 15 (dividing by 32768).</p>
 * <p>
 * <p>Positional index of chunk descriptor is used as chunk ID.</p>
 */
public class ChunkDescriptorsBuffer extends BufferObject {
    private static final int CHUNK_X_COORD_SHIFT = 0;
    private static final int CHUNK_Y_COORD_SHIFT = CHUNK_X_COORD_SHIFT + Integer.BYTES;
    private static final int CHUNK_Z_COORD_SHIFT = CHUNK_Y_COORD_SHIFT + Integer.BYTES;
    private static final int CHUNK_ALIGN1_SHIFT = CHUNK_Z_COORD_SHIFT + Integer.BYTES;
    private static final int CHUNK_FULL_STRIDE = CHUNK_ALIGN1_SHIFT + Integer.BYTES;

    private final int chunks;
    @NotNull
    private final ByteBuffer data;
    @NotNull
    private final TObjectIntHashMap<Position> coordIndex;
    @NotNull
    private final TIntArrayStack freeChunks;

    /**
     * Create buffer for amount of chunks.
     *
     * @param chunks amount of chunks to be reserved.
     */
    public ChunkDescriptorsBuffer(final int binding, final int chunks) {
        super(binding, BufferType.ShaderStorageBufferObject);
        this.chunks = chunks;
        freeChunks = new TIntArrayStack(chunks, Integer.MIN_VALUE);
        data = BufferUtils.createByteBuffer(chunks * CHUNK_FULL_STRIDE);
        for (int i = chunks - 1; i >= 0; i--) {
            freeChunks.push(i);
            data.putInt(Integer.MIN_VALUE);
            data.putInt(Integer.MIN_VALUE);
            data.putInt(Integer.MIN_VALUE);
            data.putInt(Integer.MIN_VALUE);
        }
        data.rewind();
        coordIndex = new TObjectIntHashMap<>(chunks, Constants.DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE);
    }

    /**
     * Amount of reserved chunks.
     *
     * @return amount of reserved chunks.
     */
    @Contract(pure = true)
    public int getChunks() {
        return chunks;
    }

    /**
     * Get chunk ID for mentioned coordinates.
     * <p>
     * <p>The coordinates dimension is in chunk count. E.g. getID(1,2,3) is: 1 chunk by X coordinate, 2 chunks by Y
     * coordinate and 3 chunks by Z coordinate.</p>
     *
     * @param x X coordinate for searched chunk.
     * @param y Y coordinate for searched chunk.
     * @param z Z coordinate for searched chunk.
     * @return if chunk for mentioned coordinates is reserved then return its ID.
     * Or {@link Integer#MIN_VALUE} otherwise.
     */
    public int getID(final int x, final int y, final int z) {
        return coordIndex.get(Position.get(x, y, z));
    }

    public int getID(final Position pos) {
        return coordIndex.get(Position.get(pos.getX(), pos.getY(), pos.getZ()));
    }

    public int use(final int x, final int y, final int z) {
        int result = coordIndex.get(Position.get(x, y, z));
        if (result == Integer.MIN_VALUE && freeChunks.size() > 0) {
            result = freeChunks.pop();
            write(result, x, y, z);
            // This is separate new to grant escape analysis for previous object.
            coordIndex.put(Position.get(x, y, z), result);
        }
        return result;
    }

    public void free(final int chunkId) {
        Position key = read(chunkId);
        if (coordIndex.remove(key) != Integer.MIN_VALUE) {
            freeChunks.push(chunkId);
            write(chunkId, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    public final void write(final int chunkId, final int x, final int y, final int z) {
        int address = chunkId * CHUNK_FULL_STRIDE;
        data.putInt(address + CHUNK_X_COORD_SHIFT, x);
        data.putInt(address + CHUNK_Y_COORD_SHIFT, y);
        data.putInt(address + CHUNK_Z_COORD_SHIFT, z);
    }

    @NotNull
    public final Position read(final int chunkId) {
        int address = chunkId * CHUNK_FULL_STRIDE;
        return Position.get(
                data.getInt(address + CHUNK_X_COORD_SHIFT),
                data.getInt(address + CHUNK_Y_COORD_SHIFT),
                data.getInt(address + CHUNK_Z_COORD_SHIFT)
        );
    }

    // TODO: seems strange a bit. May be it's better to split data computation and accessor to it?
    @Override
    @NotNull
    @Contract(pure = true)
    public ByteBuffer computeData(final int maxSize) {
        return data;
    }
}
