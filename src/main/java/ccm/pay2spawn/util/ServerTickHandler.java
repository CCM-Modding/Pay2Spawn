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

package ccm.pay2spawn.util;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumSet;

import static ccm.pay2spawn.types.PlayerModificationType.Type;
import static ccm.pay2spawn.util.Constants.MODID;

public class ServerTickHandler implements ITickHandler
{
    public static final ServerTickHandler INSTANCE = new ServerTickHandler();

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        EntityPlayer player = (EntityPlayer) tickData[0];
        NBTTagCompound data = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag("P2S");
        for (Type t : Type.values())
        {
            if (t.isTimable())
            {
                if (data.hasKey(t.name()))
                {
                    int i = data.getInteger(t.name());
                    if (i == 0)
                    {
                        t.undo(player);
                        data.removeTag(t.name());
                    }
                    else
                    {
                        data.setInteger(t.name(), --i);
                    }
                }
            }
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {

    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel()
    {
        return MODID + "_ServerTicker";
    }
}
