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
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static ccm.pay2spawn.types.EntityType.*;
import static ccm.pay2spawn.util.Constants.*;

public class CustomEntityTypeGui extends HelperGuiBase implements IIHasCallback
{
    public JButton      importItemYouAreButton;
    public JScrollPane  scrollPane;
    public JTextPane    jsonPane;
    public JButton      parseFromJsonButton;
    public JButton      saveButton;
    public JButton      updateJsonButton;
    public JButton      testButton;
    public JPanel       panel1;
    public JTextField   spawnRadiusTextField;
    public JRadioButton rideThisMobRadioButton;
    public JRadioButton dontRidemob;
    public JRadioButton randomlyRideMob;
    public JRadioButton thrown;
    public JRadioButton dontThrown;
    public JRadioButton RndThrown;
    public JTextField   amountTextField;
    public JTextField   HTMLTextField;
    public CustomEntityTypeGui instance = this;

    public CustomEntityTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        if (!data.has(SPAWNRADIUS_KEY)) data.addProperty(SPAWNRADIUS_KEY, 10);
        if (!data.has(AMOUNT_KEY)) data.addProperty(AMOUNT_KEY, 1);

        makeAndOpen();
    }

    @Override
    public void readJson()
    {
        spawnRadiusTextField.setText(readValue(SPAWNRADIUS_KEY, data));
        amountTextField.setText(readValue(AMOUNT_KEY, data));
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

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
        storeValue(SPAWNRADIUS_KEY, data, spawnRadiusTextField.getText());
        storeValue(AMOUNT_KEY, data, amountTextField.getText());

        storeValue(RIDETHISMOB_KEY, data, randomlyRideMob.isSelected() ? RANDOM_BOOLEAN : rideThisMobRadioButton.isSelected() ? TRUE_BYTE : FALSE_BYTE);
        storeValue(THROWTOWARDSPLAYER_KEY, data, RndThrown.isSelected() ? RANDOM_BOOLEAN : thrown.isSelected() ? TRUE_BYTE : FALSE_BYTE);

        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

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
        importItemYouAreButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NbtRequestMessage.requestEntity(instance);
            }
        });
    }

    @Override
    public void callback(Object... data)
    {
        this.data = JSON_PARSER.parse((String) data[0]).getAsJsonObject();
        updateJson();
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
        importItemYouAreButton = new JButton();
        importItemYouAreButton.setText("Import the next mob you right click ingame. Don't close this window untill you do!");
        importItemYouAreButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(importItemYouAreButton, gbc);
        spawnRadiusTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 5;
        panel2.add(spawnRadiusTextField, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Spawn radius:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 5;
        panel2.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 5;
        panel2.add(label2, gbc);
        rideThisMobRadioButton = new JRadioButton();
        rideThisMobRadioButton.setEnabled(true);
        rideThisMobRadioButton.setText("Ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(rideThisMobRadioButton, gbc);
        dontRidemob = new JRadioButton();
        dontRidemob.setText("Don't ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(dontRidemob, gbc);
        randomlyRideMob = new JRadioButton();
        randomlyRideMob.setText("Randomly ride this mob");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomlyRideMob, gbc);
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("For mobs riding other mobs: You must right click the top one.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel2.add(label3, gbc);
        thrown = new JRadioButton();
        thrown.setEnabled(true);
        thrown.setText("Trow towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(thrown, gbc);
        dontThrown = new JRadioButton();
        dontThrown.setText("Don't throw towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(dontThrown, gbc);
        RndThrown = new JRadioButton();
        RndThrown.setText("Randomly throw towards you");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(RndThrown, gbc);
        amountTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(amountTextField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Amount:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label6, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(HTMLTextField, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label7, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label8, gbc);
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
        label1.setLabelFor(spawnRadiusTextField);
        ButtonGroup buttonGroup;
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
