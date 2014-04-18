package me.libraryaddict.convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import com.notoriousdev.yamlconfig.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Bungee extends Plugin implements Listener {
    private Bungee bungee = this;
    private File configFile;

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        try {
            URL url = getClass().getClassLoader().getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
        }
        return null;
    }

    public void onEnable() {
        configFile = new File(this.getDataFolder(), "config.yml");
        getProxy().getPluginManager().registerListener(this, this);
        saveDefaultConfig();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Database.setMysql(config.getString("MysqlDetails.Host"), config.getString("MysqlDetails.User"),
                config.getString("MysqlDetails.Password"), config.getString("MysqlDetails.Database"));
        for (String key : config.getConfigurationSection("Tables").getKeys(false)) {
            Database.addTable(config.getString(key + ".Database"), config.getString(key + ".Table"),
                    config.getString(key + ".UserField"));
        }
    }

    @EventHandler
    public void onLogin(final LoginEvent event) {
        event.registerIntent(bungee);
        bungee.getProxy().getScheduler().runAsync(bungee, new Runnable() {
            public void run() {
                Database.onLogin(event.getConnection().getUUID(), event.getConnection().getName());
                event.completeIntent(bungee);
            }
        });
    }

    public void saveDefaultConfig() {
        if (!this.configFile.exists())
            saveResource("config.yml", false);
    }

    public void saveResource(String resourcePath, boolean replace) {
        if ((resourcePath == null) || (resourcePath.equals(""))) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in "
                    + this.getFile());
        }

        File outFile = new File(this.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        try {
            if ((!outFile.exists()) || (replace)) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                getLogger().log(
                        Level.WARNING,
                        "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName()
                                + " already exists.");
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }
}
