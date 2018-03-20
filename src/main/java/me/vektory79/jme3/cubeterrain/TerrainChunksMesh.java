package me.vektory79.jme3.cubeterrain;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;
import me.vektory79.jme3.cubeterrain.BlockTypeDescriptorsBuffer.Type;
import me.vektory79.jme3.cubeterrain.TerrainBlockOptionsBuffer.Face;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class TerrainChunksMesh extends Mesh {
    public static final int CHUNK_SIZE = 16;

    @NotNull
    private final ChunkDescriptorsBuffer chunkDescriptors;
    @NotNull
    private final BlockTypeDescriptorsBuffer blockTypeDescriptors;
    @NotNull
    private final TerrainBlockTypesBuffer blockType;
    @NotNull
    private final TerrainBlockOptionsBuffer blockOptions;
    private final SSBufferObject edgeTemplatesIndex = new SSBufferObject(2);
    private final SSBufferObject edgeTemplates = new SSBufferObject(3);

    public TerrainChunksMesh(final int chunks) {
        setMode(Mode.Points);
        chunkDescriptors = new ChunkDescriptorsBuffer(0, chunks);
        blockTypeDescriptors = new BlockTypeDescriptorsBuffer(1);
        blockType = new TerrainBlockTypesBuffer(this);
        blockOptions = new TerrainBlockOptionsBuffer(this);

        setBuffer(blockType);
        setBuffer(blockOptions);

        initEdgeTemplates();

        // Turn off bounding box, because it is inapplicable for this mesh.
        setBound(new BoundingBox(new Vector3f(0, 0, 0), Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
    }

    @Contract(pure = true)
    public int getChunks() {
        return chunkDescriptors.getChunks();
    }

    @NotNull
    @Contract(pure = true)
    public TerrainBlockTypesBuffer getBlockType() {
        return blockType;
    }

    @NotNull
    @Contract(pure = true)
    public TerrainBlockOptionsBuffer getBlockOptions() {
        return blockOptions;
    }

    @NotNull
    @Contract(pure = true)
    public ChunkDescriptorsBuffer getChunkDescriptors() {
        return chunkDescriptors;
    }

    @NotNull
    @Contract(pure = true)
    public BlockTypeDescriptorsBuffer getBlockTypeDescriptors() {
        return blockTypeDescriptors;
    }

    @NotNull
    @Contract(pure = true)
    public SSBufferObject getEdgeTemplatesIndex() {
        return edgeTemplatesIndex;
    }

    @NotNull
    @Contract(pure = true)
    public SSBufferObject getEdgeTemplates() {
        return edgeTemplates;
    }

    public void rebuildCulling() {
        for (int chunkID = 0; chunkID < getChunks(); chunkID++) {
            Position chunkPos = chunkDescriptors.read(chunkID);
            if (chunkPos.getX() == Integer.MIN_VALUE) {
                continue;
            }
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        final Position pos = Position.get(x, y, z);
                        Type currentType = blockType.getType(chunkID, pos);
                        if (currentType == Type.AIR) {
                            continue;
                        }
                        if (x > 0) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.LEFT, checkNearPosition(chunkID, currentType, Position.get(x - 1, y, z)));
                        }
                        if (x < CHUNK_SIZE - 1) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.RIGHT, checkNearPosition(chunkID, currentType, Position.get(x + 1, y, z)));
                        }
                        if (y < CHUNK_SIZE - 1) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.TOP, checkNearPosition(chunkID, currentType, Position.get(x, y + 1, z)));
                        }
                        if (y > 0) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.BOTTOM, checkNearPosition(chunkID, currentType, Position.get(x, y - 1, z)));
                        }
                        if (z < CHUNK_SIZE - 1) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.FRONT, checkNearPosition(chunkID, currentType, Position.get(x, y, z + 1)));
                        }
                        if (z > 0) {
                            blockOptions.setVisibleFace(chunkID, pos, Face.BACK, checkNearPosition(chunkID, currentType, Position.get(x, y, z - 1)));
                        }
                    }
                }
            }
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    final Position pos = Position.get(0, y, z);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int leftChunkId = chunkDescriptors.getID(chunkPos.getX() - 1, chunkPos.getY(), chunkPos.getZ());
                    if (leftChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.LEFT, checkNearPosition(leftChunkId, currentType, Position.get(CHUNK_SIZE - 1, y, z)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.LEFT, true);
                    }
                }
            }
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    final Position pos = Position.get(CHUNK_SIZE - 1, y, z);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int rightChunkId = chunkDescriptors.getID(chunkPos.getX() + 1, chunkPos.getY(), chunkPos.getZ());
                    if (rightChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.RIGHT, checkNearPosition(rightChunkId, currentType, Position.get(0, y, z)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.RIGHT, true);
                    }
                }
            }
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    final Position pos = Position.get(x, CHUNK_SIZE - 1, z);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int topChunkId = chunkDescriptors.getID(chunkPos.getX(), chunkPos.getY() + 1, chunkPos.getZ());
                    if (topChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.TOP, checkNearPosition(topChunkId, currentType, Position.get(x, 0, z)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.TOP, true);
                    }
                }
            }
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    final Position pos = Position.get(x, 0, z);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int bottomChunkId = chunkDescriptors.getID(chunkPos.getX(), chunkPos.getY() - 1, chunkPos.getZ());
                    if (bottomChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.BOTTOM, checkNearPosition(bottomChunkId, currentType, Position.get(x, CHUNK_SIZE - 1, z)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.BOTTOM, true);
                    }
                }
            }
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    final Position pos = Position.get(x, y, 0);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int backChunkId = chunkDescriptors.getID(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ() - 1);
                    if (backChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.BACK, checkNearPosition(backChunkId, currentType, Position.get(x, y, CHUNK_SIZE - 1)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.BACK, true);
                    }
                }
            }
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    final Position pos = Position.get(x, y, CHUNK_SIZE - 1);
                    Type currentType = blockType.getType(chunkID, pos);
                    if (currentType == Type.AIR) {
                        continue;
                    }
                    int frontChunkId = chunkDescriptors.getID(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ() + 1);
                    if (frontChunkId != Integer.MIN_VALUE) {
                        blockOptions.setVisibleFace(chunkID, pos, Face.FRONT, checkNearPosition(frontChunkId, currentType, Position.get(x, y, 0)));
                    } else {
                        blockOptions.setVisibleFace(chunkID, pos, Face.FRONT, true);
                    }
                }
            }
        }
    }

    private boolean checkNearPosition(int chunkID, Type currentType, Position nearPos) {
        Type nearType = blockType.getType(chunkID, nearPos);
        return (nearType == Type.AIR) || (nearType.isTransparent() != currentType.isTransparent());
    }

    /**
     * Build the templates of visible edges of the block, depending on visibility bit mask.
     * <p>
     * <p>
     * This templates will be used in geometry shader to generate mesh by bit mask.
     * </p>
     * <p>
     * <pre>
     *   5   4   3   2   1   0
     * +---+---+---+---+---+---+
     * |   |   |   |   |   |   |
     * +---+---+---+---+---+---+
     *   |   |   |   |   |   |
     *   |   |   |   |   |   +- Left   (-x) face is visible.
     *   |   |   |   |   +----- Right  (+x) face is visible.
     *   |   |   |   +--------- Bottom (-y) face is visible.
     *   |   |   +------------- Top    (+y) face is visible.
     *   |   +----------------- Back   (-z) face is visible.
     *   +--------------------- Front  (+z) face is visible.
     *
     *
     *      Y               (0,1,0)            (1,1,0)          bkLtTp        bkRtTp
     *      ^                     +------------+                   +------------+
     *      |                     |\            \                  |\            \
     *      |                     | \            \                 | \            \
     *      |                     |  \(0,1,1)     \(1,1,1)         |  \ftLtTp      \ftRtTp
     *      |                     |   +------------+               |   +------------+
     *      |                     |   |0          2|               |   |            |
     *      +--------->X   (0,0,0)+   | (1,0,0)+   |         bkLtBm+   |  bkRtBm+   |
     *       \                     \  |            |                \  |            |
     *        \                     \ |            |                 \ |            |
     *         V                     \|1          3|                  \|            |
     *         Z               (0,0,1)+------------+(1,0,1)      ftLtBm+------------+ftRtBm
     * </pre>
     */
    private void initEdgeTemplates() {
        int templateEdges = 0;
        for (int i = 0; i < 64; i++) {
            templateEdges += Integer.bitCount(i);
        }

        Position bkLtTp = Position.get(0, 1, 0);
        Position bkRtTp = Position.get(1, 1, 0);
        Position bkLtBm = Position.get(0, 0, 0);
        Position bkRtBm = Position.get(1, 0, 0);
        Position ftLtTp = Position.get(0, 1, 1);
        Position ftRtTp = Position.get(1, 1, 1);
        Position ftLtBm = Position.get(0, 0, 1);
        Position ftRtBm = Position.get(1, 0, 1);

        // For each bit mas value store 1 int for template address and 1 int for visible edges count.
        ByteBuffer templatesIndexBufferData = BufferUtils.createByteBuffer(64 * 2 * Integer.BYTES);
        // For each edge store 4 vec4 of edge quad and 1 vec4 of it's normal.
        ByteBuffer templatesBufferData = BufferUtils.createByteBuffer(templateEdges * 4 * 4 * Float.BYTES + templateEdges * 4 * Float.BYTES);

        int filledEdges = 0;
        IntBuffer templatesIndexBuffer = templatesIndexBufferData.asIntBuffer();
        FloatBuffer templatesBuffer = templatesBufferData.asFloatBuffer();
        for (int i = 0; i < 64; i++) {
            int templateAddr = filledEdges;
            int templateLength = Integer.bitCount(i);
            templatesIndexBuffer.put(templateAddr);
            templatesIndexBuffer.put(templateLength);

            if (checkBit(i, Face.FRONT)) {
                writePosition(templatesBuffer, ftLtTp);
                writePosition(templatesBuffer, ftLtBm);
                writePosition(templatesBuffer, ftRtTp);
                writePosition(templatesBuffer, ftRtBm);
                writePosition(templatesBuffer, Position.get(0, 0, 1));
                filledEdges++;
            }

            if (checkBit(i, Face.BACK)) {
                writePosition(templatesBuffer, bkRtTp);
                writePosition(templatesBuffer, bkRtBm);
                writePosition(templatesBuffer, bkLtTp);
                writePosition(templatesBuffer, bkLtBm);
                writePosition(templatesBuffer, Position.get(0, 0, -1));
                filledEdges++;
            }

            if (checkBit(i, Face.LEFT)) {
                writePosition(templatesBuffer, bkLtTp);
                writePosition(templatesBuffer, bkLtBm);
                writePosition(templatesBuffer, ftLtTp);
                writePosition(templatesBuffer, ftLtBm);
                writePosition(templatesBuffer, Position.get(-1, 0, 0));
                filledEdges++;
            }

            if (checkBit(i, Face.RIGHT)) {
                writePosition(templatesBuffer, ftRtTp);
                writePosition(templatesBuffer, ftRtBm);
                writePosition(templatesBuffer, bkRtTp);
                writePosition(templatesBuffer, bkRtBm);
                writePosition(templatesBuffer, Position.get(1, 0, 0));
                filledEdges++;
            }

            if (checkBit(i, Face.TOP)) {
                writePosition(templatesBuffer, bkLtTp);
                writePosition(templatesBuffer, ftLtTp);
                writePosition(templatesBuffer, bkRtTp);
                writePosition(templatesBuffer, ftRtTp);
                writePosition(templatesBuffer, Position.get(0, 1, 0));
                filledEdges++;
            }

            if (checkBit(i, Face.BOTTOM)) {
                writePosition(templatesBuffer, bkRtBm);
                writePosition(templatesBuffer, ftRtBm);
                writePosition(templatesBuffer, bkLtBm);
                writePosition(templatesBuffer, ftLtBm);
                writePosition(templatesBuffer, Position.get(0, -1, 0));
                filledEdges++;
            }
        }

        edgeTemplatesIndex.setData(templatesIndexBufferData);
        edgeTemplates.setData(templatesBufferData);
    }

    private boolean checkBit(int value, Face face) {
        return (value & (1 << face.getVisibilityBitShift())) != 0;
    }

    private void writePosition(FloatBuffer buffer, Position pos) {
        buffer.put(pos.getX());
        buffer.put(pos.getY());
        buffer.put(pos.getZ());
        // Padding
        buffer.put(0);
    }
}
