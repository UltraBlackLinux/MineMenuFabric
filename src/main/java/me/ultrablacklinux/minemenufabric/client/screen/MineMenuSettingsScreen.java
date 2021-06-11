package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.screen.util.ItemConfigCycle;
import me.ultrablacklinux.minemenufabric.client.screen.util.Tips;
import me.ultrablacklinux.minemenufabric.client.screen.util.TypeCycle;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import me.ultrablacklinux.minemenufabric.client.util.RandomUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;

@SuppressWarnings("ALL")
public class MineMenuSettingsScreen extends Screen {
    private TypeCycle typeCycle = TypeCycle.EMPTY;
    private ItemConfigCycle iconConfigCycle;
    private InputUtil.Key buttonKeyBinding = InputUtil.UNKNOWN_KEY;
    private SliderWidget keyBindreleaseSlider;
    private final Screen parent;
    ArrayList<String> localDPath;
    JsonObject localData;

    private TextFieldWidget itemName;
    private TextFieldWidget iconDataText;
    private TextFieldWidget itemData;
    private ButtonWidget iconSettingType;
    private ButtonWidget type;
    private ButtonWidget keyBindButton;
    private ButtonWidget iconDataYesNo;
    private ButtonWidget done;

    private String iconItem;
    private String skullowner;

    private boolean keyBindButtonActive = false;
    private boolean iconDataBoolean = false;
    private boolean enchanted = false;
    private boolean firstRun = true;
    private int customModelData = 0;
    private int keyBindReleaseTime = 0;

    public MineMenuSettingsScreen(Screen parent, boolean repeat) {
        super(new TranslatableText("minemenu.settings.title"));
        this.parent = parent;
        localDPath = repeat ? repeatDatapath : datapath;
        this.updateData();
        iconConfigCycle = ItemConfigCycle.ICON;

        System.out.println("-----------------------");
        System.out.println(datapath);
        System.out.println(repeatDatapath);
        System.out.println("-____________________________");
    }

    private void updateData() {
        localData = minemenuData;
        for (String s : localDPath) localData = localData.get(s).getAsJsonObject();
    }

    public void tick() {
        this.itemName.tick();
        this.iconDataText.tick();
        this.itemData.tick();
    }

    protected void init() {
        this.client.keyboard.setRepeatEvents(true);

        //---------------------------- NAME INPUT

        this.itemName = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20,
                new TranslatableText("minemenu.settings.name"));
        this.itemName.setMaxLength(32500);
        this.itemName.setChangedListener(this::updateInput);
        this.children.add(this.itemName);

        //---------------------------- ICON INPUT

