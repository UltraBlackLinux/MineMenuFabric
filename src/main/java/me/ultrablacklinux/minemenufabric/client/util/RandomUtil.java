package me.ultrablacklinux.minemenufabric.client.util;

import me.shedaniel.math.Color;

import java.awt.*;

public class RandomUtil {
    public static me.shedaniel.math.Color getColor(String inp) {
        long colorLong = Long.decode(inp);
        float f = (float) (colorLong >> 24 & 0xff) / 255F;
        float f1 = (float) (colorLong >> 16 & 0xff) / 255F;
        float f2 = (float) (colorLong >> 8 & 0xff) / 255F;
        float f3 = (float) (colorLong & 0xff) / 255F;
        return Color.ofRGBA(f, f1, f2, f3);
    }
}
