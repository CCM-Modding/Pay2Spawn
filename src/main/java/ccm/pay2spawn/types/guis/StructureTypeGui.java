package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.network.TestMessage;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.shapes.IShape;
import ccm.pay2spawn.util.shapes.Shapes;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.types.StructureType.SHAPES_KEY;
import static ccm.pay2spawn.util.Constants.*;

public class StructureTypeGui extends HelperGuiBase
{
    public final ArrayList<IShape> ishapes = new ArrayList<>();
    public JPanel        panel1;
    public JTextField    HTMLTextField;
    public JScrollPane   scrollPane;
    public JTextPane     jsonPane;
    public JButton       parseFromJsonButton;
    public JButton       saveButton;
    public JButton       updateJsonButton;
    public JButton       testButton;
    public JButton       addShapeButton;
    public JList<String> shapeList;
    public JButton       removeShapeButton;
    public JButton       importButton;
    public JCheckBox renderShapesIngameCheckBox;
    public JCheckBox renderSelectedShapeInCheckBox;
    public JsonArray shapes = new JsonArray();
    public  boolean          disabled = false;
    private StructureTypeGui instance = this;

    public StructureTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        MinecraftForge.EVENT_BUS.register(instance);

        setupModels();
        makeAndOpen();
    }

    @Override
    public void setupDialog()
    {
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                try
                {
                    MinecraftForge.EVENT_BUS.unregister(instance);
                }
                catch (Exception ex)
                {
                    // We don't cara cause its weird
                }
            }
        });
        synchronized (ishapes)
        {
            ishapes.clear();
            if (shapes == null) shapes = new JsonArray();

            for (JsonElement element : shapes) ishapes.add(Shapes.loadShape(JsonNBTHelper.parseJSON(element.getAsJsonObject())));
        }
    }

    @Override
    public void updateJson()
    {
        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        data.add(SHAPES_KEY, shapes);

        synchronized (ishapes)
        {
            ishapes.clear();
            for (JsonElement element : shapes) ishapes.add(Shapes.loadShape(JsonNBTHelper.parseJSON(element.getAsJsonObject())));
        }

        shapeList.updateUI();

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void setupListeners()
    {
        testButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                TestMessage.sendToServer(name, data);
            }
        });
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                Configurator.instance.callback(rewardID, name, data);
                dialog.dispose();
            }
        });
        parseFromJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    data = JSON_PARSER.parse(jsonPane.getText()).getAsJsonObject();
                    readJson();
                    jsonPane.setForeground(Color.black);
                    shapeList.clearSelection();
                }
                catch (Exception e1)
                {
                    jsonPane.setForeground(Color.red);
                    e1.printStackTrace();
                }
            }
        });
        updateJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
            }
        });
        addShapeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Shapes.MAP.get(JOptionPane.showInputDialog(instance.panel1, "Please pick a new shape to add.", "Pick a shape", JOptionPane.QUESTION_MESSAGE, null, Shapes.LIST.toArray(), Shapes.LIST.get(0))).openGui(-1, new JsonObject(), instance);
                shapeList.clearSelection();
            }
        });
        shapeList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    JsonObject object = shapes.get(shapeList.getSelectedIndex()).getAsJsonObject();
                    Shapes.MAP.get(readValue(Shapes.SHAPE_KEY, object)).openGui(shapeList.getSelectedIndex(), object, instance);
                    shapeList.clearSelection();
                }
            }
        });
        removeShapeButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                JsonArray newShapes = new JsonArray();
                int[] ints = shapeList.getSelectedIndices();
                HashSet<Integer> selection = new HashSet<>(ints.length);
                for (int i : ints) selection.add(i);

                for (int i = 0; i < shapes.size(); i++)
                {
                    if (!selection.contains(i)) newShapes.add(shapes.get(i));
                }
                shapes = newShapes;
                updateJson();
                removeShapeButton.setEnabled(!shapeList.isSelectionEmpty());
                shapeList.clearSelection();
            }
        });
        importButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                shapeList.clearSelection();
                new StructureImporter(instance);
            }
        });
    }

    @Override
    public void readJson()
    {
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        shapes = data.getAsJsonArray(SHAPES_KEY);
        shapeList.updateUI();

        jsonPane.setText(GSON.toJson(data));
    }

    public void importCallback(JsonArray points)
    {
        shapes.addAll(points);
        updateJson();
        shapeList.clearSelection();
    }

    public void callback(int id, JsonObject data)
    {
        if (id == -1) shapes.add(data);
        else
        {
            JsonArray newShape = new JsonArray();
            for (int i = 0; i < shapes.size(); i++) if (i != id) newShape.add(shapes.get(i));
            newShape.add(data);
            shapes = newShape;
        }
        updateJson();
        shapeList.clearSelection();
    }

    private void setupModels()
    {
        shapeList.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                return shapes.size();
            }

            @Override
            public String getElementAt(int index)
            {
                JsonObject object = shapes.get(index).getAsJsonObject();
                return instance.readValue(Shapes.SHAPE_KEY, object) + ": " + object.toString();
            }
        });
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (disabled || !renderShapesIngameCheckBox.isSelected()) return;
        if (ishapes.size() == 0) return;

        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, 1 - RenderManager.renderPosZ);
        GL11.glTranslated(Helper.round(Minecraft.getMinecraft().thePlayer.posX), Helper.round(Minecraft.getMinecraft().thePlayer.posY), Helper.round(Minecraft.getMinecraft().thePlayer.posZ));
        GL11.glScalef(1.0F, 1.0F, 1.0F);

        synchronized (ishapes)
        {
            GL11.glLineWidth(1f);
            GL11.glColor3d(1, 1, 1);
            for (IShape ishape : ishapes)
            {
                ishape.render(tess);
            }
        }

        if (renderSelectedShapeInCheckBox.isSelected())
        {
            GL11.glLineWidth(2f);
            GL11.glColor3d(0, 0, 1);
            for (int i : shapeList.getSelectedIndices())
            {
                if (i < ishapes.size()) // Fuck event based bullshit that causes IndexOutOfBoundsException out of nowhere.
                    ishapes.get(i).render(tess);
            }
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        // tess.renderingWorldRenderer = true;
        GL11.glPopMatrix();
    }

    @Override
    public JPanel getPanel()
    {
        return panel1;
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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Visual editor:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Type:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel2.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label3, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(HTMLTextField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label4, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label5, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel4, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(parseFromJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(testButton, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel5, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(panel6, gbc);
        addShapeButton = new JButton();
        addShapeButton.setText("Add shape");
        addShapeButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(addShapeButton, gbc);
        removeShapeButton = new JButton();
        removeShapeButton.setText("Remove shape");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(removeShapeButton, gbc);
        importButton = new JButton();
        importButton.setText("Import!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(importButton, gbc);
        renderShapesIngameCheckBox = new JCheckBox();
        renderShapesIngameCheckBox.setSelected(true);
        renderShapesIngameCheckBox.setText("Render shapes ingame");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel6.add(renderShapesIngameCheckBox, gbc);
        renderSelectedShapeInCheckBox = new JCheckBox();
        renderSelectedShapeInCheckBox.setSelected(true);
        renderSelectedShapeInCheckBox.setText("Render selected shape in color");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel6.add(renderSelectedShapeInCheckBox, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(scrollPane1, gbc);
        shapeList = new JList();
        shapeList.setToolTipText("Double click to edit!");
        scrollPane1.setViewportView(shapeList);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
