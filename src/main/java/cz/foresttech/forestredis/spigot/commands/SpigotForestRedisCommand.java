package cz.foresttech.forestredis.spigot.commands;

import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.adapter.CommandChannel;
import cz.foresttech.forestredis.spigot.ForestRedisSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spigot Command Class for handling ForestRedisAPI commands
 */
public class SpigotForestRedisCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (!player.hasPermission("forestredis.admin")) {
                return true;
            }
        }

        if (args.length == 0) {
            commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7You're currently running on §e" + ForestRedisSpigot.getInstance().getDescription().getVersion());
            return true;
        }

        switch (args[0]) {

            case "publish":
                if(args.length < 3) {
                    commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7Missing arguments!");
                    return true;
                }
                List<CommandChannel> commandChannels = ForestRedisSpigot.getInstance().getConfigAdapter().getCommandChannels(false);
                CommandChannel commandChannel = commandChannels.stream().filter(c->c.channel.equals(args[1])).findAny().orElse(null);
                String cmd = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                if(commandChannel != null){
                    if(!commandChannel.isAllowed(cmd)){
                        commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7 !");
                        ForestRedisSpigot.getInstance().logger().warning("[TX] Blocked disallowed redis command: " + args[1] + " -> '" + cmd + "'");
                        return true;
                    }
                    ForestRedisSpigot.getInstance().logger().info("[TX] Redis command: " + args[1] + " -> '" + cmd + "'");
                    RedisManager.getAPI().getJedis().publish(args[1], cmd);
                } else {
                    commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7Channel '"+args[1]+"' is not whitelisted for sending commands!");
                }
                return true;

            case "reload":
                ForestRedisSpigot.getInstance().load();
                commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7ForestRedis successfully reloaded!");
                return true;

            default:
                commandSender.sendMessage("§2["+ForestRedisSpigot.getInstance().getDescription().getName()+"] §7Unknown command!");
                return true;
        }
    }
}
