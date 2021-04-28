package me.ultrablacklinux.minemenufabric.client.util.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
        return template("", "empty", "", new JsonObject());
    }

    public static JsonObject fixEntryAmount(JsonObject j) {
        if (j.size() < 5) {
            int togo = 5-j.size();
            for (int i = 0; i < togo; i++) {
                j.add(String.valueOf(j.size()+1), GsonUtil.empty());
            }
        }
        return j;
    }
}
