package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;

public class MineMenuSettingsScreen extends Screen {
    private TypeCycle typeCycle;
    private ButtonWidget done;
    private TextFieldWidget itemName;
    private TextFieldWidget itemIcon;
    private TextFieldWidget itemData;
    private ButtonWidget type;

    private final Screen parent;

    public MineMenuSettingsScreen(Screen parent, ArrayList<String> datapath) {
        super(new TranslatableText("minemenu.settings.title"));

        MineMenuFabricClient.datapath = datapath; //TODO fuck this
        this.parent = parent;
    }

    public void tick() {
        this.itemIcon.tick();
        this.itemName.tick();
    }

    protected void init() {
        JsonObject data = minemenuData;

        for (String s : datapath) data = data.get(s).getAsJsonObject();

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
            this.updateButtonActiveState();
        }));


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

    private void updateButtonActiveState() {
        this.done.active = typeCycle == TypeCycle.EMPTY || !this.itemName.getText().isEmpty();
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
                this.width / 2 - 100, 30, 0xFFa0a0a0);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Icon:"),
                this.width / 2 - 100, 70, 0xFFa0a0a0);
        drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Text:"),
                this.width / 2 - 100, 110, 0xFFa0a0a0);
        this.itemName.render(matrices, mouseX, mouseY, delta);
        this.itemIcon.render(matrices, mouseX, mouseY, delta);
        this.itemData.render(matrices, mouseX, mouseY, delta);

        ItemStack stack;
        try {
            stack = Registry.ITEM.get(new Identifier(itemIcon.getText())).getDefaultStack();
        } catch (InvalidIdentifierException e) {
            stack = new ItemStack(Items.AIR);
        }
        client.getItemRenderer().renderInGui(stack, this.width / 2 - 120, 82);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void close(boolean cancel) {
        if (!cancel) applySettings();
        if (datapath.get(datapath.size()-1).equals("data")) for (int i = 0; i < 2; i++) datapath.remove(datapath.size()-1);
        if (datapath.size() != 0) datapath.remove(datapath.size()-1);
        this.client.keyboard.setRepeatEvents(false);
        this.client.openScreen(this.parent);
    }

    private void applySettings() {
        String name = this.itemName.getText();
        String icon = this.itemIcon.getText();
        String data = this.itemData.getText();
        JsonObject subData = new JsonObject();

        switch (typeCycle) {
            case EMPTY:
                name = "";
                icon = Config.get().minemenuFabric.emptyItemIcon;
                break;
            case PRINT:
                subData.add("message", new JsonPrimitive(data));
                break;
            case LINK:
                subData.add("url", new JsonPrimitive(data));
                break;
            case CATEGORY:
                subData = new JsonObject();
                break;
        }

        GsonUtil.saveJson(GsonUtil.template(name, icon, typeCycle.name().toLowerCase(), subData));
        Config.get().minemenuFabric.minemenuData = minemenuData;
        AutoConfig.getConfigHolder(Config.class).save();
    }
}
