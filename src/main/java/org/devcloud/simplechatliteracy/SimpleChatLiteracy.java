package org.devcloud.simplechatliteracy;

import org.bukkit.plugin.java.JavaPlugin;
import org.devcloud.simplechatliteracy.listener.ChatListener;

public class SimpleChatLiteracy extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getLogger().info("SimpleChatLiteracy enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleChatLiteracy disabled!");
    }
}
