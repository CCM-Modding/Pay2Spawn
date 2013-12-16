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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Handler for all forge events.
 * TODO: Make sure to check for FML only mode, inform the user if so
 *
 * @author Dries007
 */
public class EventHandler
{
    public EventHandler()
    {
        try
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Right click tracker for entity NBT data
    public static HashSet<String> entitySet = new HashSet<>();

    @ForgeSubscribe
    public void event(EntityInteractEvent event)
    {
        if (event.entityPlayer.worldObj.isRemote) return;
        if (entitySet.contains(event.entityPlayer.getEntityName()))
        {
            entitySet.remove(event.entityPlayer.getEntityName());
            NBTTagCompound tag = new NBTTagCompound();
            event.target.writeToNBT(tag);
            event.target.writeMountToNBT(tag);
            event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText(JsonNBTHelper.parseNBT(tag).toString()));
        }
    }

    // HUD messages
    final static ArrayList<String> left  = new ArrayList<>();
    final static ArrayList<String> right = new ArrayList<>();

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
