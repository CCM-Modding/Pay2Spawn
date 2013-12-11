package ccm.pay2spawn;

import ccm.pay2spawn.util.Archive;
import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumSet;

public class DonationsTickHandler implements IScheduledTickHandler
{
    private static IScheduledTickHandler instance;

    public static IScheduledTickHandler getInstance()
    {
        if (instance == null) instance = new DonationsTickHandler();
        return instance;
    }

    @Override
    public int nextTickSpacing()
    {
        return 20;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {

    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;

        switch (Helper.RANDOM.nextInt(10))
        {
            case 0:
                NBTTagCompound root = new NBTTagCompound();

                root.setString("donator", "Randomness");
                root.setString("amount", "$" + (double) ((int) (Helper.RANDOM.nextDouble() * 10000) / 10) / 100);

                EnumSpawnType spawnType = EnumSpawnType.values()[Helper.RANDOM.nextInt(EnumSpawnType.values().length)];
                spawnType.createAndSend(root, spawnType.makeRandomData());

                break;
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return Archive.MODID + "_" + this.getClass().getSimpleName();
    }
}
