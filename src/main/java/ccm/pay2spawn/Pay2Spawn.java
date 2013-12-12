package ccm.pay2spawn;

import ccm.pay2spawn.network.PacketHandler;
import ccm.pay2spawn.types.EntityType;
import ccm.pay2spawn.types.ItemType;
import ccm.pay2spawn.types.PotionEffectType;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.MetricsHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

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
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (config.printEntityList) Helper.printEntityList(new File(configFolder, "EntityList.txt"));
    }
}
