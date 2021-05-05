package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import me.ultrablacklinux.minemenufabric.client.util.RandomUtil;
import me.ultrablacklinux.minemenufabric.client.screen.button.ItemConfigCycle;
import me.ultrablacklinux.minemenufabric.client.screen.button.TypeCycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;
import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.playerHeadData;

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

    private final Screen parent;

    public MineMenuSettingsScreen(Screen parent, ArrayList<String> datapath) {
        super(new TranslatableText("minemenu.settings.title"));

        MineMenuFabricClient.datapath = datapath; //TODO fuck this
        this.parent = parent;
        this.iconDataBoolean = false;
        itemConfigCycle = ItemConfigCycle.ICON;
        iconDataBoolean = true; //wtf, works I guess

        JsonObject data = minemenuData;
        for (String s : datapath) data = data.get(s).getAsJsonObject();
        JsonObject iconData = data.get("icon").getAsJsonObject();
        this.skullowner = iconData.get("skullOwner").getAsString();
        this.enchanted = iconData.get("enchanted").getAsBoolean();
        this.iconItem = iconData.get("iconItem").getAsString();
    }

    public void tick() {
        this.itemName.tick();
        this.iconDataText.tick();
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

        this.iconDataText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 101, 200, 20,
                        new TranslatableText("minemenu.settings.icon.data"));
        this.iconDataText.setMaxLength(32500);
        this.iconDataText.setChangedListener(this::saveIconVariable);
        this.children.add(this.iconDataText);

        this.iconDataYesNo = this.addButton(
                new ButtonWidget(this.width / 2 - 100+100, 80, 100, 20, Text.of(""), (buttonWidget) -> {
                    this.iconDataBoolean = !iconDataBoolean;
                    this.saveIconVariable();
                    this.updateIconInput();
        }));

        this.itemSettingType = this.addButton(new ButtonWidget(this.width / 2 - 100, 80, 100,
                20, itemConfigCycle.getName(), (buttonWidget) -> {
            this.saveIconVariable();
            itemConfigCycle = itemConfigCycle.next();
            this.itemSettingType.setMessage(itemConfigCycle.getName());
            this.updateIconInput();
        }));

        //----------------------------

        this.itemData = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 141, 200, 20, //TEXT INPUT
                new TranslatableText("minemenu.settings.data"));
        if (typeCycle == TypeCycle.PRINT || typeCycle == TypeCycle.CLIPBOARD) {
            this.itemData.setText(data.get("data").getAsJsonObject().get("message").getAsString());
        }
        else if (typeCycle == TypeCycle.LINK) {
            this.itemData.setText(data.get("data").getAsJsonObject().get("link").getAsString());
        }

        this.itemData.setMaxLength(32500);
        this.children.add(this.itemData);

        this.type = this.addButton(new ButtonWidget(this.width / 2 - 100, 180, 200, //TYPE
                20, typeCycle.getName(), (buttonWidget) -> {
            typeCycle = typeCycle.next();
            this.type.setMessage(typeCycle.getName());
            this.updateStates();
            this.updateStates();
        }));

        //----------------------------

        this.addButton(new ButtonWidget(this.width / 2 - 100, 220, 100, 20, //DONE CANCEL
                ScreenTexts.CANCEL, (buttonWidget) -> close(true)));

        this.done = this.addButton(new ButtonWidget(this.width / 2, 220, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(false)));

        this.updateStates();
        this.updateStates();
        this.updateIconInput();
    }

    private void saveIconVariable() {
        switch (itemConfigCycle) {
            case ENCHANTED:
                this.enchanted = this.iconDataBoolean;
                break;

            case ICON:
                this.iconItem = this.iconDataText.getText();
                break;

            case SKULLOWNER:
                if (!iconItem.endsWith("player_head")) this.skullowner = "";
                this.skullowner = this.iconDataText.getText();
                break;
        }
    }

    private void updateIconInput() {
        this.iconDataYesNo.active = itemConfigCycle == ItemConfigCycle.ENCHANTED;
        this.iconDataText.setEditable(itemConfigCycle != ItemConfigCycle.ENCHANTED);
        if (itemConfigCycle != ItemConfigCycle.ENCHANTED) this.iconDataYesNo.setMessage(Text.of(""));
        if (itemConfigCycle == ItemConfigCycle.SKULLOWNER) this.iconDataText.setEditable(iconItem.endsWith("player_head"));

        switch (itemConfigCycle) {
            case ENCHANTED:
                this.iconDataYesNo.setMessage(enchanted ? ScreenTexts.YES : ScreenTexts.NO);
                break;

            case ICON:
                this.iconDataText.setText(this.iconItem);
                break;

            case SKULLOWNER:
                this.iconDataText.setText(this.skullowner);
                break;
        }
    }

    private void updateStates() {
        this.itemName.setEditable(typeCycle != TypeCycle.EMPTY);
        this.itemData.setEditable(typeCycle == TypeCycle.PRINT || typeCycle == TypeCycle.LINK || typeCycle == TypeCycle.CLIPBOARD);
        this.done.active = typeCycle == TypeCycle.EMPTY || !this.itemName.getText().isEmpty();
    }

    private void saveIconVariable(String s) {
        this.saveIconVariable();
    }

    private void buttonUpdate(String s) {
        this.updateStates();
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
        if (MineMenuFabricClient.playerHeadData.containsKey(skullowner)) {
            client.getItemRenderer().renderInGui(playerHeadData.get(skullowner), this.width / 2 - 120, 82);
        }
        else {
            i = RandomUtil.iconify(this::setSkullMap, iconItem, enchanted, skullowner);
            if (i == null) try {
                client.getItemRenderer().renderInGui(MineMenuFabricClient.playerHeadData.get(skullowner), this.width / 2 - 120, 82);
            } catch (Exception e) {}
            else client.getItemRenderer().renderInGui(i, this.width / 2 - 120, 82);
        }


        super.render(matrices, mouseX, mouseY, delta);
    }

    private void setSkullMap(ItemStack itemStack) {
        MineMenuFabricClient.playerHeadData.put(skullowner, itemStack);
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
        String data = this.itemData.getText();
        JsonObject subData = new JsonObject();

        switch (typeCycle) {
            case EMPTY:
                name = "";
                iconItem = "";
                enchanted = false;
                skullowner = "";
                break;

            case PRINT:
            case CLIPBOARD:
                subData.add("message", new JsonPrimitive(data));
                break;

            case LINK:
                subData.add("link", new JsonPrimitive(data));
                break;

            case CATEGORY:
                subData = new JsonObject();
                break;
        }

        GsonUtil.saveJson(GsonUtil.template(name, typeCycle.name().toLowerCase(), subData, iconItem, enchanted, skullowner));
        Config.get().minemenuFabric.minemenuData = minemenuData;
        AutoConfig.getConfigHolder(Config.class).save();
    }
}
