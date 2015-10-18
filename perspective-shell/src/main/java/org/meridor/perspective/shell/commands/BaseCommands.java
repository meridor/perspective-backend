package org.meridor.perspective.shell.commands;

import com.google.common.collect.Lists;
import jline.console.ConsoleReader;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.query.Query;
import org.meridor.perspective.shell.query.QueryValidator;
import org.meridor.perspective.shell.repository.impl.SettingsStorage;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class BaseCommands implements CommandMarker {

    private static final Logger LOG = HandlerUtils.getLogger(BaseCommands.class);
    
    private static final Integer DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private TableRenderer tableRenderer;
    
    @Autowired
    private QueryValidator queryValidator;

    @Autowired
    private SettingsStorage settingsStorage;

    /**
     * Shows pages one by one allowing to navigate back and forth
     * @param pages each page contents
     */
    protected static void page(List<String> pages) {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            final int NUM_PAGES = pages.size();
            int currentPage = 1;
            showPage(currentPage, NUM_PAGES, pages); //Always showing first page
            while (currentPage <= NUM_PAGES) {
                boolean pageNumberChanged = false;
                String key = String.valueOf((char) consoleReader.readCharacter());
                if (isExitKey(key)) {
                    break;
                } else if (isNextElementKey(key)) {
                    if (currentPage == NUM_PAGES) {
                        break;
                    }
                    currentPage++;
                    pageNumberChanged = true;
                } else if (isPrevElementKey(key) && currentPage > 1) {
                    currentPage--;
                    pageNumberChanged = true;
                } else if (isNumericKey(key)) {
                    Integer pageNumber = Integer.valueOf(key);
                    if (pageNumber < 1 || pageNumber > NUM_PAGES) {
                        warn(String.format("Wrong page number: %d. Should be one of 1..%d.", pageNumber, NUM_PAGES));
                        continue;
                    } else if (pageNumber != currentPage) {
                        currentPage = pageNumber;
                        pageNumberChanged = true;
                    }
                }
                if (pageNumberChanged) {
                    showPage(currentPage, NUM_PAGES, pages);
                }
            }
        } catch (IOException e) {
            error(String.format("Failed to show pages: %s", e.getMessage()));
        }
    }
    
    private static void showPage(final int pageNumber, final int numPages, List<String> entries) {
        ok(String.format("Showing page %d of %d:", pageNumber, numPages));
        ok(entries.get(pageNumber - 1));
    }
    
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
        final Integer PAGE_SIZE = getPageSize();
        if (!rows.isEmpty()){
            if (rows.size() > PAGE_SIZE) {
                ok(String.format(
                        "Results contain %d entries. Showing first %d entries.",
                        rows.size(),
                        PAGE_SIZE
                ));
                ok("Press Space, Enter or n to show next page, p or w to show previous page, a number key to show specific page and q to type next command.");
                page(preparePages(PAGE_SIZE, columns, rows));
            } else {
                ok(tableRenderer.render(columns, rows));
            }
        } else {
            ok(nothingToShow());
        }
    }
    
    private Integer getPageSize() {
        return (settingsStorage.hasSetting(Setting.PAGE_SIZE)) ?
                settingsStorage.getSettingAs(Setting.PAGE_SIZE, Integer.class) :
                DEFAULT_PAGE_SIZE;
    }
    
    private boolean alwaysSayYes() {
        return (settingsStorage.hasSetting(Setting.ALWAYS_SAY_YES));
    }
    
    private List<String> preparePages(Integer pageSize, String[] columns, List<String[]> rows) {
        return Lists.partition(rows, pageSize).stream().
                map(b -> tableRenderer.render(columns, b))
                .collect(Collectors.toList());
    }
    
    protected <T, Q extends Query<T>> void validateConfirmExecuteShowStatus(
            Q query,
            Function<T, String> confirmationMessageProvider,
            Function<T, String[]> confirmationColumnsProvider,
            Function<T, List<String[]>> confirmationRowsProvider,
            Function<Q, Set<String>> task
    ) {
        Set<String> validationErrors = queryValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            T payload = query.getPayload();
            String confirmationMessage = confirmationMessageProvider.apply(payload);
            String[] columns = confirmationColumnsProvider.apply(payload);
            List<String[]> rows = confirmationRowsProvider.apply(payload);
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
        Set<String> validationErrors = queryValidator.validate(query);
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        } else {
            List<String[]> data = task.apply(query);
            tableOrNothing(columns, data);
        }
    }
    
}
