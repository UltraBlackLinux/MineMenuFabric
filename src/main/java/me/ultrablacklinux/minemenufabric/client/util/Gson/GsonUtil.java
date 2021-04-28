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
}
