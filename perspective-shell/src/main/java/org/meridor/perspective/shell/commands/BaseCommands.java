package org.meridor.perspective.shell.commands;

import jline.console.ConsoleReader;
import org.meridor.perspective.shell.misc.LoggingUtils;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.query.Query;
import org.meridor.perspective.shell.repository.impl.SettingsStorage;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class BaseCommands implements CommandMarker {

    @Autowired
    private TableRenderer tableRenderer;
    
    @Autowired
    private ObjectValidator objectValidator;

    @Autowired
    private SettingsStorage settingsStorage;

    public static void ok() {
        LoggingUtils.ok();
    }

    public static void ok(String message) {
        LoggingUtils.ok(message);
    }
    
    public static void warn(String message) {
        LoggingUtils.warn(message);
    }
    
    public static void error(String message) {
        LoggingUtils.error(message);
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
        final Integer PAGE_SIZE = getPageSize(settingsStorage);
        if (!rows.isEmpty()){
            if (rows.size() > PAGE_SIZE) {
                ok(String.format(
                        "Results contain %d entries. Showing first %d entries.",
                        rows.size(),
                        PAGE_SIZE
                ));
                page(preparePages(tableRenderer, PAGE_SIZE, columns, rows));
            } else {
                ok(tableRenderer.render(columns, rows));
            }
        } else {
            ok(nothingToShow());
        }
    }

    private boolean alwaysSayYes() {
        return (settingsStorage.hasSetting(Setting.ALWAYS_SAY_YES));
    }

    protected <T, Q extends Query<T>> void validateConfirmExecuteShowStatus(
            Q query,
            Function<T, String> confirmationMessageProvider,
            Function<T, String[]> confirmationColumnsProvider,
            Function<T, List<String[]>> confirmationRowsProvider,
            Function<Q, Set<String>> task
    ) {
        Set<String> validationErrors = objectValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            T payload = query.getPayload();
            String confirmationMessage = confirmationMessageProvider.apply(payload);
            String[] columns = confirmationColumnsProvider.apply(payload);
            List<String[]> rows = confirmationRowsProvider.apply(payload);
            if (rows.size() == 0) {
                error("Nothing selected for this operation. Exiting.");
                return;
            }
            ok(confirmationMessage);
            tableOrNothing(columns, rows);
            if (confirmOperation()) {
                Set<String> errors = task.apply(query);
                okOrShowErrors(errors);
            } else {
                warn("Aborted.");
            }
        }
    }

    private boolean confirmOperation() {
        try {
            ok("Press y to proceed, n or q to abort operation.");
            if (alwaysSayYes()) {
                ok("Proceeding as always_say_yes mode is enabled.");
                return true;
            }
            ConsoleReader consoleReader = new ConsoleReader();
            while (true) {
                String key = String.valueOf((char) consoleReader.readCharacter());
                if (isYesKey(key)) {
                    return true;
                } else if (isNoKey(key) || isExitKey(key)) {
                    return false;
                } else {
                    warn(String.format("Invalid key: %s. Please try again.", key));
                }
            }
        } catch (IOException e) {
            error(String.format("Failed to confirm action: %s", e.getMessage()));
            return false;
        }
    }
    
    protected <T extends Query<?>> void validateExecuteShowResult(
            T query,
            String[] columns,
            Function<T, List<String[]>> task
    ) {
        Set<String> validationErrors = objectValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            List<String[]> data = task.apply(query);
            tableOrNothing(columns, data);
        }
    }
    
}
