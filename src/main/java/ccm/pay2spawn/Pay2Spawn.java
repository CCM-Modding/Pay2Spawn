package ccm.pay2spawn;

import ccm.pay2spawn.network.PacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

import static ccm.pay2spawn.util.Data.*;

@Mod(modid = MODID, name = NAME)
@NetworkMod(clientSideRequired = false, serverSideRequired = true, packetHandler = PacketHandler.class, channels = {MODID})
public class Pay2Spawn
{
    @Mod.Instance(MODID)
    public static Pay2Spawn instance;

    @Mod.Metadata(MODID)
    private ModMetadata metadata;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        new P2SConfig(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        TickRegistry.registerScheduledTickHandler(DonationsTickHandler.getInstance(), Side.CLIENT);
    }
}
