package ccm.pay2spawn.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;

import java.util.Random;

public enum EnumSpawnType
{
    ITEM
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    if (player != null)
                    {
                        ItemStack itemStack = ItemStack.loadItemStackFromNBT(data);
                        System.out.println(Data.MODID + ": Giving " + player.getDisplayName() + " item " + itemStack); //TODO: debug line
                        EntityItem entityItem = player.dropPlayerItem(itemStack);
                        entityItem.delayBeforeCanPickup = 0;
                    }
                }

                @Override
                public NBTTagCompound makeRandomData()
                {
                    NBTTagCompound nbt = new NBTTagCompound();

                    new ItemStack(Item.appleGold).writeToNBT(nbt);

                    return nbt;
                }
            },
    EFFECT
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    if (player != null)
                    {
                        PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(data);
                        System.out.println(Data.MODID + ": Giving " + player.getDisplayName() + " effect " + effect); //TODO: debug line
                        player.addPotionEffect(effect);
                    }
                }

                @Override
                public NBTTagCompound makeRandomData()
                {
                    NBTTagCompound nbt = new NBTTagCompound();

                    Potion potion = null;

                    while (potion == null)
                    {
                        potion = Potion.potionTypes[Helper.RANDOM.nextInt(Potion.potionTypes.length)];
                    }

                    new PotionEffect(potion.getId(), (int) (Helper.RANDOM.nextDouble() * 1000)).writeCustomPotionEffectToNBT(nbt);

                    return nbt;
                }
            },
    ENTITY
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    if (player != null)
                    {
                        System.out.println(Data.MODID + ": Spawning " + EntityList.getStringFromID(data.getInteger("id")) + " near " + player.getDisplayName()); //TODO: debug line
                        double x, y, z;

                        y = player.posY + 1;

                        x = player.posX + (RADIUS - Helper.RANDOM.nextInt(RADIUS));
                        z = player.posZ + (RADIUS - Helper.RANDOM.nextInt(RADIUS));

                        ItemMonsterPlacer.spawnCreature(player.getEntityWorld(), data.getInteger("id"), x, y, z);
                    }
                }

                @Override
                public NBTTagCompound makeRandomData()
                {
                    NBTTagCompound nbt = new NBTTagCompound();

                    nbt.setInteger("id", Helper.getRndEntity());

                    return nbt;
                }
            };

    public static final int RADIUS = 15;

    public abstract void spawnFromData(EntityPlayer player, NBTTagCompound data);

    public abstract NBTTagCompound makeRandomData();
}
