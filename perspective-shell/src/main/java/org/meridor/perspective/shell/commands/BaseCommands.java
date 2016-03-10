package org.meridor.perspective.shell.commands;

import jline.console.ConsoleReader;
import org.meridor.perspective.shell.misc.Logger;
import org.meridor.perspective.shell.misc.Pager;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.request.InvalidRequestException;
import org.meridor.perspective.shell.request.Request;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class BaseCommands implements CommandMarker {

    @Autowired
    private TableRenderer tableRenderer;

    @Autowired
    private SettingsAware settingsAware;

    @Autowired
    private Logger logger;
    
    @Autowired
    private Pager pager;
    
    protected void ok() {
        logger.ok();
    }

    protected void ok(String message) {
        logger.ok(message);
    }
    
    protected void warn(String message) {
        logger.warn(message);
    }
    
    protected void error(String message) {
        logger.error(message);
    }

    protected static String nothingToShow() {
        return "Nothing to show";
    }

    protected void okOrShowErrors(Set<String> errors) {
        if (!errors.isEmpty()) {
            error(joinLines(errors));
        } else {
            ok();
        }
    }
    
    protected void tableOrNothing(String[] columns, List<String[]> rows) {
        tableOrNothing(columns,  rows, false);
    }
    
    protected void tableOrNothing(String[] columns, List<String[]> rows, boolean showResultsCountAnyway) {
        final Integer PAGE_SIZE = pager.getPageSize();
        if (!rows.isEmpty()){
            if (rows.size() > PAGE_SIZE) {
                ok(String.format(
                        "Results contain %d entries. Showing first %d entries.",
                        rows.size(),
                        PAGE_SIZE
                ));
                pager.page(columns, rows);
            } else {
                if (showResultsCountAnyway) {
                    ok(String.format("Results contain %d entries.", rows.size()));
                }
                ok(tableRenderer.render(columns, rows));
            }
        } else {
            ok(nothingToShow());
        }
    }

    private boolean alwaysSayYes() {
        return (settingsAware.hasSetting(Setting.ALWAYS_SAY_YES));
    }

    protected <T, I, P, R extends Request<P>> void validateConfirmExecuteShowStatus(
            R request,
            Function<R, T> payloadProcessor,
            Function<T, String> confirmationMessageProvider,
            Function<T, String[]> confirmationColumnsProvider,
            Function<T, List<String[]>> confirmationRowsProvider,
            BiFunction<R, T, I> taskDataProvider,
            Function<I, Set<String>> task
    ) {
        try {
            T confirmationData = payloadProcessor.apply(request);
            String confirmationMessage = confirmationMessageProvider.apply(confirmationData);
            String[] columns = confirmationColumnsProvider.apply(confirmationData);
            List<String[]> rows = confirmationRowsProvider.apply(confirmationData);
            if (rows.size() == 0) {
                error("Nothing selected for this operation. Exiting.");
                return;
            }
            ok(confirmationMessage);
            tableOrNothing(columns, rows);
            if (confirmOperation(
                    () -> tableOrNothing(columns, rows)
            )) {
                I taskData = taskDataProvider.apply(request, confirmationData);
                Set<String> errors = task.apply(taskData);
                okOrShowErrors(errors);
            } else {
                warn("Aborted.");
            }
        } catch (InvalidRequestException e) {
            error(joinLines(e.getErrors()));
        }
    }

    protected <T, R extends Request<T>> void validateConfirmExecuteShowStatus(
            R request,
            Function<T, String> confirmationMessageProvider,
            Function<T, String[]> confirmationColumnsProvider,
            Function<T, List<String[]>> confirmationRowsProvider,
            Function<R, Set<String>> task
    ) {
        validateConfirmExecuteShowStatus(
                request,
                Request::getPayload,
                confirmationMessageProvider,
                confirmationColumnsProvider,
                confirmationRowsProvider,
                (r, cd) -> r,
                task
        );
    }

    private boolean confirmOperation(Runnable repeatAction) {
        try {
            ok("Press y to proceed, n or q to abort operation, r to repeat this list again.");
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
                } else if (isRepeatKey(key)) {
                    repeatAction.run();
                    return confirmOperation(repeatAction);
                } else {
                    warn(String.format("Invalid key: %s. Please try again.", key));
                }
            }
        } catch (IOException e) {
            error(String.format("Failed to confirm action: %s", e.getMessage()));
            return false;
        }
    }
    
    protected <T extends Request<?>> void validateExecuteShowResult(
            T query,
            String[] columns,
            Function<T, List<String[]>> task
    ) {
        try {
            List<String[]> data = task.apply(query);
            tableOrNothing(columns, data);
        } catch (InvalidRequestException e) {
            error(joinLines(e.getErrors()));
        }
    }
    
    protected <T extends Request<?>> void validateExecuteShowResult(
            T query,
            Consumer<T> task
    ) {
        try {
            task.accept(query);
        } catch (InvalidRequestException e) {
            error(joinLines(e.getErrors()));
        }
    }
    
}
