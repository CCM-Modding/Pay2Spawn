package ccm.pay2spawn;

import ccm.pay2spawn.network.PacketHandler;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.MetricsHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

import java.io.File;

import static ccm.pay2spawn.util.Archive.*;

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

    private File configFolder;

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static RewardsDB getRewardsDB()
    {
        return instance.rewardsDB;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configFolder = new File(event.getModConfigurationDirectory(), NAME);
        configFolder.mkdirs();

        config = new P2SConfig(new File(configFolder, NAME + ".cfg"));
        rewardsDB = new RewardsDB(new File(configFolder, NAME + ".json"));
        MetricsHelper.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        TickRegistry.registerScheduledTickHandler(DonationsTickHandler.getInstance(), Side.CLIENT);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (config.printEntityList) Helper.printEntityList(new File(configFolder, "EntityList.txt"));


    }
}
