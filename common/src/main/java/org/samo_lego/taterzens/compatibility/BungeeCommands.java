package org.samo_lego.taterzens.compatibility;

/**
 * Available / supported bungee commands.
 *
 * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#wikiPage">Spigot thread</a> on message channels.
 * @see <a href="https://github.com/VelocityPowered/Velocity/blob/65db0fad6a221205ec001f1f68a032215da402d6/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/BungeeCordMessageResponder.java#L297">Proxy implementation</a> on GitHub.
 *
 */
public enum BungeeCommands {
    SERVER("ConnectOther"),
    MESSAGE("Message"),
    MESSAGE_RAW("MessageRaw"),
    KICK("KickPlayer");

    private final String command;

    BungeeCommands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
