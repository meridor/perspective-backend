
lexer grammar SQLLexer;

// Basic types
NEWLINE
   : '\r'? '\n' -> skip
   ;

WS
   : ( ' ' | '\t' | '\n' | '\r' )+ -> skip
   ;
   
ID
   : ( 'a' .. 'z' | 'A' .. 'Z' | '_' )+
   ;

INT
   : '0' .. '9'+
   ;

STRING
   : '\'' ( ~'\'' )* '\''
   ;

NULL
   : 'null'
   ;

TRUE
   : 'true'
   ;

FALSE
   : 'false'
   ;
   
// Query keywords
SELECT
   : 'select'
   ;

FROM
   : 'from'
   ;
   
AS
   : 'as'
   ;

WHERE
   : 'where'
   ;
   
SHOW
   : 'show'
   ;
   
SHOW_TABLES
   : SHOW 'tables'
   ;

// Boolean operators
AND
   : 'and' | '&&'
   ;

OR
   : 'or' | '||'
   ;

XOR
   : 'xor'
   ;

IS
   : 'is'
   ;

LIKE
   : 'like'
   ;

IN
   : 'in'
   ;

EXISTS
   : 'exists'
   ;

BETWEEN
   : 'between'
   ;
   
ALL
   : 'all'
   ;

ANY
   : 'any'
   ;

NOT
   : 'not'
   ;
   
// Relational operators
EQ
   : '='
   ;

LT
   : '<'
   ;

GT
   : '>'
   ;

NOT_EQ
   : '!='
   ;


LTE
   : '<='
   ;

GTE
   : '>='
   ;

// Arithmetic operators
DIVIDE
   : 'div' | '/'
   ;

MOD
   : 'mod' | '%'
   ;

PLUS
   : '+'
   ;

MINUS
   : '-'
   ;

POWER_OP
   : '^'
   ;
   
// String operations
REGEXP
   : 'regexp'
   ;

// Bitwise operations
NEGATION
   : '~'
   ;

VERTBAR
   : '|'
   ;

BITAND
   : '&'
   ;

SHIFT_LEFT
   : '<<'
   ;

SHIFT_RIGHT
   : '>>'
   ;

// Misc keywords
BINARY
   : 'binary'
   ;

ESCAPE
   : 'escape'
   ;

ASTERISK
   : '*'
   ;

ALL_FIELDS
   : '.*'
   ;
   
USE
  : 'use'
  ;

IGNORE
  : 'ignore'
  ;

//Punctuation marks
RPAREN
   : ')'
   ;

LPAREN
   : '('
   ;

RBRACK
   : ']'
   ;

LBRACK
   : '['
   ;

COLON
   : ':'
   ;

SEMICOLON
   : ';'
   ;

COMMA
   : ','
   ;

DOT
   : '.'
   ;

// Join keywords
INNER
   : 'inner'
   ;

OUTER
   : 'outer'
   ;

JOIN
   : 'join'
   ;

CROSS
   : 'cross'
   ;

USING
   : 'using'
   ;

// Or
ORDER
   : 'order'
   ;
   
ASC
   : 'asc'
   ;
   
DESC
   : 'desc'
   ;

GROUP
   : 'group'
   ;

HAVING
   : 'having'
   ;
   
LIMIT
   : 'limit'
   ;
   
OFFSET
   : 'offset'
   ;
   
BY
   : 'by'
   ;

STRAIGHT_JOIN
   : 'straight_join'
   ;

NATURAL
   : 'natural'
   ;

LEFT
   : 'left'
   ;

RIGHT
   : 'right'
   ;

ON
   : 'on'
   ;

// Built-in functions
COUNT
   : 'count'
   ;