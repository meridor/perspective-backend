package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.query.Query;
import org.meridor.perspective.shell.query.QueryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.meridor.perspective.shell.repository.impl.TextUtils.joinLines;

@Component
public abstract class BaseCommands implements CommandMarker {

    private static final Logger LOG = HandlerUtils.getLogger(BaseCommands.class);

    @Autowired
    private TableRenderer tableRenderer;
    
    @Autowired
    private QueryValidator queryValidator;

    public static void ok() {
        ok("OK");
    }

    public static void ok(String message) {
        LOG.info(message);
    }
    
    public static void warn(String message) {
        LOG.warning(message);
    }
    
    public static void error(String message) {
        LOG.severe(message);
    }

    protected static String nothingToShow() {
        return "Nothing to show";
    }

    protected static void okOrShowErrors(Set<String> errors) {
        if (!errors.isEmpty()) {
            error(joinLines(errors));
        } else {
            ok();
        }
    }
    
    protected void tableOrNothing(String[] columns, List<String[]> rows) {
        if (!rows.isEmpty()){
            ok(tableRenderer.render(columns, rows));
        } else {
            ok(nothingToShow());
        }
    }
    
    protected <T extends Query<?>> void validateExecuteShowStatus(T query, Function<T, Set<String>> task) {
        Set<String> validationErrors = queryValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            Set<String> errors = task.apply(query);
            okOrShowErrors(errors);
        }
    }
    
    protected <T extends Query<?>> void validateExecuteShowResult(
            T query,
            String[] columns,
            Function<T, List<String[]>> task
    ) {
        Set<String> validationErrors = queryValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            List<String[]> data = task.apply(query);
            tableOrNothing(columns, data);
        }
    }
    
}
