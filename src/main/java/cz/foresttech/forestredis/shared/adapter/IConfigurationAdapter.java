package cz.foresttech.forestredis.shared.adapter;

import org.apache.commons.lang.NotImplementedException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ConfigurationAdapter interface which handles differences between BungeeCord and Spigot in configuration structure.
 */
public interface IConfigurationAdapter {

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Setups the configuration file by provided name.
     *
     * @param fileName Name of the file to setup
     */
    void setup(String fileName);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Checks if the configuration is setup
     *
     * @return If the configuration is setup
     */
    boolean isSetup();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Loads the configuration
     */
    void loadConfiguration();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Returns the String value from the configuration.
     *
     * @param path Path in the configuration.
     * @param def  Default value if the path is not set.
     * @return String value from the configuration. Returns "def" if path is not available.
     */
    String getString(String path, String def);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Returns the int value from the configuration.
     *
     * @param path Path in the configuration.
     * @param def  Default value if the path is not set.
     * @return int value from the configuration. Returns "def" if path is not available.
     */
    int getInt(String path, int def);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Returns the boolean value from the configuration.
     *
     * @param path Path in the configuration.
     * @param def  Default value if the path is not set.
     * @return boolean value from the configuration. Returns "def" if path is not available.
     */
    boolean getBoolean(String path, boolean def);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Returns the list of Strings from the configuration.
     *
     * @param path Path in the configuration.
     * @return list of strings from the configuration. Returns empty list if path is not available.
     */
    List<String> getStringList(String path);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Gets the requested List of Maps by path.
     * If the List does not exist, this will return an empty List.
     * This method will attempt to cast any values into a Map if possible,
     * but may miss any values out if they are not compatible.
     * @param path Path in the configuration.
     * @return Requested List of Maps
     */
    List<Map<?,?>> getMapList(String path);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Utility method for processing command channels from the config into POJOs.
     * @param receive if true, returns the list of receiving CommandChannels, otherwise the sending.
     * NOTE: Sending channels are not implemented.
     * @return a list containing channels. Returns empty list if commands are disabled.
     */
    default List<CommandChannel> getCommandChannels(boolean receive) {

        String direction;
        if(receive){
            direction = "receive";
        } else {
            direction = "send";
        }

        if(!getBoolean("commands."+direction+".enabled", false))
            return Collections.emptyList();

        return getMapList("commands."+direction+".list")
                .stream()
                .filter(m->(Boolean) m.get("enabled"))
                .map(m->new CommandChannel(
                        (String) m.get("channel"),
                        CommandChannel.AllowMode.valueOf(((String)m.get("allow")).toUpperCase()),
                        (List<String>) m.get("list")
                ))
                .collect(Collectors.toList());
    }

    /*----------------------------------------------------------------------------------------------------------*/

}
