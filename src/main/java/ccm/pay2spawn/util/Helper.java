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

import ccm.pay2spawn.util.shapes.PointI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.RANDOM;

/**
 * Static helper functions with no other home
 *
 * @author Dries007
 */
public class Helper
{
    public static final String  FORMAT_WITH_DELIMITER = "((?<=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr])|(?=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr]))";
    public static final Pattern DOUBLE_QUOTES         = Pattern.compile("\"(.*)\"");

    /**
     * NBT to byte[]
     * Use for packets
     *
     * @param nbtTagCompound the NBTTagCompound
     *
     * @return the bye array
     */
    public static byte[] nbtToByteArray(NBTTagCompound nbtTagCompound)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            writeNBTTagCompound(nbtTagCompound, stream);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return streambyte.toByteArray();
    }

    /**
     * byte[] to NBT
     * Use for packets
     *
     * @param data the byte arry
     *
     * @return the NBTTagCompound
     */
    public static NBTTagCompound byteArrayToNBT(byte[] data)
    {
        NBTTagCompound nbtTagCompound;
        ByteArrayInputStream streambyte = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(streambyte);

        try
        {
            nbtTagCompound = readNBTTagCompound(stream);
        }
        catch (IOException e)
        {
            nbtTagCompound = new NBTTagCompound();
            e.printStackTrace();
        }

        return nbtTagCompound;
    }

    /**
     * Used above, compresses
     *
     * @param nbtTagCompound the NBTTagCompound
     * @param dataOutput     the output
     */
    public static void writeNBTTagCompound(NBTTagCompound nbtTagCompound, DataOutput dataOutput) throws IOException
    {
        if (nbtTagCompound == null)
        {
            dataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(nbtTagCompound);
            dataOutput.writeShort((short) abyte.length);
            dataOutput.write(abyte);
        }
    }

    /**
     * Used above, decompresses
     *
     * @param dataInput the input
     *
     * @return The NBTTagCompound
     */
    public static NBTTagCompound readNBTTagCompound(DataInput dataInput) throws IOException
    {
        short short1 = dataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            dataInput.readFully(abyte);
            return CompressedStreamTools.decompress(abyte);
        }
    }

    /**
     * Convert & into § if the next char is a chat formatter char
     *
     * @param message the message to be converted
     *
     * @return the converted message
     */
    public static String formatColors(String message)
    {
        char[] b = message.toCharArray();
        for (int i = 0; i < b.length - 1; i++)
        {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1)
            {
                b[i] = '\u00a7';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    /**
     * Print a message client side
     *
     * @param message the message to be displayed
     */
    public static void msg(String message)
    {
        System.out.println("P2S client message: " + message);
        if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }

    /**
     * Fill in variables from a donation
     *
     * @param format   text that needs var replacing
     * @param donation the donation data
     *
     * @return the fully var-replaced string
     */
    public static String formatText(String format, Donation donation, Reward reward)
    {
        format = format.replace("$name", donation.username);
        format = format.replace("$amount", donation.amount + "");
        format = format.replace("$note", donation.note);
        if (Minecraft.getMinecraft().thePlayer != null) format = format.replace("$streamer", Minecraft.getMinecraft().thePlayer.getCommandSenderName());

        if (reward != null)
        {
            format = format.replace("$reward_message", reward.getMessage());
            format = format.replace("$reward_name", reward.getName());
            format = format.replace("$reward_amount", reward.getAmount() + "");
            format = format.replace("$reward_countdown", reward.getCountdown() + "");
        }

        return format;
    }

    /**
     * Fill in variables from a donation
     *
     * @param dataToFormat data to be formatted
     * @param donation     the donation data
     *
     * @return the fully var-replaced JsonElement
     */
    public static JsonElement formatText(JsonElement dataToFormat, Donation donation, Reward reward)
    {
        if (dataToFormat.isJsonPrimitive() && dataToFormat.getAsJsonPrimitive().isString())
        {
            return new JsonPrimitive(Helper.formatText(dataToFormat.getAsString(), donation, reward));
        }
        if (dataToFormat.isJsonArray())
        {
            JsonArray out = new JsonArray();
            for (JsonElement element : dataToFormat.getAsJsonArray())
            {
                out.add(formatText(element, donation, reward));
            }
            return out;
        }
        if (dataToFormat.isJsonObject())
        {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entity : dataToFormat.getAsJsonObject().entrySet())
            {
                out.add(entity.getKey(), Helper.formatText(entity.getValue(), donation, reward));
            }
            return out;
        }
        return dataToFormat;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isDouble(String text)
    {
        try
        {
            Double.parseDouble(text);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static void addWithEmptyLines(ArrayList<String> list, String header)
    {
        String[] lines = header.split("\\\\n");
        int i = 0;
        for (String s : lines)
            list.add(i++, s);
    }

    public static String removeQuotes(String s)
    {
        Matcher m = DOUBLE_QUOTES.matcher(s);
        if (m.matches()) return m.group(1);
        else return s;
    }

    public static boolean rndSpawnPoint(ArrayList<PointD> pointDs, Entity entity)
    {
        Collections.shuffle(pointDs, RANDOM);
        for (PointD p : pointDs)
        {
            Collections.shuffle(pointDs, RANDOM);
            if (p.canSpawn(entity))
            {
                p.setPosition(entity);
                return true;
            }
        }
        return false;
    }

    public static String readUrl(URL url) throws IOException
    {
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();

        try
        {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
        }
        finally
        {
            if (reader != null) reader.close();
        }
        return buffer.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isInt(String text)
    {
        try
        {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static double findMax(Collection<Double> vals)
    {
        double max = Double.MIN_VALUE;

        for (double d : vals)
        {
            if (d > max) max = d;
        }

        return max;
    }

    public static void sendChatToPlayer(ICommandSender player, String message, EnumChatFormatting chatFormatting)
    {
        player.addChatMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(chatFormatting)));
    }

    public static void sendChatToPlayer(EntityPlayer player, String message)
    {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static int round(double d)
    {
        return MathHelper.floor_double(d);

    }

    public static void renderPoint(PointI p, Tessellator tess, double r, double g, double b)
    {
        GL11.glColor3d(r, g, b);
        renderPoint(p, tess);
    }

    public static void renderPoint(PointI p, Tessellator tess)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(p.getX(), p.getY(), p.getZ());
        GL11.glScalef(1.01f, 1.01f, 1.01f);
        tess.startDrawing(GL11.GL_LINES);

        // FRONT
        tess.addVertex(0, 0, 0);
        tess.addVertex(0, 1, 0);

        tess.addVertex(0, 1, 0);
        tess.addVertex(1, 1, 0);

        tess.addVertex(1, 1, 0);
        tess.addVertex(1, 0, 0);

        tess.addVertex(1, 0, 0);
        tess.addVertex(0, 0, 0);

        // BACK
        tess.addVertex(0, 0, -1);
        tess.addVertex(0, 1, -1);
        tess.addVertex(0, 0, -1);
        tess.addVertex(1, 0, -1);
        tess.addVertex(1, 0, -1);
        tess.addVertex(1, 1, -1);
        tess.addVertex(0, 1, -1);
        tess.addVertex(1, 1, -1);

        // betweens.
        tess.addVertex(0, 0, 0);
        tess.addVertex(0, 0, -1);

        tess.addVertex(0, 1, 0);
        tess.addVertex(0, 1, -1);

        tess.addVertex(1, 0, 0);
        tess.addVertex(1, 0, -1);

        tess.addVertex(1, 1, 0);
        tess.addVertex(1, 1, -1);

        tess.draw();
        GL11.glPopMatrix();
    }
}
