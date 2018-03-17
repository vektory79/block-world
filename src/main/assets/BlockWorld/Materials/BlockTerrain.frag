uniform sampler2D m_ColorMap;

/*
layout(std430, binding = 5) coherent buffer m_DebugBuffer
{
    int data[];
} v_DebugBuffer;
*/

in BlockVertex {
    flat ivec2 texPosition;
    flat int texSize;
    vec2 texCoord;
    vec3 normal;
    flat int blockID;
} blockVertex;

layout (location = 0) out vec4 color;

void main(){
    ivec2 tx = blockVertex.texPosition + ivec2(blockVertex.texCoord * blockVertex.texSize);
    ivec2 size = textureSize(m_ColorMap, 0);
    vec2 texCoord = vec2(float(tx.x) / float(size.x), float(size.y - tx.y - 1) / float(size.y));
    color = texture(m_ColorMap, texCoord);
//    v_DebugBuffer.data[blockVertex.blockID] = float(tx.y) / float(size.y);
}
