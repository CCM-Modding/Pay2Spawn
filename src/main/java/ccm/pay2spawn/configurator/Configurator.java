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

package ccm.pay2spawn.configurator;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.IIHasCallback;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.common.base.Joiner;
import com.google.gson.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Configurator implements IIHasCallback
{

    public static final String[] COLUMN_KEYS  = new String[] {"name", "amount", "message", "countdown", "rewards"};
    public static final String[] COLUMN_NAMES = new String[] {"Name", "Amount", "Message", "Countdown", "Types of rewards"};
    public static Configurator instance;
    public        JFrame       frame;

    private JsonArray     rootArray;
    private JPanel        panel1;
    private JTabbedPane   tabbedPane1;
    private JTable        mainTable;
    private JTextField    nameField;
    private JTextField    amountField;
    private JTextField    messageField;
    private JButton       saveOverOldGroupButton;
    private JButton       helpMeWithRandomizationButton;
    private JButton       clearButton;
    private JList<String> typeList;
    private JList<String> rewards;
    private JButton       saveAsNewGroupButton;
    private JButton       removeGroupButton;
    private JLabel        nameLabel;
    private JLabel        amountLabel;
    public  JButton       duplicateSelectedRewardButton;
    public  JButton       deleteSelectedRewardButton;
    public  JTextField    countdownTextField;
    public  JButton       makeNiceHtmlPageButton;
    private JsonObject    currentlyEditingData;
    private int           currentlyEditingID;
    private JsonArray     rewardData;

    private Configurator() throws FileNotFoundException
    {
        $$$setupUI$$$();

        rootArray = JsonNBTHelper.PARSER.parse(new FileReader(Pay2Spawn.getRewardDBFile())).getAsJsonArray();

        frame = new JFrame("Configurator");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(750, 600);
        frame.pack();
        setupModels();
        setupListeners();

        frame.setVisible(true);
        tabbedPane1.setSelectedIndex(0);
        clear();
        ColumnsAutoSizer.sizeColumnsToFit(mainTable, 20);
    }

    @Override
    public void callback(Object... data)
    {
        int rewardID = (int) data[0];
        String type = (String) data[1];
        JsonObject newData = (JsonObject) data[2];

        if (rewardID == -1)
        {
            JsonObject object = new JsonObject();
            object.addProperty("type", type);
            object.add("data", newData);
            rewardData.add(object);
            rewards.updateUI();
        }
        else
        {
            rewardData.get(rewardID).getAsJsonObject().add("data", newData);
            rewards.updateUI();
        }
    }

    private void setupListeners()
    {
        typeList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    TypeRegistry.getByName(typeList.getSelectedValue()).openNewGui(-1, new JsonObject());
                }
            }
        });
        rewards.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int id = rewards.getSelectedIndex();
                    TypeRegistry.getByName(rewards.getSelectedValue()).openNewGui(id, rewardData.get(id).getAsJsonObject().getAsJsonObject("data"));
                }
            }
        });
        rewards.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                deleteSelectedRewardButton.setEnabled(!rewards.isSelectionEmpty());
                duplicateSelectedRewardButton.setEnabled(!rewards.isSelectionEmpty());
            }
        });
        helpMeWithRandomizationButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Help.init();
            }
        });
        mainTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    currentlyEditingID = mainTable.getSelectedRow();
                    currentlyEditingData = rootArray.get(currentlyEditingID).getAsJsonObject();
                    if (currentlyEditingData.has("rewards")) rewardData = JsonNBTHelper.cloneJSON(currentlyEditingData.getAsJsonArray("rewards")).getAsJsonArray();
                    else rewardData = new JsonArray();
                    rewards.updateUI();
                    rewards.clearSelection();
                    typeList.clearSelection();
                    saveOverOldGroupButton.setEnabled(true);
                    removeGroupButton.setEnabled(true);

                    tabbedPane1.setSelectedIndex(1);
                    nameField.setText(currentlyEditingData.getAsJsonPrimitive(COLUMN_KEYS[0]).getAsString());
                    amountField.setText(currentlyEditingData.getAsJsonPrimitive(COLUMN_KEYS[1]).getAsString());
                    messageField.setText(currentlyEditingData.getAsJsonPrimitive(COLUMN_KEYS[2]).getAsString());
                    countdownTextField.setText(currentlyEditingData.has(COLUMN_KEYS[3]) ? currentlyEditingData.getAsJsonPrimitive(COLUMN_KEYS[3]).getAsString() : "");
                }
            }
        });
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                clear();
            }
        });
        saveOverOldGroupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (saveEdits())
                {
                    JsonArray newRoot = new JsonArray();
                    for (int i = 0; i < rootArray.size(); i++) if (i != currentlyEditingID) newRoot.add(rootArray.get(i));

                    rootArray = newRoot;
                    rootArray.add(currentlyEditingData);
                    mainTable.updateUI();
                    saveMainJsonToFile();
                    ColumnsAutoSizer.sizeColumnsToFit(mainTable, 20);
                }
            }
        });
        saveAsNewGroupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (saveEdits())
                {
                    rootArray.add(currentlyEditingData);
                    currentlyEditingID = rootArray.size() - 1;
                    saveOverOldGroupButton.setEnabled(true);
                    removeGroupButton.setEnabled(true);
                    mainTable.updateUI();
                    saveMainJsonToFile();
                    ColumnsAutoSizer.sizeColumnsToFit(mainTable, 20);
                }
            }
        });
        removeGroupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JsonArray newRoot = new JsonArray();
                for (int i = 0; i < rootArray.size(); i++) if (i != currentlyEditingID) newRoot.add(rootArray.get(i));

                rootArray = newRoot;
                clear();
                tabbedPane1.setSelectedIndex(0);
                saveMainJsonToFile();
            }
        });
        duplicateSelectedRewardButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int toDuplicate = rewards.getSelectedIndex();
                rewardData.add(JsonNBTHelper.cloneJSON(rewardData.get(toDuplicate)));

                rewards.updateUI();
            }
        });
        deleteSelectedRewardButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int toRemove = rewards.getSelectedIndex();
                JsonArray newRewardData = new JsonArray();
                for (int i = 0; i < rewardData.size(); i++) if (i != toRemove) newRewardData.add(rewardData.get(i));
                rewardData = newRewardData;

                rewards.clearSelection();
                rewards.updateUI();
            }
        });
        makeNiceHtmlPageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    HTMLGenerator.generate();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void setupModels()
    {
        mainTable.setModel(new AbstractTableModel()
        {
            @Override
            public int getRowCount()
            {
                return rootArray.size();
            }

            @Override
            public int getColumnCount()
            {
                return COLUMN_NAMES.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                JsonObject jsonObject = rootArray.get(rowIndex).getAsJsonObject();
                if (!jsonObject.has(COLUMN_KEYS[columnIndex])) return "";
                switch (columnIndex)
                {
                    default:
                        return jsonObject.get(COLUMN_KEYS[columnIndex]).getAsString();
                    case 4:
                        HashSet<String> types = new HashSet<>();
                        for (JsonElement element : jsonObject.getAsJsonArray(COLUMN_KEYS[columnIndex])) types.add(element.getAsJsonObject().get("type").getAsString());
                        return Helper.JOINER.join(types);
                }
            }

            @Override
            public String getColumnName(int column)
            {
                return COLUMN_NAMES[column];
            }
        });

        typeList.setModel(new AbstractListModel<String>()
        {
            final ArrayList<String> names = TypeRegistry.getNames();

            @Override
            public int getSize()
            {
                return names.size();
            }

            @Override
            public String getElementAt(int index)
            {
                return names.get(index);
            }
        });

        rewards.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                return rewardData == null ? 0 : rewardData.size();
            }

            @Override
            public String getElementAt(int index)
            {
                if (rewardData.get(index).getAsJsonObject().has("type")) return rewardData.get(index).getAsJsonObject().getAsJsonPrimitive("type").getAsString();
                else return "ERROR IN CONFIG - No type for reward " + index;
            }
        });
    }

    public void saveMainJsonToFile()
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Pay2Spawn.getRewardDBFile()));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            bw.write(gson.toJson(rootArray));
            bw.close();
        }
        catch (IOException e)
        {
            Pay2Spawn.getLogger().severe("Could not save JSON file from configurator!");
            e.printStackTrace();
        }

        Pay2Spawn.reloadDB();
    }

    public boolean saveEdits()
    {
        currentlyEditingData = new JsonObject();

        boolean flag = true;
        if (nameField.getText().equals(""))
        {
            nameLabel.setForeground(Color.red);
            flag = false;
        }
        else
        {
            nameLabel.setForeground(Color.black);
            currentlyEditingData.addProperty(COLUMN_KEYS[0], nameField.getText());
        }

        if (amountField.getText().equals("") || !Helper.isDouble(amountField.getText()))
        {
            amountLabel.setForeground(Color.red);
            flag = false;
        }
        else
        {
            amountLabel.setForeground(Color.black);
            currentlyEditingData.addProperty(COLUMN_KEYS[1], amountField.getText());
        }
        currentlyEditingData.addProperty(COLUMN_KEYS[2], messageField.getText());
        currentlyEditingData.addProperty(COLUMN_KEYS[3], countdownTextField.getText());
        currentlyEditingData.add(COLUMN_KEYS[4], rewardData);

        return flag;
    }

    public void clear()
    {
        currentlyEditingID = -1;
        currentlyEditingData = new JsonObject();
        rewardData = new JsonArray();
        rewards.updateUI();
        rewards.clearSelection();
        typeList.clearSelection();
        saveOverOldGroupButton.setEnabled(false);
        removeGroupButton.setEnabled(false);

        nameField.setText("");
        amountField.setText("");
        messageField.setText("");
        mainTable.updateUI();
        countdownTextField.setText("");

        nameLabel.setForeground(Color.black);
        amountLabel.setForeground(Color.black);
    }

    public static void show() throws FileNotFoundException
    {
        if (instance != null) instance.frame.dispose();
        instance = new Configurator();
    }

    public void update()
    {
        if (frame != null && frame.isVisible()) mainTable.updateUI();
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
        panel1.setLayout(new BorderLayout(0, 0));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        tabbedPane1.addTab("List", panel2);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("<html><b>Double click any row to edit!</b></html>");
        label1.putClientProperty("html.disable", Boolean.FALSE);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(label1, gbc);
        makeNiceHtmlPageButton = new JButton();
        makeNiceHtmlPageButton.setText("Make nice html page!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(makeNiceHtmlPageButton, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane1, gbc);
        mainTable = new JTable();
        mainTable.setAutoCreateRowSorter(false);
        mainTable.setAutoResizeMode(2);
        mainTable.putClientProperty("html.disable", Boolean.TRUE);
        scrollPane1.setViewportView(mainTable);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Add/edit", panel3);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 1;
        panel3.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder("Basic info:"));
        nameLabel = new JLabel();
        nameLabel.setText("Name:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(nameLabel, gbc);
        nameField = new JTextField();
        nameField.setColumns(0);
        nameField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(nameField, gbc);
        amountLabel = new JLabel();
        amountLabel.setText("Amount:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(amountLabel, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("<html>The (minimum) amount that needs to be donated.<html>");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Use to identify this group of rewards");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Message:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label4, gbc);
        messageField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(messageField, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("The message for this group. Colors allowd, see help below.");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(label5, gbc);
        amountField = new JFormattedTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(amountField, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Countdown:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label6, gbc);
        countdownTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(countdownTextField, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("0 for no countdown");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label7, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel5, gbc);
        saveOverOldGroupButton = new JButton();
        saveOverOldGroupButton.setEnabled(false);
        saveOverOldGroupButton.setText("Save over old group");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(saveOverOldGroupButton, gbc);
        helpMeWithRandomizationButton = new JButton();
        helpMeWithRandomizationButton.setText("Help me");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(helpMeWithRandomizationButton, gbc);
        clearButton = new JButton();
        clearButton.setText("Clear");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(clearButton, gbc);
        saveAsNewGroupButton = new JButton();
        saveAsNewGroupButton.setText("Save as new group");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(saveAsNewGroupButton, gbc);
        removeGroupButton = new JButton();
        removeGroupButton.setEnabled(false);
        removeGroupButton.setText("Remove group");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(removeGroupButton, gbc);
        duplicateSelectedRewardButton = new JButton();
        duplicateSelectedRewardButton.setEnabled(false);
        duplicateSelectedRewardButton.setText("Duplicate selected reward");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(duplicateSelectedRewardButton, gbc);
        deleteSelectedRewardButton = new JButton();
        deleteSelectedRewardButton.setEnabled(false);
        deleteSelectedRewardButton.setText("Delete selected reward");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(deleteSelectedRewardButton, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder("Add reward"));
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel6.add(scrollPane2, gbc);
        typeList = new JList();
        scrollPane2.setViewportView(typeList);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel7, gbc);
        panel7.setBorder(BorderFactory.createTitledBorder("Reward list"));
        final JScrollPane scrollPane3 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(scrollPane3, gbc);
        rewards = new JList();
        rewards.setLayoutOrientation(0);
        rewards.setSelectionMode(0);
        scrollPane3.setViewportView(rewards);
        label6.setLabelFor(countdownTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
