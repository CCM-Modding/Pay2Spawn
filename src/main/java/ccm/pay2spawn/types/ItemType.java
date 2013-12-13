package ccm.pay2spawn.types;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemType extends TypeBase<ItemStack>
{
    private static final String NAME    = "item";
    private static       String message = "&a[$name donated $amount]&f $spawned given!";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public ItemStack getExample()
    {
        ItemStack is = new ItemStack(Item.appleGold);
        is.setItemName("$name");
        return is;
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
}
