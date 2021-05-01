package me.ultrablacklinux.minemenufabric.client.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSelectScreen;

import java.util.ArrayList;

public class GsonUtil {
    public static JsonObject template(String name, String icon, String type, JsonObject data) {
        JsonObject item = new JsonObject();
        item.add("name", new JsonPrimitive(name));
        item.add("icon", new JsonPrimitive(icon));
        item.add("type", new JsonPrimitive(type));
        item.add("data", data);
        return item;
    }

    public static JsonObject empty() {
        return template("", "", "empty", new JsonObject());
    }

    public static JsonObject fixEntryAmount(JsonObject j) {
        if (j.size() < 5) {
            for (int i = j.size(); i < 5; i++) {
                j.add(String.valueOf(j.size()), GsonUtil.empty());
            }
        }
        return j;
    }
}
