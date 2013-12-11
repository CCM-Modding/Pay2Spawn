package ccm.pay2spawn;

import net.minecraftforge.common.Configuration;

import java.io.File;

import static ccm.pay2spawn.util.Archive.MODID;

public class P2SConfig
{
    private Configuration configuration;
    public boolean printEntityList = true;
    public int     port            = 1562;

    P2SConfig(File file)
    {
        configuration = new Configuration(file);

        printEntityList = configuration.get(MODID, "printEntityList", printEntityList, "Make a list with entity to ID").getBoolean(true);
        port = configuration.get(MODID, "port", port, "The port the IPN listener listens on.").getInt();

        configuration.save();
    }
}
