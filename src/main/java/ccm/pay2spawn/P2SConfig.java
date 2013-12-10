package ccm.pay2spawn;

import net.minecraftforge.common.Configuration;
import static ccm.pay2spawn.util.Archive.*;

import java.io.File;

public class P2SConfig
{
    private Configuration configuration;
    public  boolean       printEntityList = true;

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        printEntityList = configuration.get(MODID, "printEntityList", printEntityList, "Make a list with entity to ID").getBoolean(true);

        configuration.save();
    }
}
