package ccm.pay2spawn;

import net.minecraftforge.common.Configuration;

import java.io.File;

public class P2SConfig
{
    private Configuration configuration;

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        configuration.save();
    }
}
