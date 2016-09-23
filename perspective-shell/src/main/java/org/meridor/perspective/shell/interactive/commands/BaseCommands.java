package org.meridor.perspective.shell.interactive.commands;

import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.common.misc.Pager;
import org.meridor.perspective.shell.common.misc.TableRenderer;
import org.meridor.perspective.shell.common.repository.FiltersAware;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.common.request.InvalidRequestException;
import org.meridor.perspective.shell.common.request.Request;
import org.meridor.perspective.shell.common.validator.FilterProcessor;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.*;

@Component
public abstract class BaseCommands implements CommandMarker {

    private static final String NOTHING_TO_SHOW = "Nothing to show";
    
    @Autowired
    private TableRenderer tableRenderer;

    @Autowired
    private SettingsAware settingsAware;

    @Autowired
    private FiltersAware filtersAware;
    
    @Autowired
    private FilterProcessor filterProcessor;

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

    protected void okOrShowErrors(Set<String> errors) {
        if (!errors.isEmpty()) {
            error(joinLines(errors));
        } else {
            ok();
        }
    }
    
    protected void tableOrNothing(String[] columns, List<String[]> rows) {
        tableOrNothing(columns, rows, true);
    }
    
    protected void tableOrNothing(String[] columns, List<String[]> rows, boolean showMessage) {
        final Integer PAGE_SIZE = pager.getPageSize();
        if (!rows.isEmpty()){
            if (pager.isExternalPaging()) {
                pager.page(columns, rows);
            } else if (rows.size() > PAGE_SIZE) {
                if (showMessage) {
                    ok(String.format(
                            "Results contain %d entries. Showing first %d entries.",
                            rows.size(),
                            PAGE_SIZE
                    ));
                }
                pager.page(columns, rows);
            } else {
                if (showMessage) {
                    ok(String.format("Results contain %d entries.", rows.size()));
                }
                ok(tableRenderer.render(columns, rows));
            }
        } else {
            ok(NOTHING_TO_SHOW);
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
                if (filterProcessor.hasAppliedFilters(request)) {
                    ok("Nothing selected for this operation. However the following filters are set:");
                    listFilters();
                    ok("Would you like to search again without filters? Press y to proceed, n or q to abort operation.");
                    Optional<Boolean> confirmationResult = routeByKey(new LinkedHashMap<Predicate<String>, Function<String, Boolean>>() {
                        {
                            put(TextUtils::isYesKey, k -> true);
                            put(k -> isNoKey(k) || isExitKey(k), k -> false);
                        }
                    });
                    if (confirmationResult.isPresent() && confirmationResult.get()) {
                        ok("Processing...");
                        R newRequest = filterProcessor.unsetFilters(request);
                        validateConfirmExecuteShowStatus(
                                newRequest,
                                payloadProcessor,
                                confirmationMessageProvider,
                                confirmationColumnsProvider,
                                confirmationRowsProvider,
                                taskDataProvider,
                                task
                        );
                    }
                } else {
                    error("Nothing selected for this operation. Exiting.");
                }
                return;
            }
            ok(confirmationMessage);
            tableOrNothing(columns, rows, false);
            if (confirmOperation(
                    () -> tableOrNothing(columns, rows, false)
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

    private void listFilters() {
        Map<String, String> allFilters = filtersAware.getFilters(false);
        allFilters.keySet().forEach(k -> ok(String.format("%s = %s", k, allFilters.get(k))));
    }
    
    protected <T, R extends Request<T>> void validateConfirmExecuteShowStatus(
            R request,
            Function<T, String> confirmationMessageProvider,
            Function<T, String[]> confirmationColumnsProvider,
            Function<T, List<String[]>> confirmationRowsProvider,
            Function<T, Set<String>> task
    ) {
        validateConfirmExecuteShowStatus(
                request,
                Request::getPayload,
                confirmationMessageProvider,
                confirmationColumnsProvider,
                confirmationRowsProvider,
                (r, cd) -> cd,
                task
        );
    }

    private boolean confirmOperation(Runnable repeatAction) {
        ok("Press y to proceed, n or q to abort operation, r to repeat this list again.");
        if (alwaysSayYes()) {
            ok("Proceeding as always_say_yes mode is enabled.");
            return true;
        }
        Map<Predicate<String>, Function<String, Boolean>> routes = new LinkedHashMap<Predicate<String>, Function<String, Boolean>>(){
            {
                put(TextUtils::isYesKey, key -> true);
                put(key -> isNoKey(key) || isExitKey(key), key -> false);
                put(TextUtils::isRepeatKey, key -> {
                    repeatAction.run();
                    return confirmOperation(repeatAction);
                });
            }
        };
        Optional<Boolean> result = routeByKey(
                routes,
                k -> warn(String.format("Invalid key: %s. Please try again.", k)),
                e -> error(String.format("Failed to read key: %s", e.getMessage()))
        );
        return result.isPresent() && result.get();
    }
    
    protected <T extends Request<?>> void validateExecuteShowResult(
            T request,
            String[] columns,
            Function<T, List<String[]>> task
    ) {
        try {
            List<String[]> data = task.apply(request);
            if (data.isEmpty() && filterProcessor.hasAppliedFilters(request)) {
                ok("Nothing selected. However the following filters are set:");
                listFilters();
                ok("Would you like to search again without filters? Press y to proceed, n or q to abort operation.");
                Optional<Boolean> confirmationResult = routeByKey(new LinkedHashMap<Predicate<String>, Function<String, Boolean>>() {
                    {
                        put(TextUtils::isYesKey, k -> true);
                        put(k -> isNoKey(k) || isExitKey(k), k -> false);
                    }
                });
                if (confirmationResult.isPresent() && confirmationResult.get()) {
                    ok("Processing...");
                    T newRequest = filterProcessor.unsetFilters(request);
                    validateExecuteShowResult(newRequest, columns, task);
                    return;
                }
            }
            tableOrNothing(columns, data);
        } catch (InvalidRequestException e) {
            error(joinLines(e.getErrors()));
        }
    }
    
    protected <T extends Request<?>> void validateExecuteShowResult(
            T request,
            Consumer<T> task
    ) {
        try {
            task.accept(request);
        } catch (InvalidRequestException e) {
            error(joinLines(e.getErrors()));
        }
    }
    
}
