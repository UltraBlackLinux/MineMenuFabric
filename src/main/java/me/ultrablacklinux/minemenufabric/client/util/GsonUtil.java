package me.ultrablacklinux.minemenufabric.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.ultrablacklinux.minemenufabric.client.MineMenuFabricClient;
import me.ultrablacklinux.minemenufabric.client.screen.MineMenuSelectScreen;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
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
        if (j.size() < 5) {
            for (int i = j.size(); i < 5; i++) {
                j.add(String.valueOf(j.size()), GsonUtil.empty());
            }
        }
        return j;
    }

    /**
     * This is real shit
     * definitely didn't take me like 3 hours to come up with
     */
    public static void saveJson(JsonObject jso) { //TODO just add to the stuff, if lowest layer
        ArrayList<String> dp = MineMenuFabricClient.datapath;
        if (dp.size() == 1) {
            MineMenuFabricClient.minemenuData.add(dp.get(0), jso);
        }
        else if (dp.size() > 1) {
            JsonObject building = jso;
            JsonObject tmp = new JsonObject();
            while (dp.size() != 0) {
                JsonObject mmData = MineMenuFabricClient.minemenuData;
                for (int i = 0; i < dp.size() - 1; i++) mmData = mmData.get(dp.get(i)).getAsJsonObject();

                mmData.add(dp.get(dp.size() - 1), building);
                dp.remove(dp.size() - 1);
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
