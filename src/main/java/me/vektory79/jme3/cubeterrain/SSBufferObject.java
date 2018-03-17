package me.vektory79.jme3.cubeterrain;

import com.jme3.shader.BufferObject;

import java.nio.ByteBuffer;

public class SSBufferObject extends BufferObject {
    private ByteBuffer data;

    public SSBufferObject(int binding) {
        super(binding, BufferType.ShaderStorageBufferObject);
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    public ByteBuffer computeData(int maxSize) {
        return data;
    }
}
