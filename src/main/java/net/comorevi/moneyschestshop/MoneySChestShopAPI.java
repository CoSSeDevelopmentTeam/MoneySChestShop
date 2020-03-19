package net.comorevi.moneyschestshop;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import net.comorevi.moneyschestshop.util.SQLite3DataProvider;

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

    public void removeShopBySign(Object[] condition) {
        dataProvider.removeShopBySign(condition);
    }

    public void removeShopByChest(Object[] condition) {
        dataProvider.removeShopByChest(condition);
    }

    public void registerShop(String shopOwner, int saleNum, int price, int productID, int productMeta, Block sign, Block chest) {
        dataProvider.registerShop(shopOwner, saleNum, price, productID, productMeta, sign, chest);
    }

    public void updateShop(String shopOwner, int saleNum, int price, int productID, int productMeta, Block sign) {
        dataProvider.updateShop(shopOwner, saleNum, price, productID, productMeta, sign);
    }

    public LinkedHashMap<String, Object> getShopData(Object[] condition) {
        return dataProvider.getShopInfo(condition);
    }

    public boolean isOwner(Object[] condition, Player player) {
        return dataProvider.isShopOwner(condition, player);
    }

    public boolean existsShop(Object[] condition) {
        return dataProvider.existsShop(condition);
    }
}
