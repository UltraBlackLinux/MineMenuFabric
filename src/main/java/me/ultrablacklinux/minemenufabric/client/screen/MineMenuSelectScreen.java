package me.ultrablacklinux.minemenufabric.client.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.util.AngleHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.Map;


/**
 * @variable circleEntries entries in the circle
 */


public class MineMenuSelectScreen extends Screen {
    public static JsonObject jsonItems = new JsonObject();
    private final int circleEntries = 8;


    public MineMenuSelectScreen(JsonObject menuData) {
        super(new TranslatableText("minemenu.menu.title"));
        jsonItems = menuData;
        //sortItems();

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderGui(matrices, mouseX, mouseY);
    }

    public static void sortItems() {
        JsonObject out = new JsonObject();
        int index = 0;
        while(out.size() <= jsonItems.size()) {
            for(Map.Entry<String, JsonElement> entry : jsonItems.entrySet()) {
                int compare = Integer.parseInt(entry.getKey());
                if (compare == index) {
                    out.add(entry.getKey(), entry.getValue());
                    index++;
                }
            }
        }
        jsonItems = out;
    }


    private void renderGui(MatrixStack matrixStack, int mouseX, int mouseY) {
        int circleEntries = jsonItems.size();
        int outerRadius = 15 * circleEntries;
        int innerRadius = 5  * circleEntries;

        //int circleEntries = PingType.values().length - 1;

        int centerX = this.client.getWindow().getScaledWidth() / 2;
        int centerY = this.client.getWindow().getScaledHeight() / 2;
        this.client.textRenderer.draw(matrixStack, this.title,
                centerX - this.client.textRenderer.getWidth(this.title) / 2.0F,
                centerY - outerRadius - 20,
                0xFFFFFF);

        int degrees = (int) (360.0D / circleEntries);
        int currentAngle = 360 - degrees / 2;
        int mouseAngle = (int) AngleHelper.getMouseAngle();

        for (Map.Entry<String, JsonElement> entry : jsonItems.entrySet()) {
            JsonObject value = entry.getValue().getAsJsonObject();

            int nextAngle = currentAngle + degrees;
            nextAngle = (int) AngleHelper.correctAngle(nextAngle);

            boolean mouseIn = AngleHelper.isAngleBetween(mouseAngle, currentAngle, nextAngle);

            boolean isHovered = !AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, 25)
                    && AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, 75)
                    && mouseIn;

            double drawX = centerX;
            double drawY = centerY;

            double sin = Math.sin(Math.toRadians(currentAngle + degrees * 0.5D));
            double cos = Math.cos(Math.toRadians(currentAngle + degrees * 0.5D));

            double outerPointX = (isHovered ? outerRadius + 5 : outerRadius) * sin;
            double outerPointY = (isHovered ? outerRadius + 5 : outerRadius) * cos;
            double innerPointX = innerRadius * sin;
            double innerPointY = innerRadius * cos;

            drawX += (outerPointX + innerPointX) / 2;
            drawY -= (outerPointY + innerPointY) / 2;

            /*
            float min = -32 / 2.0F;
            float max = 32 / 2.0F;

            matrixStack.push();
            RenderSystem.enableBlend();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            //MinecraftClient.getInstance().getTextureManager().bindTexture(PingHandler.TEXTURE);
            // Button Icon
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
            bufferBuilder.vertex(drawX + min, drawY + min, 0).texture(type.getMinU(),
                    type.getMinV()).color(255, 255, 255, 255).next();

            tessellator.draw();
            RenderSystem.disableBlend();
            matrixStack.pop();
             */

            if (isHovered) {
                this.drawDoughnutSegment(matrixStack,
                        currentAngle,
                        currentAngle + degrees / 2, centerX, centerY,
                        outerRadius + 5,
                        innerRadius, 0xCC000000); //TODO CUSTOM COLOR
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2,
                        currentAngle + degrees, centerX, centerY,
                        outerRadius + 5, innerRadius,
                        0xCC000000);

