package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;

public interface IShape
{
    NBTTagCompound toNBT();

    IShape fromNBT(NBTTagCompound compound);

    IShape move(int x, int y, int z);

    Collection<PointI> getPoints();

    PointI getCenter();

    IShape setCenter(PointI pointI);

    IShape setHollow(boolean hollow);

    boolean getHollow();

    IShape setReplaceableOnly(boolean replaceableOnly);

    boolean getReplaceableOnly();
}
