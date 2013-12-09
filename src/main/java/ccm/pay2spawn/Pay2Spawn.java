package ccm.pay2spawn;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import static ccm.pay2spawn.util.Data.*;

@Mod(modid = MODID, name = MODID)
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
}