        this.iconDataText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 101, 200, 20,
                        new TranslatableText("minemenu.settings.icon.data"));
        this.iconDataText.setMaxLength(32500);
        this.iconDataText.setChangedListener(this::saveIconVariable);
        this.children.add(this.iconDataText);

        this.iconDataYesNo = this.addButton(
                new ButtonWidget(this.width / 2 - 100, 101, 200, 20, Text.of(""), (buttonWidget) -> {
                    this.iconDataBoolean = !iconDataBoolean;
                    this.saveIconVariable();
                    this.updateInput();
        }));

        this.iconSettingType = this.addButton(new ButtonWidget(this.width / 2 - 100, 80, 200,
                20, iconConfigCycle.getName(), (buttonWidget) -> {
            this.saveIconVariable();
            iconConfigCycle = iconConfigCycle.next();
            if (iconConfigCycle == ItemConfigCycle.SKULLOWNER &&
                    !this.iconItem.replace("minecraft:", "").equals("player_head")) {
                iconConfigCycle = iconConfigCycle.next();
            }
            this.iconSettingType.setMessage(iconConfigCycle.getName());
            this.updateInput();
        }));

        //---------------------------- DATA INIPUT/TYPE

        this.itemData = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 141, 200, 20,
                new TranslatableText("minemenu.settings.data"));
        this.itemData.setMaxLength(32500);
        this.children.add(this.itemData);

        this.keyBindButton = this.addButton(new ButtonWidget(this.width / 2 - 100, 140, 200, 20,
                InputUtil.UNKNOWN_KEY.getLocalizedText(), (buttonWidget) -> {
            this.keyBindButtonActive = !this.keyBindButtonActive;
            this.updateInput();
            if (keyBindButtonActive) this.keyBindButton.setMessage((new LiteralText("> "))
                    .append(this.keyBindButton.getMessage().shallowCopy().formatted(Formatting.YELLOW))
                    .append(" <").formatted(Formatting.YELLOW)); //Definitely not stolen from minecraft's code
        }));

        this.type = this.addButton(new ButtonWidget(this.width / 2 - 100, 180, 200,
                20, typeCycle.getName(), (buttonWidget) -> {
            typeCycle = typeCycle.next();
            this.type.setMessage(typeCycle.getName());
            this.updateInput();
        }));

        if (firstRun) {
            this.itemName.setText(localData.get("name").getAsString());
            this.readTypes(localData);
            firstRun = false;
        }

        this.keyBindreleaseSlider = this.addButton(new SliderWidget(this.width / 2 - 100, 160, 200,
                20, LiteralText.EMPTY, this.keyBindReleaseTime / 25000D) {
            { //hippedy, hoppedy, your code is now my property
                this.updateMessage();
            }

            protected void updateMessage() {
                this.setMessage(new TranslatableText("minemenu.setting.input.key.delay",
                        MineMenuSettingsScreen.this.keyBindReleaseTime));
            }

            protected void applyValue() {
                MineMenuSettingsScreen.this.keyBindReleaseTime
                        = MathHelper.floor(MathHelper.clampedLerp(0, 25000, this.value));
            }
        });

        //---------------------------- DONE CANCEL

        this.addButton(new ButtonWidget(this.width / 2 - 100, 200, 100, 20,
                ScreenTexts.CANCEL, (buttonWidget) -> close(true)));

        this.done = this.addButton(new ButtonWidget(this.width / 2, 200, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(false)));

        this.updateInput();
    }

    private void updateInput() {
        if (firstRun) return;
        this.iconDataText.setEditable(iconConfigCycle != ItemConfigCycle.ENCHANTED && typeCycle != TypeCycle.EMPTY);

        if (typeCycle != TypeCycle.EMPTY) {
            if (iconConfigCycle == iconConfigCycle.ENCHANTED) {
                this.iconDataYesNo.visible = true;
                this.iconDataText.visible = false;
            }
            else {
                this.iconDataYesNo.visible = false;
                this.iconDataText.visible = true;
            }
        }
        else {
            this.iconDataYesNo.visible = false;
        }

        switch (iconConfigCycle) {
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

        if (typeCycle == TypeCycle.KEYBINDING) {
            this.keyBindButton.visible = true;
            this.keyBindreleaseSlider.visible = true;
            this.itemData.visible = false;
        }
        else {
            this.keyBindButton.visible = false;
            this.keyBindreleaseSlider.visible = false;
            this.itemData.visible = true;
        }

        switch(typeCycle) {
            case PRINT:
            case LINK:
            case CLIPBOARD:
            case CHATBOX:
                try {
                    this.itemData.setText(localData.get("data").getAsString());
                } catch (UnsupportedOperationException ingore) {}
                this.itemData.setEditable(true);
                break;

            case KEYBINDING:
                this.itemData.setEditable(false);
                this.keyBindButton.setMessage(this.buttonKeyBinding.getLocalizedText());
                break;

            default:
                this.itemData.setEditable(false);
                break;
        }

        this.iconSettingType.setMessage(iconConfigCycle.getName());
        this.type.setMessage(typeCycle.getName());

        this.iconSettingType.active = typeCycle != TypeCycle.EMPTY;
        this.iconDataYesNo.active = iconConfigCycle == ItemConfigCycle.ENCHANTED;
        if (iconConfigCycle != ItemConfigCycle.ENCHANTED) this.iconDataYesNo.setMessage(Text.of(""));
        else this.iconDataYesNo.setMessage(iconDataBoolean ? ScreenTexts.YES : ScreenTexts.NO);
        this.itemName.setEditable(typeCycle != TypeCycle.EMPTY);
        this.done.active = typeCycle == TypeCycle.EMPTY || !this.itemName.getText().isEmpty();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyBindButtonActive) {
            this.handleKeys(keyCode, scanCode, modifiers, false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.keyBindButtonActive) {
            this.handleKeys(button, 0, 0, true);
            return true;
        }

        else if (button == 1 && type.isMouseOver(mouseX, mouseY)) {
            typeCycle = typeCycle.previous();
            this.type.playDownSound(client.getSoundManager());
            this.updateInput();
            return true;
        }

        else if (button == 1 && iconSettingType.isMouseOver(mouseX, mouseY)) {
            iconConfigCycle = iconConfigCycle.previous();
            this.iconSettingType.playDownSound(client.getSoundManager());
            this.updateInput();
            return true;
        }

        else return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleKeys(int keyCode, int scanCode, int modifiers, boolean mouse) {
        if (mouse) {
            this.buttonKeyBinding = InputUtil.Type.MOUSE.createFromCode(keyCode);
            //TODO fix button staying active after detecting press when using a mouse
        }
        else {
            if (keyCode == 256) this.buttonKeyBinding = InputUtil.UNKNOWN_KEY;
            else this.buttonKeyBinding = InputUtil.fromKeyCode(keyCode, scanCode);
        }

        this.keyBindButtonActive = false;
        KeyBinding.updateKeysByCode();
        this.updateInput();
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
        drawTextWithShadow(matrices, this.textRenderer, this.keyBindButton.visible ?
                        new TranslatableText("minemenu.setting.input.key") :
                        new TranslatableText("minemenu.setting.input.text"),
                this.width / 2 - 100, 131, 0xFFa0a0a0);

        if (Config.get().minemenuFabric.showTips) {
            drawCenteredText(matrices, this.textRenderer, tips.getName(), this.width / 2,
                    this.height-15, Formatting.YELLOW.getColorValue());
        }

        this.itemName.render(matrices, mouseX, mouseY, delta);
        if (iconConfigCycle != ItemConfigCycle.ENCHANTED) this.iconDataText.render(matrices, mouseX, mouseY, delta);
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
        type.renderToolTip(matrices, mouseX, mouseY);
        type.renderToolTip(matrices, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void close(boolean cancel) {
        datapath = localDPath;
        this.client.keyboard.setRepeatEvents(false);
        if (!cancel) applySettings();
        if (localDPath.size() > 1) {
            if (localDPath.get(localDPath.size() - 1).equals("data")) {
                for (int i = 0; i < 2; i++) localDPath.remove(localDPath.size() - 1);
            }
            else if (!isRepeatEdit) localDPath.remove(localDPath.size() - 1);
        }

        if (isRepeatEdit) {
            this.updateData();
            MineMenuSelectScreen.updateRepeatData(typeCycle.getName().asString().toLowerCase(), localData);
            if (typeCycle == TypeCycle.EMPTY) {
                repeatData = null;
                repeatDatapath = null;
            }
            isRepeatEdit = false;

            client.openScreen(new MineMenuSelectScreen(MineMenuFabricClient.minemenuData,
                    new TranslatableText("minemenu.default.title").getString(), null));
        }
        else this.client.openScreen(this.parent);
    }

    private void saveIconVariable() {
        switch (iconConfigCycle) {
            case ENCHANTED:
                this.enchanted = this.iconDataBoolean;
                break;

            case ICON:
                this.iconItem = this.iconDataText.getText();
                if (!iconItem.replace("minecraft:", "").equals("player_head")) this.skullowner = "";
                break;

            case CUSTOMMODELDATA:
                try {
                    this.customModelData = Integer.parseInt(this.iconDataText.getText()); }
                catch (Exception e) {
                    this.customModelData = 0;
                }
                break;

            case SKULLOWNER:
                this.skullowner = this.iconDataText.getText();
                break;
        }
    }

    private void applySettings() {
        String nameOut = this.itemName.getText();
        String dataTextOut = this.itemData.getText();
        String dataKeybinding = this.buttonKeyBinding.getTranslationKey();
        JsonElement subDataOut = new JsonObject();
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
            case LINK:
                subDataOut = new JsonPrimitive(dataTextOut);
                break;

            case KEYBINDING:
                subDataOut.getAsJsonObject().add("key", new JsonPrimitive(dataKeybinding));
                subDataOut.getAsJsonObject().add("releaseDelay", new JsonPrimitive(keyBindReleaseTime));
                break;

            case CATEGORY:
                try {
                    if (this.localData.size() > 1) {
                        subDataOut = this.localData.get("data");
                        subDataOut.getAsJsonObject().remove("key");
                        subDataOut.getAsJsonObject().remove("releaseDelay");
                    }
                    subDataOut = GsonUtil.fixEntryAmount(subDataOut.getAsJsonObject());
                } catch (IllegalStateException e) {
                    subDataOut = GsonUtil.fixEntryAmount(new JsonObject());
                }
                break;
        }

        GsonUtil.saveJson(GsonUtil.template(nameOut, typeCycle.name().toLowerCase(), subDataOut, iconItem, enchanted,
                skullowner, customModelData));

        Config.get().minemenuFabric.minemenuData = minemenuData;
        AutoConfig.getConfigHolder(Config.class).save();
    }

    private void readTypes(JsonObject data) {
        JsonObject iconData = data.get("icon").getAsJsonObject();
        this.skullowner = iconData.get("skullOwner").getAsString();
        this.enchanted = iconData.get("enchanted").getAsBoolean();
        this.iconItem = iconData.get("iconItem").getAsString();
        this.customModelData = iconData.get("customModelData").getAsInt();
        typeCycle = TypeCycle.valueOf(data.get("type").getAsString().toUpperCase());

        switch (typeCycle) {
            case PRINT:
            case CLIPBOARD:
            case LINK:
            case CHATBOX:
                this.itemData.setText(data.get("data").getAsString());
                break;

            case KEYBINDING:
                this.buttonKeyBinding = InputUtil.fromTranslationKey(data.get("data").getAsJsonObject().get("key").getAsString());
                this.keyBindReleaseTime = data.get("data").getAsJsonObject().get("releaseDelay").getAsInt();
                break;
        }
    }
}
