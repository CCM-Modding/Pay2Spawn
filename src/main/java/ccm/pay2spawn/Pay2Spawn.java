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

import ccm.pay2spawn.network.PacketHandler;
import ccm.pay2spawn.types.*;
import ccm.pay2spawn.util.HudHelper;
import ccm.pay2spawn.util.MetricsHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ccm.pay2spawn.util.Archive.MODID;
import static ccm.pay2spawn.util.Archive.NAME;

@Mod(modid = MODID, name = NAME)
@NetworkMod(clientSideRequired = false, serverSideRequired = true, packetHandler = PacketHandler.class, channels = {MODID})
public class Pay2Spawn
{
    @Mod.Instance(MODID)
    public static Pay2Spawn instance;

    public static boolean   debug = false;

    @Mod.Metadata(MODID)
    private ModMetadata metadata;

    private RewardsDB rewardsDB;

    private P2SConfig config;

    private File   configFolder;
    private Logger logger;

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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        logger.setLevel(Level.ALL);

        configFolder = new File(event.getModConfigurationDirectory(), NAME);
        //noinspection ResultOfMethodCallIgnored
        configFolder.mkdirs();

        config = new P2SConfig(new File(configFolder, NAME + ".cfg"));


        logger.info("Make sure you configure your PayPal account correctly BEFORE making bug reports!");

        TypeRegistry.register(new EntityType());
        TypeRegistry.register(new ItemType());
        TypeRegistry.register(new PotionEffectType());
        TypeRegistry.register(new LightningType());
        TypeRegistry.register(new XPOrbsType());
        TypeRegistry.register(new SoundType());
        TypeRegistry.register(new FireworksType());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        TypeRegistry.doConfig(config.configuration);
        config.configuration.save();
        rewardsDB = new RewardsDB(new File(configFolder, NAME + ".json"));
        MetricsHelper.init();

        if (event.getSide().isClient())
        {
            new DonationCheckerThread(config.interval, config.channel, config.API_Key).start();
            new HudHelper();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (config.printHelpLists && event.getSide().isClient())
        {
            for (TypeBase base : TypeRegistry.getAllTypes())
            {
                base.printHelpList(configFolder);
            }
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandP2S());
    }
}
