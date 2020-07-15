package net.comorevi.np.moneys.chestshop.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import net.comorevi.np.moneys.chestshop.Main;
import net.comorevi.np.moneys.chestshop.util.DataCenter;

public class ChestShopCommand extends Command {
    public ChestShopCommand(String name) {
        super(name, "チェストショップの編集モードを有効/無効化します。", "/cshop");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.isPlayer()) {
            commandSender.sendMessage("このコマンドはゲーム内から実行できます。");
            return true;
        }

        if (DataCenter.existsEditCmdQueue((Player) commandSender)) {
            DataCenter.removeEditCmdQueue((Player) commandSender);
            commandSender.sendMessage(Main.MESSAGE_PREFIX + "チェストショップ編集モードを無効化しました。");
        } else {
            DataCenter.addEditCmdQueue((Player) commandSender);
            commandSender.sendMessage(Main.MESSAGE_PREFIX + "チェストショップ編集モードを有効化しました。");
        }
        return true;
    }
}
