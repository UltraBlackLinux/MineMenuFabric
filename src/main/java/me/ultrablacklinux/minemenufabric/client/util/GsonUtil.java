package me.ultrablacklinux.minemenufabric.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

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
        if (j.size() < Config.get().minemenuFabric.menuEntries) {
            for (int i = j.size(); i < Config.get().minemenuFabric.menuEntries; i++) {
                j.add(String.valueOf(j.size()), GsonUtil.empty());
            }
        }
        else if (j.size() > Config.get().minemenuFabric.menuEntries) {
            for (int i = j.size() - Config.get().minemenuFabric.menuEntries; i > 0; i--) {
                ArrayList<String> a = new ArrayList<>(j.keySet());
                Collections.reverse(a);
                j.remove(a.get(0));
            }
        }
        return j;
    }

    /**
     * This is real shit
     * definitely didn't take me like 3 hours to come up with
     * And even more due to a stupid bug
     */
    public static void saveJson(JsonObject jso) {
        ArrayList<String> localDatapath = (ArrayList<String>) MineMenuFabricClient.datapath.clone();
        if (localDatapath.size() == 1) {
            MineMenuFabricClient.minemenuData.add(localDatapath.get(0), jso);
        }
        else if (localDatapath.size() > 1) {
            JsonObject building = jso;
            JsonObject tmp = new JsonObject();
            while (localDatapath.size() != 0) {
                JsonObject mmData = MineMenuFabricClient.minemenuData;
                for (int i = 0; i < localDatapath.size() - 1; i++) mmData = mmData.get(localDatapath.get(i)).getAsJsonObject();
                mmData.add(localDatapath.get(localDatapath.size() - 1), building);
                localDatapath.remove(localDatapath.size() - 1);
                building = mmData;
            }
            building = tmp;
            for (Map.Entry<String, JsonElement> entry : building.entrySet()) {
                MineMenuFabricClient.minemenuData.add(entry.getKey(), entry.getValue());
                break;
            }
        }
    }
}
