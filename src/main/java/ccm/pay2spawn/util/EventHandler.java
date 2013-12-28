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

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.network.NbtRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

    static boolean entityTracking = false;

    public static void addEntityTracking()
    {
        entityTracking = true;
    }

    @ForgeSubscribe
    public void event(EntityInteractEvent event)
    {
        if (entityTracking)
        {
            entityTracking = false;
            NbtRequestPacket.requestByEntityID(event.target.entityId);
        }
    }

    // HUD messages
    public final static ArrayList<String> TOP    = new ArrayList<>();
    public final static ArrayList<String> RECENT = new ArrayList<>();

    @ForgeSubscribe
    public void hudEvent(RenderGameOverlayEvent.Text event)
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;
        int baseHeight = event.resolution.getScaledHeight() - TOP.size() * 10;
        switch (hudSettings.top)
        {
            case 1:
                event.left.addAll(TOP);
                break;
            case 2:
                event.right.addAll(TOP);
                break;
            case 5:
                baseHeight -= 25;
            case 3:
                if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) break;
                for (int x = 0; x < TOP.size(); x++)
                {
                    String msg = TOP.get(x);
                    fontRenderer.drawStringWithShadow(msg, 2, baseHeight + 2 + x * 10, 0xFFFFFF);
                }
                break;
            case 6:
                baseHeight -= 25;
            case 4:
                if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) break;
                for (int x = 0; x < TOP.size(); x++)
                {
                    String msg = TOP.get(x);
                    int w = fontRenderer.getStringWidth(msg);
                    fontRenderer.drawStringWithShadow(msg, event.resolution.getScaledWidth() - w - 10, baseHeight + 2 + x * 10, 0xFFFFFF);
                }
                break;
        }

        baseHeight = event.resolution.getScaledHeight() - RECENT.size() * 10;
        switch (hudSettings.recent)
        {
            case 1:
                event.left.addAll(RECENT);
                break;
            case 2:
                event.right.addAll(RECENT);
                break;
            case 5:
                baseHeight -= 25;
            case 3:
                if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) break;
                for (int x = 0; x < RECENT.size(); x++)
                {
                    String msg = RECENT.get(x);
                    fontRenderer.drawStringWithShadow(msg, 2, baseHeight + 2 + x * 10, 0xFFFFFF);
                }
                break;
            case 6:
                baseHeight -= 25;
            case 4:
                if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) break;
                for (int x = 0; x < RECENT.size(); x++)
                {
                    String msg = RECENT.get(x);
                    int w = fontRenderer.getStringWidth(msg);
                    fontRenderer.drawStringWithShadow(msg, event.resolution.getScaledWidth() - w - 10, baseHeight + 2 + x * 10, 0xFFFFFF);
                }
                break;
        }
    }
}
