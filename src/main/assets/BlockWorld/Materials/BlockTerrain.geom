#define FLOAT_MIN_INFINITY (3.402823466e+38)
#define INT_MIN_VALUE -2147483648

layout (points) in;
layout (triangle_strip, max_vertices = 36) out;

uniform bool m_DrawTransparent;

uniform mat3 g_NormalMatrix;
uniform mat4 g_WorldViewProjectionMatrix;
uniform vec3 g_CameraPosition;
uniform vec3 g_CameraDirection;

/*
layout(std430, binding = 2) coherent buffer m_DebugBuffer
{
    int data[];
} v_DebugBuffer;
*/

in BlockInfo {
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
} blockInfo[];

out BlockVertex {
    flat ivec2 texPosition;
    flat int texSize;
    vec2 texCoord;
    vec3 normal;
    vec4 color;
    flat int blockID;
} blockVertex;

/*
    Amplify the block coordinate to full block mesh:

      Y               (0,1,0)            (1,1,0)          BkLtTp        BkRtTp
      ^                     +------------+                   +------------+
      |                     |\            \                  |\            \
      |                     | \            \                 | \            \
      |                     |  \(0,1,1)     \(1,1,1)         |  \FtLtTp      \FtRtTp
      |                     |   +------------+               |   +------------+
      |                     |   |0          2|               |   |            |
      +--------->X   (0,0,0)+   | (1,0,0)+   |         BkLtBm+   |  BkRtBm+   |
       \                     \  |            |                \  |            |
        \                     \ |            |                 \ |            |
         V                     \|1          3|                  \|            |
         Z               (0,0,1)+------------+(1,0,1)      FtLtBm+------------+FtRtBm
*/
void main(void) {
    vec4 position = gl_in[0].gl_Position;

//    if (m_DrawTransparent == blockInfo[0].isBlockTransparent && position.x < FLOAT_MIN_INFINITY) {
    if (m_DrawTransparent == blockInfo[0].isBlockTransparent) {
        vec2 texCoord1 = vec2(0, 0);
        vec2 texCoord2 = vec2(0, 1);
        vec2 texCoord3 = vec2(1, 0);
        vec2 texCoord4 = vec2(1, 1);

        vec4 BkLtTp = g_WorldViewProjectionMatrix * (position + vec4(0,1,0,1));
        vec4 BkRtTp = g_WorldViewProjectionMatrix * (position + vec4(1,1,0,1));
        vec4 BkLtBm = g_WorldViewProjectionMatrix * (position + vec4(0,0,0,1));
        vec4 BkRtBm = g_WorldViewProjectionMatrix * (position + vec4(1,0,0,1));
        vec4 FtLtTp = g_WorldViewProjectionMatrix * (position + vec4(0,1,1,1));
        vec4 FtRtTp = g_WorldViewProjectionMatrix * (position + vec4(1,1,1,1));
        vec4 FtLtBm = g_WorldViewProjectionMatrix * (position + vec4(0,0,1,1));
        vec4 FtRtBm = g_WorldViewProjectionMatrix * (position + vec4(1,0,1,1));

//        v_DebugBuffer.data[gl_PrimitiveIDIn] = blockInfo[0].textureSize;

        if (blockInfo[0].isFrontFaceVisible) {
            vec4 color = vec4(1, mod(position.x, 16) / 16, mod(position.y, 16) / 16, 1);

            vec3 normal = normalize(g_NormalMatrix * vec3(0, 0, 1));

            gl_Position = FtLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }

        if (blockInfo[0].isBackFaceVisible) {
            vec4 color = vec4(1, mod(position.x, 16) / 16, mod(position.y, 16) / 16, 1);
            vec3 normal = normalize(g_NormalMatrix * vec3(0, 0, -1));

            gl_Position = BkRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }

        if (blockInfo[0].isLeftFaceVisible) {
            vec4 color = vec4(mod(position.y, 16) / 16, 1, mod(position.z, 16) / 16, 1);
            vec3 normal = normalize(g_NormalMatrix * vec3(-1, 0, 0));

            gl_Position = BkLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }

        if (blockInfo[0].isRightFaceVisible) {
            vec4 color = vec4(mod(position.y, 16) / 16, 1, mod(position.z, 16) / 16, 1);
            vec3 normal = normalize(g_NormalMatrix * vec3(1, 0, 0));

            gl_Position = FtRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }

        if (blockInfo[0].isTopFaceVisible) {
            vec4 color = vec4(mod(position.x, 16) / 16, mod(position.z, 16) / 16, 1, 1);
            vec3 normal = normalize(g_NormalMatrix * vec3(0, 1, 0));

            gl_Position = BkLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtLtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtRtTp;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }

        if (blockInfo[0].isBottomFaceVisible) {
            vec4 color = vec4(mod(position.x, 16) / 16, mod(position.z, 16) / 16, 1, 1);
            vec3 normal = normalize(g_NormalMatrix * vec3(0, -1, 0));

            gl_Position = BkRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord1;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtRtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord2;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = BkLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord3;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            gl_Position = FtLtBm;
            blockVertex.texPosition = blockInfo[0].textureCoord;
            blockVertex.texSize = blockInfo[0].textureSize;
            blockVertex.texCoord = texCoord4;
            blockVertex.normal = normal;
            blockVertex.color = color;
            blockVertex.blockID = gl_PrimitiveIDIn;
            EmitVertex();

            EndPrimitive();
        }
    }
}