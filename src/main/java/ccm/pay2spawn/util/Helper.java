package ccm.pay2spawn.util;

import argo.jdom.JsonStringNode;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.util.Map;
import java.util.Random;

public class Helper
{
    public static final Random     RANDOM = new Random();
    public static final JsonParser PARSER = new JsonParser();

    public static String getRndEntity()
    {
        int size = EntityList.entityEggs.size();
        int item = RANDOM.nextInt(size);
        int i = 0;
        for (Object obj : EntityList.entityEggs.keySet())
        {
            if (i == item) return EntityList.getStringFromID((Integer) obj);
            i = i + 1;
        }
        return "";
    }

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

    public static void msg(String message)
    {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    public static String formatText(String format, JsonObject donation)
    {
        if (donation.has("twitchUsername") && donation.get("twitchUsername").isJsonPrimitive()) format = format.replace("$name", donation.get("twitchUsername").getAsString());
        if (donation.has("amount") && donation.get("amount").isJsonPrimitive())                 format = format.replace("$amount", donation.get("amount").getAsString());
        if (donation.has("note") && donation.get("note").isJsonPrimitive())                     format = format.replace("$note", donation.get("note").getAsString());
        return format;
    }

    public static JsonElement formatText(JsonElement dataToFormat, JsonObject donation)
    {
        if (dataToFormat.isJsonPrimitive() && dataToFormat.getAsJsonPrimitive().isString())
        {
            return new JsonPrimitive(Helper.formatText(dataToFormat.getAsString(), donation));
        }
        if (dataToFormat.isJsonArray())
        {
            JsonArray out = new JsonArray();
            for (JsonElement element : dataToFormat.getAsJsonArray())
            {
                out.add(formatText(element, donation));
            }
            return out;
        }
        if (dataToFormat.isJsonObject())
        {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entity : dataToFormat.getAsJsonObject().entrySet())
            {
                out.add(entity.getKey(), Helper.formatText(entity.getValue(), donation));
            }
            return out;
        }
        return dataToFormat;
    }
}
