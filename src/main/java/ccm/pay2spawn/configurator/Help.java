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

import ccm.pay2spawn.util.Helper;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class Help
{
    private static Help        instance;
    public         JPanel      panel1;
    public         JTextPane   textPane;
    public         JScrollPane scrollpane;
    private        JFrame      frame;

    public static void init()
    {
        if (instance == null) instance = new Help();
        if (instance.frame == null)
        {
            instance.frame = new JFrame("Help");
            instance.frame.setContentPane(instance.panel1);
            instance.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            instance.frame.setPreferredSize(new Dimension(500, 750));
            instance.frame.setSize(600, 800);
            instance.frame.pack();

            try
            {
                instance.textPane.setText(Helper.readUrl(instance.getClass().getResource("/p2s/helptext.html")));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            instance.textPane.setSelectionStart(1);
            instance.textPane.setSelectionEnd(1);
            instance.textPane.addHyperlinkListener(new HyperlinkListener()
            {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e)
                {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported())
                    {
                        try
                        {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        }
                        catch (IOException | URISyntaxException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }

        if (!instance.frame.isVisible()) instance.frame.setVisible(true);
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
        scrollpane = new JScrollPane();
        scrollpane.setHorizontalScrollBarPolicy(31);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollpane, gbc);
        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setEnabled(true);
        scrollpane.setViewportView(textPane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
