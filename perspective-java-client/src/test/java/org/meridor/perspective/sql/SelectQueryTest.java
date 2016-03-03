package org.meridor.perspective.sql;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SelectQueryTest {
    
    @Test
    public void testSelectAll() {
        Query query = new SelectQuery()
                .all()
                .from()
                .table("test")
                .getQuery();
        assertThat(query.getSql(), equalTo("select * from test"));
        assertThat(query.getParameters().getParameters(), is(empty()));
    }
    
    @Test
    public void testSelectColumns() {
        Query query = new SelectQuery()
                .column("one")
                .column("two", "alias")
                .columns("three", "four")
                .from()
                .table("test")
                .getQuery();
        assertThat(query.getSql(), equalTo("select one, two as alias, three, four from test"));
        assertThat(query.getParameters().getParameters(), is(empty()));
    }
    
    @Test
    public void testSimpleWhereClause() {
        Query query = new SelectQuery()
                .column("one")
                .from()
                .table("test")
                .where()
                    .equal("two", "three")
                    .and()
                    .regexp("five", "six")
                    .or()
                    .like("seven", "eight")
                .getQuery();
        assertThat(query.getSql(), equalTo("select one from test where two = :two: and five regexp :five: or seven like :seven:"));
        assertThat(query.getParameters().getParameters(), hasSize(3));
        Parameter firstParameter = query.getParameters().getParameters().get(0);
        assertThat(firstParameter.getName(), equalTo("two"));
        assertThat(firstParameter.getValue(), equalTo("three"));
        Parameter secondParameter = query.getParameters().getParameters().get(1);
        assertThat(secondParameter.getName(), equalTo("five"));
        assertThat(secondParameter.getValue(), equalTo("six"));
        Parameter thirdParameter = query.getParameters().getParameters().get(2);
        assertThat(thirdParameter.getName(), equalTo("seven"));
        assertThat(thirdParameter.getValue(), equalTo("eight"));
    }
    
    @Test
    public void testOrderBy() {
        Query query = new SelectQuery()
                .column("one")
                .from()
                .table("test")
                .orderBy()
                    .columns("three", "four")
                .getQuery();
        assertThat(query.getSql(), equalTo("select one from test order by three asc, four asc"));
        assertThat(query.getParameters().getParameters(), is(empty()));
    }
    
    @Test
    public void testLimit() {
        Query query = new SelectQuery()
                .column("one")
                .from()
                .table("test")
                .limit(10)
                .getQuery();
        assertThat(query.getSql(), equalTo("select one from test limit 10"));
        assertThat(query.getParameters().getParameters(), is(empty()));
    }
    
    @Test
    public void testMultiOr() {
        Map<String, Collection<String>> columnValues = new LinkedHashMap<String, Collection<String>>() {
            {
                put("one", Arrays.asList("two", "%three%"));
                put("four", Arrays.asList("^five$", "six"));
            }
        };
        Query query = new SelectQuery()
                .column("one")
                .from()
                .table("test")
                .where()
                .or(columnValues)
                .getQuery();
        assertThat(query.getSql(), equalTo("select one from test where one regexp :one: or one like :one1: or four = :four: or four regexp :four1:"));
        assertThat(query.getParameters().getParameters(), hasSize(4));
        Parameter firstParameter = query.getParameters().getParameters().get(0);
        assertThat(firstParameter.getName(), equalTo("one"));
        assertThat(firstParameter.getValue(), equalTo("two"));
        Parameter secondParameter = query.getParameters().getParameters().get(1);
        assertThat(secondParameter.getName(), equalTo("one1"));
        assertThat(secondParameter.getValue(), equalTo("%three%"));
        Parameter thirdParameter = query.getParameters().getParameters().get(2);
        assertThat(thirdParameter.getName(), equalTo("four"));
        assertThat(thirdParameter.getValue(), equalTo("five"));
        Parameter fourthParameter = query.getParameters().getParameters().get(3);
        assertThat(fourthParameter.getName(), equalTo("four1"));
        assertThat(fourthParameter.getValue(), equalTo("six"));
    }
    
    @Test
    public void testInnerJoin() {
        Query query = new SelectQuery()
                .all()
                .from()
                .table("one", "o")
                .innerJoin()
                .table("two", "t")
                .on()
                .equal("o.id", "t.id")
                .getQuery();
        assertThat(query.getSql(), equalTo("select * from one as o inner join two as t on o.id = t.id"));
        assertThat(query.getParameters().getParameters(), is(empty()));
    }

    @Test
    public void testInCondition() {
        Query query = new SelectQuery()
                .all()
                .from()
                .table("one")
                .where()
                .in("two", Arrays.asList("three", "four"))
                .getQuery();
        assertThat(query.getSql(), equalTo("select * from one where two in (:two:, :two1:)"));
        assertThat(query.getParameters().getParameters(), hasSize(2));
        Parameter firstParameter = query.getParameters().getParameters().get(0);
        assertThat(firstParameter.getName(), equalTo("two"));
        assertThat(firstParameter.getValue(), equalTo("three"));
        Parameter secondParameter = query.getParameters().getParameters().get(1);
        assertThat(secondParameter.getName(), equalTo("two1"));
        assertThat(secondParameter.getValue(), equalTo("four"));
    }
    
}