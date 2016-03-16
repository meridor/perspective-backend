
lexer grammar ParametersLexer;

ID
   : ( '0' .. '9' | 'a' .. 'z' | 'A' .. 'Z' | '_'  | '.' )+
   ;

LITERAL
   : '\'' .+? '\''
   ;
   
COLON
   : ':'
   ;
      
POSITIONAL_PLACEHOLDER
   : '?'
   ;

DELIMITER
   : ( '\n' | ';' )+
   ;
   
ANY
   : ~( '?' | ':' | ';' | '\n' | '\'' )+
   ;
    
