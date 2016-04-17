package org.meridor.perspective.shell.misc.impl;

import com.google.common.collect.Lists;
import org.meridor.perspective.shell.misc.Logger;
import org.meridor.perspective.shell.misc.Pager;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class PagerImpl implements Pager {
    
    private static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final String PAGING_MODE_EXTERNAL = "external"; 
    
    @Autowired
    private Logger logger;
    
    @Autowired
    private SettingsAware settingsAware;
    
    @Autowired
    private TableRenderer tableRenderer;
    
    @Override
    public void page(String[] columns, List<String[]> rows) {
        if (!isExternalPaging() || !pageExternal(preparePage(columns, rows))) {
            page(preparePages(columns, rows));
        }
    }

    @Override
    public void page(List<String> pages) {
        try {
            final int NUM_PAGES = pages.size();
            if (NUM_PAGES == 0) {
                logger.ok("Zero results provided: nothing to show.");
                return;
            }
            if (NUM_PAGES > 1) {
                logger.ok("Press Space, Enter or n to show next page, p or w to show previous page, g to show first page, G to show last page, a number key to show specific page and q to type next command.");
            }
            int currentPage = 1;
            showPage(currentPage, NUM_PAGES, pages); //Always showing first page
            if (NUM_PAGES == 1) {
                return; //No need to wait for keys in case of one page
            }
            while (currentPage <= NUM_PAGES) {
                final int cp = currentPage;
                Map<Predicate<String>, Function<String, Integer>> routes = new LinkedHashMap<Predicate<String>, Function<String, Integer>>(){
                    {
                        put(TextUtils::isExitKey, k -> 0);
                        put(TextUtils::isNextElementKey, k -> (cp == NUM_PAGES) ? 0 : cp + 1);
                        put(k -> isPrevElementKey(k) && cp > 1, k -> cp - 1);
                        put(k -> isFirstElementKey(k) && cp != 1, k -> 1);
                        put(k -> isLastElementKey(k) && cp != NUM_PAGES, k -> NUM_PAGES);
                        put(TextUtils::isNumericKey, k -> {
                            Integer pageNumber = Integer.valueOf(k);
                            if (pageNumber < 1 || pageNumber > NUM_PAGES) {
                                logger.warn(String.format("Wrong page number: %d. Should be one of 1..%d.", pageNumber, NUM_PAGES));
                            } else if (pageNumber != cp) {
                                return pageNumber;
                            }
                            return cp;
                        });
                    }
                };

                Optional<Integer> nextPageCandidate = routeByKey(
                        routes,
                        k -> logger.warn(String.format("Invalid key: %s. Please try again.", k))
                );
                if (!nextPageCandidate.isPresent() || nextPageCandidate.get() < 1) {
                    break;
                }

                Integer nextPage = nextPageCandidate.get();
                if (nextPage != currentPage) {
                    currentPage = nextPage;
                    showPage(currentPage, NUM_PAGES, pages);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to show pages: %s", e.getMessage()));
        }
    }

    private boolean pageExternal(String data) {
        Map<String, Map<String, String>> candidateCommands = new LinkedHashMap<String, Map<String, String>>(){
            {
                //We try the following commands as external pagers
                put("less", Collections.singletonMap("LESS", "FRX"));
                put("more", Collections.emptyMap());
                put("most", Collections.emptyMap());
            }
        };
        for (String command : candidateCommands.keySet()) {
            Map<String, String> environment = candidateCommands.get(command);
            if (pageExternalCommand(data, command, environment)) {
                return true;
            }
        }
        logger.warn("Failed to show data in external pager. Using built-in instead.");
        return false;
    }
    
    private boolean pageExternalCommand(String data, String command, Map<String, String> environment) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.inheritIO();
            processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            processBuilder.environment().putAll(environment);
            Process process = processBuilder.start();
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.close();
            int returnCode = process.waitFor();
            return returnCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void showPage(final int pageNumber, final int numPages, List<String> entries) {
        if (numPages > 1) {
            logger.ok(String.format("Showing page %d of %d:", pageNumber, numPages));
        }
        logger.ok(entries.get(pageNumber - 1));
    }

    @Override
    public int getPageSize() {
        return (settingsAware.hasSetting(Setting.PAGE_SIZE)) ?
                settingsAware.getSettingAs(Setting.PAGE_SIZE, Integer.class) :
                DEFAULT_PAGE_SIZE;
    }
    
    private boolean isExternalPaging() {
        return 
                settingsAware.hasSetting(Setting.PAGING_MODE) &&
                settingsAware.getSettingAs(Setting.PAGING_MODE, String.class)
                        .equals(PAGING_MODE_EXTERNAL);
    }
    
    private List<String> preparePages(String[] columns, List<String[]> rows) {
        return Lists.partition(rows, getPageSize()).stream().
                map(b -> preparePage(columns, b))
                .collect(Collectors.toList());
    }
    
    private String preparePage(String[] columns, List<String[]> rows) {
        return tableRenderer.render(columns, rows);
    }

}
