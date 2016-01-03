package org.meridor.perspective.sql.impl;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.meridor.perspective.sql.SQLLexer;
import org.meridor.perspective.sql.SQLParser;
import org.meridor.perspective.sql.SQLParserBaseListener;
import org.meridor.perspective.sql.impl.task.LimitTask;
import org.meridor.perspective.sql.impl.task.ShowTablesTask;
import org.meridor.perspective.sql.impl.task.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLSyntaxErrorException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QuerySchedulerImpl extends SQLParserBaseListener implements QueryScheduler {

    private class InternalErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            String message = String.format("Parse error: \"%s\" near \"%s\" at %d:%d", msg, String.valueOf(offendingSymbol), line, charPositionInLine);
            SQLSyntaxErrorException ex = new SQLSyntaxErrorException(message, e);
            exception = Optional.of(ex);
        }
    }

    private Optional<SQLSyntaxErrorException> exception = Optional.empty();
    private final Queue<Task> tasksQueue = new LinkedList<>();
    
    //Miscellaneous values and flags used in parser
    private boolean isSelectClause;
    private boolean isFromClause;
    private boolean isWhereClause;
    private boolean isHavingClause;
    private boolean isGroupByClause;
    private boolean isOrderClause;
    private Optional<Integer> limitCount = Optional.empty();
    private Optional<Integer> limitOffset = Optional.empty();
    
    
    @Override
    public Queue<Task> schedule(String sql) throws SQLSyntaxErrorException {
        CharStream input = new ANTLRInputStream(sql);
        ANTLRErrorListener errorListener = new InternalErrorListener();
        SQLLexer sqlLexer = new SQLLexer(input);
        sqlLexer.removeErrorListeners();
        sqlLexer.addErrorListener(errorListener);
        CommonTokenStream commonTokenStream = new CommonTokenStream(sqlLexer);
        SQLParser sqlParser = new SQLParser(commonTokenStream);
        sqlParser.removeErrorListeners();
        sqlParser.addErrorListener(errorListener);
        ParseTree parseTree = sqlParser.query();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
        if (exception.isPresent()) {
            throw exception.get();
        }
        return tasksQueue;
    }

    //Show tables query
    @Override
    public void exitShow_tables_query(SQLParser.Show_tables_queryContext ctx) {
        tasksQueue.add(new ShowTablesTask());
    }

    //Select query
    @Override
    public void enterSelect_clause(SQLParser.Select_clauseContext ctx) {
        isSelectClause = true;
    }

    @Override
    public void exitSelect_clause(SQLParser.Select_clauseContext ctx) {
        isSelectClause = false;
    }

    @Override
    public void enterFrom_clause(SQLParser.From_clauseContext ctx) {
        isFromClause = true;
    }

    @Override
    public void exitFrom_clause(SQLParser.From_clauseContext ctx) {
        isFromClause = false;
    }

    @Override
    public void enterWhere_clause(SQLParser.Where_clauseContext ctx) {
        isWhereClause = true;
    }

    @Override
    public void exitWhere_clause(SQLParser.Where_clauseContext ctx) {
        isWhereClause = false;
    }

    @Override
    public void enterHaving_clause(SQLParser.Having_clauseContext ctx) {
        isHavingClause = true;
    }

    @Override
    public void exitHaving_clause(SQLParser.Having_clauseContext ctx) {
        isHavingClause = false;
    }

    @Override
    public void enterGroup_clause(SQLParser.Group_clauseContext ctx) {
        isGroupByClause = true;
    }

    @Override
    public void exitGroup_clause(SQLParser.Group_clauseContext ctx) {
        isGroupByClause = false;
    }

    @Override
    public void enterOrder_clause(SQLParser.Order_clauseContext ctx) {
        isOrderClause = true;
    }

    @Override
    public void exitOrder_clause(SQLParser.Order_clauseContext ctx) {
        isOrderClause = false;
    }

    @Override
    public void exitOffset(SQLParser.OffsetContext ctx) {
        Integer offset = Integer.valueOf(ctx.INT().getText());
        this.limitOffset = Optional.of(offset);
    }

    @Override
    public void exitRow_count(SQLParser.Row_countContext ctx) {
        Integer limitCount = Integer.valueOf(ctx.INT().getText());
        this.limitCount = Optional.of(limitCount);
    }

    @Override
    public void exitLimit_clause(SQLParser.Limit_clauseContext ctx) {
        if (limitCount.isPresent()) {
            tasksQueue.add(
                    limitOffset.isPresent() ?
                    new LimitTask(limitOffset.get(), limitCount.get()) :
                    new LimitTask(limitCount.get())
            );
        }
    }
}
