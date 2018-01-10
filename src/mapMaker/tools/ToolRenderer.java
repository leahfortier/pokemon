package mapMaker.tools;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

public class ToolRenderer extends JLabel implements ListCellRenderer<Tool> {
    private static final long serialVersionUID = 6750963470094004328L;

    @Override
    public Component getListCellRendererComponent(JList<? extends Tool> list,
                                                  Tool value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean hasFocus) {
        String s = value.toString();
        setText(s);
        //setIcon((s.length() > 10) ? longIcon : shortIcon);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);

        return this;
    }
}
