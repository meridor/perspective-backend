package org.meridor.perspective.shell.misc;

import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableRenderer {
    
    public String render(String[] header, List<String[]> rows) {
        V2_AsciiTable table = new V2_AsciiTable();
        
        table.addStrongRule();
        table.addRow(header);
        for (String[] row : rows) {
            table.addRule();
            table.addRow(row);
        }
        table.addStrongRule();

        V2_AsciiTableRenderer renderer = new V2_AsciiTableRenderer();
        renderer.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        renderer.setWidth(new WidthAbsoluteEven(120));
        return renderer.render(table).toString();
    }
    
}
