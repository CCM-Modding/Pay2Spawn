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

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.P2SConfig;
import ccm.pay2spawn.misc.Point;
import ccm.pay2spawn.misc.Reward;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.util.ChatMessageComponent;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Static helper functions with no other home
 *
 * @author Dries007
 */
public class Helper
{
    /**
     * NBT to byte[]
     * Use for packets
     *
     * @param nbtTagCompound the NBTTagCompound
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
     * @throws IOException
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
     * @return The NBTTagCompound
     * @throws IOException
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

    public static final String FORMAT_WITH_DELIMITER = "((?<=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr])|(?=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr]))";

    /**
     * Print a message client side
     *
     * @param message the message to be displayed
     */
    public static void msg(String message)
    {
        System.out.println("P2S client message: " + message);
        if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    /**
     * Fill in variables from a donation
     *
     * @param format   text that needs var replacing
     * @param donation the donation data
     * @param reward
     * @return the fully var-replaced string
     */
    public static String formatText(String format, JsonObject donation, Reward reward)
    {
        donation = filter(donation);
        if (donation.has(DONATION_USERNAME)) format = format.replace("$name", donation.get(DONATION_USERNAME).getAsString());
        if (donation.has(DONATION_AMOUNT)) format = format.replace("$amount", donation.get(DONATION_AMOUNT).getAsString());
        if (donation.has(DONATION_NOTE)) format = format.replace("$note", donation.get(DONATION_NOTE).getAsString());
        if (Minecraft.getMinecraft().thePlayer != null) format = format.replace("$streamer", Minecraft.getMinecraft().thePlayer.getEntityName());

        if (reward != null)
        {
            format = format.replace("$reward_message", reward.getMessage());
            format = format.replace("$reward_name", reward.getName());
            format = format.replace("$reward_amount", reward.getAmount() + "");
            format = format.replace("$reward_countdown", reward.getCountdown() + "");
        }

        return format;
    }

    private static JsonObject filter(JsonObject donation)
    {
        P2SConfig config = Pay2Spawn.getConfig();
        if (donation.has(DONATION_USERNAME) && !donation.get(DONATION_USERNAME).getAsString().equalsIgnoreCase(ANONYMOUS))
        {
            for (Pattern p : config.blacklist_Name_p)
            {
                if (p.matcher(donation.get(DONATION_USERNAME).getAsString()).matches())
                {
                    donation.addProperty(DONATION_USERNAME, ANONYMOUS);
                    break;
                }
            }
        }
        if (donation.has(DONATION_USERNAME) && !donation.get(DONATION_USERNAME).getAsString().equalsIgnoreCase(ANONYMOUS))
        {
            for (Pattern p : config.whitelist_Name_p)
            {
                if (!p.matcher(donation.get(DONATION_USERNAME).getAsString()).matches())
                {
                    donation.addProperty(DONATION_USERNAME, ANONYMOUS);
                    break;
                }
            }
        }

        if (donation.has(DONATION_NOTE) && !Strings.isNullOrEmpty(donation.get(DONATION_NOTE).getAsString()))
        {
            for (Pattern p : config.blacklist_Note_p)
            {
                Matcher m = p.matcher(donation.get(DONATION_NOTE).getAsString());
                donation.addProperty(DONATION_NOTE, m.replaceAll(""));
            }
        }

        if (donation.has(DONATION_NOTE) && !Strings.isNullOrEmpty(donation.get(DONATION_NOTE).getAsString()))
        {
            for (Pattern p : config.whitelist_Note_p)
            {
                if (!p.matcher(donation.get(DONATION_NOTE).getAsString()).matches())
                {
                    donation.addProperty(DONATION_NOTE, "");
                    break;
                }
            }
        }

        return donation;
    }

    /**
     * Fill in variables from a donation
     *
     * @param dataToFormat data to be formatted
     * @param donation     the donation data
     * @return the fully var-replaced JsonElement
     */
    public static JsonElement formatText(JsonElement dataToFormat, JsonObject donation, Reward reward)
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

    public static final Pattern DOUBLE_QUOTES = Pattern.compile("\"(.*)\"");

    public static String removeQuotes(String s)
    {
        Matcher m = DOUBLE_QUOTES.matcher(s);
        if (m.matches()) return m.group(1);
        else return s;
    }

    public static boolean rndSpawnPoint(ArrayList<Point> points, Entity entity)
    {
        Collections.shuffle(points, RANDOM);
        for (Point p : points)
        {
            Collections.shuffle(points, RANDOM);
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

    public static void doMessage(Packet250CustomPayload packet) throws IOException
    {
        String format = formatColors(Pay2Spawn.getConfig().serverMessage);
        if (Strings.isNullOrEmpty(format)) return;

        JsonObject data = JSON_PARSER.parse(new String(packet.data)).getAsJsonObject();

        data = filter(data);
        if (data.has(DONATION_USERNAME))    format = format.replace("$name",             data.get(DONATION_USERNAME).getAsString());
        if (data.has(DONATION_AMOUNT))      format = format.replace("$amount",           data.get(DONATION_AMOUNT).getAsString());
        if (data.has(DONATION_NOTE))        format = format.replace("$note",             data.get(DONATION_NOTE).getAsString());
        if (data.has("streamer"))           format = format.replace("$streamer",         data.get("streamer").getAsString());
        if (data.has("reward_message"))     format = format.replace("$reward_message",   data.get("reward_message").getAsString());
        if (data.has("reward_name"))        format = format.replace("$reward_name",      data.get("reward_name").getAsString());
        if (data.has("reward_amount"))      format = format.replace("$reward_amount",    data.get("reward_amount").getAsString());
        if (data.has("reward_countdown"))   format = format.replace("$reward_countdown", data.get("reward_countdown").getAsString());

        PacketDispatcher.sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText(format)));
    }

    public static void sendMessage(Reward reward, JsonObject donation)
    {
        if (Minecraft.getMinecraft().thePlayer != null) donation.addProperty("streamer", Minecraft.getMinecraft().thePlayer.username);
        donation.addProperty("reward_message",      reward.getMessage());
        donation.addProperty("reward_name",         reward.getName());
        donation.addProperty("reward_amount",       reward.getAmount());
        donation.addProperty("reward_countdown",    reward.getCountdown());

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_MESSAGE, GSON.toJson(donation).getBytes()));
    }
}
