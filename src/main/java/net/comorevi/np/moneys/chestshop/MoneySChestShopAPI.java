package net.comorevi.np.moneys.chestshop;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Location;
import net.comorevi.np.moneys.chestshop.util.SQLite3DataProvider;

import java.sql.SQLException;
import java.util.LinkedHashMap;

public class MoneySChestShopAPI {
    private static MoneySChestShopAPI instance = new MoneySChestShopAPI();
    private SQLite3DataProvider dataProvider = new SQLite3DataProvider();

    private MoneySChestShopAPI() {
        instance = this;
    }

    public static MoneySChestShopAPI getInstance() {
        return instance;
    }

    public void createShop(String shopOwner, int saleNum, int price, int productID, int productMeta, Block sign, Block chest) {
        dataProvider.addShop(shopOwner, saleNum, price, productID, productMeta, sign.getLocation(), chest.getLocation());
    }

    public void removeShopBySign(Location location) {
        dataProvider.removeShopBySign(location);
    }

    public void removeShopByChest(Location location) {
        dataProvider.removeShopByChest(location);
    }

    public boolean isOwnerBySign(Location location, Player player) {
        if (!existsShopBySign(location)) throw new NullPointerException("There is no shop in that location.");
        return getShopDataBySign(location).get("shopOwner").equals(player.getName());
    }

    public boolean isOwnerByChest(Location location, Player player) {
        if (!existsShopByChest(location)) throw new NullPointerException("There is no shop in that location.");
        return getShopDataByChest(location).get("shopOwner").equals(player.getName());
    }

    public boolean existsShopBySign(Location location) {
        try {
            return dataProvider.existsShopBySign(location);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("There is no shop in that location.");
    }

    public boolean existsShopByChest(Location location) {
        try {
            return dataProvider.existsShopByChest(location);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("There is no shop in that location.");
    }

    public LinkedHashMap<String, Object> getShopDataBySign(Location location) {
        return dataProvider.getShopInfoMapBySign(location);
    }

    public LinkedHashMap<String, Object> getShopDataByChest(Location location) {
        return dataProvider.getShopInfoMapByChest(location);
    }

    protected void disconnectSQL() {
        try {
            dataProvider.DisconnectSQL();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
