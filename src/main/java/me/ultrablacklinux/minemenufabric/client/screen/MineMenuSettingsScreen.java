package me.ultrablacklinux.minemenufabric.client.screen;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.screen.util.IconConfigOptions;
import me.ultrablacklinux.minemenufabric.client.screen.util.MenuTypes;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import me.ultrablacklinux.minemenufabric.client.util.RandomUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient.*;

@SuppressWarnings("ALL")
public class MineMenuSettingsScreen extends Screen {
    private MenuTypes itemTypes = MenuTypes.EMPTY;
    private IconConfigOptions iconConfigCycle = IconConfigOptions.ICON;;
    private SliderWidget delayTimeSlider;
    private final Screen parent;
    private ArrayList<String> localDPath;
    private JsonObject localData;
    private InputUtil.Key usedKey = null;
    private KeyBinding usedBinding = null;
    public static List<KeyBinding> keyBindings;

    private TextFieldWidget itemName;
    private TextFieldWidget iconDataText;
    private TextFieldWidget itemData;
    private ButtonWidget iconSettingType;
    private ButtonWidget itemType;
    private ButtonWidget keyBindButton;
    private ButtonWidget iconDataYesNo;
    private ButtonWidget done;

    private String iconItem;
    private String skullowner;

    private boolean keyListenerActive = false;
    private boolean iconDataBoolean = false;
    private boolean enchanted = false;
    private boolean firstRun = true;
    private int customModelData = 0;
    private int delayTime = 10;

    public MineMenuSettingsScreen(Screen parent, boolean repeat) {
        super(new TranslatableText("minemenu.settings.title"));
        this.parent = parent;
        localDPath = repeat ? repeatDatapath : datapath;
        this.updateData();

        /*System.out.println("----SettingsScreen----"); //TODO REMOVE
        System.out.println(datapath);
        System.out.println(repeatDatapath);
        System.out.println("-----------------------");*/
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
        this.addDrawableChild(this.itemName);

        //---------------------------- ICON INPUT

        this.iconDataText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 101, 200, 20,
                        new TranslatableText("minemenu.settings.icon.data"));
        this.iconDataText.setMaxLength(32500);
        this.iconDataText.setChangedListener(this::saveIcon);
        this.addDrawableChild(this.iconDataText);

        this.iconDataYesNo = this.addDrawableChild(
                new ButtonWidget(this.width / 2 - 100, 101, 200, 20, Text.of(""), (buttonWidget) -> {
                    this.iconDataBoolean = !iconDataBoolean;
                    this.saveIcon();
                    this.updateInput();
        }));

