/**
 * MoneySChestShop
 * PocketMine-MPのプラグインであるChestShopのNukkit移植＆MoneySAPI対応版
 *
 * CosmoSunriseServerPluginEditorsTeam
 *
 * HP: http://info.comorevi.net
 * GitHub: https://github.com/CosmoSunriseServerPluginEditorsTeam
 *
 *
 * [Java版]
 * @author popkechupki
 *
 * [PHP版]
 * @author MinecrafterJPN
 *
 * 機能版 アップデート履歴
 *
 * - 1.0.0
 *    基本的な機能を追加。ほぼ動作するように。
 *  - 1.1.0
 *    手数料の概念を追加
 *   - 1.1.1
 *     手数料の数値が一桁ずれていたのを修正
 *   - 1.1.2
 *     手数料の処理を変更。データベースの金額に購入時に再計算するように
 *   - 1.1.3
 *     ショプ作成時重複して作成される問題の修正
 *     在庫がなくても変えてしまう問題の修正
 *     購入金額が一定の金額に固定される問題の修正
 *     ショップオーナーに登録された金額が支払われていない問題の修正
 *   - 1.1.4
 *     ショップ情報にワールドも保管するように変更
 *
 */

package net.comorevi.moneyschestshop;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import net.comorevi.moneyapi.MoneySAPI;

import java.util.ArrayList;

public class MoneySChestShop extends PluginBase {

    private MoneySAPI moneySAPI;
    private ArrayList<String> list = new ArrayList();

    @Override
    public void onEnable() {
        //データ保存ファイルがなければ作成
        if(!(getDataFolder().exists())) {
            getDataFolder().mkdir();
        }
        SQLite3DataProvider sqLite3DataProvider = new SQLite3DataProvider(this);
        //イベントを登録
        this.getServer().getPluginManager().registerEvents(new EventListener(this, sqLite3DataProvider), this);
        //MoneySAPIが読み込まれているか確認
        try {
            this.moneySAPI = (MoneySAPI) getServer().getPluginManager().getPlugin("MoneySAPI");
        } catch(Exception e) {
            getServer().getLogger().alert("MoneySAPIが読み込まれていないため無効化します");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        switch(command.getName()) {
            case "id":
                if(commandSender instanceof Player) {
                    String username = commandSender.getName();
                    if(containsUser(username)) {
                        addPlayerToList((Player) commandSender, username);
                    } else {
                        removePlayerFromList((Player) commandSender, username);
                    }
                } else {
                    getServer().getLogger().info("このコマンドはゲーム内からのみ実行可能です。");
                }
                return true;
        }
        return false;
    }
    
    public MoneySAPI getMoneySAPI() {
        return moneySAPI;
    }
    
    public boolean containsUser(String username) {
        if(this.list.contains(username)) {
            return true;
        } else {
            return false;
        }
    }

    public void addPlayerToList(Player player, String username) {
        this.list.remove(username);
        player.sendMessage(TextFormat.AQUA + "システム>> " + TextFormat.RED + "無効化しました。/Disabled.");
    }

    public void removePlayerFromList(Player player, String username) {
        this.list.add(username);
        player.sendMessage(TextFormat.AQUA + "システム>> " + TextFormat.RED + "有効化しました。/Enabled.");
    }
}
