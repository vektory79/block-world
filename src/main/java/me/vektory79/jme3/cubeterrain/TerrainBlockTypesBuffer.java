package me.vektory79.jme3.cubeterrain;

import org.jetbrains.annotations.NotNull;

public class TerrainBlockTypesBuffer extends AbstractTerrainBlockBuffer {
    public TerrainBlockTypesBuffer(@NotNull final TerrainChunksMesh terrainChunksMesh) {
        super(terrainChunksMesh, Type.Position);
    }

    public final void setType(final int blockAddress, @NotNull final BlockTypeDescriptorsBuffer.Type type) {
        setValue(blockAddress, type.ordinal());
    }

    public final void setType(final int chunkId, final Position position,
                           @NotNull final BlockTypeDescriptorsBuffer.Type type) {
        setValue(chunkId, position, type.ordinal());
    }

    public final void setType(final Position position,
                          @NotNull final BlockTypeDescriptorsBuffer.Type type) {
        setValue(position, type.ordinal());
    }

    @NotNull
    public final BlockTypeDescriptorsBuffer.Type getType(final int blockAddress) {
        return BlockTypeDescriptorsBuffer.Type.values()[getValue(blockAddress)];
    }

    @NotNull
    public final BlockTypeDescriptorsBuffer.Type getType(final int chunkId, final Position position) {
        return BlockTypeDescriptorsBuffer.Type.values()[getValue(chunkId, position)];
    }

    @NotNull
    public final BlockTypeDescriptorsBuffer.Type getType(final Position position) {
        return BlockTypeDescriptorsBuffer.Type.values()[getValue(position)];
    }
}
