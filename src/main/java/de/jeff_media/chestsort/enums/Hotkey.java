package de.jeff_media.chestsort.enums;

import de.jeff_media.chestsort.ChestSortPlugin;
import de.jeff_media.chestsort.data.PlayerSetting;
import org.bukkit.entity.Player;

import java.util.Locale;

public enum Hotkey {

    AUTO_SORT, AUTO_INV_SORT,
    SHIFT_CLICK,
    MIDDLE_CLICK, DOUBLE_CLICK, SHIFT_RIGHT_CLICK,
    OUTSIDE, LEFT_CLICK, RIGHT_CLICK;

    private static final ChestSortPlugin main = ChestSortPlugin.getInstance();

    public boolean hasPermission(Player player) {
        if(!player.hasPermission(getPermission(this))) return false;
        switch(this) {
            case AUTO_SORT:
                return main.getConfig().getBoolean("allow-automatic-sorting");
            case AUTO_INV_SORT:
                return main.getConfig().getBoolean("allow-automatic-inventory-sorting");
            case SHIFT_CLICK:
            case MIDDLE_CLICK:
            case DOUBLE_CLICK:
            case SHIFT_RIGHT_CLICK:
                return main.getConfig().getBoolean("allow-sorting-hotkeys");
            case LEFT_CLICK:
            case RIGHT_CLICK:
                return main.getConfig().getBoolean("allow-additional-hotkeys");
            case OUTSIDE:
                return main.getConfig().getBoolean("allow-left-click-to-sort");
            default:
                throw new IllegalArgumentException("Invalid hotkey: " + this.name());
        }
    }

    public static String getPermission(Hotkey hotkey) {

        if(hotkey == AUTO_SORT) {
            return "chestsort.automatic";
        }

        if(hotkey == AUTO_INV_SORT) {
            return "chestsort.use.inventory";
        }

        String permission = "chestsort.hotkey." + hotkey.name().toLowerCase(Locale.ROOT).replace("_", "");
        //System.out.println("Permission for " + hotkey.name()+ ": " + permission);
        return permission;
    }

    public static Hotkey fromPermission(String permission) {
        //System.out.println("Checking permission " + permission + " and returning the proper hotkey...");
        if(permission == null) return null;
        switch(permission) {
            case "shiftclick": return SHIFT_CLICK;
            case "middleclick": return MIDDLE_CLICK;
            case "doubleclick": return DOUBLE_CLICK;
            case "shiftrightclick": return SHIFT_RIGHT_CLICK;
            case "leftclick": return LEFT_CLICK;
            case "rightclick": return RIGHT_CLICK;
            case "outside": return OUTSIDE;
            case "autosorting": return AUTO_SORT;
            case "autoinvsorting": return AUTO_INV_SORT;
            default: return null;
        }
    }

    public boolean hasEnabled(Player player) {
        PlayerSetting setting = ChestSortPlugin.getInstance().getPlayerSetting(player);
        switch(this) {
            case SHIFT_CLICK: return setting.shiftClick;
            case MIDDLE_CLICK: return setting.middleClick;
            case DOUBLE_CLICK: return setting.doubleClick;
            case SHIFT_RIGHT_CLICK: return setting.shiftRightClick;
            case LEFT_CLICK: return setting.leftClick;
            case RIGHT_CLICK: return setting.rightClick;
            case OUTSIDE: return setting.leftClickOutside;
            case AUTO_INV_SORT: return setting.invSortingEnabled;
            case AUTO_SORT: return setting.sortingEnabled;
            default: return false;
        }
    }
}
