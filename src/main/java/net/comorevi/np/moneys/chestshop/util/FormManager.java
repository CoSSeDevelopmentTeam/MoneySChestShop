package net.comorevi.np.moneys.chestshop.util;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import net.comorevi.np.moneys.chestshop.Main;
import net.comorevi.np.moneys.chestshop.MoneySChestShopAPI;
import net.comorevi.np.moneys.util.TaxType;
import ru.nukkitx.forms.elements.CustomForm;

public class FormManager {

    public void sendCreateCShopWindow(Player player) {
        getCreateCShopWindow().send(player, (targetPlayer, targetForm, data) -> {
            if (data == null) return;
            FormResponseCustom response = targetForm.getResponse();
            if (response.getInputResponse(1) != null && response.getInputResponse(2) != null && response.getInputResponse(4) != null) {
                int itemId = 0;
                int itemMeta = 0;
                int itemAmount = (int) response.getSliderResponse(3);
                int itemPrice = 0;
                try {
                    itemId = Integer.parseInt(response.getInputResponse(1));
                    itemMeta = Integer.parseInt(response.getInputResponse(2));
                    itemPrice = Integer.parseInt(response.getInputResponse(4));
                } catch (NumberFormatException e) {
                    player.sendMessage(Main.MESSAGE_PREFIX+"適切な値を入力してください。");
                }
                if (itemId <= 0 || itemAmount <= 0 || itemPrice < 0 || getSideChest(DataCenter.getRegisteredBlockByEditCmdQueue(player)).getId() != Block.CHEST || itemMeta < 0) {
                    player.sendMessage(Main.MESSAGE_PREFIX+"適切な値を入力してください。または看板がチェストに接しているか確認してください。");
                } else {
                    BlockEntitySign sign = (BlockEntitySign) player.getLevel().getBlockEntity(DataCenter.getRegisteredBlockByEditCmdQueue(player).getLocation());
                    sign.setText(TextFormat.WHITE + Item.get(itemId).getName(), "個数: " + itemAmount, "値段(手数料込): " + (int) (itemPrice * TaxType.USER_SHOP.getRatio()), player.getName());
                    MoneySChestShopAPI.getInstance().createShop(player.getName(), itemAmount, itemPrice, itemId, itemMeta, DataCenter.getRegisteredBlockByEditCmdQueue(player), getSideChest(DataCenter.getRegisteredBlockByEditCmdQueue(player).getLocation()));
                    player.sendMessage(Main.MESSAGE_PREFIX+"チェストショップを作成しました。\n編集モードをオフにするには/cshopを実行。");
                }
            } else {
                player.sendMessage(Main.MESSAGE_PREFIX+"すべての入力欄に適切な値を入力してください。");
            }
        });
    }

    public CustomForm getCreateCShopWindow() {
        CustomForm customForm = new CustomForm("作成 - ChestShop")
                .addLabel("ショップの情報を入力してください。適切な値を入力しなければ作成できません。")
                .addInput("Item ID", "アイテムIDを入力...")
                .addInput("Item META(DAMAGE)", "メタ値を入力...", String.valueOf(0))
                .addSlider("Amount", 1, 64, 1, 4)
                .addInput("Price", "0以上で入力...");
        return customForm;
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
