package org.meridor.perspective.sql.impl;

import org.junit.Test;
import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.Query;

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
                    .equal("five", "six")
                .getQuery();
        assertThat(query.getSql(), equalTo("select one from test where two = :two: and five = :five:"));
        assertThat(query.getParameters().getParameters(), hasSize(2));
        Parameter firstParameter = query.getParameters().getParameters().get(0);
        assertThat(firstParameter.getName(), equalTo("two"));
        assertThat(firstParameter.getValue(), equalTo("three"));
        Parameter secondParameter = query.getParameters().getParameters().get(1);
        assertThat(secondParameter.getName(), equalTo("five"));
        assertThat(secondParameter.getValue(), equalTo("six"));
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

}