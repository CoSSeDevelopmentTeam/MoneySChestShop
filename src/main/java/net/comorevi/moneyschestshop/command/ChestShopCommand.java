package net.comorevi.moneyschestshop.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import net.comorevi.moneyschestshop.util.DataCenter;

public class ChestShopCommand extends Command {
    public ChestShopCommand(String name) {
        super(name, "チェストショップの編集モードを有効/無効化します。", "/cshop");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.isPlayer()) {
            commandSender.sendMessage("このコマンドはゲームないから実行できます。");
            return true;
        }

        if (DataCenter.existsEditCmdQueue((Player) commandSender)) {
            DataCenter.removeEditCmdQueue((Player) commandSender);
        } else {
            DataCenter.addEditCmdQueue((Player) commandSender);
        }
        return true;
    }
}
