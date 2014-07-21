package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

public class Box extends AbstractShape
{
    int x, y, z;

    public Box(int x, int y, int z)
    {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Box(PointI center, int x, int y, int z)
    {
        super(center);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Box()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        x = compound.getInteger("x");
        y = compound.getInteger("y");
        z = compound.getInteger("z");
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        for (int x = -this.x; x <= this.x; x++)
        {
            for (int z = -this.z; z <= this.z; z++)
            {
                for (int y = -this.y; y <= this.y; y++)
                {
                    points.add(new PointI(center.x + x, center.y + y, center.z + z));
                }
            }
        }

        if (hollow) points.removeAll(new Box(center, x - 1, y - 1, z - 1).getPoints());

        return points;
    }

    @Override
    public IShape clone()
    {
        return new Box(center, x, y, z);
    }
}
