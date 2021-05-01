package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collections;

public class MineMenuSettingsScreen extends Screen {
    TypeCycle typeCycle;
    private ButtonWidget done;
    private TextFieldWidget itemName;
    private TextFieldWidget itemIcon;
    private TextFieldWidget itemData;
    private ButtonWidget type;

    private final Screen parent;
    ArrayList<String> datapath;

    public MineMenuSettingsScreen(Screen parent, ArrayList<String> dataPath) {
        super(new TranslatableText("minemenu.settings.title"));
        this.parent = parent;
        this.datapath = dataPath;
    }

    public void tick() {
        this.itemIcon.tick();
        this.itemName.tick();
    }

    protected void init() {
        JsonObject data = MineMenuFabricClient.minemenuData;
        for (String s : datapath) {
            data = data.get(s).getAsJsonObject();
        }

        System.out.println(data);

        typeCycle = TypeCycle.valueOf(data.get("type").getAsString().toUpperCase());

        this.client.keyboard.setRepeatEvents(true);

        this.itemName = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20,
                new TranslatableText("minemenu.settings.name"));
        this.itemName.setText(data.get("name").getAsString());
        this.itemName.setChangedListener(this::buttonUpdate);
        this.itemName.setMaxLength(32500);
        this.children.add(this.itemName);

        this.itemIcon = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 80, 200, 20,
                new TranslatableText("minemenu.settings.icon"));
        this.itemIcon.setText(data.get("icon").getAsString());
        this.itemIcon.setMaxLength(32500);
        this.children.add(this.itemIcon);

        this.itemData = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 120, 200, 20,
                new TranslatableText("minemenu.settings.data"));
        if (typeCycle == TypeCycle.PRINT) {
            this.itemData.setText(data.get("data").getAsJsonObject().get("message").getAsString());
        }
        this.itemData.setMaxLength(32500);
        this.children.add(this.itemData);


        this.type = this.addButton(new ButtonWidget(this.width / 2 - 100, 160, 200,
                20, typeCycle.getName(), (buttonWidget) -> {
            typeCycle = typeCycle.next();
            this.type.setMessage(typeCycle.getName());
            this.updateTextBoxActiveState();
        }));

        System.out.println(this.height / 4 + 120 + 18);


        this.addButton(new ButtonWidget(this.width / 2 - 100, 200, 100, 20,
                ScreenTexts.CANCEL, (buttonWidget) -> close(true)));

        this.done = this.addButton(new ButtonWidget(this.width / 2, 200, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(false)));

        this.updateButtonActiveState();
        this.updateTextBoxActiveState();
    }

    private void buttonUpdate(String s) {
        this.updateButtonActiveState();
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.itemName.getText();
        String string2 = this.itemIcon.getText();
        this.init(client, width, height);
        this.itemName.setText(string);
        this.itemIcon.setText(string2);
    }

    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }

    private void close(boolean cancel) {
        if (!cancel) applySettings();
        this.client.openScreen(this.parent);
    }

    private void updateButtonActiveState() {
        this.done.active = !this.itemName.getText().isEmpty() || typeCycle == TypeCycle.EMPTY;
    }
    private void updateTextBoxActiveState() {
        this.itemName.setEditable(typeCycle != TypeCycle.EMPTY);
        this.itemIcon.setEditable(typeCycle != TypeCycle.EMPTY);
        this.itemData.setEditable(typeCycle == TypeCycle.PRINT);
    }
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 17, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Name:"),
                this.width / 2 - 100, 30, 10526880);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Icon:"),
                this.width / 2 - 100, 70, 10526880);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Text:"),
                this.width / 2 - 100, 110, 10526880);
        this.itemName.render(matrices, mouseX, mouseY, delta);
        this.itemIcon.render(matrices, mouseX, mouseY, delta);
        this.itemData.render(matrices, mouseX, mouseY, delta);

        ItemStack stack = Registry.ITEM.get(new Identifier(itemIcon.getText())).getDefaultStack();
        client.getItemRenderer().renderInGui(stack, this.width / 2 - 120, 80);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void applySettings() {
        String name = this.itemName.getText();
        String icon = this.itemIcon.getText();
        String data = this.itemData.getText(); //TODO somehow make the data work xD
        JsonObject subData = new JsonObject();

        if (typeCycle == TypeCycle.EMPTY) {
            name = "";
            icon = "minecraft:air";
        }

        JsonObject building = MineMenuFabricClient.minemenuData;
        ArrayList<String> d = datapath;

        System.out.println(building); //----------------

        for (String s : d) {
            building = building.get(s).getAsJsonObject();
        }
        building.add("name", new JsonPrimitive(name));
        building.add("icon", new JsonPrimitive(icon));
        //building.add("data", new JsonPrimitive(icon));

        while (d.size() != 0) {
            JsonObject tmp = MineMenuFabricClient.minemenuData;
            for (String s : d) tmp = tmp.get(s).getAsJsonObject();
            tmp.add(d.get(d.size()-1), building);
            d.remove(d.size()-1);
        }
        System.out.println(building); //----------------
    }
}

//TODO LiteralText -> Translatable Text for idk some reason
//TODO callback: this.callback.accept("string"); - what comes out at the end
//TODO callback2: this.client.openScreen(new AddServerScreen(this, this::editEntry, this.selectedEntry)); - what uses the stuff on the other end
//TODO datapath: jsobj.get().get().get()...add()
