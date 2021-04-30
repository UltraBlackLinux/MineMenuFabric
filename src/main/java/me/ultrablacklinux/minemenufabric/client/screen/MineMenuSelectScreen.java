package me.ultrablacklinux.minemenufabric.client.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.util.AngleHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MineMenuSelectScreen extends Screen {
    static JsonObject jsonItems;
    int circleEntries; //at least 5!
    int outerRadius;
    int innerRadius;

    //ArrayList<String> dataPath;


    public MineMenuSelectScreen(JsonObject menuData, String menuTitle, ArrayList<String> dataPath) {
        super(Text.of(menuTitle));
        jsonItems = null;
        jsonItems = menuData;
        //TODO sort?

        //this.dataPath = dataPath;

        circleEntries = jsonItems.size();
        outerRadius = 15 * circleEntries;
        innerRadius = 5 * circleEntries;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderGui(matrices, mouseX, mouseY);
    }

    private void renderGui(MatrixStack matrixStack, int mouseX, int mouseY) {
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

            boolean isHovered = !AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, innerRadius)
                    && AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY,  outerRadius)
                    && mouseIn;

            int drawX = centerX-8;
            int drawY = centerY-8;

            double sin = Math.sin(Math.toRadians(currentAngle + degrees * 0.5D));
            double cos = Math.cos(Math.toRadians(currentAngle + degrees * 0.5D));

            double outerPointX = (isHovered ? outerRadius + 5 : outerRadius) * sin;
            double outerPointY = (isHovered ? outerRadius + 5 : outerRadius) * cos;
            double innerPointX = innerRadius * sin;
            double innerPointY = innerRadius * cos;

            drawX += (outerPointX + innerPointX) / 2;
            drawY -= (outerPointY + innerPointY) / 2;


            ItemStack icon = Registry.ITEM.get(new Identifier(value.get("icon").getAsString())).getDefaultStack();

            client.getItemRenderer().renderInGui(icon, drawX, drawY);



            //0x00000000
            //0xOORRGGBB

            if (isHovered) {
                this.drawDoughnutSegment(matrixStack,
                        currentAngle, currentAngle + degrees / 2, centerX, centerY,
                        outerRadius + 5, innerRadius,
                        0xCCA00000); //TODO CUSTOM COLOR
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2, currentAngle + degrees, centerX, centerY,
                        outerRadius + 5, innerRadius,
                        0xCCA00000);

                this.client.textRenderer.draw(matrixStack,
                        value.get("name").getAsString(),
                        centerX - this.client.textRenderer.getWidth(value.get("name").getAsString()) / 2.0F,
                        centerY + outerRadius + 10,
                        0xFFFFFF);
            } else {
                this.drawDoughnutSegment(matrixStack,
                        currentAngle,
                        currentAngle + degrees / 2, centerX, centerY,
                        outerRadius, innerRadius,
                        0xD0212121);
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2,
                        currentAngle + degrees,
                        centerX, centerY,
                        outerRadius, innerRadius,
                        0xD0212121);
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
        int centerX = this.client.getWindow().getScaledWidth() / 2;
        int centerY = this.client.getWindow().getScaledHeight() / 2;
        if (!AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, innerRadius)
                && AngleHelper.isInsideCircle(mouseX, mouseY, centerX, centerY, outerRadius)) {

            int degrees = (int) (360.0D / circleEntries);
            int currentAngle = 360 - degrees / 2;
            int mouseAngle = (int) AngleHelper.getMouseAngle();

            for (Map.Entry<String, JsonElement> entry : jsonItems.entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                int nextAngle = currentAngle + degrees;
                nextAngle = (int) AngleHelper.correctAngle(nextAngle);


                boolean mouseIn = AngleHelper.isAngleBetween(mouseAngle, currentAngle, nextAngle);
                if (mouseIn) {
                    if (button == 0) {
                        switch (value.get("type").getAsString()) {
                            case "print":
                                client.player.sendChatMessage(
                                        value.get("data").getAsJsonObject().get("message").getAsString());
                                this.client.openScreen(null);
                                break;

                            case "category":
                                client.openScreen(new MineMenuSelectScreen(entry, value.get("name").getAsString()));
                                break;
                        }
                    }
                    else if (button == 1) {
                        client.openScreen(new MineMenuSettingsScreen(
                                this, this::doStuff, new ArrayList<>(Collections.singletonList(""))));
                    }
                }

                currentAngle += degrees;
                currentAngle = (int) AngleHelper.correctAngle(currentAngle);
            }
        }
        else {
            this.client.openScreen(null);
        }
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


    private void doStuff(JsonObject inp) {
        System.out.println(inp);
    }
}