package me.ultrablacklinux.minemenufabric.client.screen.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;

public enum MenuTypes implements Nameable {
    PRINT,
    PRINTMANY,
    CHATBOX,
    CLIPBOARD,
    LINK,
    KEYSELECT,
    CATEGORY,
    EMPTY;

    public MenuTypes next() {
        MenuTypes[] v = values();
        if (v.length == this.ordinal() + 1) {
            return v[0]; }
        return v[this.ordinal() + 1];
    }

    public MenuTypes previous() {
        MenuTypes[] v = values();
        if (this.ordinal() == 0) {
            return v[v.length-1]; }
        return v[this.ordinal()-1];
    }

    @Override
    public Text getName() {
        return new TranslatableText("minemenu.setting.MenuTypes." + this.name().charAt(0) + this.name().substring(1).toLowerCase());
    }
}
