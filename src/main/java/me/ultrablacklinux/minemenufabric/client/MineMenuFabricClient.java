package me.ultrablacklinux.minemenufabric.client;

import com.google.gson.JsonObject;
import me.shedaniel.autoconfig.AutoConfig;
import me.ultrablacklinux.minemenufabric.client.config.Config;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSelectScreen;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSettingsScreen;
import me.ultrablacklinux.minemenufabric.client.screen.util.Tips;
import me.ultrablacklinux.minemenufabric.client.util.GsonUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class MineMenuFabricClient implements ClientModInitializer {
    MineMenuSelectScreen mineMenuSelectScreen;
    public static KeyBinding keyBinding;
    public static JsonObject minemenuData;
    public static JsonObject repeatData = null;
    public static ArrayList<String> repeatDatapath = null;
    public static boolean isRepeatEdit = false;
    public static ArrayList<String> datapath;
    public static HashMap<String, ItemStack> playerHeadCache = new HashMap<>();
    public static Tips tips = Tips.REPEATEDIT;
    private static int tipSwitchCooldown = 0;


    @Override
    public void onInitializeClient() {
        Config.init();
        minemenuData = Config.get().minemenuFabric.minemenuData;
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "minemenu.key.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R,
                "minemenu.category"));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (MinecraftClient.getInstance().currentScreen instanceof MineMenuSettingsScreen) {
                if (tipSwitchCooldown == 0) {
                    tipSwitchCooldown = 300;
                    tips = tips.next();
                }
                else --tipSwitchCooldown;
            }

            if (Config.get().minemenuFabric.resetConfig) {
                Config.get().minemenuFabric.resetConfig = false;
                Config.get().minemenuFabric.minemenuData = new JsonObject();
                minemenuData = new JsonObject();
                AutoConfig.getConfigHolder(Config.class).save();
            }

            if (Config.get().minemenuFabric.resetHeadCache) {
                Config.get().minemenuFabric.resetHeadCache = false;
                playerHeadCache.clear();
            }

            if (mineMenuSelectScreen != null) this.mineMenuSelectScreen.tick();

            if (keyBinding.wasPressed()) {
                if (!(client.currentScreen instanceof MineMenuSelectScreen)) {
                    //noinspection ConstantConditions
                    minemenuData = GsonUtil.fixEntryAmount(minemenuData);
                    try {
                        client.openScreen(new MineMenuSelectScreen(minemenuData,
                                new TranslatableText("minemenu.default.title").getString(), null));
                    } catch (Exception e) {
                        client.openScreen(null);
                        assert client.player != null;
                        client.player.sendMessage(new TranslatableText("minemenu.error.config"), false);
                    }

                }
            }
        });
    }
}

