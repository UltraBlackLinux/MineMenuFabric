package me.ultrablacklinux.minemenufabric.client.screen;

import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import org.jetbrains.annotations.NotNull;

public enum TypeCycle implements Nameable {
    PRINT,
    CATEGORY,
    EMPTY;

    public TypeCycle next() {
        TypeCycle[] v = values();
        if (v.length == this.ordinal() + 1) {
            return v[0]; }
        return v[this.ordinal() + 1];
    }

    @Override
    public Text getName() {
        return Text.of(this.name().substring(0,1) + this.name().substring(1).toLowerCase());
    }
}
