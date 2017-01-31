package org.meridor.perspective.shell.common.format;

import org.meridor.perspective.sql.Data;

public interface DataFormatter {

    String format(Data data);

    String getActivationDelimiter();

}
