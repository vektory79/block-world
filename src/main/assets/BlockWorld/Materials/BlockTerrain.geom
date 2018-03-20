#define FLOAT_MIN_INFINITY (3.402823466e+38)
#define INT_MIN_VALUE -2147483648

layout (points) in;
layout (triangle_strip, max_vertices = 24) out;

uniform bool m_DrawTransparent;

uniform mat3 g_NormalMatrix;
uniform mat4 g_WorldViewProjectionMatrix;
uniform vec3 g_CameraPosition;
uniform vec3 g_CameraDirection;

struct EdgeTemplateIndex {
    int address;
    int edges;
};

layout(std430, binding = 2) readonly buffer m_EdgeTemplatesIndex
{
    EdgeTemplateIndex index[];
} v_EdgeTemplatesIndex;

struct EdgeTemplate {
    vec4 vert1;
    vec4 vert2;
    vec4 vert3;
    vec4 vert4;
    vec4 normal;
};

layout(std430, binding = 3) readonly buffer m_EdgeTemplates
{
    EdgeTemplate templ[];
} v_EdgeTemplates;


layout(std430, binding = 5) coherent buffer m_DebugBuffer
{
    uint data[];
} v_DebugBuffer;

in BlockInfo {
    int textureSize;
    ivec2 textureCoord;
    bool isBlockTransparent;
    bool isAnimatedTexture;
    uint textureMappingType;
    uint textureVariants;

    uint visibilityMask;

    // This is combined data of front face direction and rotation.
    // It's combined becaus all of that bits is used for single raw mapping to texturing details in geometry shader.
    // See me.vektory79.jme3.cubeterrain.TerrainBlockOptionsBuffer class for detail.
    uint blockOrientation;
} blockInfo[];

out BlockVertex {
    flat ivec2 texPosition;
    flat int texSize;
    vec2 texCoord;
    vec3 normal;
    flat int blockID;
} blockVertex;

void buildBlock() {
    vec3 position = gl_in[0].gl_Position.xyz;
    vec2 texCoord1 = vec2(0, 0);
    vec2 texCoord2 = vec2(0, 1);
    vec2 texCoord3 = vec2(1, 0);
    vec2 texCoord4 = vec2(1, 1);
    EdgeTemplateIndex index = v_EdgeTemplatesIndex.index[blockInfo[0].visibilityMask];

//    v_DebugBuffer.data[gl_PrimitiveIDIn] = blockInfo[0].visibilityMask;

    for (int i = index.address; i < index.address + index.edges; i++) {
        EdgeTemplate templ = v_EdgeTemplates.templ[i];

        vec3 normal = normalize(g_NormalMatrix * templ.normal.xyz);

        gl_Position = g_WorldViewProjectionMatrix * vec4(templ.vert1.xyz + position.xyz, 1);
        blockVertex.texPosition = blockInfo[0].textureCoord;
        blockVertex.texSize = blockInfo[0].textureSize;
        blockVertex.texCoord = texCoord1;
        blockVertex.normal = normal;
        blockVertex.blockID = gl_PrimitiveIDIn;
        EmitVertex();

        gl_Position = g_WorldViewProjectionMatrix * vec4(templ.vert2.xyz + position.xyz, 1);
        blockVertex.texPosition = blockInfo[0].textureCoord;
        blockVertex.texSize = blockInfo[0].textureSize;
        blockVertex.texCoord = texCoord2;
        blockVertex.normal = normal;
        blockVertex.blockID = gl_PrimitiveIDIn;
        EmitVertex();

        gl_Position = g_WorldViewProjectionMatrix * vec4(templ.vert3.xyz + position.xyz, 1);
        blockVertex.texPosition = blockInfo[0].textureCoord;
        blockVertex.texSize = blockInfo[0].textureSize;
        blockVertex.texCoord = texCoord3;
        blockVertex.normal = normal;
        blockVertex.blockID = gl_PrimitiveIDIn;
        EmitVertex();

        gl_Position = g_WorldViewProjectionMatrix * vec4(templ.vert4.xyz + position.xyz, 1);
        blockVertex.texPosition = blockInfo[0].textureCoord;
        blockVertex.texSize = blockInfo[0].textureSize;
        blockVertex.texCoord = texCoord4;
        blockVertex.normal = normal;
        blockVertex.blockID = gl_PrimitiveIDIn;
        EmitVertex();

        EndPrimitive();
    }
}

void main(void) {
//    if (m_DrawTransparent == blockInfo[0].isBlockTransparent && position.x < FLOAT_MIN_INFINITY) {
    if (m_DrawTransparent == blockInfo[0].isBlockTransparent) {
        buildBlock();
    }
}