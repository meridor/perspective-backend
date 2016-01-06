
lexer grammar SQLLexer;

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
   
TABLES
   : 'tables'
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
   : 'not' | '!'
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
MULTIPLY
   : '*'
   ;
   
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
   
// String operations
REGEXP
   : 'regexp'
   ;

// Bitwise operations
BIT_NOT
   : '~'
   ;

BIT_OR
   : '|'
   ;

BIT_AND
   : '&'
   ;

BIT_XOR
   : '^'
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

//RBRACK
//   : ']'
//   ;
//
//LBRACK
//   : '['
//   ;
//
//COLON
//   : ':'
//   ;

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
   
ABS
   : 'abs'
   ;
   
TYPEOF
   : 'typeof'
   ;
   
// Basic types
NULL
   : 'null'
   ;

TRUE
   : 'true'
   ;

FALSE
   : 'false'
   ;
   
INT
   : '0' .. '9'+
   ;
   
FLOAT
   : ('0' .. '9')+.('0' .. '9')+
   ;

STRING
   : '\'' ( ~'\'' )* '\''
   ;
   
ID
   : ( '0' .. '9' | 'a' .. 'z' | 'A' .. 'Z' | '_' )+
   ;
   
NEWLINE
   : '\r'? '\n' -> skip
   ;
   
WS
   : ( ' ' | '\t' | '\n' | '\r' )+ -> skip
   ;