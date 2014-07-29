package ccm.pay2spawn.types.guis.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.util.shapes.Cylinder;
import ccm.pay2spawn.util.shapes.PointI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import static ccm.pay2spawn.types.StructureType.BLOCKDATA_KEY;
import static ccm.pay2spawn.util.Constants.GSON;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;
import static ccm.pay2spawn.util.shapes.AbstractShape.*;
import static ccm.pay2spawn.util.shapes.Cylinder.HEIGHT_KEY;
import static ccm.pay2spawn.util.shapes.Cylinder.RADIUS_KEY;

public class CylinderGui extends ShapeGuiBase
{
    public JTextField   centerXTextField;
    public JTextField   centerYTextField;
    public JTextField   centerZTextField;
    public JRadioButton noHollowRadioButton;
    public JRadioButton hollowRadioButton;
    public JRadioButton randomHollowRadioButton;
    public JRadioButton noReplaceableRadioButton;
    public JRadioButton replaceableRadioButton;
    public JRadioButton randomReplaceableRadioButton;
    public JScrollPane  scrollPane;
    public JTextPane    jsonPane;
    public JList        blockList;
    public JButton      addBlockTypeButton;
    public JButton      removeBlockTypebtn;
    public JPanel       panel1;
    public JButton      parseFromJsonButton;
    public JButton      updateJsonButton;
    public JButton      saveButton;
    public JTextField   heightTextField;
    public JTextField   radiusTextField;
    public JTextField   radius;

    public CylinderGui(int index, JsonObject jsonObject, StructureTypeGui callback, HashMap<String, String> typeMap)
    {
        super(index, jsonObject, Cylinder.class, callback, typeMap);
        init();
    }

    @Override
    public void readJson()
    {
        if (!data.has(CENTER_KEY)) data.add(CENTER_KEY, new JsonObject());

        radiusTextField.setText(readValue(RADIUS_KEY, data));
        heightTextField.setText(readValue(HEIGHT_KEY, data));

        centerXTextField.setText(readValue(PointI.X_KEY, data.getAsJsonObject(CENTER_KEY)));
        centerYTextField.setText(readValue(PointI.Y_KEY, data.getAsJsonObject(CENTER_KEY)));
        centerZTextField.setText(readValue(PointI.Z_KEY, data.getAsJsonObject(CENTER_KEY)));

        String hollow = readValue(HOLLOW_KEY, data);
        noHollowRadioButton.setSelected(hollow.equals(FALSE_BYTE) || hollow.equals(""));
        hollowRadioButton.setSelected(hollow.equals(TRUE_BYTE));
        randomHollowRadioButton.setSelected(hollow.startsWith(RANDOM_BOOLEAN));

        String replaceable = readValue(REPLACEABLEONLY_KEY, data);
        noReplaceableRadioButton.setSelected(replaceable.equals(FALSE_BYTE) || replaceable.equals(""));
        replaceableRadioButton.setSelected(replaceable.equals(TRUE_BYTE));
        randomReplaceableRadioButton.setSelected(replaceable.startsWith(RANDOM_BOOLEAN));

        blockData = data.getAsJsonArray(BLOCKDATA_KEY);

        blockList.updateUI();
        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        if (!data.has(CENTER_KEY)) data.add(CENTER_KEY, new JsonObject());

        storeValue(PointI.X_KEY, data.getAsJsonObject(CENTER_KEY), centerXTextField.getText());
        storeValue(PointI.Y_KEY, data.getAsJsonObject(CENTER_KEY), centerYTextField.getText());
        storeValue(PointI.Z_KEY, data.getAsJsonObject(CENTER_KEY), centerZTextField.getText());

        storeValue(RADIUS_KEY, data, radiusTextField.getText());
        storeValue(HEIGHT_KEY, data, heightTextField.getText());

        if (hollowRadioButton.isSelected()) storeValue(HOLLOW_KEY, data, TRUE_BYTE);
        if (noHollowRadioButton.isSelected()) storeValue(HOLLOW_KEY, data, FALSE_BYTE);
        if (randomHollowRadioButton.isSelected()) storeValue(HOLLOW_KEY, data, RANDOM_BOOLEAN);

        if (replaceableRadioButton.isSelected()) storeValue(REPLACEABLEONLY_KEY, data, TRUE_BYTE);
        if (noReplaceableRadioButton.isSelected()) storeValue(REPLACEABLEONLY_KEY, data, FALSE_BYTE);
        if (randomReplaceableRadioButton.isSelected()) storeValue(REPLACEABLEONLY_KEY, data, RANDOM_BOOLEAN);

        data.add(BLOCKDATA_KEY, blockData);

        blockList.updateUI();
        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void setupListeners()
    {
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                callback.callback(rewardID, data);
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
        addBlockTypeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new BlockDataGui(-1, new JsonObject(), instance);
            }
        });
        blockList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    new BlockDataGui(blockList.getSelectedIndex(), blockData.get(blockList.getSelectedIndex()).getAsJsonObject(), instance);
                }

                removeBlockTypebtn.setEnabled(!blockList.isSelectionEmpty());
            }
        });
        removeBlockTypebtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIndex = blockList.getSelectedIndex();
                if (selectedIndex != -1)
                {
                    JsonArray newBlockData = new JsonArray();
                    for (int i = 0; i < blockData.size(); i++) if (i != selectedIndex) newBlockData.add(blockData.get(i));
                    blockData = newBlockData;
                }
                updateJson();
                removeBlockTypebtn.setEnabled(!blockList.isSelectionEmpty());
            }
        });
        blockList.setModel(getBlockDataModel());
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
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Center X: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label1, gbc);
        centerXTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(centerXTextField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Center Y: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label3, gbc);
        centerYTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(centerYTextField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("CenterZ: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label5, gbc);
        centerZTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(centerZTextField, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label6, gbc);
        noHollowRadioButton = new JRadioButton();
        noHollowRadioButton.setText("Not hollow");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(noHollowRadioButton, gbc);
        hollowRadioButton = new JRadioButton();
        hollowRadioButton.setText("Hollow");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(hollowRadioButton, gbc);
        randomHollowRadioButton = new JRadioButton();
        randomHollowRadioButton.setText("Random hollow");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomHollowRadioButton, gbc);
        noReplaceableRadioButton = new JRadioButton();
        noReplaceableRadioButton.setText("All blocks");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(noReplaceableRadioButton, gbc);
        replaceableRadioButton = new JRadioButton();
        replaceableRadioButton.setText("Replaceable only");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(replaceableRadioButton, gbc);
        randomReplaceableRadioButton = new JRadioButton();
        randomReplaceableRadioButton.setText("Random");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomReplaceableRadioButton, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label7, gbc);
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
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane1, gbc);
        blockList = new JList();
        scrollPane1.setViewportView(blockList);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel4, gbc);
        addBlockTypeButton = new JButton();
        addBlockTypeButton.setText("Add blockType");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(addBlockTypeButton, gbc);
        removeBlockTypebtn = new JButton();
        removeBlockTypebtn.setText("Remove selected blockType");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(removeBlockTypebtn, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Radius: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label8, gbc);
        radiusTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(radiusTextField, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label9, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("Height: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label10, gbc);
        heightTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(heightTextField, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label11, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel5, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(parseFromJsonButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(updateJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(saveButton, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(hollowRadioButton);
        buttonGroup.add(noHollowRadioButton);
        buttonGroup.add(randomHollowRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(replaceableRadioButton);
        buttonGroup.add(noReplaceableRadioButton);
        buttonGroup.add(randomReplaceableRadioButton);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
