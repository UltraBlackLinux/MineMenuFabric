package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MineMenuSettingsScreen extends Screen {
    TypeCycle typeCycle = TypeCycle.PRINT;

    private ButtonWidget done;

    private final Consumer<JsonObject> callback;

    private TextFieldWidget itemname;
    private TextFieldWidget itemIcon;

    private ButtonWidget type;
    private final Screen parent;

    JsonObject out;


    public MineMenuSettingsScreen(Screen parent, Consumer<JsonObject> callback) {
        super(new TranslatableText("minemenu.settings.title"));
        this.parent = parent;
        this.callback = callback;
    }

    public void tick() {
        this.itemIcon.tick();
        this.itemname.tick();
    }

    protected void init() {
        this.itemname = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 66, 200, 20,
                new TranslatableText("minemenu.settings.name"));
        this.itemname.setMaxLength(128);
        this.itemname.setChangedListener(this::textBoxUpdate);
        this.children.add(this.itemname);

        this.client.keyboard.setRepeatEvents(true);
        this.itemIcon = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 106, 200, 20,
                new TranslatableText("minemenu.settings.icon"));
        this.itemIcon.setText("minecraft:air");
        this.itemIcon.setChangedListener(this::textBoxUpdate);
        this.children.add(this.itemIcon);



        this.type = this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 72, 200,
                20, typeCycle.getName(), (buttonWidget) -> {
            typeCycle = typeCycle.next();
            this.type.setMessage(typeCycle.getName());
        }));


        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 18, 100, 20,
                ScreenTexts.CANCEL, (buttonWidget) -> close(null)));

        this.done = this.addButton(new ButtonWidget(this.width / 2, this.height / 4 + 120 + 18, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(out)));

        this.updateButtonActiveState();
    }

    private void textBoxUpdate(String s) {
        this.updateButtonActiveState();
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.itemname.getText();
        String string2 = this.itemIcon.getText();
        this.init(client, width, height);
        this.itemname.setText(string);
        this.itemIcon.setText(string2);
    }

    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }

    public void close(JsonObject output) {
        this.callback.accept(this.out);
        this.client.openScreen(this.parent);
    }

    private void updateButtonActiveState() {
        this.done.active = !this.itemname.getText().isEmpty();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 17, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Name:"),
                this.width / 2 - 100, 53, 10526880);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Icon:"),
                this.width / 2 - 100, 94, 10526880);
        this.itemname.render(matrices, mouseX, mouseY, delta);
        this.itemIcon.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}

//TODO LiteralText -> Translatable Text for idk some reason
//TODO callback: this.callback.accept("string"); - what comes out at the end
//TODO callback2: this.client.openScreen(new AddServerScreen(this, this::editEntry, this.selectedEntry)); - what uses the stuff on the other end

