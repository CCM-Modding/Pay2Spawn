package ccm.pay2spawn.util;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.network.P2SPacket;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public enum EnumSpawnType
{
    ITEM
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(data);
                    EntityItem entityItem = player.dropPlayerItem(itemStack);
                    entityItem.delayBeforeCanPickup = 0;
                }

                @Override
                public void createAndSend(String name, String amount, NBTTagCompound data)
                {
                    ItemStack itemStack = (ItemStack.loadItemStackFromNBT(data));
                    doMessage(this, name, amount, itemStack.getDisplayName());
                    send(this, data);
                }

                @Override
                public Object makeRandomData()
                {
                    return new ItemStack(Item.appleGold);
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    return ((ItemStack) data).writeToNBT(new NBTTagCompound());
                }

                @Override
                public String getDefaultMessage()
                {
                    return "&a[$name donated $amount]&f $spawned given!";
                }
            },
    EFFECT
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(data);
                    player.addPotionEffect(effect);
                }

                @Override
                public void createAndSend(String name, String amount, NBTTagCompound data)
                {
                    PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(data);
                    doMessage(this, name, amount, StatCollector.translateToLocal(effect.getEffectName()));
                    send(this, data);
                }

                @Override
                public Object makeRandomData()
                {
                    Potion potion = null;
                    while (potion == null)
                    {
                        potion = Potion.potionTypes[Helper.RANDOM.nextInt(Potion.potionTypes.length)];
                    }
                    return new PotionEffect(potion.getId(), (int) (Helper.RANDOM.nextDouble() * 1000));
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    return ((PotionEffect) data).writeCustomPotionEffectToNBT(new NBTTagCompound());
                }

                @Override
                public String getDefaultMessage()
                {
                    return "&a[$name donated $amount]&f $spawned applied!";
                }
            },
    ENTITY
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    double x, y, z;

                    y = player.posY + 1;

                    x = player.posX + (RADIUS - Helper.RANDOM.nextInt(RADIUS));
                    z = player.posZ + (RADIUS - Helper.RANDOM.nextInt(RADIUS));

                    Entity entity = EntityList.createEntityByName(data.getString("name"), player.getEntityWorld());
                    if (entity != null && entity instanceof EntityLivingBase)
                    {
                        EntityLiving entityliving = (EntityLiving) entity;
                        entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(player.getEntityWorld().rand.nextFloat() * 360.0F), 0.0F);
                        entityliving.rotationYawHead = entityliving.rotationYaw;
                        entityliving.renderYawOffset = entityliving.rotationYaw;
                        entityliving.onSpawnWithEgg(null);
                        player.getEntityWorld().spawnEntityInWorld(entity);
                        entityliving.playLivingSound();
                    }
                }

                @Override
                public void createAndSend(String name, String amount, NBTTagCompound data)
                {
                    doMessage(this, name, amount, data.getString("name"));
                    send(this, data);
                }

                @Override
                public Object makeRandomData()
                {
                    return Helper.getRndEntity();
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    NBTTagCompound dataTag = new NBTTagCompound();
                    dataTag.setString("name", (String) data);
                    return dataTag;
                }

                @Override
                public String getDefaultMessage()
                {
                    return "&a[$name donated $amount]&f $spawned spawned!";
                }
            };

    private static void doMessage(EnumSpawnType type, String name, String amount, String spawned)
    {
        Helper.msg(Pay2Spawn.getConfig().messages[type.ordinal()].replace("$name", name).replace("$amount", amount).replace("$spawned", spawned));
    }

    public static final int RADIUS = 5;

    public abstract void spawnFromData(EntityPlayer player, NBTTagCompound data);

    public abstract void createAndSend(String name, String amount, NBTTagCompound data);

    public abstract Object makeRandomData();

    public abstract NBTTagCompound getNBTfromData(Object data);

    public abstract String getDefaultMessage();

    private static void send(EnumSpawnType type, NBTTagCompound data)
    {
        new P2SPacket(type, data).sendToServer();
    }


}
