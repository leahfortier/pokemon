package util;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

public class GUIUtils {

    // I can't help it I like a nice white background
    public static void setStyle(JComponent component) {
        component.setFont(DrawUtils.getFont(16));
        component.setBackground(Color.WHITE);
    }

    public static JMenuItem createMenuItem(String text, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(actionListener);
        GUIUtils.setStyle(menuItem);

        return menuItem;
    }

    public static JMenuItem createMenuItem(String text, int keyEvent, ActionListener actionListener) {
        JMenuItem menuItem = createMenuItem(text, actionListener);

        // System shortcut key. Control for windows, command for mac.
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, shortcut));

        return menuItem;
    }

    public static JMenu createMenu(String text, JMenuItem... menuItems) {
        JMenu menu = new JMenu(text);
        setStyle(menu);

        for (JMenuItem menuItem : menuItems) {
            menu.add(menuItem);
        }

        return menu;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        setStyle(label);

        return label;
    }

    public static JPanel createComboBoxComponent(String text, JComboBox comboBox) {
        JPanel component = new JPanel();
        JLabel label = createLabel(text);

        setHorizontalLayout(component, label, comboBox);

        return component;
    }

    public static <T> JComboBox<T> createComboBox(T[] values, ActionListener actionListener) {
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setModel(new DefaultComboBoxModel<>(values));
        comboBox.addActionListener(actionListener);

        setStyle(comboBox);

        return comboBox;
    }

    public static JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        setStyle(button);

        return button;
    }

    public static JPanel createTextFieldComponent(String text, JTextField textField) {
        JPanel component = new JPanel();
        JLabel label = createLabel(text);

        setStyle(textField);
        textField.setColumns(10);

        setHorizontalLayout(component, label, textField);

        return component;
    }

    public static JPanel createTextAreaComponent(String text, JTextArea textArea) {
        JPanel component = new JPanel();

        JLabel label = createLabel(text);

        textArea.setRows(3);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        setStyle(textArea);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(textArea);

        setHorizontalLayout(component, label, scrollPane);

        return component;
    }

    public static JPanel createHorizontalLayoutComponent(JComponent... components) {
        JPanel panel = new JPanel();
        setHorizontalLayout(panel, components);
        return panel;
    }

    public static JPanel createVerticalLayoutComponent(JComponent... components) {
        JPanel panel = new JPanel();
        setVerticalLayout(panel, components);
        return panel;
    }

    public static void setHorizontalLayout(JComponent panel, JComponent... components) {
        setLayout(true, panel, components);
    }

    public static void setVerticalLayout(JComponent panel, JComponent... components) {
        setLayout(false, panel, components);
    }

    private static void setLayout(boolean horizontal, JComponent panel, JComponent... components) {
        setStyle(panel);

        GroupLayout groupLayout = new GroupLayout(panel);
        ParallelGroup parallelGroup = groupLayout.createParallelGroup();
        SequentialGroup sequentialGroup = groupLayout.createSequentialGroup();
        for (JComponent component : components) {
            if (component == null) {
                continue;
            }

            setStyle(component);

            parallelGroup.addGap(6).addComponent(component).addGap(6);
            sequentialGroup.addGap(6).addComponent(component).addGap(6);
        }

        if (horizontal) {
            groupLayout.setHorizontalGroup(sequentialGroup);
            groupLayout.setVerticalGroup(parallelGroup);
        } else {
            groupLayout.setHorizontalGroup(parallelGroup);
            groupLayout.setVerticalGroup(sequentialGroup);
        }

        panel.setLayout(groupLayout);
    }
}
