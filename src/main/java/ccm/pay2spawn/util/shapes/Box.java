package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.types.guis.shapes.BoxGui;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public class Box extends AbstractShape
{
    int x, y, z;

    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String Z_KEY = "z";

    static
    {
        typeMap.put(X_KEY, NBTTypes[INT]);
        typeMap.put(Y_KEY, NBTTypes[INT]);
        typeMap.put(Z_KEY, NBTTypes[INT]);
    }

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
        compound.setInteger(X_KEY, x);
        compound.setInteger(Y_KEY, y);
        compound.setInteger(Z_KEY, z);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        x = compound.getInteger(X_KEY);
        y = compound.getInteger(Y_KEY);
        z = compound.getInteger(Z_KEY);
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
    public void openGui(int index, JsonObject jsonObject, StructureTypeGui instance)
    {
        new BoxGui(index, jsonObject, instance, typeMap);
    }

    @Override
    public IShape clone()
    {
        return new Box(center, x, y, z);
    }
}
