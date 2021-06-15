package me.ultrablacklinux.minemenufabric.client.util;

import com.mojang.authlib.GameProfile;
import me.shedaniel.math.Color;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSettingsScreen;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class RandomUtil {
    public static me.shedaniel.math.Color getColor(String inp) {
        long colorLong = Long.decode(inp);
        float f = (float) (colorLong >> 24 & 0xff) / 255F;
        float f1 = (float) (colorLong >> 16 & 0xff) / 255F;
        float f2 = (float) (colorLong >> 8 & 0xff) / 255F;
        float f3 = (float) (colorLong & 0xff) / 255F;
        return Color.ofRGBA(f, f1, f2, f3);
    }

    @SuppressWarnings("ConstantConditions")
    public static ItemStack iconify(String iconItem, boolean enchanted, String skullowner, int customModelData) {
        ItemStack out;
        try {
            out = itemStackFromString(iconItem);
            CompoundTag customModelTag = out.getOrCreateTag();
            customModelTag.putInt("CustomModelData", customModelData);
            try {
                if (enchanted) {
                    Map<Enchantment, Integer> e = new HashMap<>();
                    e.put(Enchantment.byRawId(1), 1);
                    EnchantmentHelper.set(e, out);
                }

                if (!skullowner.isEmpty() && isSkullItem(out)) {
                    ItemStack finalOut = out;
                    Thread nbTater = new Thread(() -> {
                        CompoundTag skullTag = finalOut.getOrCreateTag();
                        GameProfile gameProfile = new GameProfile(null, skullowner);
                        gameProfile = SkullBlockEntity.loadProperties(gameProfile);
                        skullTag.put("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), gameProfile));
                        MineMenuFabricClient.playerHeadCache.putIfAbsent(skullowner, finalOut);
                    });
                    nbTater.start();
                    out = null;

                } else out.removeSubTag("SkullOwner");

            } catch (Exception e) {e.printStackTrace();}
        } catch (InvalidIdentifierException e) {
            out = new ItemStack(Items.AIR);
        }
        return out;
    }

    public static ItemStack itemStackFromString(String itemStack) {
        return Registry.ITEM.get(new Identifier(itemStack)).getDefaultStack();
    }

    public static boolean isSkullItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((net.minecraft.item.BlockItem)
                stack.getItem()).getBlock() instanceof AbstractSkullBlock;
    }


    public static void openConfigScreen(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            client.openScreen(new MineMenuSettingsScreen(parent, false));
        } catch (NullPointerException e) {
            e.printStackTrace();
            client.openScreen(null);
            assert client.player != null;
            client.player.sendMessage(new TranslatableText("minemenu.error.config"), false);
        }
    }
}
