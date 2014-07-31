package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
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

    boolean getHollow();

    IShape setHollow(boolean hollow);

    boolean getReplaceableOnly();

    IShape setReplaceableOnly(boolean replaceableOnly);

    void openGui(int i, JsonObject jsonObject, StructureTypeGui instance);

    void render(Tessellator tess);

    IShape cloneShape();

    IShape rotate(int baseRotation);
}
