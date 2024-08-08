package cz.foresttech.forestredis.bungee.commands;

import cz.foresttech.forestredis.bungee.ForestRedisBungee;
import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.adapter.CommandChannel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BungeeCord Command Class for handling ForestRedisAPI commands
 */
public class BungeeForestRedisCommand extends Command {

    public BungeeForestRedisCommand() {
        super("forestredisproxy");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if ((commandSender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            if (p.hasPermission("forestredis.admin")) {
                return;
            }
        }

        if (args.length == 0) {
            commandSender.sendMessage("§2["+ForestRedisBungee.getInstance().getDescription().getName()+"] §7You're currently running on §e" + ForestRedisBungee.getInstance().getDescription().getVersion());
            return;
        }

        switch (args[0]) {

            case "publish":
                if(args.length < 3) {
                    commandSender.sendMessage("§2["+ ForestRedisBungee.getInstance().getDescription().getName()+"] §7Missing arguments!");
                    return;
                }
                List<CommandChannel> commandChannels = ForestRedisBungee.getInstance().getConfigAdapter().getCommandChannels(false);
                CommandChannel commandChannel = commandChannels.stream().filter(c->c.channel.equals(args[1])).findAny().orElse(null);
                String cmd = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                if(commandChannel != null){
                    if(!commandChannel.isAllowed(cmd)){
                        commandSender.sendMessage("§2["+ForestRedisBungee.getInstance().getDescription().getName()+"] §7 !");
                        ForestRedisBungee.getInstance().logger().warning("[TX] Blocked disallowed redis command: " + args[1] + " -> '" + cmd + "'");
                        return;
                    }
                    ForestRedisBungee.getInstance().logger().info("[TX] Redis command: " + args[1] + " -> '" + cmd + "'");
                    ForestRedisBungee.getInstance().runAsync(() -> {
                        try (Jedis jedis = RedisManager.getAPI().getJedis()) {
                            jedis.publish(args[1], cmd);
                        } catch (Exception e) {
                            ForestRedisBungee.getInstance().logger().warning("Could not send message to the Redis server!");
                        }
                    });
                } else {
                    commandSender.sendMessage("§2["+ForestRedisBungee.getInstance().getDescription().getName()+"] §7Channel '"+args[1]+"' is not whitelisted for sending commands!");
                }
                return;

            case "reload":
                ForestRedisBungee.getInstance().load();
                commandSender.sendMessage("§2["+ForestRedisBungee.getInstance().getDescription().getName()+"] §7ForestRedis successfully reloaded!");
                return;

            default:
                commandSender.sendMessage("§2["+ForestRedisBungee.getInstance().getDescription().getName()+"] §7Unknown command!");
                return;
        }
    }
}
