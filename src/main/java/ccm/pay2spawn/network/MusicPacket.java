package ccm.pay2spawn.network;

import ccm.libs.javazoom.jl.decoder.JavaLayerException;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.MusicType;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class MusicPacket extends AbstractPacket
{
    private String name;

    public MusicPacket()
    {

    }

    public MusicPacket(String name)
    {
        this.name = name;
    }

    private static void play(final File file)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new ccm.libs.javazoom.jl.player.Player(new FileInputStream(file)).play();
                }
                catch (JavaLayerException | FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }, "Pay2Spawn music thread").start();
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, name);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        File file = new File(MusicType.musicFolder, name);

        if (file.exists() && file.isFile()) play(file);
        else
        {
            if (!file.isDirectory()) file = file.getParentFile();

            File[] files = file.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(name);
                }
            });

            if (files.length == 1)
            {
                play(files[0]);
            }
            else
            {
                Pay2Spawn.getLogger().warn("Multiple matches with music:");
                for (File file1 : files) Pay2Spawn.getLogger().warn(file1.getName());
            }
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {

    }
}
