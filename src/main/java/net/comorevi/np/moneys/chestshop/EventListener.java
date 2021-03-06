package net.comorevi.np.moneys.chestshop;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import net.comorevi.cphone.presenter.SharingData;
import net.comorevi.np.moneys.chestshop.util.DataCenter;
import net.comorevi.np.moneys.chestshop.util.FormManager;
import net.comorevi.np.moneys.MoneySAPI;
import net.comorevi.np.moneys.util.TaxType;

import java.util.LinkedHashMap;

public class EventListener implements Listener {

    private Main mainClass;
    private FormManager form = new FormManager();
    private Boolean isCPhoneLoaded;

    public EventListener(Main plugin) {
        this.mainClass = plugin;
        if (mainClass.getServer().getPluginManager().getPlugin("CPhone") == null) {
            this.isCPhoneLoaded = false;
        } else {
            this.isCPhoneLoaded = true;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (DataCenter.existsIdCmdQueue(event.getPlayer())) DataCenter.removeIdCmdQueue(event.getPlayer());
        if (DataCenter.existsEditCmdQueue(event.getPlayer())) DataCenter.removeEditCmdQueue(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        Block block = event.getBlock();
        String blockName = block.getName();
        int blockId = block.getId();
        int blockMeta = block.getDamage();
        if(DataCenter.existsIdCmdQueue(player)) {
            player.sendMessage(Main.MESSAGE_PREFIX+"BlockData" + "\n" +
                    "- Name: " + blockName + ", " +
                    "ID: " + blockId + ", " +
                    "Meta: " + blockMeta);
            player.sendMessage(Main.MESSAGE_PREFIX+"HoldItemData" + "\n" +
                    "- Name: " + player.getInventory().getItemInHand().getName() + ", " +
                    "ID: " + player.getInventory().getItemInHand().getId() + ", " +
                    "Damage: " + player.getInventory().getItemInHand().getDamage() + "/" + player.getInventory().getItemInHand().getMaxDurability());
            event.setCancelled();
        } else if (DataCenter.existsEditCmdQueue(player)) {
            switch (blockId) {
                case Block.SIGN_POST:
                case BlockID.WALL_SIGN:
                    if (isCPhoneLoaded && event.getPlayer().getInventory().getItemInHand().getId() == SharingData.triggerItemId) {
                        event.getPlayer().sendMessage(Main.MESSAGE_PREFIX+"しふぉんを持たずに看板をヒットしてください。");
                        event.setCancelled();
                        return;
                    }
                    if (player.getLevel().getBlockEntity(block.getLocation()) instanceof BlockEntitySign) {
                        BlockEntitySign sign = (BlockEntitySign) player.getLevel().getBlockEntity(block.getLocation());
                        if (sign.getText()[0].equals("cshop") && sign.getText()[1].equals(player.getName()) && getSideChest(block.getLocation()).getId() == Block.CHEST) {
                            DataCenter.addEditCmdQueue(player, block);
                            form.sendCreateCShopWindow(player);
                        }
                    } else {
                        player.sendMessage(Main.MESSAGE_PREFIX+"チェストショップにアクセスするには/cshopでショップ作成モードを無効にしてください。");
                    }
                    event.setCancelled();
                    break;
            }
        } else {
            switch(blockId) {
                case Block.SIGN_POST:
                case Block.WALL_SIGN:
                    if(MoneySChestShopAPI.getInstance().existsShopBySign(block.getLocation())) {

                        LinkedHashMap<String, Object> shopSignInfo = MoneySChestShopAPI.getInstance().getShopDataBySign(block.getLocation());

                        if(shopSignInfo.get("shopOwner").equals(username)) {
                            player.sendMessage(Main.MESSAGE_PREFIX+"自分のショップからは購入できません");
                            return;
                        }

                        int price = (int) shopSignInfo.get("price");
                        if (MoneySAPI.getInstance().isPayable(player, price, TaxType.USER_SHOP)) {
                            player.sendMessage(Main.MESSAGE_PREFIX+"所持金が不足しているため購入できません");
                            break;
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
                            player.sendMessage(Main.MESSAGE_PREFIX+"このショップは在庫切れです");
                            if(shopOwner != null) {
                                shopOwner.sendMessage(Main.MESSAGE_PREFIX+"あなたのチェストショップが在庫切れになっています！ 補給すべきもの " + pID + ":" + pMeta);
                            }
                            return;
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
                        MoneySAPI.getInstance().payMoney(username, String.valueOf(shopSignInfo.get("shopOwner")), price, TaxType.USER_SHOP);

                        player.sendMessage(Main.MESSAGE_PREFIX+"購入処理が完了しました");
                        if(shopOwner != null) {
                            shopOwner.sendMessage(Main.MESSAGE_PREFIX + username + "が" + pID + ":" + pMeta + "を購入しました （" + shopSignInfo.get("price") + MoneySAPI.UNIT + "）");
                        }
                    }
                    break;

                case Block.CHEST:
                    if(MoneySChestShopAPI.getInstance().existsShopBySign(block.getLocation())) {
                        if(!MoneySChestShopAPI.getInstance().isOwnerBySign(block.getLocation(), player)) {
                            player.sendMessage(Main.MESSAGE_PREFIX+"このチェストは保護されています");
                            event.setCancelled();
                        }
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        switch(block.getId()) {
            case Block.SIGN_POST:
            case Block.WALL_SIGN:
                if(MoneySChestShopAPI.getInstance().existsShopBySign(block.getLocation())) {
                    if(MoneySChestShopAPI.getInstance().isOwnerBySign(block.getLocation(), player)) {
                        MoneySChestShopAPI.getInstance().removeShopBySign(block.getLocation());
                        player.sendMessage(Main.MESSAGE_PREFIX+"チェストショップを閉じました");
                    } else {
                        player.sendMessage(Main.MESSAGE_PREFIX+"この看板は保護されています");
                        event.setCancelled();
                    }
                }
                break;
            case Block.CHEST:
                if(MoneySChestShopAPI.getInstance().existsShopByChest(block.getLocation())) {
                    if(MoneySChestShopAPI.getInstance().isOwnerByChest(block.getLocation(), player)) {
                        MoneySChestShopAPI.getInstance().removeShopByChest(block.getLocation());
                        player.sendMessage(Main.MESSAGE_PREFIX+"チェストショップを閉じました");
                    } else {
                        player.sendMessage(Main.MESSAGE_PREFIX+"このチェストは保護されています");
                        event.setCancelled();
                    }
                }
                break;
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
