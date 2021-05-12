package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.screen.button.ItemConfigCycle;
import me.ultrablacklinux.minemenufabric.client.screen.button.TypeCycle;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import me.ultrablacklinux.minemenufabric.client.util.RandomUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;

public class MineMenuSettingsScreen extends Screen {
    private TypeCycle typeCycle;
    private ItemConfigCycle itemConfigCycle;
    private ButtonWidget done;
    private TextFieldWidget itemName;
    private ButtonWidget itemSettingType;
    private TextFieldWidget itemData;
    private ButtonWidget type;
    private TextFieldWidget iconDataText;
    private ButtonWidget iconDataYesNo;

    private boolean iconDataBoolean;
    private String iconItem;
    private boolean enchanted;
    private String skullowner;
    private int customModelData;

    private final Screen parent;
    JsonObject data;

    public MineMenuSettingsScreen(Screen parent, ArrayList<String> datapath) {
        super(new TranslatableText("minemenu.settings.title"));
        this.parent = parent;
        MineMenuFabricClient.datapath = datapath; //TODO fuck this

        itemConfigCycle = ItemConfigCycle.ICON;

        data = minemenuData;
        for (String s : datapath) {
            data = data.get(s).getAsJsonObject();
        }

        JsonObject iconData = data.get("icon").getAsJsonObject();
        this.skullowner = iconData.get("skullOwner").getAsString();
        this.enchanted = iconData.get("enchanted").getAsBoolean();
        this.iconItem = iconData.get("iconItem").getAsString();
        this.customModelData = iconData.get("customModelData").getAsInt();
        this.iconDataBoolean = false;
        typeCycle = TypeCycle.valueOf(data.get("type").getAsString().toUpperCase());
    }

    public void tick() {
        this.itemName.tick();
        this.iconDataText.tick();
        this.itemData.tick();
    }

    protected void init() {
        this.client.keyboard.setRepeatEvents(true);

        this.itemName = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20,
                new TranslatableText("minemenu.settings.name"));
        this.itemName.setMaxLength(32500);
        this.itemName.setText(data.get("name").getAsString());
        this.itemName.setChangedListener(this::updateInput);
        this.children.add(this.itemName);

