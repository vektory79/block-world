//#define INT_MIN_VALUE 0x80000000
#define FLOAT_MIN_INFINITY (3.402823466e+38)
#define INT_MIN_VALUE -2147483648

#define CHUNK_DIMENSION_FACTOR 4
#define CHUNK_DIMENSION (1 << CHUNK_DIMENSION_FACTOR)
#define BLOCK_DIMENSION_MASK (~(~(0) << CHUNK_DIMENSION_FACTOR))
#define CHUNK_SIZE (CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION)

layout(std430, binding = 0) readonly buffer m_ChunkDescriptors {
    ivec3 positions[];
} v_ChunkDescriptors;

struct BlockTypeDescriptor {
    uint options;
    int size;
    int texX;
    int texY;
};

layout(std430, binding = 1) readonly buffer m_BlockTypeDescriptors
{
    BlockTypeDescriptor types[];
} v_BlockTypeDescriptors;

/*
layout(std430, binding = 2) coherent buffer m_DebugBuffer
{
    int data[];
} v_DebugBuffer;
*/

// Interprets as block type index in v_BlockTypeDescriptors.types array.
in float inPosition;
// Interprets as block options (see me.vektory79.jme3.cubeterrain.TerrainBlockOptionsBuffer class).
in float inTexCoord8;

out BlockInfo {
    int textureSize;
    ivec2 textureCoord;
    bool isBlockTransparent;
    bool isAnimatedTexture;
    uint textureMappingType;
    uint textureVariants;

    bool isFrontFaceVisible;
    bool isBackFaceVisible;
    bool isLeftFaceVisible;
    bool isRightFaceVisible;
    bool isTopFaceVisible;
    bool isBottomFaceVisible;

    // This is combined data of front face direction and rotation.
    // It's combined becaus all of that bits is used for single raw mapping to texturing details in geometry shader.
    // See me.vektory79.jme3.cubeterrain.TerrainBlockOptionsBuffer class for detail.
    uint blockOrientation;
} blockInfo;

void main() {
    vec4 position = vec4(FLOAT_MIN_INFINITY, FLOAT_MIN_INFINITY, FLOAT_MIN_INFINITY, 1);
    int blockType = floatBitsToInt(inPosition);
    int blockOptions = floatBitsToInt(inTexCoord8);
    // Calculate block coordinates
    int chunkID = gl_VertexID / CHUNK_SIZE;
    vec4 chunkPosition = vec4(v_ChunkDescriptors.positions[chunkID] * CHUNK_DIMENSION, 1.0);
    if (v_ChunkDescriptors.positions[chunkID].x != INT_MIN_VALUE) {
        uint blockID = uint(gl_VertexID);
        vec4 localBlockPosition = vec4(
                bitfieldExtract(blockID, 0, CHUNK_DIMENSION_FACTOR),
                bitfieldExtract(blockID, CHUNK_DIMENSION_FACTOR, CHUNK_DIMENSION_FACTOR),
                bitfieldExtract(blockID, 2 * CHUNK_DIMENSION_FACTOR, CHUNK_DIMENSION_FACTOR),
                0);
//        position = chunkPosition;
        position = chunkPosition + localBlockPosition;

        // Unpack block type options
        BlockTypeDescriptor blockTypeDescr = v_BlockTypeDescriptors.types[blockType];
        blockInfo.textureCoord = ivec2(blockTypeDescr.texX, blockTypeDescr.texY);
        blockInfo.textureSize = blockTypeDescr.size;
        blockInfo.isBlockTransparent = !bool(bitfieldExtract(blockTypeDescr.options, 0, 1));
        blockInfo.isAnimatedTexture = bool(bitfieldExtract(blockTypeDescr.options, 1, 1));
        blockInfo.textureMappingType = bitfieldExtract(blockTypeDescr.options, 2, 2);
        blockInfo.textureVariants = bitfieldExtract(blockTypeDescr.options, 16, 16);

//        v_DebugBuffer.data[gl_VertexID] = blockTypeDescr.size;

        // Unpack block itself options
        blockInfo.isFrontFaceVisible = bool(bitfieldExtract(blockOptions, 5, 1));
        blockInfo.isBackFaceVisible = bool(bitfieldExtract(blockOptions, 4, 1));
        blockInfo.isLeftFaceVisible = bool(bitfieldExtract(blockOptions, 0, 1));
        blockInfo.isRightFaceVisible = bool(bitfieldExtract(blockOptions, 1, 1));
        blockInfo.isTopFaceVisible = bool(bitfieldExtract(blockOptions, 3, 1));
        blockInfo.isBottomFaceVisible = bool(bitfieldExtract(blockOptions, 2, 1));

        blockInfo.blockOrientation = bitfieldExtract(blockOptions, 6, 5);
    } else {
        blockInfo.textureCoord = ivec2(0, 0);
        blockInfo.textureSize = 0;
        blockInfo.isBlockTransparent = false;
        blockInfo.isAnimatedTexture = false;
        blockInfo.textureMappingType = 0;
        blockInfo.textureVariants = 0;
        blockInfo.isFrontFaceVisible = false;
        blockInfo.isBackFaceVisible = false;
        blockInfo.isLeftFaceVisible = false;
        blockInfo.isRightFaceVisible = false;
        blockInfo.isTopFaceVisible = false;
        blockInfo.isBottomFaceVisible = false;
        blockInfo.blockOrientation = 0;
    }
    gl_Position = position;
}
