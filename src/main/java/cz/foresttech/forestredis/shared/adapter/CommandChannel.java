package cz.foresttech.forestredis.shared.adapter;

import java.util.List;

public class CommandChannel {

    public final String channel;
    public final AllowMode allowMode;
    public final List<String> list;

    public CommandChannel(String channel, AllowMode allowMode, List<String> list) {
        this.channel = channel;
        this.allowMode = allowMode;
        this.list = list;
    }

    public boolean isAllowed(String cmd){
        switch (allowMode) {
            case ALL: return true;
            case WHITELIST_HARD: return list.contains(cmd);
            case WHITELIST_SOFT: return list.contains(cmd.split(" ")[0]);
            case BLACKLIST_HARD: return !list.contains(cmd);
            case BLACKLIST_SOFT: return !list.contains(cmd.split(" ")[0]);
        }
        return false;
    }

    public enum AllowMode {
        ALL,
        WHITELIST_HARD, //check command and arguments
        WHITELIST_SOFT, //only check command, not arguments
        BLACKLIST_HARD, //check command and arguments
        BLACKLIST_SOFT //only check command, not arguments
    }

}
