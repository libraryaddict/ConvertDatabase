package me.libraryaddict.convert;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Bukkit extends JavaPlugin implements Listener {

    public void onEnable() {
        if (getServer().getVersion().toLowerCase().contains("spigot")) {
            throw new RuntimeException("Not enabling this plugin as you do not run spigot");
        }
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        Database.setMysql(getConfig().getString("MysqlDetails.Host"), getConfig().getString("MysqlDetails.User"), getConfig()
                .getString("MysqlDetails.Password"), getConfig().getString("MysqlDetails.Database"));
        for (String key : getConfig().getConfigurationSection("Tables").getKeys(false)) {
            Database.addTable(getConfig().getString(key + ".Database"), getConfig().getString(key + ".Table"), getConfig()
                    .getString(key + ".UserField"));
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        Database.onLogin(event.getUniqueId().toString(), event.getName());
    }
}
