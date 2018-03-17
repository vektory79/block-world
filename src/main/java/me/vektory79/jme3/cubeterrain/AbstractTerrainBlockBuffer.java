package me.vektory79.jme3.cubeterrain;

import com.jme3.scene.VertexBuffer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class AbstractTerrainBlockBuffer extends VertexBuffer {
    public static final int CHUNK_DIMENSION_FACTOR = 4;
    public static final int CHUNK_DIMENSION = 1 << CHUNK_DIMENSION_FACTOR; // 16
    protected static final int BLOCK_DIMENSION_MASK = ~(~(0) << CHUNK_DIMENSION_FACTOR); // 15
    public static final int CHUNK_SIZE = CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION;

    private static final int CHUNK_X_STEP = 1;
    private static final int CHUNK_Y_STEP = CHUNK_DIMENSION;
    private static final int CHUNK_Z_STEP = CHUNK_DIMENSION * CHUNK_DIMENSION;

    private final TerrainChunksMesh terrainChunksMesh;

    private final ByteBuffer internalData;

    public AbstractTerrainBlockBuffer(@NotNull final TerrainChunksMesh terrainChunksMesh, @NotNull final Type type) {
        super(type);
        this.terrainChunksMesh = terrainChunksMesh;
        internalData = BufferUtils.createByteBuffer(this.terrainChunksMesh.getChunks() * CHUNK_SIZE * Integer.BYTES);
        initialize();
    }

    public void setValue(final int blockAddress, final int value) {
        internalData.asIntBuffer().put(blockAddress, value);
        setUpdateNeeded();
    }

    public void setValue(
            final int chunkId,
            @NotNull final Position position,
            final int value) {
        int blockAddress = getBlockAddress(chunkId, position);
        setValue(blockAddress, value);
    }

    public void setValue(
            @NotNull final Position position,
            final int value) {
        int chunkID = getChunkID(position);
        setValue(chunkID, position.mask(BLOCK_DIMENSION_MASK), value);
    }

    public int getValue(final int blockAddress) {
        return internalData.asIntBuffer().get(blockAddress);
    }

    public int getValue(
            final int chunkId,
            @NotNull final Position position) {
        int blockAddress = getBlockAddress(chunkId, position);
        return getValue(blockAddress);
    }

    public int getValue(@NotNull final Position position) {
        int chunkID = getChunkID(position);
        return getValue(chunkID, position.mask(BLOCK_DIMENSION_MASK));
    }

    protected TerrainChunksMesh getTerrainChunksMesh() {
        return terrainChunksMesh;
    }

    @Contract(pure = true)
    public static int getBlockAddress(final int chunkId, @NotNull final Position position) {
        int chunkStart = chunkId * CHUNK_SIZE;
        int blockX = position.getX() * CHUNK_X_STEP;
        int blockY = position.getY() * CHUNK_Y_STEP;
        int blockZ = position.getZ() * CHUNK_Z_STEP;
        return chunkStart + blockX + blockY + blockZ;
    }

    public int getChunkID(@NotNull final Position position) {
        Position chunkPosition = position.rightShift(CHUNK_DIMENSION_FACTOR);
        return terrainChunksMesh.getChunkDescriptors().use(chunkPosition.getX(), chunkPosition.getY(), chunkPosition.getZ());
    }

    public void clear(final int chunkId) {
        int address = getBlockAddress(chunkId, Position.get(0, 0, 0));
        for (int i = address; i < address + CHUNK_SIZE; i++) {
            setValue(i, 0);
        }
    }

    private void initialize() {
        data = internalData.asFloatBuffer();
        usage = Usage.Dynamic;
        format = Format.Float;
        components = 1;
        componentsLength = components * format.getComponentSize();
        lastLimit = data.limit();
        normalized = false;
        setUpdateNeeded();
    }
}
