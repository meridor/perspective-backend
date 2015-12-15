
parser grammar ParametersParser;

options
   { tokenVocab = ParametersLexer; }

queries
   : query+
   ;

query
   //Placeholder can't be the first in query
   : ( text placeholder? )+ delimiter?
   ;

text
   : ANY
   | LITERAL
   ;
   
placeholder
   : named_placeholder
   | positional_placeholder
   ;

named_placeholder
   : COLON ID COLON
   ;

positional_placeholder
   : POSITIONAL_PLACEHOLDER
   ;
   
delimiter
   : DELIMITER
   ;