package me.ultrablacklinux.minemenufabric.client.config;

import com.google.gson.JsonObject;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@me.shedaniel.autoconfig.annotation.Config(name = "MinemenuFabric")
@me.shedaniel.autoconfig.annotation.Config.Gui.Background("minecraft:textures/block/gray_concrete.png")
public class Config extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Gui.TransitiveObject
    public MinemenuFabric minemenuFabric = new MinemenuFabric();

    public static void init() {
        AutoConfig.register(Config.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
    }

    public static Config get() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    @me.shedaniel.autoconfig.annotation.Config(name = "MinemenuFabric")
    public static class MinemenuFabric implements ConfigData {

        @ConfigEntry.Gui.Excluded
        public JsonObject minemenuData = new JsonObject();

        @ConfigEntry.BoundedDiscrete(min = 2, max = 40)
        @ConfigEntry.Gui.Tooltip
        public int menuEntries = 5;

        @ConfigEntry.Gui.Tooltip
        public int outerRadius = 75;

        @ConfigEntry.Gui.Tooltip
        public int innerRadius = 25;

        @ConfigEntry.Gui.Tooltip
        public String primaryColor = "#A00000CC";

        @ConfigEntry.Gui.Tooltip
        public String secondaryColor = "#212121D0";

        public String emptyItemIcon = "minecraft:air";

        public String multiPrintSeparator = ";";

        public int multiPrintDelay = 10;

        public boolean showTips = true;

        public boolean repeatButton = true;

        public boolean inScreenWalk = false;

        @ConfigEntry.Gui.Tooltip
        public boolean resetHeadCache = false;

        @ConfigEntry.Gui.Tooltip
        public boolean resetConfig = false;
    }
}
