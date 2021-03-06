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
 *   - 1.3.1
 *     メッセージに関わる部分を修正
 *   - 1.3.2
 *     SQLite3DataProviderの修正とAPIの書き直しに伴う変更
 *     データベースの形式は維持。下位互換性確保。
 *   - 1.3.3
 *     メッセージ部分の変更
 *  - 1.4.0
 *    MoneySAPI v4.0.0対応
 *   - 1.4.1
 *     手数料の割合をMoneySAPIから取得するように変更
 *   - 1.4.2
 *     しふぉんを持ってチェストや看板をタッチした時には操作をキャンセル
 *   - 1.4.3
 *     しふぉんを持って看板をタッチした時しふぉんを開かないように変更
 *   - 1.4.4
 *     1.4.4の変更を取り消し
 *  - 1.5.0
 *    FormAPIを変更
 *
 **/

package net.comorevi.np.moneys.chestshop;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import net.comorevi.np.moneys.chestshop.command.ChestShopCommand;
import net.comorevi.np.moneys.chestshop.command.IdCommand;

public class Main extends PluginBase {
    public static final String MESSAGE_PREFIX = TextFormat.GRAY+"ChestShop>> "+TextFormat.RESET;

    @Override
    public void onEnable() {
        //データ保存ファイルがなければ作成
        if(!(getDataFolder().exists())) {
            getDataFolder().mkdir();
        }
        //イベントを登録
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        //コマンドを登録
        this.getServer().getCommandMap().register("MoneySChestShop", new IdCommand("id"));
        this.getServer().getCommandMap().register("MoneySChestShop", new ChestShopCommand("cshop"));
    }

    @Override
    public void onDisable() {
        MoneySChestShopAPI.getInstance().disconnectSQL();
    }

}
