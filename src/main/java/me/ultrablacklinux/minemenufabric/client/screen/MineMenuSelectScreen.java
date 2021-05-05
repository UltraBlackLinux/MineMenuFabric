package me.ultrablacklinux.minemenufabric.client.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.util.AngleHelper;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import me.ultrablacklinux.minemenufabric.client.util.RandomUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Map;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;


/**
 * The actual menu rendering is by FlashyReese
 */


public class MineMenuSelectScreen extends Screen {
    private JsonObject jsonItems; //MUST NEVER BE STATIC - WILL BE NULL OTHERWISE
    private int circleEntries; //at least 5!
    private int outerRadius;
    private int innerRadius;
    private ItemStack skull;
    private String skullowner;

    public MineMenuSelectScreen(JsonObject menuData, String menuTitle, Screen parent) {
        super(Text.of(menuTitle));
        this.jsonItems = menuData;
        this.skull = RandomUtil.itemStackFromString(Config.get().minemenuFabric.emptyItemIcon);

        if (parent == null) datapath = new ArrayList<>();


        circleEntries = Config.get().minemenuFabric.menuEntries;
        outerRadius = Config.get().minemenuFabric.outerRadius;
        innerRadius = Config.get().minemenuFabric.innerRadius;
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

            JsonObject iconData = value.get("icon").getAsJsonObject();
            this.skullowner = iconData.get("skullOwner").getAsString();
            ItemStack i;
            if (MineMenuFabricClient.playerHeadData.containsKey(iconData.get("skullOwner").getAsString()) && !iconData.get("skullOwner").getAsString().isEmpty()) {
                client.getItemRenderer().renderInGui(playerHeadData.get(iconData.get("skullOwner").getAsString()), drawX, drawY);
            }
            else {
                 i = RandomUtil.iconify(this::setSkullMap, iconData.get("iconItem").getAsString(),
                        iconData.get("enchanted").getAsBoolean(), iconData.get("skullOwner").getAsString());
                if (i == null) try {
                    client.getItemRenderer().renderInGui(playerHeadData.get(iconData.get("skullOwner").getAsString()), drawX, drawY);
                } catch (Exception e) {}
                else client.getItemRenderer().renderInGui(i, drawX, drawY);
            }

            int primaryColor;
            try { primaryColor = RandomUtil.getColor(Config.get().minemenuFabric.primaryColor).getColor(); }
            catch (Exception e) { primaryColor = RandomUtil.getColor("#A00000CC").getColor(); }

            int secondaryColor;
            try { secondaryColor = RandomUtil.getColor(Config.get().minemenuFabric.secondaryColor).getColor(); }
            catch (Exception e) { secondaryColor = RandomUtil.getColor("#212121D0").getColor(); }

            if (isHovered) {
                this.drawDoughnutSegment(matrixStack,
                        currentAngle, currentAngle + degrees / 2, centerX, centerY,
                        outerRadius + 5, innerRadius,
                        primaryColor);
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2, currentAngle + degrees, centerX, centerY,
                        outerRadius + 5, innerRadius,
                        primaryColor);

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
                        secondaryColor);
                this.drawDoughnutSegment(matrixStack,
                        currentAngle + degrees / 2,
                        currentAngle + degrees,
                        centerX, centerY,
                        outerRadius, innerRadius,
                        secondaryColor);
            }

            currentAngle += degrees;
            currentAngle = (int) AngleHelper.correctAngle(currentAngle);
        }
    }

    private void setSkullMap(ItemStack itemStack) {
        MineMenuFabricClient.playerHeadData.put(skullowner, itemStack);
    }

    @Override
    public void tick() {
        if (true){ //keybinding mode - hold or pressed
            if (keyBinding.wasPressed()) { //check for keybinding pressed
                final double mouseX = this.client.mouse.getX() * ((double) this.client.getWindow().getScaledWidth() /
                        this.client.getWindow().getWidth());
                final double mouseY = this.client.mouse.getY() * ((double) this.client.getWindow().getScaledHeight() /
                        this.client.getWindow().getHeight());

                this.mouseClicked(mouseX, mouseY, 0);
            }
        }
    }

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
                    datapath.add(entry.getKey());
                    if (button == 0) {
                        if (value.get("type").getAsString() == "category") {
                            datapath.add("data");
                            GsonUtil.saveJson(GsonUtil.fixEntryAmount(value.get("data").getAsJsonObject()));
                            client.openScreen(new MineMenuSelectScreen(value.get("data").getAsJsonObject(),
                                    value.get("name").getAsString(), this));
                        } else {
                            switch (value.get("type").getAsString()) {
                                case "empty":
                                    this.openConfigScreen();
                                    break;

                                case "print":
                                    client.player.sendChatMessage(
                                            value.get("data").getAsJsonObject().get("message").getAsString());
                                    break;

                                case "clipboard":
                                    this.client.keyboard.setClipboard(value.get("data").getAsJsonObject().get("message").getAsString());
                                    break;

                                case "link":
                                    Util.getOperatingSystem().open(value.get("data").getAsJsonObject().get("link").getAsString());
                                    break;
                            }
                            this.client.openScreen(null);
                        }
                    }
                    else if (button == 1) {
                        this.openConfigScreen();
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

    private void openConfigScreen() {
        try {
            client.openScreen(new MineMenuSettingsScreen(this, datapath));
        } catch (NullPointerException e) {
            client.openScreen(null);
            client.player.sendMessage(Text.of("§l§c Corrupt config! Reset it via the config menu!"), false);
        }
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