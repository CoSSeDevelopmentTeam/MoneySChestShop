package net.comorevi.moneyschestshop;

import FormAPI.api.FormAPI;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.*;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import net.comorevi.moneyschestshop.util.DataCenter;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class EventListener implements Listener {

    private Main mainClass;
    private FormAPI formAPI = new FormAPI();

    public EventListener(Main plugin) {
        this.mainClass = plugin;
        formAPI.add("create-cshop", getCreateCShopWindw());
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
            player.sendMessage("システム>> BlockData" + "\n" +
                    "Name: " + blockName + "\n" +
                    "ID: " + blockId + "\n" +
                    "Meta: " + blockMeta);
            player.sendMessage("システム>> HoldItemData" + "\n" +
                    "Name: " + player.getInventory().getItemInHand().getName() + "\n" +
                    "ID: " + player.getInventory().getItemInHand().getId() + "\n" +
                    "Damage: " + player.getInventory().getItemInHand().getDamage() + "/" + player.getInventory().getItemInHand().getMaxDurability() + "\n");
            event.setCancelled();
        } else if (DataCenter.existsEditCmdQueue(player)) {
            switch (blockId) {
                case Block.SIGN_POST:
                case BlockID.WALL_SIGN:
                    if (mainClass.getServer().getLevelByName(block.level.getName()).getBlockEntity(block.getLocation()) instanceof BlockEntitySign) {
                        BlockEntitySign sign = (BlockEntitySign) mainClass.getServer().getLevelByName(block.level.getName()).getBlockEntity(block.getLocation());
                        if (sign.getText()[0].equals("cshop") && sign.getText()[1].equals(player.getName()) && getSideChest(block.getLocation()).getId() == Block.CHEST) {
                            player.showFormWindow(formAPI.get("create-cshop"), formAPI.getId("create-cshop"));
                            DataCenter.addEditCmdQueue(player, block);
                        }
                    }
                    break;
            }
        } else {
            switch(blockId) {
                case Block.SIGN_POST:
                case Block.WALL_SIGN:
                    Object[] signCondition = {(int) block.x, (int) block.y, (int) block.z, block.getLevel().getName()};
                    if(MoneySChestShopAPI.getInstance().existsShop(signCondition)) {
                        LinkedHashMap<String, Object> shopSignInfo = MoneySChestShopAPI.getInstance().getShopData(signCondition);

                        if(shopSignInfo.get("shopOwner").equals(username)) {
                            player.sendMessage("システム>> 自分のショップからは購入できません");
                            return;
                        }

                        int price = (int) shopSignInfo.get("price");
                        int priceIncludeCommission = (int) (price * 1.05);
                        int buyermoney = mainClass.moneySAPI.getMoney(player.getName());
                        if(buyermoney < priceIncludeCommission) {
                            player.sendMessage("システム>> 所持金が不足しているため購入できません");
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
                            player.sendMessage("システム>> このショップは在庫切れです");
                            if(shopOwner != null) {
                                shopOwner.sendMessage("システム>> あなたのチェストショップが在庫切れになっています！ 補給すべきもの " + pID + ":" + pMeta);
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
                        mainClass.moneySAPI.reduceMoney(username, (int) (price * 0.05));
                        mainClass.moneySAPI.payMoney(username, String.valueOf(shopSignInfo.get("shopOwner")), (int) shopSignInfo.get("price"));

                        player.sendMessage("システム>> 購入処理が完了しました");
                        if(shopOwner != null) {
                            shopOwner.sendMessage("システム>> " + username + "が" + pID + ":" + pMeta + "を購入しました （" + shopSignInfo.get("price") + mainClass.moneySAPI.getMoneyUnit() + "）");
                        }
                    }
                    break;

                case Block.CHEST:
                    Object[] chestCondition = {(int) block.x, (int) block.y, (int) block.z, block.getLevel().getName()};
                    if(MoneySChestShopAPI.getInstance().existsShop(chestCondition)) {
                        if(!MoneySChestShopAPI.getInstance().isOwner(chestCondition, player)) {
                            player.sendMessage("システム>> このチェストは保護されています");
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
                Object[] signCondition = {(int) block.x, (int) block.y, (int) block.z, block.getLevel().getName()};
                if(MoneySChestShopAPI.getInstance().existsShop(signCondition)) {
                    if(MoneySChestShopAPI.getInstance().isOwner(signCondition, player)) {
                        MoneySChestShopAPI.getInstance().removeShopBySign(signCondition);
                        player.sendMessage("システム>> チェストショップを閉じました");
                    } else {
                        player.sendMessage("システム>> この看板は保護されています");
                        event.setCancelled();
                    }
                }
                break;
            case Block.CHEST:
                Object[] chestCondition = {(int) block.x, (int) block.y, (int) block.z, block.getLevel().getName()};
                if(MoneySChestShopAPI.getInstance().existsShop(chestCondition)) {
                    if(MoneySChestShopAPI.getInstance().isOwner(chestCondition, player)) {
                        MoneySChestShopAPI.getInstance().removeShopByChest(chestCondition);
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
    public void onFormResponded(PlayerFormRespondedEvent event) {
        if (event.getFormID() == formAPI.getId("create-cshop")) {
            if (event.wasClosed()) return;
            FormResponseCustom responseCustom = (FormResponseCustom) event.getResponse();
            if (responseCustom.getInputResponse(1) != null && responseCustom.getInputResponse(2) != null && responseCustom.getInputResponse(4) != null) {
                int itemId = 0;
                int itemMeta = 0;
                int itemAmount = (int) responseCustom.getSliderResponse(3);
                int itemPrice = 0;
                try {
                    itemId = Integer.parseInt(responseCustom.getInputResponse(1));
                    itemMeta = Integer.parseInt(responseCustom.getInputResponse(2));
                    itemPrice = Integer.parseInt(responseCustom.getInputResponse(4));
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage(TextFormat.GRAY + "システム>>" + TextFormat.RESET + "適切な値を入力してください。");
                }
                if (itemId <= 0 || itemAmount <= 0 || itemPrice < 0 || getSideChest(DataCenter.getRegisteredBlockByEditCmdQueue(event.getPlayer())).getId() != Block.CHEST || itemMeta < 0) {
                    event.getPlayer().sendMessage(TextFormat.GRAY + "システム>>" + TextFormat.RESET + "適切な値を入力してください。または看板がチェストに接しているか確認してください。");
                } else {
                    BlockEntitySign sign = (BlockEntitySign) event.getPlayer().getLevel().getBlockEntity(DataCenter.getRegisteredBlockByEditCmdQueue(event.getPlayer()).getLocation());
                    sign.setText(TextFormat.WHITE + Item.get(itemId).getName(), "個数: " + itemAmount, "値段(手数料込): " + (int) (itemPrice * 1.05), event.getPlayer().getName());
                    MoneySChestShopAPI.getInstance().registerShop(event.getPlayer().getName(), itemAmount, itemPrice, itemId, itemMeta, DataCenter.getRegisteredBlockByEditCmdQueue(event.getPlayer()), getSideChest(DataCenter.getRegisteredBlockByEditCmdQueue(event.getPlayer()).getLocation()));
                    event.getPlayer().sendMessage(TextFormat.GRAY + "システム>>" + TextFormat.RESET + "チェストショップを作成しました。\n編集モードをオフにするには/cshopを実行。");
                }
            } else {
                event.getPlayer().sendMessage(TextFormat.GRAY + "システム>>" + TextFormat.RESET + "すべての入力欄に適切な値を入力してください。");
            }
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

    private FormWindowCustom getCreateCShopWindw() {
        Element[] elements = {
                new ElementLabel("ショップの情報を入力してください。適切な値を入力しなければ作成できません。"),
                new ElementInput("Item ID", "1以上256以下で入力..."),
                new ElementInput("Item META(DAMAGE)", "メタ値を入力...", String.valueOf(0)),
                new ElementSlider("Amount", 1, 64, 1, 4),
                new ElementInput("Price", "0以上で入力...")
        };
        return new FormWindowCustom("Create - ChestShop", Arrays.asList(elements));
    }
}
