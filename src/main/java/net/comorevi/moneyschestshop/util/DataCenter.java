package net.comorevi.moneyschestshop.util;

import cn.nukkit.Player;
import cn.nukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataCenter {
    private static List<Player> idCmdQueue = new ArrayList<>();
    private static Map<Player, Block> editCmdQueue = new LinkedHashMap<>();

    public static void addIdCmdQueue(Player player) {
        idCmdQueue.add(player);
    }

    public static void removeIdCmdQueue(Player player) {
        idCmdQueue.remove(player);
    }

    public static boolean existsIdCmdQueue(Player player) {
        return idCmdQueue.contains(player);
    }

    public static void addEditCmdQueue(Player player) {
        editCmdQueue.put(player, null);
    }

    public static void addEditCmdQueue(Player player, Block block) {
        editCmdQueue.put(player, block);
    }

    public static void removeEditCmdQueue(Player player) {
        editCmdQueue.remove(player);
    }

    public static Block getRegisteredBlockByEditCmdQueue(Player player) {
        return editCmdQueue.get(player);
    }

    public static boolean existsEditCmdQueue(Player player) {
        return editCmdQueue.containsKey(player);
    }
}
