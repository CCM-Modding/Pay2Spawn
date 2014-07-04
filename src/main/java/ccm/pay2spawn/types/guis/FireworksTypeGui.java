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
import ccm.pay2spawn.network.NbtRequestMessage;
import ccm.pay2spawn.network.TestMessage;
import ccm.pay2spawn.util.IIHasCallback;
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

import static ccm.pay2spawn.types.EntityType.RIDETHISMOB_KEY;
import static ccm.pay2spawn.types.FireworksType.*;
import static ccm.pay2spawn.util.Constants.*;

public class FireworksTypeGui extends HelperGuiBase implements IIHasCallback
{
    public JTextField    flightMultiplierTextField;
    public JScrollPane   scrollPane;
    public JTextPane     jsonPane;
    public JButton       parseFromJsonButton;
    public JButton       saveButton;
    public JButton       updateJsonButton;
    public JButton       testButton;
    public JPanel        panel1;
    public JList<String> explosionList;
    public JButton       importFireworkStartButton;
    public JButton       addExplosionManuallyButton;
    public JTextField    amountTextField;
    public JTextField    radiusTextField;
    public JRadioButton  rideThisFireworkRadioButton;
    public JRadioButton  dontRidemob;
    public JRadioButton  randomlyRideMob;
    public JTextField    HTMLTextField;
    public FireworksTypeGui instance = this;

    public JsonObject fireworks;

    public FireworksTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        data.addProperty("id", "SHORT:401");
        data.addProperty("Damage", "SHORT:0");
        data.addProperty("Count", "BYTE:1");

        setupModels();
        makeAndOpen();
    }

    private void setupModels()
    {
        explosionList.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                return fireworks.getAsJsonArray(EXPLOSIONS_KEY).size();
            }

            @Override
            public String getElementAt(int index)
            {
                return fireworks.getAsJsonArray(EXPLOSIONS_KEY).get(index).toString();
            }
        });
    }

    @Override
    public void readJson()
    {
        amountTextField.setText(readValue(AMOUNT_KEY, data));
        radiusTextField.setText(readValue(RADIUS_KEY, data));
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        if (!data.has("tag")) data.add("tag", new JsonObject());
        if (!data.getAsJsonObject("tag").has(FIREWORKS_KEY)) data.getAsJsonObject("tag").add(FIREWORKS_KEY, new JsonObject());
        if (!data.getAsJsonObject("tag").getAsJsonObject(FIREWORKS_KEY).has(EXPLOSIONS_KEY)) data.getAsJsonObject("tag").getAsJsonObject(FIREWORKS_KEY).add(EXPLOSIONS_KEY, new JsonArray());

        fireworks = data.getAsJsonObject("tag").getAsJsonObject(FIREWORKS_KEY);
        flightMultiplierTextField.setText(readValue(FLIGHT_KEY, fireworks));

        String ride = readValue(RIDETHISMOB_KEY, data);
        dontRidemob.setSelected(ride.equals("0") || ride.equals(""));
        rideThisFireworkRadioButton.setSelected(ride.equals("1"));
        randomlyRideMob.setSelected(ride.startsWith("$random"));

        explosionList.updateUI();

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        storeValue(FLIGHT_KEY, fireworks, flightMultiplierTextField.getText());
        storeValue(AMOUNT_KEY, data, amountTextField.getText());
        storeValue(RADIUS_KEY, data, radiusTextField.getText());

        storeValue(RIDETHISMOB_KEY, data, randomlyRideMob.isSelected() ? "$random" : rideThisFireworkRadioButton.isSelected() ? "1" : "0");
        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        explosionList.updateUI();

        jsonPane.setText(GSON.toJson(data));
    }

    public void callback(int id, JsonObject data)
    {
        if (id == -1) fireworks.getAsJsonArray(EXPLOSIONS_KEY).add(data);
        else
        {
            JsonArray newFireworks = new JsonArray();
            for (int i = 0; i < fireworks.getAsJsonArray(EXPLOSIONS_KEY).size(); i++)
                if (i != id) newFireworks.add(fireworks.getAsJsonArray(EXPLOSIONS_KEY).get(i));
            newFireworks.add(data);
        }
        readJson();
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
        importFireworkStartButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NbtRequestMessage.requestFirework(instance);
            }
        });
        addExplosionManuallyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ExplosionGui(-1, new JsonObject(), instance, typeMap);
            }
        });
        explosionList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    new ExplosionGui(explosionList.getSelectedIndex(), fireworks.getAsJsonArray(EXPLOSIONS_KEY).get(explosionList.getSelectedIndex()).getAsJsonObject(), instance, typeMap);
                }
            }
        });
    }


    @Override
    public void callback(Object... data)
    {
        this.data = JSON_PARSER.parse((String) data[0]).getAsJsonObject();
        this.fireworks = this.data.getAsJsonObject("tag").getAsJsonObject(FIREWORKS_KEY);
        this.data.addProperty(AMOUNT_KEY, "INT:5");
        this.data.addProperty(RADIUS_KEY, "INT:5");
        readJson();
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
        flightMultiplierTextField = new JTextField();
        flightMultiplierTextField.setToolTipText("0 is eye-hight");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(flightMultiplierTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Flight multiplier:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("BYTE");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Amount:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Radius:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label6, gbc);
        amountTextField = new JTextField();
        amountTextField.setToolTipText("How many rockets fire");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(amountTextField, gbc);
        radiusTextField = new JTextField();
        radiusTextField.setToolTipText("The radius around the player they fire in");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(radiusTextField, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        panel2.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("BYTE");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        panel2.add(label8, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        rideThisFireworkRadioButton = new JRadioButton();
        rideThisFireworkRadioButton.setText("Ride this firework");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(rideThisFireworkRadioButton, gbc);
        dontRidemob = new JRadioButton();
        dontRidemob.setText("Don't ride this firework");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(dontRidemob, gbc);
        randomlyRideMob = new JRadioButton();
        randomlyRideMob.setText("Randomly ride this firework");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(randomlyRideMob, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label9, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(HTMLTextField, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label10, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel4, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label11, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel4.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
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
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(testButton, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel6, gbc);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel6.add(panel7, gbc);
        addExplosionManuallyButton = new JButton();
        addExplosionManuallyButton.setText("Add explosion manually");
        addExplosionManuallyButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(addExplosionManuallyButton, gbc);
        importFireworkStartButton = new JButton();
        importFireworkStartButton.setText("Import firework start");
        importFireworkStartButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(importFireworkStartButton, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel6.add(scrollPane1, gbc);
        explosionList = new JList();
        explosionList.setToolTipText("Double click to edit!");
        scrollPane1.setViewportView(explosionList);
        label3.setLabelFor(flightMultiplierTextField);
        label5.setLabelFor(amountTextField);
        label6.setLabelFor(radiusTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(rideThisFireworkRadioButton);
        buttonGroup.add(dontRidemob);
        buttonGroup.add(randomlyRideMob);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
