package ccm.pay2spawn;

import ccm.pay2spawn.util.JsonNBTHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;

import java.util.Arrays;
import java.util.List;

public class CommandP2S extends CommandBase
{
    static final String HELP = "Use command to capture custom things.";

    @Override
    public String getCommandName()
    {
        return "pay2spawn";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2s");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return HELP;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayer player = (EntityPlayer) sender;
        if (args.length == 0)
        {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(HELP));
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Protip: Use tab completion!"));
            return;
        }

        String text = JsonNBTHelper.parseNBT(player.inventory.getCurrentItem().writeToNBT(new NBTTagCompound())).toString();
        sender.sendChatToPlayer(ChatMessageComponent.createFromText(text));
    }
}
