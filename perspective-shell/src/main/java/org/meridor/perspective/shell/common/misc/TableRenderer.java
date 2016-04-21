package org.meridor.perspective.shell.common.misc;

import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableRenderer {
    
    @Autowired
    private SettingsAware settingsAware;
    
    public String render(String[] header, List<String[]> rows) {
        V2_AsciiTable table = new V2_AsciiTable();
        
        table.addStrongRule();
        table.addRow(header);
        for (String[] row : rows) {
            table.addRule();
            table.addRow(row);
        }
        if (showBottomHeader()) {
            table.addRule();
            table.addRow(header);
        }
        table.addStrongRule();

        V2_AsciiTableRenderer renderer = new V2_AsciiTableRenderer();
        renderer.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        final int tableWidth = getTableWidth();
        renderer.setWidth(new WidthAbsoluteEven(tableWidth));
        return renderer.render(table).toString();
    }
    
    private int getTableWidth() {
        final int DEFAULT_TABLE_WIDTH = 180;
        final int REASONABLE_MIN_TABLE_WIDTH = 10;
        try {
            Integer tableWidth = settingsAware.getSettingAs(Setting.TABLE_WIDTH, Integer.class);
            return (tableWidth >= REASONABLE_MIN_TABLE_WIDTH) ? tableWidth : DEFAULT_TABLE_WIDTH;
        } catch (Exception e) {
            return DEFAULT_TABLE_WIDTH;
        }
    }
    
    private boolean showBottomHeader() {
        return settingsAware.hasSetting(Setting.SHOW_BOTTOM_TABLE_HEADER);
    }
    
}
