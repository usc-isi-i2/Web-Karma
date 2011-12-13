lexer grammar DomainModel;
@rulecatch {
catch (RecognitionException re)
{
    reportError(re);
    throw re;
}
}
@members {
protected void mismatch(IntStream input, int ttype, BitSet follow)
    throws RecognitionException
{ 
    throw new MismatchedTokenException(ttype, input);
}

public void recoverFromMismatchedSet(IntStream input, RecognitionException re, BitSet follow)
    throws RecognitionException
{
    throw re;
}
}

T29 : 'SOURCE_SCHEMA:' ;
T30 : 'DOMAIN_SCHEMA:' ;
T31 : 'FUNCTIONS:' ;
T32 : 'NAMESPACES:' ;
T33 : ':' ;
T34 : '(' ;
T35 : ',' ;
T36 : ')' ;
T37 : 'LAV_RULES:' ;
T38 : 'GAV_RULES:' ;
T39 : 'UAC_RULES:' ;
T40 : 'QUERIES:' ;
T41 : '<-' ;
T42 : '::' ;
T43 : ':.' ;
T44 : '->' ;
T45 : '^' ;
T46 : 'NULL' ;
T47 : '[' ;
T48 : ']' ;
T49 : '=' ;
T50 : '!=' ;
T51 : '<' ;
T52 : '>' ;
T53 : '>=' ;
T54 : '<=' ;
T55 : '<>' ;
T56 : 'LIKE' ;
T57 : 'IS' ;
T58 : 'NOT' ;
T59 : 'IN' ;

// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 185
ID	:	('a'..'z' | 'A'..'Z' | '_' | '$') ( ('a'..'z' | 'A'..'Z') | ('0'..'9') | '_' | '#' )* 
	|	'`' (~('\''|'\n'|'\r'|'`')) ( (~('\''|'\n'|'\r'|'`')) )* '`' ;
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 187
FLOAT	:	('0'..'9')+ '.' ('0'..'9')+ ;
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 188
INT	:	('0'..'9')+ ;
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 189
NUMERIC	:	(INT | FLOAT) 'E' ('+' | '-')? INT;
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 190
STRING	:	'"' (~('"'|'\n'|'\r'))*  '"'
	|	'\'' (~('\''|'\n'|'\r'))*  '\'';
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 192
WS	:	(' ' | '\t' | '\r' | '\n' ) {skip();} ;
// $ANTLR src "C:\Documents and Settings\mariam\My Documents\mediator-new\workspace\domainparser\DomainModel.g" 193
COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;
	
