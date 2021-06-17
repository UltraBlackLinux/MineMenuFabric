package me.ultrablacklinux.minemenufabric.client.util;

import net.minecraft.client.MinecraftClient;

/**
 * (c) 2021 FlashyReese, Girafi
 */

public class AngleHelper {

    public static double getMouseAngle() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return getRelativeAngle(mc.getWindow().getWidth() * 0.5D, mc.getWindow().getHeight() * 0.5D, mc.mouse.getX(), mc.mouse.getY());
    }

    private static double getRelativeAngle(double originX, double originY, double x, double y) {
        return correctAngle(Math.toDegrees(Math.atan2(y - originY, x - originX)) + 90);
    }

    public static double correctAngle(double angle) {
        if (angle < 0) {
            angle += 360;
        } else if (angle > 360) {
            angle -= 360;
        }
        return angle;
    }


    public static boolean isInsideCircle(double mouseX, double mouseY, double centerX, double centerY, int radius) {
        double distX = mouseX - centerX;
        double distY = mouseY - centerY;
        double distance = Math.sqrt((distX * distX) + (distY * distY));
        return distance <= radius;
    }

    public static boolean isAngleBetween(float target, float angle1, float angle2) {
        float rAngle = ((angle2 - angle1) % 360 + 360) % 360;
        if (rAngle > 180) {
            float temp = angle1;
            angle1 = angle2;
            angle2 = temp;
        }
        if (angle1 <= angle2)
            return target >= angle1 && target <= angle2;
        else
            return target >= angle1 || target <= angle2;
    }
}