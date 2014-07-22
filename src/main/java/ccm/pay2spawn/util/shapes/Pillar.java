package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.types.guis.shapes.PillarGui;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public class Pillar extends AbstractShape
{
    public static final String HEIGHT_KEY = "height";
    static
    {
        typeMap.put(HEIGHT_KEY, NBTTypes[INT]);
    }
    int height = 0;


    public Pillar(int height)
    {
        super();
        this.height = height;
    }

    public Pillar(PointI center, int height)
    {
        super(center);
        this.height = height;
    }

    Pillar()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger("height", this.height);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        this.height = compound.getInteger("height");
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        for (int y = -this.height; y <= this.height; y++) points.add(new PointI(this.center.x, this.center.y + y, this.center.z));
        if (this.height == 0) points.add(new PointI(this.center.x, this.center.y, this.center.z));

        return points;
    }

    @Override
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new PillarGui(i, jsonObject, instance, typeMap);
    }

    @Override
    public IShape clone()
    {
        return new Pillar(center, height);
    }
}
