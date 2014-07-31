/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.network.TestMessage;
import ccm.pay2spawn.random.RandomRegistry;
import ccm.pay2spawn.random.RndEntity;
import ccm.pay2spawn.types.EntityType;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static ccm.pay2spawn.types.EntityType.*;
import static ccm.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class EntityTypeGui extends HelperGuiBase
{
    public JComboBox<String> entityNameComboBox;
    public JTextField        customNameTextField;
    public JScrollPane       scrollPane;
    public JTextPane         jsonPane;
    public JButton           parseFromJsonButton;
    public JButton           saveButton;
    public JButton           updateJsonButton;
    public JButton           testButton;
    public JPanel            panel1;
    public JRadioButton      notAgroRadioButton;
    public JRadioButton      agroRadioButton;
    public JRadioButton      randomAgroRadioButton;
    public JRadioButton      randomizeMobRadioButton;
    public JRadioButton      donTRandomizeMobRadioButton;
    public JButton           addMobThisEntityButton;
    public JRadioButton      randomlyRandomizeMobRadioButton;
    public JTextField        spawnRadiusTextField;
    public JRadioButton      rideThisMobRadioButton;
    public JRadioButton      dontRidemob;
    public JRadioButton      randomlyRideMob;
    public JRadioButton      thrown;
    public JRadioButton      dontThrown;
    public JRadioButton      RndThrown;
    public JTextField        amountTextField;
    public JTextField        HTMLTextField;
    public EntityTypeGui     superGui;
    public EntityTypeGui instance = this;
    public EntityTypeGui clientGui;

    public EntityTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(EntityType.NAMES);
        list.add(RandomRegistry.getInstanceFromClass(RndEntity.class).getIdentifier());
        Collections.sort(list);
        entityNameComboBox.setModel(new DefaultComboBoxModel<>(list.toArray(new String[list.size()])));

        if (!data.has(EntityType.SPAWNRADIUS_KEY)) data.addProperty(EntityType.SPAWNRADIUS_KEY, 10);

        makeAndOpen();
    }

    public EntityTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap, EntityTypeGui superGui)
    {
        super(rewardID, name, inputData, typeMap);
        this.superGui = superGui;

        ArrayList<String> list = new ArrayList<>();
        list.addAll(EntityType.NAMES);
        list.add(RandomRegistry.getInstanceFromClass(RndEntity.class).getIdentifier());
        Collections.sort(list);
        entityNameComboBox.setModel(new DefaultComboBoxModel<>(list.toArray(new String[list.size()])));

        if (!data.has(EntityType.SPAWNRADIUS_KEY)) data.addProperty(EntityType.SPAWNRADIUS_KEY, 10);

        makeAndOpen();
    }

    private void callback(JsonObject superData)
    {
        data.add(RIDING_KEY, superData);
        updateJson();
    }

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
                if (superGui == null) Configurator.instance.callback(rewardID, name, data);
                else superGui.callback(data);
                if (clientGui != null) clientGui.close();
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
        addMobThisEntityButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JsonObject object;
                if (data.has(RIDING_KEY)) object = data.getAsJsonObject(RIDING_KEY);
                else object = new JsonObject();
                clientGui = new EntityTypeGui(rewardID, name, object, typeMap, instance);
            }
        });
    }

    @Override
    public void close()
    {
        if (clientGui != null) clientGui.close();
        super.close();
    }

    @Override
    public JPanel getPanel()
    {
        return panel1;
    }

    @Override
    public void readJson()
    {
        entityNameComboBox.setSelectedItem(readValue(ENTITYNAME_KEY, data));
        customNameTextField.setText(readValue(CUSTOMNAME_KEY, data));
        spawnRadiusTextField.setText(readValue(SPAWNRADIUS_KEY, data));
        amountTextField.setText(readValue(AMOUNT_KEY, data));
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        String agro = readValue(AGRO_KEY, data);
        notAgroRadioButton.setSelected(agro.equals(FALSE_BYTE) || agro.equals(""));
        agroRadioButton.setSelected(agro.equals(TRUE_BYTE));
        randomAgroRadioButton.setSelected(agro.startsWith(RANDOM_BOOLEAN));

        String random = readValue(RANDOM_KEY, data);
        donTRandomizeMobRadioButton.setSelected(random.equals(FALSE_BYTE) || random.equals(""));
        randomizeMobRadioButton.setSelected(random.equals(TRUE_BYTE));
        randomlyRandomizeMobRadioButton.setSelected(random.startsWith(RANDOM_BOOLEAN));

        String ride = readValue(RIDETHISMOB_KEY, data);
        dontRidemob.setSelected(ride.equals(FALSE_BYTE) || ride.equals(""));
        rideThisMobRadioButton.setSelected(ride.equals(TRUE_BYTE));
        randomlyRideMob.setSelected(ride.startsWith(RANDOM_BOOLEAN));

        String thrownS = readValue(THROWTOWARDSPLAYER_KEY, data);
        dontThrown.setSelected(thrownS.equals(FALSE_BYTE) || thrownS.equals(""));
        thrown.setSelected(thrownS.equals(TRUE_BYTE));
        RndThrown.setSelected(thrownS.startsWith(RANDOM_BOOLEAN));

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        storeValue(ENTITYNAME_KEY, data, entityNameComboBox.getSelectedItem());
        storeValue(CUSTOMNAME_KEY, data, customNameTextField.getText());
        storeValue(SPAWNRADIUS_KEY, data, spawnRadiusTextField.getText());
        storeValue(AMOUNT_KEY, data, amountTextField.getText());

        storeValue(AGRO_KEY, data, randomAgroRadioButton.isSelected() ? RANDOM_BOOLEAN : agroRadioButton.isSelected() ? TRUE_BYTE : FALSE_BYTE);
        storeValue(RANDOM_KEY, data, randomlyRandomizeMobRadioButton.isSelected() ? RANDOM_BOOLEAN : randomizeMobRadioButton.isSelected() ? TRUE_BYTE : FALSE_BYTE);
        storeValue(RIDETHISMOB_KEY, data, randomlyRideMob.isSelected() ? RANDOM_BOOLEAN : rideThisMobRadioButton.isSelected() ? TRUE_BYTE : FALSE_BYTE);
        storeValue(THROWTOWARDSPLAYER_KEY, data, RndThrown.isSelected() ? RANDOM_BOOLEAN : thrown.isSelected() ? TRUE_BYTE : FALSE_BYTE);

        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        jsonPane.setText(GSON.toJson(data));
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
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel2.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("EntityName:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        panel2.add(label4, gbc);
        entityNameComboBox = new JComboBox();
        entityNameComboBox.setEditable(true);
        entityNameComboBox.setToolTipText("Look, a build in list!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(entityNameComboBox, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("CustomName:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label5, gbc);
        customNameTextField = new JTextField();
        customNameTextField.setToolTipText("Nametag of entity");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(customNameTextField, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 6;
        panel2.add(label6, gbc);
        notAgroRadioButton = new JRadioButton();
        notAgroRadioButton.setText("Not agro");
        notAgroRadioButton.setToolTipText("Don't piss off the mob.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(notAgroRadioButton, gbc);
        agroRadioButton = new JRadioButton();
        agroRadioButton.setText("Agro");
        agroRadioButton.setToolTipText("Piss off the mob.");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(agroRadioButton, gbc);
        randomAgroRadioButton = new JRadioButton();
        randomAgroRadioButton.setText("Random agro");
        randomAgroRadioButton.setToolTipText("Maybe piss off the mob.");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomAgroRadioButton, gbc);
        randomlyRandomizeMobRadioButton = new JRadioButton();
        randomlyRandomizeMobRadioButton.setText("Randomly randomize mob");
        randomlyRandomizeMobRadioButton.setToolTipText("Useless option if you ask me -_-");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomlyRandomizeMobRadioButton, gbc);
        randomizeMobRadioButton = new JRadioButton();
        randomizeMobRadioButton.setText("Randomize mob");
        randomizeMobRadioButton.setToolTipText("Randomly assign traits to the mob (as with spawneggs)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomizeMobRadioButton, gbc);
        donTRandomizeMobRadioButton = new JRadioButton();
        donTRandomizeMobRadioButton.setText("Don't randomize mob");
        donTRandomizeMobRadioButton.setToolTipText("Don't randomly assign traits to the mob (as with spawneggs)");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(donTRandomizeMobRadioButton, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Spawn radius:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label7, gbc);
        spawnRadiusTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(spawnRadiusTextField, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 7;
        panel2.add(label8, gbc);
        rideThisMobRadioButton = new JRadioButton();
        rideThisMobRadioButton.setText("Ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(rideThisMobRadioButton, gbc);
        dontRidemob = new JRadioButton();
        dontRidemob.setText("Don't ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(dontRidemob, gbc);
        randomlyRideMob = new JRadioButton();
        randomlyRideMob.setText("Randomly ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomlyRideMob, gbc);
        thrown = new JRadioButton();
        thrown.setEnabled(true);
        thrown.setText("Trow towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(thrown, gbc);
        dontThrown = new JRadioButton();
        dontThrown.setText("Don't throw towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(dontThrown, gbc);
        RndThrown = new JRadioButton();
        RndThrown.setText("Randomly throw towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(RndThrown, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Amount:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label9, gbc);
        amountTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(amountTextField, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 8;
        panel2.add(label10, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label11, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(HTMLTextField, gbc);
        final JLabel label12 = new JLabel();
        label12.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label12, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label13 = new JLabel();
        label13.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label13, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        addMobThisEntityButton = new JButton();
        addMobThisEntityButton.setText("Edit/add the mob this entity rides on");
        addMobThisEntityButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(addMobThisEntityButton, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
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
        label3.setLabelFor(entityNameComboBox);
        label5.setLabelFor(customNameTextField);
        label7.setLabelFor(spawnRadiusTextField);
        label9.setLabelFor(spawnRadiusTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(agroRadioButton);
        buttonGroup.add(randomAgroRadioButton);
        buttonGroup.add(notAgroRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(randomizeMobRadioButton);
        buttonGroup.add(donTRandomizeMobRadioButton);
        buttonGroup.add(randomlyRandomizeMobRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(rideThisMobRadioButton);
        buttonGroup.add(dontRidemob);
        buttonGroup.add(randomlyRideMob);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(thrown);
        buttonGroup.add(dontThrown);
        buttonGroup.add(RndThrown);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
