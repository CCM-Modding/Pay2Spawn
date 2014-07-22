package ccm.pay2spawn.network;

import ccm.libs.javazoom.jl.decoder.JavaLayerException;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.MusicType;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class MusicMessage implements IMessage
{
    private String name;

    public MusicMessage(String name)
    {
        this.name = name;
    }

    public MusicMessage()
    {

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
    public void fromBytes(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public static class Handler implements IMessageHandler<MusicMessage, IMessage>
    {
        @Override
        public IMessage onMessage(final MusicMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                File file = new File(MusicType.musicFolder, message.name);

                if (file.exists() && file.isFile()) play(file);
                else
                {
                    if (!file.isDirectory()) file = file.getParentFile();

                    File[] files = file.listFiles(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return name.startsWith(message.name);
                        }
                    });

                    if (files.length == 1)
                    {
                        play(files[0]);
                    }
                    else if (files.length == 0)
                    {
                        Pay2Spawn.getLogger().warn("MUSIC FILE NOT FOUND: " + message.name);
                    }
                    else
                    {
                        Pay2Spawn.getLogger().warn("Multiple matches with music:");
                        for (File file1 : files) Pay2Spawn.getLogger().warn(file1.getName());
                    }
                }
            }
            return null;
        }
    }
}