        this.iconDataText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 101, 200, 20,
                        new TranslatableText("minemenu.settings.icon.data"));
        this.iconDataText.setMaxLength(32500);
        this.iconDataText.setChangedListener(this::saveIconVariable);
        this.children.add(this.iconDataText);

        this.iconDataYesNo = this.addButton(
                new ButtonWidget(this.width / 2 + 1, 80, 101, 20, Text.of(""), (buttonWidget) -> {
                    this.iconDataBoolean = !iconDataBoolean;
                    this.saveIconVariable();
                    this.updateInput();
        }));

        this.itemSettingType = this.addButton(new ButtonWidget(this.width / 2 - 101, 80, 102,
                20, itemConfigCycle.getName(), (buttonWidget) -> {
            this.saveIconVariable();
            itemConfigCycle = itemConfigCycle.next();
            if (itemConfigCycle == ItemConfigCycle.SKULLOWNER &&
                    !this.iconItem.replace("minecraft:", "").equals("player_head")) {
                itemConfigCycle = itemConfigCycle.next();
            }
            this.itemSettingType.setMessage(itemConfigCycle.getName());
            this.updateInput();
        }));

        //----------------------------

        this.itemData = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 141, 200, 20, //TEXT INPUT
                new TranslatableText("minemenu.settings.data"));
        this.itemData.setMaxLength(32500);
        this.children.add(this.itemData);

        this.type = this.addButton(new ButtonWidget(this.width / 2 - 100, 180, 200, //TYPE
                20, typeCycle.getName(), (buttonWidget) -> {
            typeCycle = typeCycle.next();
            this.type.setMessage(typeCycle.getName());
            this.updateInput();
        }));

        //----------------------------

        this.addButton(new ButtonWidget(this.width / 2 - 100, 220, 100, 20, //DONE CANCEL
                ScreenTexts.CANCEL, (buttonWidget) -> close(true)));

        this.done = this.addButton(new ButtonWidget(this.width / 2, 220, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(false)));

        this.readTypes(data);
        this.updateInput();
    }

    private void updateInput() {
        this.iconDataText.setEditable(itemConfigCycle != ItemConfigCycle.ENCHANTED && typeCycle != TypeCycle.EMPTY);
        iconDataBoolean = false;

        switch (itemConfigCycle) {
            case ICON:
                this.iconDataText.setText(this.iconItem);
                break;

            case ENCHANTED:
                iconDataBoolean = enchanted;

            case CUSTOMMODELDATA:
                this.iconDataText.setText(String.valueOf(this.customModelData));
                break;

            case SKULLOWNER:
                iconDataText.setEditable(this.iconItem.replace("minecraft:", "").equals("player_head"));
                this.iconDataText.setText(this.skullowner);
                break;
        }

        switch(typeCycle) {
            case PRINT:
            case LINK:
            case CLIPBOARD:
            case CHATBOX:
                this.itemData.setEditable(true);
                break;

            default:
                this.itemData.setEditable(false);
                break;
        }

        this.itemSettingType.active = typeCycle != TypeCycle.EMPTY;

        this.iconDataYesNo.active = itemConfigCycle == ItemConfigCycle.ENCHANTED;
        if (itemConfigCycle != ItemConfigCycle.ENCHANTED) this.iconDataYesNo.setMessage(Text.of(""));
        else this.iconDataYesNo.setMessage(iconDataBoolean ? ScreenTexts.YES : ScreenTexts.NO);
        this.itemName.setEditable(typeCycle != TypeCycle.EMPTY);
        this.done.active = typeCycle == TypeCycle.EMPTY || !this.itemName.getText().isEmpty();
    }

    private void saveIconVariable(String s) {
        this.saveIconVariable();
    }

    private void updateInput(String s) {
        this.updateInput();
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.itemName.getText();
        String string2 = this.iconDataText.getText();
        this.init(client, width, height);
        this.itemName.setText(string);
        this.iconDataText.setText(string2);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 17, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("minemenu.setting.input.name"),
                this.width / 2 - 100, 30, 0xFFa0a0a0);
        drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("minemenu.setting.input.icon"),
                this.width / 2 - 100, 70, 0xFFa0a0a0);
        drawTextWithShadow(matrices, this.textRenderer, new TranslatableText("minemenu.setting.input.text"),
                this.width / 2 - 100, 131, 0xFFa0a0a0);
        this.itemName.render(matrices, mouseX, mouseY, delta);
        if (itemConfigCycle != ItemConfigCycle.ENCHANTED) this.iconDataText.render(matrices, mouseX, mouseY, delta);
        this.iconDataYesNo.render(matrices, mouseX, mouseY, delta);
        this.itemData.render(matrices, mouseX, mouseY, delta);

        ItemStack i;
        if (playerHeadCache.containsKey(skullowner) && !skullowner.trim().isEmpty()) {
            client.getItemRenderer().renderInGui(playerHeadCache.get(skullowner), this.width / 2 - 120, 82);
        }
        else {
            i = RandomUtil.iconify(iconItem, enchanted, skullowner, customModelData);
            if (i == null) {
                try {
                    client.getItemRenderer().renderInGui(playerHeadCache.get(skullowner), this.width / 2 - 120, 82);
                } catch (Exception e) {}
            }
            else client.getItemRenderer().renderInGui(i, this.width / 2 - 120, 82);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void close(boolean cancel) {
        if (!cancel) applySettings();
        if (datapath.get(datapath.size()-1).equals("data")) for (int i = 0; i < 2; i++) datapath.remove(datapath.size()-1);
        if (datapath.size() != 0) datapath.remove(datapath.size()-1);
        this.client.keyboard.setRepeatEvents(false);
        this.client.openScreen(this.parent);
    }

    private void saveIconVariable() {
        switch (itemConfigCycle) {
            case ENCHANTED:
                this.enchanted = this.iconDataBoolean;
                break;

            case ICON:
                this.iconItem = this.iconDataText.getText();
                if (!iconItem.replace("minecraft:", "").equals("player_head")) this.skullowner = "";
                break;

            case CUSTOMMODELDATA:
                try { this.customModelData = Integer.parseInt(this.iconDataText.getText()); }
                catch (Exception e) { this.customModelData = 1;}
                break;

            case SKULLOWNER:
                this.skullowner = this.iconDataText.getText();
                break;
        }
    }

    private void applySettings() {
        String nameOut = this.itemName.getText();
        String dataTextOut = this.itemData.getText();
        JsonObject subDataOut = new JsonObject();
        skullowner = skullowner.replaceAll("[^a-zA-Z0-9_]", "");

        switch (typeCycle) {
            case EMPTY:
                nameOut = "";
                iconItem = "";
                enchanted = false;
                skullowner = "";
                break;

            case PRINT:
            case CHATBOX:
            case CLIPBOARD:
                subDataOut.add("message", new JsonPrimitive(dataTextOut));
                break;

            case LINK:
                subDataOut.add("link", new JsonPrimitive(dataTextOut));
                break;

            case CATEGORY:
                if (!this.data.has("link") && !this.data.has("message")) {
                    subDataOut = this.data.get("data").getAsJsonObject();
                }
                GsonUtil.fixEntryAmount(subDataOut);
                break;
        }

        GsonUtil.saveJson(GsonUtil.template(nameOut, typeCycle.name().toLowerCase(), subDataOut, iconItem, enchanted,
                skullowner, customModelData));
        Config.get().minemenuFabric.minemenuData = minemenuData;
        AutoConfig.getConfigHolder(Config.class).save();
    }

    private void readTypes(JsonObject data) {
        switch (typeCycle) {
            case PRINT:
            case CLIPBOARD:
            case CHATBOX:
                try {
                    this.itemData.setText(data.get("data").getAsJsonObject().get("message").getAsString());
                } catch (Exception e) {};
                break;

            case LINK:
                try {
                    this.itemData.setText(data.get("data").getAsJsonObject().get("link").getAsString()); //TODO change to "data":"link"
                } catch (Exception e) {};
                break;
        }
    }
}
