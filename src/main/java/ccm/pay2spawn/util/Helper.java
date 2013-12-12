package ccm.pay2spawn.util;

import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.net.URL;
import java.util.Random;

public class Helper
{
    public static final Random RANDOM = new Random();

    public static String getRndEntity()
    {
        int size = EntityList.entityEggs.size();
        int item = RANDOM.nextInt(size); // In real life, the Random object should be rather more shared than this
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

    public static void printEntityList(File file)
    {
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("## This is a list of all the entities you can use in the json file.");
            pw.println("## Not all of them will work, some are system things that shouldn't be messed with.");
            pw.println("## This file gets deleted and remade every startup, can be disabled in the config.");

            for (Object key : EntityList.stringToClassMapping.keySet())
            {
                pw.println(key.toString());
            }
            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