                this.client.textRenderer.draw(matrixStack,
                        value.get("name").getAsString(),
                        centerX - this.client.textRenderer.getWidth(value.get("name").getAsString()) / 2.0F,
                        centerY + outerRadius + 10, 0xFFFFFF);
            } else {
                this.drawDoughnutSegment(matrixStack,
                        currentAngle,
                        currentAngle + degrees / 2, centerX, centerY,
                        outerRadius, innerRadius,
                        0x90000000);
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2,
                        currentAngle + degrees,
                        centerX, centerY,
                        outerRadius, innerRadius,
                        0x90000000);
            }

            currentAngle += degrees;
            currentAngle = (int) AngleHelper.correctAngle(currentAngle);
        }
    }

    @Override
    public void tick() {
        if (true){ //keybinding mode - hold or pressed
            if (MineMenuFabricClient.keyBinding.wasPressed()) { //check for keybinding pressed
                final double mouseX = this.client.mouse.getX() * ((double) this.client.getWindow().getScaledWidth() /
                        this.client.getWindow().getWidth());
                final double mouseY = this.client.mouse.getY() * ((double) this.client.getWindow().getScaledHeight() /
                        this.client.getWindow().getHeight());

                this.mouseClicked(mouseX, mouseY, 0);
            }
        }
    }


    /**
     * @variable button Index of item in list
     */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int circleEntries = jsonItems.size();
        int centerX = this.client.getWindow().getScaledWidth() / 2;
        int centerY = this.client.getWindow().getScaledHeight() / 2;
        if (!AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, 25)
                && AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, 75)) {
            //int circleEntries = 13; //circle entries

            int degrees = (int) (360.0D / circleEntries);
            int currentAngle = 360 - degrees / 2;
            int mouseAngle = (int) AngleHelper.getMouseAngle();

            for (Map.Entry<String, JsonElement> entry : jsonItems.entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                int nextAngle = currentAngle + degrees;
                nextAngle = (int) AngleHelper.correctAngle(nextAngle);

                boolean mouseIn = AngleHelper.isAngleBetween(mouseAngle, currentAngle, nextAngle);
                if (mouseIn) {
                    //action - do something with the circleEntry
                    if (value.get("type").getAsString().equals("print")) client.player.sendChatMessage(
                            value.get("data").getAsJsonObject().get("message").getAsString());

                    if (value.get("type").getAsString().equals("category")) client.openScreen(new MineMenuSelectScreen(
                            value.get("data").getAsJsonObject()));
                }

                currentAngle += degrees;
                currentAngle = (int) AngleHelper.correctAngle(currentAngle);
            }
        }
        this.client.openScreen(null);
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public void drawDoughnutSegment(MatrixStack matrixStack, int startingAngle, int endingAngle, float centerX,
                                    float centerY, double outerRingRadius, double innerRingRadius, int color) {
        float f = (float) (color >> 24 & 0xff) / 255F;
        float f1 = (float) (color >> 16 & 0xff) / 255F;
        float f2 = (float) (color >> 8 & 0xff) / 255F;
        float f3 = (float) (color & 0xff) / 255F;
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = startingAngle; i <= endingAngle; i++) {
            double x = Math.sin(Math.toRadians(i)) * innerRingRadius;
            double y = Math.cos(Math.toRadians(i)) * innerRingRadius;
            bufferBuilder.vertex(centerX + x, centerY - y, 0).color(f1, f2, f3, f).next();
        }
        for (int i = endingAngle; i >= startingAngle; i--) {
            double x = Math.sin(Math.toRadians(i)) * outerRingRadius;
            double y = Math.cos(Math.toRadians(i)) * outerRingRadius;
            bufferBuilder.vertex(centerX + x, centerY - y, 0).color(f1, f2, f3, f).next();
        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }
}
