package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.shapes.PointI;
import ccm.pay2spawn.util.shapes.Shapes;
import com.google.gson.JsonArray;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

public class StructureImporter
{
    final StructureImporter instance  = this;
    final HashSet<PointI>   points    = new HashSet<>();
    final HashSet<PointI>   selection = new HashSet<>();
    private final StructureTypeGui callback;
    private final JDialog          dialog;
    public JPanel          panel1;
    public JList<String>   pointList;
    public JLabel          helpText;
    public JComboBox<Mode> modeComboBox;
    public JButton         addFromSelectionButton;
    public JButton         removeFromSelectionButton;
    public JCheckBox       renderSelectionOnlyCheckBox;
    public JButton         clearSelectionButton;
    public JButton         importButton;
    public JCheckBox       disableAlreadyImportedShapesCheckBox;
    PointI[] tempPointsArray = points.toArray(new PointI[points.size()]);
    Mode     mode            = Mode.SINGLE;

    public StructureImporter(final StructureTypeGui callback)
    {
        this.callback = callback;

        modeComboBox.setModel(new DefaultComboBoxModel<>(Mode.values()));
        pointList.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                tempPointsArray = points.toArray(new PointI[points.size()]);
                return tempPointsArray.length;
            }

            @Override
            public String getElementAt(int index)
            {
                tempPointsArray = points.toArray(new PointI[points.size()]);
                return tempPointsArray[index].toString() + " " + Minecraft.getMinecraft().theWorld.getBlock(tempPointsArray[index].getX(), tempPointsArray[index].getY(), tempPointsArray[index].getZ()).getLocalizedName();
            }
        });
        modeComboBox.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mode = (Mode) modeComboBox.getSelectedItem();
                helpText.setText(mode.helpText);
            }
        });
        addFromSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (points)
                {
                    synchronized (selection)
                    {
                        points.addAll(selection);
                        selection.clear();
                    }
                }
                pointList.updateUI();
            }
        });
        removeFromSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (points)
                {
                    synchronized (selection)
                    {
                        points.removeAll(selection);
                        selection.clear();
                    }
                }
                pointList.updateUI();
            }
        });
        clearSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (selection)
                {
                    selection.clear();
                }
                pointList.updateUI();
                updateBtns();
            }
        });
        importButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int x = Helper.round(Minecraft.getMinecraft().thePlayer.posX), y = Helper.round(Minecraft.getMinecraft().thePlayer.posY), z = Helper.round(Minecraft.getMinecraft().thePlayer.posZ);

                JsonArray jsonArray = new JsonArray();
                synchronized (points)
                {
                    for (PointI point : points) jsonArray.add(JsonNBTHelper.parseNBT(Shapes.storeShape(point.move(-x, -y, -z))));
                }
                callback.importCallback(jsonArray);

                dialog.dispose();
            }
        });
        disableAlreadyImportedShapesCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                callback.disabled = disableAlreadyImportedShapesCheckBox.isSelected();
            }
        });

        MinecraftForge.EVENT_BUS.register(this.instance);

        dialog = new JDialog();
        dialog.setContentPane(panel1);
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle("Structure importer");
        dialog.setPreferredSize(new Dimension(600, 750));
        dialog.setSize(400, 750);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                MinecraftForge.EVENT_BUS.unregister(instance);
            }
        });

        helpText.setText(mode.helpText);
        dialog.pack();
        dialog.setVisible(true);

        updateBtns();
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (selection.size() == 0 && points.size() == 0) return;

        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, 1 - RenderManager.renderPosZ);
        GL11.glScalef(1.0F, 1.0F, 1.0F);

        if (!renderSelectionOnlyCheckBox.isSelected())
        {
            synchronized (points)
            {
                GL11.glLineWidth(1f);
                for (PointI point : points) Helper.renderPoint(point, tess, 0, 1, 0);
            }
        }

        synchronized (selection)
        {
            GL11.glLineWidth(2f);
            for (PointI point : selection) Helper.renderPoint(point, tess, 1, 0, 0);
        }

        if (pointList.getSelectedIndex() != -1)
        {
            Helper.renderPoint(tempPointsArray[pointList.getSelectedIndex()], tess, 0, 0, 1);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        // tess.renderingWorldRenderer = true;
        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void clickEvent(PlayerInteractEvent e)
    {
        if (e.entityPlayer.getHeldItem() == null || e.entityPlayer.getHeldItem().getItem() != Items.stick) return;
        e.setCanceled(true);

        if (e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) click(Click.LEFT, e.x, e.y, e.z);
        else if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) click(Click.RIGHT, e.x, e.y, e.z);
    }

    private void click(Click click, int x, int y, int z)
    {
        switch (mode)
        {
            case SINGLE:
                synchronized (selection)
                {
                    if (click == Click.LEFT) selection.remove(new PointI(x, y, z));
                    if (click == Click.RIGHT) selection.add(new PointI(x, y, z));
                }
                break;
        }
        updateBtns();
    }

    private void updateBtns()
    {
        addFromSelectionButton.setEnabled(selection.size() != 0);
        removeFromSelectionButton.setEnabled(selection.size() != 0);
        clearSelectionButton.setEnabled(selection.size() != 0);
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        final JScrollPane scrollPane1 = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        pointList = new JList();
        scrollPane1.setViewportView(pointList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        modeComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(modeComboBox, gbc);
        helpText = new JLabel();
        helpText.setHorizontalAlignment(0);
        helpText.setHorizontalTextPosition(0);
        helpText.setText("HELP TEXT");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        panel1.add(helpText, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        addFromSelectionButton = new JButton();
        addFromSelectionButton.setEnabled(false);
        addFromSelectionButton.setText("Add from selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(addFromSelectionButton, gbc);
        removeFromSelectionButton = new JButton();
        removeFromSelectionButton.setEnabled(false);
        removeFromSelectionButton.setText("Remove from selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(removeFromSelectionButton, gbc);
        renderSelectionOnlyCheckBox = new JCheckBox();
        renderSelectionOnlyCheckBox.setText("Render selection only");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(renderSelectionOnlyCheckBox, gbc);
        clearSelectionButton = new JButton();
        clearSelectionButton.setEnabled(false);
        clearSelectionButton.setText("Clear selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(clearSelectionButton, gbc);
        disableAlreadyImportedShapesCheckBox = new JCheckBox();
        disableAlreadyImportedShapesCheckBox.setText("Disable already imported shapes ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(disableAlreadyImportedShapesCheckBox, gbc);
        importButton = new JButton();
        importButton.setText("Import relative to player!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(importButton, gbc);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }

    enum Mode
    {
        SINGLE("Single block mode", "Right click => add block, Left click => remove block");
        public final String name;
        public final String helpText;

        Mode(String name, String helpText)
        {
            this.name = name;
            this.helpText = helpText;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    enum Click
    {
        LEFT, RIGHT
    }
}
