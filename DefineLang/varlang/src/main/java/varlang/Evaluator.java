package varlang;
import static varlang.AST.*;
import static varlang.Value.*;

import java.util.List;
import java.util.ArrayList;

import varlang.AST.AddExp;
import varlang.AST.NumExp;
import varlang.AST.DivExp;
import varlang.AST.MultExp;
import varlang.AST.Program;
import varlang.AST.SubExp;
import varlang.AST.VarExp;
import varlang.AST.Visitor;
import varlang.Env.EmptyEnv;
import varlang.Env.ExtendEnv;

public class Evaluator implements Visitor<Value> {

	Printer.Formatter ts = new Printer.Formatter();

	Env initEnv = new EmptyEnv(); //New for definelang

	Value valueOf(Program p) {
		return (Value) p.accept(this, initEnv);
	}

	@Override
	public Value visit(Program p, Env env) {
		try {
			for(DefineDecl d: p.decls())
				d.accept(this, initEnv);
			return (Value) p.e().accept(this, initEnv);
		} catch (ClassCastException e) {
			return new DynamicError(e.getMessage());
		}
	}

	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(NumExp e, Env env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			if (rVal.v() == 0) {
				return new DynamicError("Division by zero error in expression " + ts.visit(e, env));
			}
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) { // New for varlang
		return env.get(e.name());
	}

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();

		System.out.println(names + "  :1");
		System.out.println(value_exps + "  :2");

		Env new_env = env;

		for(int i = 0; i < names.size(); i++) {
			Exp exp = value_exps.get(i);
			System.out.println(value_exps.get(i) + "  :3");

			System.out.println(names.get(i) + "   :names");

			Value value = (Value)exp.accept(this, new_env);
			System.out.println(exp.accept(this, new_env) + "  :4");

			new_env = new ExtendEnv(new_env, names.get(i), value);
		}

		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(DefineDecl e, Env env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		initEnv = new ExtendEnv(initEnv, name, value);
		return new Value.UnitVal();
	}

	@Override
	public Value visit(LeteExp e, Env env) { // New for varlang
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());
		NumVal key = (NumVal) e.key().accept(this, env);
		for(Exp exp : value_exps){
			values.add((Value) exp.accept(this, env));
		}
		Env new_env = env;
		for(int i = 0; i < names.size(); i++) {
			Value val = values.get(i);
			if(val instanceof NumVal){
				val = new NumVal(((NumVal)val).v()*key.v());
			}
			new_env = new ExtendEnv(new_env, names.get(i), val);
		}

		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(DecExp e, Env env){
		Value val = env.get(e.name());
		if(val instanceof NumVal)
		{
			NumVal key = (NumVal) e.key().accept(this, env);
			return new NumVal((((NumVal)val).v())/key.v());
		}
		return val;
	}

	@Override
	public Value visit(UnitExp e, Env env) {
		return new UnitVal();
	} // New for definelang.

}
