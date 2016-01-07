
parser grammar SQLParser;

options
   { tokenVocab = SQLLexer; }

query
   : select_query
   | show_tables_query
   ;

//Entities
table_name
   : ID
   ;
   
alias
   : ID
   ;
   
column_name
   : ( table_name DOT )? ( ID | ASTERISK ) 
   | ( alias DOT )? ( ID | ASTERISK )
   ;
   
   
// Select query
select_query
   : select_clause ( from_clause )? ( where_clause )? ( group_clause )? ( having_clause )? ( order_clause )? ( limit_clause )? ( SEMICOLON )?
   ;

//subquery
//   : LPAREN select_query RPAREN
//   ;
   
select_clause
   : SELECT select_expression
   ;

from_clause
   : FROM table_references
   ;

where_clause
   : WHERE complex_boolean_expression
   ;
   
group_clause
   : GROUP BY expressions
   ;
   
having_clause
   : HAVING complex_boolean_expression
   ;

order_clause
   : ORDER BY order_expressions
   ;

offset : INT;

row_count: INT;

limit_clause
   : LIMIT ( ( offset COMMA )? row_count | row_count OFFSET offset )
   ;

alias_clause
   : ( AS )? alias
   ;

aliased_expression
   : expression ( alias_clause )?
   ;
   
select_expression
   : aliased_expression ( COMMA aliased_expression )*
   ;
   
columns_list
   : column_name ( COMMA column_name )*
   ;

complex_boolean_expression
   : simple_boolean_expression
   | simple_boolean_expression binary_boolean_operator simple_boolean_expression
   | unary_boolean_operator complex_boolean_expression
   | complex_boolean_expression (binary_boolean_operator complex_boolean_expression)+
   ;

function_name
   : COUNT
   | ABS
   | TYPEOF
   ;

expressions
   : expression ( COMMA expression )*
   ;
   
order_expressions
   : order_expression ( COMMA order_expression )*
   ;

order_expression
   : expression ( ASC | DESC )?
   ;
   
function_call
   : function_name LPAREN expressions RPAREN
   ;

literal
   : STRING 
   | INT
   | FLOAT
   | TRUE
   | FALSE
   | NULL
   ;

expression
   : literal
   | column_name 
   | function_call
   | expression binary_arithmetic_operator expression
   | unary_arithmetic_operator expression
//   | subquery
   ;

relational_operator
   : EQ 
   | LT 
   | GT 
   | NOT_EQ 
   | LTE 
   | GTE
   ;

binary_arithmetic_operator
   : PLUS
   | MINUS
   | MULTIPLY
   | DIVIDE
   | MOD
   | BIT_AND
   | BIT_OR
   | BIT_XOR
   | SHIFT_LEFT
   | SHIFT_RIGHT
   ; 

unary_arithmetic_operator
   : PLUS
   | MINUS
   | BIT_NOT
   ;

binary_boolean_operator
   : AND 
   | XOR 
   | OR 
   ;

unary_boolean_operator
   : NOT
   ;

simple_boolean_expression
   : expression relational_operator expression 
   | expression (NOT)? BETWEEN expression AND expression 
   | expression IS (NOT)? NULL
   | expression (NOT)? LIKE expression
   | expression (NOT)? REGEXP expression
   | expression (NOT)? IN LPAREN expression (COMMA expression)* RPAREN
   ;

table_references
   : table_reference ( ( COMMA table_reference ) | join_clause )*
   ;

table_reference
   : generic_join 
   | table_atom
   ;

generic_join
   : complex_join ( ( INNER | CROSS )? JOIN table_atom ( join_condition )? )?
   ;

complex_join
   : medium_join ( STRAIGHT_JOIN table_atom ( ON complex_boolean_expression )? )?
   ;

medium_join
   : simple_join ( ( LEFT | RIGHT ) ( OUTER )? JOIN simple_join join_condition )?
   ;

simple_join
   : table_atom ( NATURAL ( ( LEFT | RIGHT ) ( OUTER )? )? JOIN table_atom )?
   ;

table_atom
   : table_name ( alias_clause )? 
//   | subquery alias_clause 
   | LPAREN table_references RPAREN
   ;

join_clause
   : ( INNER | CROSS )? JOIN table_atom ( join_condition )? 
   | STRAIGHT_JOIN table_atom ( ON complex_boolean_expression )?
   | ( LEFT | RIGHT ) ( OUTER )? JOIN simple_join join_condition 
   | NATURAL ( ( LEFT | RIGHT ) ( OUTER )? )? JOIN table_atom
   ;

join_condition
   : ON complex_boolean_expression 
   | USING LPAREN columns_list RPAREN
   ;


// Show tables query
show_tables_query
   : SHOW TABLES
   ;