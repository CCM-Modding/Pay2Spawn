/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn.configurator;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.util.EnumChatFormatting;

/**
 * Manages instances of the NBTgrabber and the configurator
 *
 * @author Dries007
 */
public class ConfiguratorManager
{
    public static void openNbt()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            try
            {
                new NBTGrabber();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Pay2Spawn.getLogger().warn("WTF? Can't open the NBT Grabber on the server. How did this happen?");
        }
    }

    public static void openCfg()
    {
        if (!Pay2Spawn.getRewardsDB().editable) Helper.msg(EnumChatFormatting.GOLD + "[P2S] You can't edit a server side config.");
        else
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                try
                {
                    Configurator.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Pay2Spawn.getLogger().warn("WTF? Can't open the Configurator on the server. How did this happen?");
            }
        }
    }

    public static void reload()
    {
        if (Configurator.instance != null) Configurator.instance.update();
    }

    public static void exit()
    {
        if (Configurator.instance != null) Configurator.instance.close();
    }
}
