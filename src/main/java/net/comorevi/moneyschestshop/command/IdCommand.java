package net.comorevi.moneyschestshop.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import net.comorevi.moneyschestshop.Main;
import net.comorevi.moneyschestshop.util.DataCenter;

public class IdCommand extends Command {
    public IdCommand(String name) {
        super(name, "IDチェッカーを有効/無効化します。", "/id");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.isPlayer()) {
            commandSender.sendMessage("このコマンドはゲームないから実行できます。");
            return true;
        }

        if (DataCenter.existsIdCmdQueue((Player) commandSender)) {
            DataCenter.removeIdCmdQueue((Player) commandSender);
            commandSender.sendMessage(Main.MESSAGE_PREFIX + "ID確認モードを無効化しました。");
        } else {
            DataCenter.addIdCmdQueue((Player) commandSender);
            commandSender.sendMessage(Main.MESSAGE_PREFIX + "ID確認モードを有効化しました。");
        }
        return true;
    }
}
