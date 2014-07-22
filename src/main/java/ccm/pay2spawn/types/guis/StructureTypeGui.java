package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.network.TestMessage;
import ccm.pay2spawn.util.shapes.Shapes;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import static ccm.pay2spawn.types.StructureType.SHAPES_KEY;
import static ccm.pay2spawn.util.Constants.*;

public class StructureTypeGui extends HelperGuiBase
{
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
    public StructureTypeGui instance = this;

    public JsonArray shapes;

    public StructureTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        setupModels();
        makeAndOpen();
    }

    @Override
    public void updateJson()
    {
        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        data.add(SHAPES_KEY, shapes);

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
                }
            }
        });
        removeShapeButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int selectedIndex = shapeList.getSelectedIndex();
                if (selectedIndex != -1)
                {
                    JsonArray newShapes = new JsonArray();
                    for (int i = 0; i < shapes.size(); i++) if (i != selectedIndex) newShapes.add(shapes.get(i));
                    shapes = newShapes;
                }
                updateJson();
                removeShapeButton.setEnabled(!shapeList.isSelectionEmpty());
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

    public void callback(int id, JsonObject data)
    {
        if (id == -1) shapes.add(data);
        else
        {
            JsonArray newShape = new JsonArray();
            for (int i = 0; i < shapes.size(); i++)
                if (i != id) newShape.add(shapes.get(i));
            newShape.add(data);
        }
        readJson();
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
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(addShapeButton, gbc);
        removeShapeButton = new JButton();
        removeShapeButton.setText("Remove shape");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(removeShapeButton, gbc);
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
