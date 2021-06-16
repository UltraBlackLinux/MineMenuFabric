package me.ultrablacklinux.minemenufabric.mixin;

import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSelectScreen;
import me.ultrablacklinux.minemenufabric.access.KeyBindingInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements KeyBindingInterface {
    @Shadow private int timesPressed;

    @Inject(method = "unpressAll()V", at = @At("HEAD"), cancellable = true)
    private static void unpressAll(CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof MineMenuSelectScreen &&
                Config.get().minemenuFabric.inScreenWalk) ci.cancel();
    }

    @Override
    public void setTimesPressed(int timesPressed) {
        this.timesPressed = timesPressed;
    }

    @Override
    public int getTimesPressed() {
        return this.timesPressed;
    }
}

