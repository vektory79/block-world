package me.vektory79.utils;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.function.Supplier;

public final class ArrayUtils {
    private ArrayUtils() {

    }

    @NotNull
    public static byte[] bytesFrom(float... values) {
        byte[] bytes = new byte[values.length * Float.BYTES];
        ByteBuffer byteBuf = ByteBuffer.wrap(bytes);
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();
        floatBuf.put(values);
        return bytes;
    }

    @NotNull
    public static byte[] bytesFrom(long... values) {
        byte[] bytes = new byte[values.length * Long.BYTES];
        ByteBuffer byteBuf = ByteBuffer.wrap(bytes);
        LongBuffer longBuf = byteBuf.asLongBuffer();
        longBuf.put(values);
        return bytes;
    }

    @NotNull
    public static float[] floatsFrom(byte... values) {
        float[] floats = new float[values.length / Float.BYTES];
        ByteBuffer byteBuf = ByteBuffer.wrap(values);
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();
        floatBuf.get (floats);
        return floats;
    }

    @NotNull
    public static long[] longsFrom(byte... values) {
        long[] longs = new long[values.length / Long.BYTES];
        ByteBuffer byteBuf = ByteBuffer.wrap(values);
        LongBuffer longBuf = byteBuf.asLongBuffer();
        longBuf.get (longs);
        return longs;
    }

    @NotNull
    public static float[] floatsFrom(@NotNull Vector3f... values) {
        return floatsFrom(values.length, values);
    }

    @NotNull
    public static float[] floatsFrom(int count, @NotNull Vector3f... values) {
        float[] floats = new float[count * 3];
        for (int i = 0; i < count; i++) {
            floats[i * 3] = values[i].x;
            floats[i * 3 + 1] = values[i].y;
            floats[i * 3 + 2] = values[i].z;
        }
        return floats;
    }

    @NotNull
    public static float[] floatsFrom(@NotNull Vector2f... values) {
        return floatsFrom(values.length, values);
    }

    @NotNull
    public static float[] floatsFrom(int count, @NotNull Vector2f... values) {
        float[] floats = new float[count * 2];
        for (int i = 0; i < count; i++) {
            floats[i * 2] = values[i].x;
            floats[i * 2 + 1] = values[i].y;
        }
        return floats;
    }

    @NotNull
    public static <T> T[] fill(@NotNull T[] array, @NotNull Supplier<T> generator) {
        for (int i = 0; i < array.length; i++) {
            array[i] = generator.get();
        }
        return array;
    }
}
