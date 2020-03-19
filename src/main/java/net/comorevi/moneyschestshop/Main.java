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
 *   - 1.1.5
 *     ID表示機能に手に持ってるアイテムの情報も追加
 *  - 1.2.0
 *    MoneySAPIv3.1.2に対応、nukkitxにレポジトリを変更
 *  - 1.3.0
 *    ショップ作成にフォームを使用するように変更、依存ライブラリはFormAPI
 *
 */

package net.comorevi.moneyschestshop;

import cn.nukkit.plugin.PluginBase;
import net.comorevi.moneyapi.MoneySAPI;
import net.comorevi.moneyschestshop.command.ChestShopCommand;
import net.comorevi.moneyschestshop.command.IdCommand;

public class Main extends PluginBase {

    protected MoneySAPI moneySAPI;

    @Override
    public void onEnable() {
        //データ保存ファイルがなければ作成
        if(!(getDataFolder().exists())) {
            getDataFolder().mkdir();
        }
        //イベントを登録
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        //コマンドを登録
        this.getServer().getCommandMap().register("id", new IdCommand("id"));
        this.getServer().getCommandMap().register("cshop", new ChestShopCommand("cshop"));
        //
        this.moneySAPI = (MoneySAPI) getServer().getPluginManager().getPlugin("MoneySAPI");
    }

    /*
    @Override
    public void onDisable() {
        //disconnect sql
    }

     */
}