        this.iconSettingType = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 80, 200,
                20, iconConfigCycle.getName(), (buttonWidget) -> {
            this.saveIcon();
            iconConfigCycle = iconConfigCycle.next();
            if (!isSkull(iconItem) && iconConfigCycle == IconConfigOptions.SKULLOWNER) iconConfigCycle = iconConfigCycle.next();
            this.iconSettingType.setMessage(iconConfigCycle.getName());
            this.updateInput();
        }));

        //---------------------------- DATA INIPUT/TYPE

        this.itemData = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 141, 200, 20,
                new TranslatableText("minemenu.settings.data"));
        this.itemData.setMaxLength(32500);
        this.addDrawableChild(this.itemData);

        this.keyBindButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 140, 200, 20,
                InputUtil.UNKNOWN_KEY.getLocalizedText(), (buttonWidget) -> {
            if (itemTypes == itemTypes.KEYSELECT) this.keyBindCycle(false);
        }));

        this.itemType = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 180, 200,
                20, itemTypes.getName(), (buttonWidget) -> {
            itemTypes = itemTypes.next();
            this.itemType.setMessage(itemTypes.getName());
            this.updateInput();
        }));

        if (firstRun) {
            this.itemName.setText(localData.get("name").getAsString());
            this.readTypes(localData);
            firstRun = false;
        }

        this.delayTimeSlider = this.addDrawableChild(new SliderWidget(this.width / 2 - 100, 160, 200,
                20, LiteralText.EMPTY, (this.delayTime - 10) / (25001 - 10)) {
            { //hippedy, hoppedy, your code is now my property
                this.updateMessage();
            }

            protected void updateMessage() {
                if (this.value == 1) {
                    this.setMessage(new TranslatableText("minemenu.gui.toggle"));
                }
                else {
                    this.setMessage(new TranslatableText("minemenu.setting.input.key.delay",
                            MineMenuSettingsScreen.this.delayTime));
                }
            }

            protected void applyValue() {
                MineMenuSettingsScreen.this.delayTime
                        = MathHelper.floor(MathHelper.clampedLerp(10, 25001, this.value));
            }
        });

        //---------------------------- DONE CANCEL

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, 200, 100, 20,
                ScreenTexts.CANCEL, (buttonWidget) -> close(true)));

        this.done = this.addDrawableChild(new ButtonWidget(this.width / 2, 200, 100, 20,
                ScreenTexts.DONE, (buttonWidget) -> close(false)));

        this.updateInput();
    }

    private boolean isSkull(String skull) {
        String iconString = this.iconItem.replace("minecraft:", "");//only player_head works for custom skulls
        if (iconString.equals("player_head")) return true;
        return false;
    }

    private void keyBindCycle(boolean reverse) {
        int index = keyBindings.indexOf(usedBinding);
        if (this.usedBinding == null || (index == keyBindings.size() - 1 && !reverse)) {
            this.usedBinding = keyBindings.get(0);
        }

        else if (index == 0 && reverse) {
            this.usedBinding = keyBindings.get(keyBindings.size() - 1);
        }

        else if (!reverse) {
            this.usedBinding = keyBindings.get(index + 1);
        }

        else if (reverse) {
            this.usedBinding = keyBindings.get(index - 1);
        }

        this.keyBindButton.setMessage(new TranslatableText(usedBinding.getTranslationKey()));
    }

    private void updateInput() {
        if (firstRun) return;

        switch (iconConfigCycle) {
            case ICON:
                this.iconDataText.setText(this.iconItem);
                break;

            case ENCHANTED:
                iconDataBoolean = enchanted;
                break;

            case CUSTOMMODELDATA:
                this.iconDataText.setText(String.valueOf(this.customModelData));
                break;

            case SKULLOWNER:
                iconDataText.setEditable(this.isSkull(iconItem));
                this.iconDataText.setText(this.skullowner);
                break;
        }

        if (itemTypes == MenuTypes.KEYSELECT) {
            this.keyBindButton.visible = true;
            this.delayTimeSlider.visible = true;
            this.itemData.visible = false;
        }
        else {
            this.keyBindButton.visible = false;
            this.delayTimeSlider.visible = false;
            this.itemData.visible = true;
        }

        if (itemTypes != MenuTypes.EMPTY) {
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

        switch(itemTypes) {
            case PRINT:
            case PRINTMANY:
            case LINK:
            case CLIPBOARD:
            case CHATBOX:
                try {
                    this.itemData.setText(localData.get("data").getAsString());
                } catch (Exception ingore) {}
                this.itemData.setEditable(true);
                break;

            case KEYSELECT:
                this.itemData.setEditable(false);
                try {
                    this.keyBindButton.setMessage(new TranslatableText(this.usedBinding.getTranslationKey()));
                } catch (Exception ingore) {}
                break;

            default:
                this.itemData.setEditable(false);
                break;
        }

        this.iconDataText.setEditable(iconConfigCycle != IconConfigOptions.ENCHANTED && itemTypes != MenuTypes.EMPTY);
        this.iconSettingType.setMessage(iconConfigCycle.getName());
        this.itemType.setMessage(itemTypes.getName());
        this.iconSettingType.active = itemTypes != MenuTypes.EMPTY;
        this.iconDataYesNo.active = iconConfigCycle == IconConfigOptions.ENCHANTED;
        if (iconConfigCycle != IconConfigOptions.ENCHANTED) this.iconDataYesNo.setMessage(Text.of(""));
        else this.iconDataYesNo.setMessage(iconDataBoolean ? ScreenTexts.YES : ScreenTexts.NO);
        this.itemName.setEditable(itemTypes != MenuTypes.EMPTY);
        this.done.active = itemTypes == MenuTypes.EMPTY || !this.itemName.getText().isEmpty();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyListenerActive) {
            this.handleKeys(keyCode, scanCode, modifiers, false);
            return true;
        }
        else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.keyListenerActive) {
            this.handleKeys(button, 0, 0, true);
            return true;
        }

        if (button == 1) { //RIGHTCLICK
            if (this.itemTypes == itemTypes.KEYSELECT && keyBindButton.isMouseOver(mouseX, mouseY)) {
                this.keyBindButton.playDownSound(client.getSoundManager());
                this.keyBindCycle(true);
                return true;
            }
            else if (itemType.isMouseOver(mouseX, mouseY)) {
                itemTypes = itemTypes.previous();
                this.itemType.playDownSound(client.getSoundManager());
                this.updateInput();
                return true;
            }
            else if (iconSettingType.isMouseOver(mouseX, mouseY)) {
                iconConfigCycle = iconConfigCycle.previous();
                if (!isSkull(this.iconItem) && iconConfigCycle == IconConfigOptions.SKULLOWNER) iconConfigCycle = iconConfigCycle.previous();
                this.iconSettingType.playDownSound(client.getSoundManager());
                this.updateInput();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleKeys(int keyCode, int scanCode, int modifiers, boolean mouse) {
        if (mouse) {
            this.usedKey = InputUtil.Type.MOUSE.createFromCode(keyCode);
        }
        else {
            if (keyCode == 256) this.usedKey = InputUtil.UNKNOWN_KEY;
            else this.usedKey = InputUtil.fromKeyCode(keyCode, scanCode);
        }

        this.keyListenerActive = false;
        KeyBinding.updateKeysByCode();
        this.updateInput();
    }

    private void saveIcon(String s) {
        this.saveIcon();
    }

    private void updateInput(String s) {
        this.updateInput();
    }

    public void resize(MinecraftClient client, int width, int height) {
        this.init(client, width, height);
        this.itemName.setText(this.itemName.getText());
        this.iconDataText.setText( this.iconDataText.getText());
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
        if (iconConfigCycle != IconConfigOptions.ENCHANTED) this.iconDataText.render(matrices, mouseX, mouseY, delta);
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
        itemType.renderToolTip(matrices, mouseX, mouseY);
        itemType.renderToolTip(matrices, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void close(boolean cancel) {
        this.client.keyboard.setRepeatEvents(false);
        if (!cancel) applySettings();
        if (localDPath.size() >= 1) {
            if (localDPath.get(localDPath.size() - 1).equals("data")) {
                for (int i = 0; i < 2; i++) localDPath.remove(localDPath.size() - 1);
            }
            else if (!isRepeatEdit) {
                localDPath.remove(localDPath.size() - 1);
            }
        }

        datapath = localDPath;

        if (isRepeatEdit) {
            this.updateData();
            MineMenuSelectScreen.updateRepeatData(itemTypes.getName().asString().toLowerCase(), localData);
            if (itemTypes == MenuTypes.EMPTY) {
                repeatData = null;
                repeatDatapath = null;
            }
            isRepeatEdit = false;

            client.openScreen(new MineMenuSelectScreen(MineMenuFabricClient.minemenuData,
                    new TranslatableText("minemenu.default.title").getString(), null));
        }
        else this.client.openScreen(this.parent);

    }

    private void saveIcon() {
        switch (iconConfigCycle) {
            case ENCHANTED:
                this.enchanted = this.iconDataBoolean;
                break;

            case ICON:
                this.iconItem = this.iconDataText.getText();
                if (!isSkull(iconItem)) this.skullowner = "";
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
        JsonElement subDataOut = new JsonObject();
        skullowner = skullowner.replaceAll("[^a-zA-Z0-9_]", "");

        switch (itemTypes) {
            case EMPTY:
                nameOut = "";
                iconItem = "";
                enchanted = false;
                skullowner = "";
                break;

            case PRINT:
            case PRINTMANY:
            case CHATBOX:
            case CLIPBOARD:
            case LINK:
                subDataOut = new JsonPrimitive(dataTextOut);
                break;

            case KEYSELECT:
                subDataOut.getAsJsonObject().add("key", new JsonPrimitive(this.usedBinding.getTranslationKey()));
                subDataOut.getAsJsonObject().add("releaseDelay", new JsonPrimitive(delayTime));
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

        GsonUtil.saveJson(GsonUtil.template(nameOut, itemTypes.name().toLowerCase(), subDataOut, iconItem, enchanted,
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
        this.itemTypes = MenuTypes.valueOf(data.get("type").getAsString().toUpperCase());
        this.keyBindings = Arrays.asList(client.options.keysAll);

        switch (itemTypes) {
            case PRINT:
            case PRINTMANY:
            case CLIPBOARD:
            case LINK:
            case CHATBOX:
                this.itemData.setText(data.get("data").getAsString());
                break;

            case KEYSELECT:
                try {
                    this.usedBinding = this.keyBindings.stream()
                            .filter(keyBindingstream -> keyBindingstream.getTranslationKey().equals(data.get("data")
                                    .getAsJsonObject().get("key").getAsString())).findFirst().get();
                } catch (Exception e) {}
                this.delayTime = data.get("data").getAsJsonObject().get("releaseDelay").getAsInt();
                break;
        }
    }
}
