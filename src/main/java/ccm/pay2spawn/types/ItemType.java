package ccm.pay2spawn.types;

import ccm.pay2spawn.util.Helper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

public class ItemType extends TypeBase<ItemStack>
{
    private static final String NAME = "item";
    private static String message = "&a[$name donated $amount]&f $spawned given!";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getMessageTemplate()
    {
        return message;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        message = Helper.formatColors(configuration.get(CONFIGCAT_MESSAGE, NAME, message).getString());
    }

    @Override
    public ItemStack getExample()
    {
        return new ItemStack(Item.appleGold);
    }

    @Override
    public NBTTagCompound convertToNBT(ItemStack thing)
    {
        return thing.writeToNBT(new NBTTagCompound());
    }

    @Override
    public ItemStack convertFromNBT(NBTTagCompound nbt)
    {
        return ItemStack.loadItemStackFromNBT(nbt);
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        player.dropPlayerItem(convertFromNBT(dataFromClient)).delayBeforeCanPickup = 0;
    }

    @Override
    public void sendToServer(String name, String amount, NBTTagCompound dataFromDB)
    {
        //TODO: Change item name here + add config option for this
        doMessage(name, amount, convertFromNBT(dataFromDB).getDisplayName());
        send(dataFromDB);
    }
}
