package cz.foresttech.forestredis.velocity;

import com.velocitypowered.api.plugin.Plugin;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandManager;
import cz.foresttech.forestredis.shared.IForestRedisPlugin;
import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.velocity.adapter.VelocityConfigAdapter;
import cz.foresttech.forestredis.velocity.commands.VelocityForestRedisCommand;
import cz.foresttech.forestredis.velocity.events.RedisMessageReceivedEvent;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Bootstrap Velocity plugin to setup the {@link RedisManager} using configuration file.
 * Also provides server with reload and version command.
 */
@Plugin(
        id = "forestredisapi",
        name = "ForestRedisAPI",
        version = "1.3.0",
        authors = {"ForestTech"}
)
public class ForestRedisVelocity implements IForestRedisPlugin {

    private static ForestRedisVelocity instance;
    private final ProxyServer server;
    private final CommandManager commandManager;
    private final Logger logger;
    private final Path dataDirectory;
    private final String version;

    @Inject
    public ForestRedisVelocity(ProxyServer server, CommandManager commandManager, Logger logger, @DataDirectory Path dataDirectory, PluginContainer container) {
        this.server = server;
        this.commandManager = commandManager;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.version = container.getDescription().getVersion().orElse("unknown");
        instance = this;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        load();
        commandManager.register("forestredisproxy", new VelocityForestRedisCommand());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // Close the RedisManager
        if (RedisManager.getAPI() == null) {
            return;
        }
        RedisManager.getAPI().close();
    }

    @Override
    public void runAsync(Runnable task) {
        server.getScheduler().buildTask(this, task).schedule();
    }

    @Override
    public void onMessageReceived(String channel, MessageTransferObject messageTransferObject) {
        server.getEventManager().fireAndForget(new RedisMessageReceivedEvent(channel, messageTransferObject));
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public IConfigurationAdapter getConfigAdapter() {
        VelocityConfigAdapter velocityConfigAdapter = new VelocityConfigAdapter(dataDirectory, logger);
        velocityConfigAdapter.setup("config");
        return velocityConfigAdapter;
    }

    @Override
    public void executeCmd(String cmd) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), cmd);
    }

    public String getVersion() {
        return version;
    }

    /**
     * Obtains the instance of the plugin
     *
     * @return  Instance of {@link ForestRedisVelocity}
     */
    public static ForestRedisVelocity getInstance() {
        return instance;
    }
}