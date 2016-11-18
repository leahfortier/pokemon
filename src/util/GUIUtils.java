package util;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
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

    public static JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        setStyle(button);

        return button;
    }
}
