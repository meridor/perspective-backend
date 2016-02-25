package org.meridor.perspective.shell.misc.impl;

import com.google.common.collect.Lists;
import jline.console.ConsoleReader;
import org.meridor.perspective.shell.misc.Logger;
import org.meridor.perspective.shell.misc.Pager;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class PagerImpl implements Pager {
    
    private static final Integer DEFAULT_PAGE_SIZE = 20;
    
    @Autowired
    private Logger logger;
    
    @Autowired
    private SettingsAware settingsAware;
    
    @Autowired
    private TableRenderer tableRenderer;
    
    @Override
    public void page(String[] columns, List<String[]> rows) {
        page(preparePages(columns, rows));
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
            ConsoleReader consoleReader = new ConsoleReader();
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
                } else if (isFirstElementKey(key) && currentPage != 1) {
                    currentPage = 1;
                    pageNumberChanged = true;
                } else if (isLastElementKey(key) && currentPage != NUM_PAGES) {
                    currentPage = NUM_PAGES;
                    pageNumberChanged = true;
                } else if (isNumericKey(key)) {
                    Integer pageNumber = Integer.valueOf(key);
                    if (pageNumber < 1 || pageNumber > NUM_PAGES) {
                        logger.warn(String.format("Wrong page number: %d. Should be one of 1..%d.", pageNumber, NUM_PAGES));
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
            logger.error(String.format("Failed to show pages: %s", e.getMessage()));
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

    private List<String> preparePages(String[] columns, List<String[]> rows) {
        return Lists.partition(rows, getPageSize()).stream().
                map(b -> tableRenderer.render(columns, b))
                .collect(Collectors.toList());
    }

}
