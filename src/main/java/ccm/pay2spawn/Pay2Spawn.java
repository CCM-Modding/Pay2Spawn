/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn;

import ccm.libs.com.jadarstudios.developercapes.DevCapes;
import ccm.pay2spawn.checkers.CheckerHandler;
import ccm.pay2spawn.checkers.TwitchChecker;
import ccm.pay2spawn.cmd.CommandP2S;
import ccm.pay2spawn.cmd.CommandP2SPermissions;
import ccm.pay2spawn.cmd.CommandP2SServer;
import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.configurator.HTMLGenerator;
import ccm.pay2spawn.network.*;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.*;
import com.google.common.base.Strings;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import static ccm.pay2spawn.util.Constants.*;

/**
 * The main mod class
 *
 * @author Dries007
 */
@Mod(modid = MODID, name = NAME)
public class Pay2Spawn
{
    @Mod.Instance(MODID)
    public static Pay2Spawn instance;
    public static boolean enable  = true;
    public static boolean forceOn = false;

    @Mod.Metadata(MODID)
    private ModMetadata          metadata;
    private RewardsDB            rewardsDB;
    private P2SConfig            config;
    private File                 configFolder;
    private Logger               logger;
    private SimpleNetworkWrapper snw;
    private boolean              newConfig;

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static RewardsDB getRewardsDB()
    {
        return instance.rewardsDB;
    }

    public static Logger getLogger() { return instance.logger; }

    public static P2SConfig getConfig() { return instance.config; }

    public static File getFolder()
    {
        return instance.configFolder;
    }

    public static SimpleNetworkWrapper getSnw()
    {
        return instance.snw;
    }

    public static File getRewardDBFile() { return new File(instance.configFolder, NAME + ".json"); }

    public static void reloadDB()
    {
        instance.rewardsDB = new RewardsDB(new File(instance.configFolder, NAME + ".json"));
        ConfiguratorManager.reload();
        try
        {
            PermissionsHandler.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void reloadDB_Server() throws Exception
    {
        StatusMessage.serverConfig = GSON_NOPP.toJson(JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))));
        StatusMessage.sendConfigToAllPlayers();
    }

    public static void reloadDBFromServer(String input)
    {
        instance.rewardsDB = new RewardsDB(input);
        ConfiguratorManager.reload();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        logger = event.getModLog();

        configFolder = new File(event.getModConfigurationDirectory(), NAME);
        //noinspection ResultOfMethodCallIgnored
        configFolder.mkdirs();

        File configFile = new File(configFolder, NAME + ".cfg");
        newConfig = !configFile.exists();
        config = new P2SConfig(configFile);
        MetricsHelper.init();

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        snw.registerMessage(MessageMessage.Handler.class, MessageMessage.class, id++, Side.SERVER);
        snw.registerMessage(MusicMessage.Handler.class, MusicMessage.class, id++, Side.CLIENT);
        snw.registerMessage(NbtRequestMessage.Handler.class, NbtRequestMessage.class, id++, Side.CLIENT);
        snw.registerMessage(NbtRequestMessage.Handler.class, NbtRequestMessage.class, id++, Side.SERVER);
        snw.registerMessage(RewardMessage.Handler.class, RewardMessage.class, id++, Side.SERVER);
        snw.registerMessage(StatusMessage.Handler.class, StatusMessage.class, id++, Side.SERVER);
        snw.registerMessage(StatusMessage.Handler.class, StatusMessage.class, id++, Side.CLIENT);
        snw.registerMessage(TestMessage.Handler.class, TestMessage.class, id++, Side.SERVER);
        snw.registerMessage(StructureImportMessage.Handler.class, StructureImportMessage.class, id++, Side.SERVER);
        snw.registerMessage(StructureImportMessage.Handler.class, StructureImportMessage.class, id++, Side.CLIENT);

        TypeRegistry.preInit();
        Statistics.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws MalformedURLException
    {
        ServerTickHandler.INSTANCE.init();

        TypeRegistry.doConfig(config.configuration);
        config.save();

        rewardsDB = new RewardsDB(getRewardDBFile());

        if (event.getSide().isClient())
        {
            CheckerHandler.init();
            new EventHandler();
            ClientCommandHandler.instance.registerCommand(new CommandP2S());
            DevCapes.getInstance().addGroup(Constants.MODID, Constants.CAPEURL);
        }

        ClientTickHandler.INSTANCE.init();
        ConnectionHandler.INSTANCE.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        for (TypeBase base : TypeRegistry.getAllTypes()) base.printHelpList(configFolder);

        TypeRegistry.registerPermissions();
        try
        {
            HTMLGenerator.init();
        }
        catch (IOException e)
        {
            logger.warn("Error initializing the HTMLGenerator.");
            e.printStackTrace();
        }

        if (newConfig)
        {
            JOptionPane.showMessageDialog(null, "Please configure Pay2Spawn properly BEFORE you try launching this instance again.\n" +
                            "You should provide AT LEAST your channel in the config. Pay2Spawn will crash otherwise.\n\n" +
                            "If you need help with the configuring of your rewards, contact us!", "Please configure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );

            System.exit(1);
        }

        if (Pay2Spawn.getConfig().majorConfigVersionChange)
        {
            JOptionPane.showMessageDialog(null, "Please reconfigure Pay2Spawn properly BEFORE you try launching this instance again.\n" +
                            "There have been major config changes.\n" +
                            "We made a backup for you, you should start fresh to avoid clutter.", "Please reconfigure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );

            System.exit(1);
        }

        boolean deobf = Launch.blackboard.containsKey("fml.deobfuscatedEnvironment") ? Boolean.valueOf(Launch.blackboard.get("fml.deobfuscatedEnvironment").toString()) : false;

        if (Strings.isNullOrEmpty(TwitchChecker.INSTANCE.getChannel()) && !MetricsHelper.metrics.isOptOut() && event.getSide().isClient() && !deobf)
        {
            JOptionPane.showMessageDialog(null, "You must provide your channel in the config for statistics.\n" +
                            "If you don't agree with this, opt out of the statistics program all together trough the 'PluginMetrics' config file.\n\n" +
                            "Important note: Don't send the PluginMetrics config to other users, that will screw up analytics.", "Please configure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );

            System.exit(1);
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) throws IOException
    {
        PermissionsHandler.init();
        try
        {
            StatusMessage.serverConfig = GSON_NOPP.toJson(JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        event.registerServerCommand(new CommandP2SPermissions());
        event.registerServerCommand(new CommandP2SServer());
    }
}
