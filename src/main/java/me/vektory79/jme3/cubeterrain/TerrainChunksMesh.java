package me.vektory79.jme3.cubeterrain;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import me.vektory79.jme3.cubeterrain.BlockTypeDescriptorsBuffer.Type;
import me.vektory79.jme3.cubeterrain.TerrainBlockOptionsBuffer.Face;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    public TerrainChunksMesh(final int chunks, final int chunkDescrBinding, final int blockTypesBinding) {
        setMode(Mode.Points);
        chunkDescriptors = new ChunkDescriptorsBuffer(chunkDescrBinding, chunks);
        blockTypeDescriptors = new BlockTypeDescriptorsBuffer(blockTypesBinding);
        blockType = new TerrainBlockTypesBuffer(this);
        blockOptions = new TerrainBlockOptionsBuffer(this);

        setBuffer(blockType);
        setBuffer(blockOptions);

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
}
