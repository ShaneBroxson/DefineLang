grammar VarLang;

import ArithLang; //Import all rules from Arithlang grammar.
 
 // New elements in the Grammar of this Programming Language
 //  - grammar rules start with lowercase

 // We are redefining programs to be zero or more define declarations
 // followed by an optional expression.
 program returns [Program ast]
   		locals [ArrayList<DefineDecl> defs, Exp expr]
   		@init { $defs = new ArrayList<DefineDecl>(); $expr = new UnitExp(); } :
  		// (def=definedecl { $defs.add($def.ast); } )*
  		(def=definedecl { $defs.add($def.ast); } )*
          (e=exp { $expr = $e.ast; } )?
  		{ $ast = new Program($defs, $expr); }
  		;

 exp returns [Exp ast]: 
		v=varexp { $ast = $v.ast; }
		| n=numexp { $ast = $n.ast; }
        | a=addexp { $ast = $a.ast; }
        | s=subexp { $ast = $s.ast; }
        | m=multexp { $ast = $m.ast; }
        | d=divexp { $ast = $d.ast; }
        | l=letexp { $ast = $l.ast; }
        | le=leteexp {$ast = $le.ast; }
        | de=decexp {$ast = $de.ast; }
        ;

 varexp returns [VarExp ast]: 
 		id=Identifier { $ast = new VarExp($id.text); }
 		;

 numexp returns [NumExp ast]:
  		n0=Number { $ast = new NumExp(Integer.parseInt($n0.text)); }
   		| '-' n0=Number { $ast = new NumExp(-Integer.parseInt($n0.text)); }
   		| n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble($n0.text+"."+$n1.text)); }
   		| '-' n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble("-" + $n0.text+"."+$n1.text)); }
   		;

 letexp returns [LetExp ast]
        locals [ArrayList<String> names = new ArrayList<String>(), ArrayList<Exp> value_exps = new ArrayList<Exp>()] :
 		'(' Let 
 			'(' ( '(' id=Identifier e=exp ')' { $names.add($id.text); $value_exps.add($e.ast); } )+  ')'
 			body=exp 
 			')' { $ast = new LetExp($names, $value_exps, $body.ast); }
 		;

 leteexp returns [LeteExp ast]
        locals [ArrayList<String> names = new ArrayList<String>(), ArrayList<Exp> value_exps = new ArrayList<Exp>()] :
 		'(' Lete
 		key=numexp
 			'(' ( '(' id=Identifier e=exp ')' { $names.add($id.text); $value_exps.add($e.ast); } )+  ')'
 			body=exp
 			')' { $ast = new LeteExp($names, $value_exps, $body.ast, $key.ast); }
 		;

 decexp returns [DecExp ast]:
        '(' Dec
        key=numexp
        id=Identifier
        ')' {$ast = new DecExp($key.ast, $id.text); }
        ;



 definedecl returns [DefineDecl ast] :
 		'(' Define
 			id=Identifier
 			e=exp
 			')' { $ast = new DefineDecl($id.text, $e.ast); }
    ;

Lete: 'lete';
Dec : 'dec';
 // Lexical Specification of this Programming Language
 //  - lexical specification rules start with uppercase



// Hints to wrote define macro grammar.
// (define (macro_name argument1, argument2, ...) expression)

