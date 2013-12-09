package ccm.pay2spawn;

import static ccm.pay2spawn.util.Data.MODID;
import net.minecraftforge.common.Configuration;

import java.io.File;

public class P2SConfig
{
    private static P2SConfig instance;
    private Configuration configuration;

    P2SConfig(File file)
    {
        if (instance != null) return;
        instance = this;

        configuration = new Configuration(file);

        configuration.save();
    }
}
