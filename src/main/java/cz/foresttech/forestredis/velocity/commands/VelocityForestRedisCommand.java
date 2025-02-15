package cz.foresttech.forestredis.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.adapter.CommandChannel;
import cz.foresttech.forestredis.velocity.ForestRedisVelocity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BungeeCord Command Class for handling ForestRedisAPI commands
 */
public class VelocityForestRedisCommand implements SimpleCommand {

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if ((source instanceof Player)) {
            Player p = (Player) source;
            if (p.hasPermission("forestredis.admin")) {
                return;
            }
        }

        if (args.length == 0) {
            source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7You're currently running on §e" + ForestRedisVelocity.getInstance().getVersion()));
            return;
        }

        switch (args[0]) {

            case "publish":
                if(args.length < 3) {
                    source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7Missing arguments!"));
                    return;
                }
                List<CommandChannel> commandChannels = ForestRedisVelocity.getInstance().getConfigAdapter().getCommandChannels(false);
                CommandChannel commandChannel = commandChannels.stream().filter(c->c.channel.equals(args[1])).findAny().orElse(null);
                String cmd = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                if(commandChannel != null){
                    if(!commandChannel.isAllowed(cmd)){
                        source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7 !"));
                        ForestRedisVelocity.getInstance().logger().warning("[TX] Blocked disallowed redis command: " + args[1] + " -> '" + cmd + "'");
                        return;
                    }
                    ForestRedisVelocity.getInstance().logger().info("[TX] Redis command: " + args[1] + " -> '" + cmd + "'");
                    ForestRedisVelocity.getInstance().runAsync(() -> {
                        try (Jedis jedis = RedisManager.getAPI().getJedis()) {
                            jedis.publish(args[1], cmd);
                        } catch (Exception e) {
                            ForestRedisVelocity.getInstance().logger().warning("Could not send message to the Redis server!");
                        }
                    });
                } else {
                    source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7Channel '"+args[1]+"' is not whitelisted for sending commands!"));
                }
                return;

            case "reload":
                ForestRedisVelocity.getInstance().load();
                source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7ForestRedis successfully reloaded!"));
                return;

            default:
                source.sendMessage(serializer.deserialize("§2[ForestRedisAPI] §7Unknown command!"));
                return;
        }
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission("forestredis.admin");
    }
}
