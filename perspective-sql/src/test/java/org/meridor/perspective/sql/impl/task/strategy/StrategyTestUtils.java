package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataRow;

import java.util.*;
import java.util.stream.Collectors;

public abstract class StrategyTestUtils {

    static final String TABLE_NAME = "mock";
    static final String TABLE_ALIAS = "m";
    static final Map<String, String> TABLE_ALIASES = new HashMap<String, String>(){
        {
            put(TABLE_ALIAS, TABLE_NAME);
        }
    };

    public static final String INSTANCES = "instances";
    public static final String INSTANCES_ALIAS = "i";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PROJECT_NAME = "project_name";
    public static final String PROJECT_ID = "project_id";
    static final List<String> INSTANCES_COLUMNS = Arrays.asList(ID, NAME, PROJECT_ID);
    static final Map<String, List<String>> INSTANCES_COLUMNS_MAP = Collections.singletonMap(INSTANCES_ALIAS, INSTANCES_COLUMNS);
    static final List<List<Object>> INSTANCES_DATA = Arrays.asList(
            Arrays.asList("1", "first", "2"),
            Arrays.asList("2", "second", "1"),
            Arrays.asList("3", "third", "2"),
            Arrays.asList("4", "third", "3"),
            Arrays.asList("5", "fifth", "2")
    );

    public static final String PROJECTS = "projects";
    public static final String PROJECTS_ALIAS = "p";
    static final List<String> PROJECTS_COLUMNS = Arrays.asList(ID, PROJECT_NAME);
    static final List<List<Object>> PROJECTS_DATA = Arrays.asList(
            Arrays.asList("1", "first_project"),
            Arrays.asList("2", "second_project"),
            Arrays.asList("3", "third_project")
    );

    static final Map<String, String> TWO_TABLE_ALIASES = new HashMap<String, String>(){
        {
            put(INSTANCES_ALIAS, INSTANCES);
            put(PROJECTS_ALIAS, PROJECTS);
        }
    };


    static List<List<Object>> rowsAsValues(List<DataRow> rows) {
        return rows.stream().map(DataRow::getValues).collect(Collectors.toList());
    }
}
