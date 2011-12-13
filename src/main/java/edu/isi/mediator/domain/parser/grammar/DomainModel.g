grammar DomainModel;


options 
{
	output = AST;
	ASTLabelType = CommonTree;
	backtrack=true;
}

tokens{
	DOMAIN_MODEL;
	SOURCE_SCHEMA;
	DOMAIN_SCHEMA;
	GAV_RULES;
	LAV_RULES;
	GLAV_RULES;
	UAC_RULES;
	QUERIES;
	RULE;
	ANTECEDENT;
	CONSEQUENT;
	RELATION_PRED;
	FUNCTION_PRED;
	BUILTIN_PRED;
	FUNCTIONS;
	NAMESPACES;
	IS_NULL;
	ISNOT_NULL;
	IN;
	NOT_IN;
}

@members 
{
protected void mismatch(IntStream input, int ttype, BitSet follow)
    throws RecognitionException
{ 
    throw new MismatchedTokenException(ttype, input);
}

public Object recoverFromMismatchedSet(IntStream input, RecognitionException re, BitSet follow)
    throws RecognitionException
{
    throw re;
}
}

@lexer::members 
{
protected void mismatch(IntStream input, int ttype, BitSet follow)
    throws RecognitionException
{ 
    throw new MismatchedTokenException(ttype, input);
}

public Object recoverFromMismatchedSet(IntStream input, RecognitionException re, BitSet follow)
    throws RecognitionException
{
    throw re;
}
}

@rulecatch {
catch (RecognitionException re)
{
    reportError(re);
    throw re;
}
}

@lexer::rulecatch {
catch (RecognitionException re)
{
    reportError(re);
    throw re;
}
}

domain_model
	:	(section)* -> ^(DOMAIN_MODEL section+);

section
	:	schema | functions | rules | namespaces;
	
schema
	:	schema_name (schema_exp)* -> ^(schema_name schema_exp*);

schema_name
	:	'SOURCE_SCHEMA:' -> ^(SOURCE_SCHEMA)
	|	'DOMAIN_SCHEMA:' -> ^(DOMAIN_SCHEMA);
	
functions
	:	'FUNCTIONS:' (function_name)* -> ^(FUNCTIONS function_name*);
	
namespaces
	:	'NAMESPACES:' (namespace)* -> ^(NAMESPACES namespace*);
	
namespace
	:	namespace_prefix ':' namespace_uri -> ^(namespace_prefix namespace_uri);

schema_exp
	:	table_name '(' column_identifier (',' column_identifier)* ')' -> ^(table_name column_identifier+);

column_identifier
	: 	column_name ':' column_type (':' column_binding)?  -> ^(column_name column_type column_binding? );
	
rules
	:	rule_name (rule)* -> ^(rule_name rule+)
	|	'LAV_RULES:' (lav_rule)* -> ^(LAV_RULES lav_rule+)
	|	'GLAV_RULES:' (glav_rule)* -> ^(GLAV_RULES glav_rule+);
	
rule_name
	:	'GAV_RULES:' -> ^(GAV_RULES)
	|	'UAC_RULES:' -> ^(UAC_RULES)
	|	'QUERIES:' -> ^(QUERIES);

rule
	:	relation_predicate ('<-' | '::') rule_body -> ^(RULE ^(ANTECEDENT rule_body) ^(CONSEQUENT relation_predicate))
	|	relation_predicate ':.' -> ^(RULE ^(CONSEQUENT relation_predicate));
	
lav_rule
	:	relation_predicate ('->') rule_body -> ^(RULE ^(ANTECEDENT relation_predicate) ^(CONSEQUENT rule_body));
	
glav_rule
	:	v1=rule_body ('->') v2=rule_body -> ^(RULE ^(ANTECEDENT $v1) ^(CONSEQUENT $v2));

rule_body
	:	predicate ('^' predicate)* -> ^(predicate)+;

predicate
	:	relation_predicate | builtin_predicate;
	
relation_predicate
	:	table_name '(' column_value (',' column_value)* ')' -> ^(RELATION_PRED table_name column_value+);

function_predicate
	:	function_name '(' column_value (',' column_value)* ')' -> ^(FUNCTION_PRED function_name column_value+)
	|	function_name '('')' -> ^(FUNCTION_PRED function_name);

builtin_predicate
	:	'(' v1=column_value comparison v2=column_value? ')' -> ^(BUILTIN_PRED comparison $v1 $v2?)
	|	'(' v1=column_value in_comparison v3=value_list ')' -> ^(BUILTIN_PRED in_comparison $v1 $v3);
	
column_value
	:	column_name | constant | function_predicate;
	
constant
	:	STRING | INT | FLOAT | 'NULL';
	
value_list
	:	'[' constant (',' constant)* ']' -> constant+;
	
comparison
	:	'=' | '!=' | '<' | '>' | '>=' | '<=' | '<>' | 'LIKE' | null_comparison;
	
null_comparison
	:	'IS' 'NULL' -> ^(IS_NULL) 
	| 	'IS' 'NOT' 'NULL' -> ^(ISNOT_NULL);
	
in_comparison
	:	'IN' -> ^(IN) 
	| 	'NOT' 'IN' -> ^(NOT_IN);

//let it be anything and test after parsing
column_type
	:	ID ;
	
//if I use 'b' | 'f' it is ambiguous with ID and causes an error
column_binding
	:	ID ;
		
table_name
	: ID;
	
column_name
	: ID;
	
function_name
	: ID;

namespace_prefix
	: ID;

namespace_uri
	: STRING;


//'#' used only in naming of UAC rules; '#' will not make it to the SQL query
//'$' as in $USERID is used in UAC rules, it denotes that we have to replace at runtime with the actual userid
ID	:	('a'..'z' | 'A'..'Z' | '_' | '$') ( ('a'..'z' | 'A'..'Z') | ('0'..'9') | '_' | '#' )* 
	|	'`' (~('\''|'\n'|'\r'|'`')) ( (~('\''|'\n'|'\r'|'`')) )* '`' ;
FLOAT	:	('0'..'9')+ '.' ('0'..'9')+ ;
INT	:	('0'..'9')+ ;
NUMERIC	:	(INT | FLOAT) 'E' ('+' | '-')? INT;
STRING	:	'"' (~('"'|'\n'|'\r'))*  '"'
	|	'\'' (~('\''|'\n'|'\r'))*  '\'';
WS	:	(' ' | '\t' | '\r' | '\n' ) {skip();} ;
COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;
	
