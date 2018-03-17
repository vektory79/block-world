package me.vektory79.mapgen;

import me.vektory79.utils.ArrayUtils;
import me.vektory79.utils.SipHashInline;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class MapGenerator {

    private static final double NORMALIZE = 1.0 / (double) Integer.MAX_VALUE;
    private final int sizeFactor;
    private final int size;
    private final int size2;
    private final double roughness;
    private final long seed;
    @NotNull
    private final double[] data;

    public MapGenerator(int sizeFactor, double roughness, long seed) {
        this.sizeFactor = sizeFactor;
        size = 1 << sizeFactor;
        size2 = size * size;
        this.roughness = roughness;
        this.seed = seed;
        data = new double[size2];
    }

    public final void generate() {
        for (int blockSize = size >> 1; blockSize >= 1; blockSize >>= 1) {
            square(blockSize);
            diamond(blockSize);
        }
        // filter();
    }

    private void square(int blockSize) {
        int x = blockSize;
        int y = blockSize;
        int step = blockSize << 1;
        while (true) {
            double value = getData(x - blockSize, y - blockSize)
                    + getData(x - blockSize, y + blockSize)
                    + getData(x + blockSize, y + blockSize)
                    + getData(x + blockSize, y - blockSize);
            value *= 0.25D;
            value = displace(value, blockSize, x, y);
            setDataFast(x, y, value);
            x += step;
            if (x >= size) {
                x = blockSize;
                y += step;
            }
            // TODO: inline in while condition
            if (y >= size) {
                break;
            }
        }
    }

    private void diamond(int blockSize) {
        int x = blockSize;
        int y = 0;
        int stepX = blockSize << 1;
        int line = 0;
        while (true) {
            double value = getData(x - blockSize, y)
                    + getData(x + blockSize, y) + getData(x, y - blockSize)
                    + getData(x, y + blockSize);
            value *= 0.25D;
            value = displace(value, blockSize, x, y);
            setDataFast(x, y, value);
            x += stepX;
            if (x >= size) {
                x = blockSize & line;
                line ^= -1;
                y += blockSize;
            }
            // TODO: inline in while condition
            if (y >= size) {
                break;
            }
        }
    }

    @Contract(pure = true)
    public final double getData(int x, int y) {
        if (x < 0) {
            x = x + size;
        }
        if (x >= size) {
            x = x - size;
        }
        if (y < 0) {
            y = y + size;
        }
        if (y >= size) {
            y = y - size;
        }
        return data[(y << sizeFactor) + x];
    }

    public final void setData(int x, int y, double value) {
        if (x < 0) {
            x = x + size;
        }
        if (x >= size) {
            x = x - size;
        }
        if (y < 0) {
            y = y + size;
        }
        if (y >= size) {
            y = y - size;
        }
        setDataFast(x, y, value);
    }

    public final void setDataFast(int x, int y, double value) {
        if (value < -1D) {
            value = -1D;
        }
        if (value > 1D) {
            value = 1D;
        }
        data[(y << sizeFactor) + x] = value;
    }

    public final void filter() {
        for (int i = 0; i < size2; i++) {
            double value = data[i];
            if (value > 0D) {
                data[i] = Math.pow(value, 3.8D);
            } else if (value < 0D) {
                data[i] = -1D * Math.pow(-1D * value, 0.2D);
            }
        }
    }

    private double displace(double v, int blockSize, int x, int y) {
        return (v + (randFromPair(x, y) * 2 - 1.0) * blockSize * 2 / size
                * roughness);
    }

    private double randFromPair(long ix, long iy) {
        int hash = (int) SipHashInline.hash24(seed, seed, ArrayUtils.bytesFrom(ix, iy));
        return hash * NORMALIZE * 0.5 + 0.5;
    }

    public final void drawTo(@NotNull BufferedImage image) {
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
                .getData();
        for (int i = 0; i < size2; i++) {
            double v = data[i];
            double sign = (v < 0.0) ? -1.0 : 1.0;
            double color = v * sign;
            int r;
            int g;
            int b;
            if (sign < 0.0) {
                color = 1.0 - color;
                r = 0;
                g = 0;
                b = (int) (color * 127.0) + 128;
            } else {
                r = (int) (color * 127.0) + 128;
                g = (int) (color * 127.0) + 128;
                b = (int) (color * 127.0) + 128;
            }
            pixels[i] = (r << 16) + (g << 8) + b;
        }
    }
}
