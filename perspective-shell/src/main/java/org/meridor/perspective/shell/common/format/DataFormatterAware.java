package org.meridor.perspective.shell.common.format;

import java.util.Optional;

public interface DataFormatterAware {

    Optional<DataFormatter> getDataFormatter(String sql);

    String removeDelimiter(String sql);

}
