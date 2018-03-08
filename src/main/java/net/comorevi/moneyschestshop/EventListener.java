package net.comorevi.moneyschestshop;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import java.util.LinkedHashMap;

public class EventListener implements Listener {

    private MoneySChestShop mainClass;
    private SQLite3DataProvider sqLite3DataProvider;

    public EventListener(MoneySChestShop plugin, SQLite3DataProvider sql) {
        this.mainClass = plugin;
        this.sqLite3DataProvider = sql;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(mainClass.containsUser(event.getPlayer().getName())) {
            mainClass.removePlayerFromList(event.getPlayer(), event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        Block block = event.getBlock();
        String blockName = block.getName();
        int blockId = block.getId();
        int blockMeta = block.getDamage();
        if(mainClass.containsUser(username)) {
            player.sendMessage("システム>> BlockData" + "\n" +
                    "Name: " + blockName + "\n" +
                    "ID: " + blockId + "\n" +
                    "Meta: " + blockMeta);
        }
        
        switch(blockId) {
            case Block.SIGN_POST:
            case Block.WALL_SIGN:
                int[] signCondition = {(int) block.x, (int) block.y, (int) block.z};
                if(sqLite3DataProvider.existsShop(signCondition)) {
                    LinkedHashMap<String, Object> shopSignInfo = sqLite3DataProvider.getShopInfo(signCondition);
                    
                    if(shopSignInfo.get("shopOwner").equals(username)) {
                        player.sendMessage("システム>> 自分のショップからは購入できません");
                        return;
                    }
                    
                    int buyermoney = mainClass.getMoneySAPI().getMoney(player);
                    if((int) shopSignInfo.get("price") < buyermoney) {
                        player.sendMessage("システム>> 所持金が不足しているため購入できません");
                        return;
                    }
    
                    BlockEntityChest chest = (BlockEntityChest) player.getLevel().getBlockEntity(new Vector3((int) shopSignInfo.get("chestX"), (int) shopSignInfo.get("chestY"), (int) shopSignInfo.get("chestZ")));
                    int itemNum = 0;
                    int pID = (int) shopSignInfo.get("productID");
                    int pMeta = (int) shopSignInfo.get("productMeta");
                    for(int i = 0; i < chest.getSize(); i++) {
                        Item item = chest.getInventory().getItem(i);
                        if(item.getId() == pID && item.getDamage() == pMeta) itemNum += item.getCount();
                    }
                    Player shopOwner = mainClass.getServer().getPlayer(String.valueOf(shopSignInfo.get("shopOwner")));
                    if(itemNum < (int) shopSignInfo.get("saleNum")) {
                        player.sendMessage("システム>> このショップは在庫切れです");
                        if(shopOwner != null) {
                            shopOwner.sendMessage("システム>> あなたのチェストショップが在庫切れになっています！ 補給すべきもの " + pID + ":" + pMeta);
                        }
                    }
                    
                    Item shopItem = Item.get(pID, pMeta, (int) shopSignInfo.get("saleNum"));
                    if(player.getInventory().canAddItem(shopItem)) player.getInventory().addItem(shopItem);
                    
                    int tmpNum = (int) shopSignInfo.get("saleNum");
                    for(int i = 0; i < chest.getSize(); i++) {
                        Item item = chest.getInventory().getItem(i);
                        if(item.getId() == pID && item.getDamage() == pMeta) {
                            if(item.getCount() <= tmpNum) {
                                chest.getInventory().setItem(i, Item.get(Item.AIR, 0, 0));
                                tmpNum -= item.getCount();
                            } else {
                                chest.getInventory().setItem(i, Item.get(item.getId(), pMeta, item.getCount() - tmpNum));
                                break;
                            }
                        }
                    }
                    mainClass.getMoneySAPI().payMoney(username, String.valueOf(shopSignInfo.get("shopOwner")), (int) shopSignInfo.get("price"));
                    
                    player.sendMessage("システム>> 購入処理が完了しました");
                    if(shopOwner != null) {
                        shopOwner.sendMessage("システム>> " + username + "が" + pID + ":" + pMeta + "を購入しました （" + shopSignInfo.get("price") + mainClass.getMoneySAPI().getMoneyUnit() + "）");
                    }
                }
                break;
            
            case Block.CHEST:
                int[] chestCondition = {(int) block.x, (int) block.y, (int) block.z};
                if(sqLite3DataProvider.existsShop(chestCondition)) {
                    if(!sqLite3DataProvider.isShopOwner(chestCondition, player)) {
                        player.sendMessage("システム>> このチェストは保護されています");
                        event.setCancelled();
                    }
                }
                break;
        }
    }
    
    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        switch(block.getId()) {
            case Block.SIGN_POST:
            case Block.WALL_SIGN:
                int[] signCondition = {(int) block.x, (int) block.y, (int) block.z};
                if(sqLite3DataProvider.existsShop(signCondition)) {
                    if(sqLite3DataProvider.isShopOwner(signCondition, player)) {
                        sqLite3DataProvider.removeShopBySign(signCondition);
                        player.sendMessage("システム>> チェストショップを閉じました");
                    } else {
                        player.sendMessage("システム>> この看板は保護されています");
                        event.setCancelled();
                    }
                }
                break;
            case Block.CHEST:
                int[] chestCondition = {(int) block.x, (int) block.y, (int) block.z};
                if(sqLite3DataProvider.existsShop(chestCondition)) {
                    if(sqLite3DataProvider.isShopOwner(chestCondition, player)) {
                        sqLite3DataProvider.removeShopByChest(chestCondition);
                        player.sendMessage("システム>> チェストショップを閉じました");
                    } else {
                        player.sendMessage("システム>> このチェストは保護されています");
                        event.setCancelled();
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        try {
            if(event.getLine(0).equals("") && !event.getLine(1).equals("") && !event.getLine(2).equals("") && !event.getLine(3).equals("")) {
                Player player = event.getPlayer();
                String shopOwner = player.getName();
                String[] productData = event.getLine(3).split(":");
                int saleNum = 0;
                int price = 0;
                int priceIncludeCommission = 0;
                int pID = 0;
                int pMeta = 0;
                try {
                    saleNum = Integer.parseInt(event.getLine(1));
                    price = Integer.parseInt(event.getLine(2));
                    priceIncludeCommission = (int) (price * 0.05);
                    pID = Integer.parseInt(productData[0]);
                    pMeta = Integer.parseInt(productData[1]);
                } catch (NullPointerException e) {
                    pMeta = 0;
                } catch (ArrayIndexOutOfBoundsException e) {
                    pMeta = 0;
                } catch (NumberFormatException e) {
                    event.setCancelled();
                    player.sendMessage("システム>> 不適切なデータが入力されました");
                }

                Block sign = event.getBlock();
                Block chest = getSideChest(sign);
                
                if (!event.getLine(0).equals("")) return;
                if (saleNum <= 0) return;
                if (price < 0) return;
                if (pID == 0) return;
                if (chest == null) return;

                String productName = Block.get(pID, pMeta).getName();
                event.setLine(0, TextFormat.WHITE + shopOwner);
                event.setLine(1, "数量/amount:" + saleNum);
                event.setLine(2, "値段/price:" + priceIncludeCommission);
                event.setLine(3, productName);
	
				int[] signCondition = {(int) event.getBlock().x, (int) event.getBlock().y, (int) event.getBlock().z};
                if(sqLite3DataProvider.existsShop(signCondition)) {
                    sqLite3DataProvider.updateShop(shopOwner, saleNum, priceIncludeCommission, pID, pMeta, sign, chest);
				} else {
					sqLite3DataProvider.registerShop(shopOwner, saleNum, priceIncludeCommission, pID, pMeta, sign, chest);
				}
                player.sendMessage("システム>> チェストショップを作成しました");
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            event.setCancelled();
        }
    }

    private Block getSideChest(Position pos) {
        Block block = null;
        block = pos.getLevel().getBlock(new Vector3(pos.getX() + 1, pos.getY(), pos.getZ()));
        if(block.getId() == Block.CHEST) return block;
        block = pos.getLevel().getBlock(new Vector3(pos.getX() - 1, pos.getY(), pos.getZ()));
        if(block.getId() == Block.CHEST) return block;
        block = block.getLevel().getBlock(new Vector3(pos.getX(), pos.getY(), pos.getZ() + 1));
        if(block.getId() == Block.CHEST) return block;
        block = block.getLevel().getBlock(new Vector3(pos.getX(), pos.getY(), pos.getZ() - 1));
        if(block.getId() == Block.CHEST) return block;
        return block;
    }
}
