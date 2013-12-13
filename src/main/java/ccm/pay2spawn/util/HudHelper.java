package ccm.pay2spawn.util;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

import java.util.ArrayList;

public class HudHelper
{
    final static ArrayList<String> left  = new ArrayList<>();
    final static ArrayList<String> right = new ArrayList<>();

    public HudHelper()
    {
        try
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        catch (Exception e)
        {
            e.printStackTrace(); //TODO: debug and test w/o Forge
        }
    }

    @ForgeSubscribe
    public void hudEvent(RenderGameOverlayEvent.Text event)
    {
        event.left.addAll(left);
        event.right.addAll(right);
    }

    public static synchronized void reset()
    {
        left.clear();
        right.clear();
    }

    public static synchronized void addLeft(String line)
    {
        left.add(line);
    }

    public static synchronized void addRight(String line)
    {
        right.add(line);
    }

    public static void add(int side, String line)
    {
        switch (side)
        {
            case 1:
                addLeft(line);
                break;
            case 2:
                addRight(line);
                break;
        }
    }
}