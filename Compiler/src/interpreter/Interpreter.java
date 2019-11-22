package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import com.sun.org.apache.xpath.internal.functions.FuncRound;

//import cop5556fa19.BuildSymbolTable;
import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.Token.Kind;
import cop5556fa19.AST.*;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;


public class Interpreter extends ASTVisitorAdapter{



	
	LuaTable _G; //global environment

	/* Instantiates and initializes global environment
	 * 
	 * Initially, the "standard library" routines implemented in Java are loaded.  For this assignment,
	 * this is just print and println.  
	 * 
	 * These functions impl
	 */
	void init_G() {
		_G = new LuaTable();
		_G.put("print", new print());
		_G.put("println", new println());
		_G.put("toNumber", new toNumber());
	}
	
	ASTNode root; //useful for debugging
		
	public Interpreter() {
		init_G();
	}
	

	
	@SuppressWarnings("unchecked")
	public List<LuaValue> load(Reader r) throws Exception {
		Scanner scanner = new Scanner(r); 
		Parser parser = new Parser(scanner);
		Chunk chunk = parser.parse();
		if(chunk == null)
			return null;
		else
			root = chunk;
		//Perform static analysis to prepare for goto.  Uncomment after u
		StaticAnalysis hg = new StaticAnalysis();
		chunk.visit(hg,null);	
		//Interpret the program and return values returned from chunk.visit
		List<LuaValue> vals = (List<LuaValue>) chunk.visit(this,_G);
		return vals;
	}

	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		Block block = chunk.block;
		List<LuaValue> retList = (List<LuaValue>) block.visit(this, arg);
		if((retList.size() != 0)) {
			if(retList.get(0) instanceof LuaBreak) {
				retList.remove(0);
			} 
		}
		return retList;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<LuaValue> retList = new ArrayList<LuaValue>();
		for(Stat stat : block.stats) {
			if(stat instanceof RetStat) {
				RetStat retStat = (RetStat)stat;
				return retStat.visit(this, arg);
				
			} else if(stat instanceof StatAssign) {
				StatAssign statAssign = (StatAssign)stat;
				statAssign.visit(this, arg);
				
			} else if (stat instanceof StatLabel) {
				//to be implemented
			} else if (stat instanceof StatBreak) {
				retList.add(new LuaBreak());
				return retList;
				
			} else if(stat instanceof StatGoto) {
				//to be implemented
			} else if(stat instanceof StatDo) {
				StatDo statDo = (StatDo)stat;
				retList = (List<LuaValue>) statDo.visit(this, arg);
				
			} else if(stat instanceof StatWhile) {
				StatWhile statWhile = (StatWhile)stat;
				retList = (List<LuaValue>) statWhile.visit(this, arg);
				
			} else if(stat instanceof StatRepeat) {
				//to be implemented
			} else if(stat instanceof StatIf) {
				StatIf statIf = (StatIf)stat;
				retList = (List<LuaValue>) statIf.visit(this, arg);
				//to be implemented
			} else if(stat instanceof StatFor) {
				//to be implemented
			} else if(stat instanceof StatForEach) {
				//to be implemented
			} else if(stat instanceof StatFunction) {
				//to be implemented
			} else if(stat instanceof StatLocalFunc) {
				//to be implemented
			} else if(stat instanceof StatLocalAssign) {
				//to be implemented
			}
			

			if(retList.size() != 0) {
				return retList;
			}
		}
		return retList;
	}

	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		List<LuaValue> list = new ArrayList<LuaValue>();
		for(Exp exp : retStat.el) {
			if (exp instanceof ExpName) {
				ExpName expName = (ExpName)exp;
				list.add((LuaValue) expName.visit(this, arg));
				
			} else {
				list.add(visitExp(exp, arg));
			}
		}
		
		return list;
	}

	public LuaValue visitExp(Exp exp, Object arg) throws Exception {
		if(exp instanceof ExpNil) {
			ExpNil expNil = (ExpNil)exp;
			return (LuaValue) expNil.visit(this, arg);
			
		} else if(exp instanceof ExpFalse) {
			ExpFalse expFalse = (ExpFalse)exp;
			return (LuaValue) expFalse.visit(this, arg);
			
		} else if (exp instanceof ExpTrue) {
			ExpTrue expTrue= (ExpTrue)exp;
			return (LuaValue) expTrue.visit(this, arg);
			
		} else if (exp instanceof ExpInt) {
			ExpInt expInt = (ExpInt)exp;
			return (LuaValue) expInt.visit(this, arg);
			
		} else if (exp instanceof ExpString) {
			ExpString expString = (ExpString)exp;
			return (LuaValue) expString.visit(this, arg);
			
		} else if (exp instanceof ExpVarArgs) {
			ExpVarArgs expVarArgs = (ExpVarArgs)exp;
			return (LuaValue) expVarArgs.visit(this, arg);
			//to be implemented
		} else if (exp instanceof ExpFunction) {
			//to be implemented
		} else if (exp instanceof ExpName) {
			ExpName expName = (ExpName)exp;
			return (LuaValue)expName.visit(this, arg);
		} else if (exp instanceof ExpTableLookup) {
			//to be implemented
		} else if (exp instanceof ExpFunctionCall) {
			//to be implemented
		} else if (exp instanceof ExpTable) {
			ExpTable expTable = (ExpTable) exp;
			return (LuaValue) expTable.visit(this, arg);
			
		} else if (exp instanceof ExpBinary) {
			ExpBinary expBin = (ExpBinary)exp;
			return (LuaValue) expBin.visit(this, arg);
		}
		return new LuaNil();
	}

	public List<LuaValue> visitExpList(List<Exp> expList, Object arg) throws Exception {
		List<LuaValue> list = new ArrayList<LuaValue>();
		for(Exp exp : expList) {
			list.add(visitExp(exp, arg));
		}
		return list;
	}

	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		if(statAssign.varList.size() == statAssign.expList.size()) {
			LuaString []varList = new LuaString[statAssign.varList.size()];
			int count = 0;
			for(Exp var : statAssign.varList) {
				if(var instanceof ExpName) {
					ExpName expName = (ExpName)var;
					varList[count++] = new LuaString(expName.name);
				} else if(var instanceof ExpTableLookup) {
					ExpTableLookup expTableLookup = (ExpTableLookup)var;
					varList[count++] = (LuaString) expTableLookup.visit(this, arg);
				} else {
					throw new StaticSemanticException(var.firstToken, "Expected variable.");
				}
			}
			
			count = 0;
			List<LuaValue> expList = visitExpList(statAssign.expList, arg);
			for(LuaString ls : varList) {
				_G.put(ls, expList.get(count++));
			}
			
		}else {
			throw new StaticSemanticException(statAssign.firstToken, "Number of variables does not match the left hand side.");
		}
		return null;
	}

	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) {
		LuaInt luaInt = new LuaInt(expInt.v);
		return luaInt;
	}

	@Override
	public LuaValue visitName(Name name, Object arg) {
		LuaString luaString = new LuaString(name.name);
		return luaString;
	}

	@Override
	public LuaValue visitExpName(ExpName expName, Object arg) throws StaticSemanticException {
		LuaString luaString = new LuaString(expName.name);
		LuaValue luaValue = _G.get(luaString);
		if(luaValue.equals(new LuaNil())) {
			throw new StaticSemanticException(expName.firstToken, "Variable not initalized");
		}
		return luaValue;
	}

	@Override
	public LuaValue visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		
		// to be implemented
		return null;
	}
	
	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		return new LuaNil();
	}

	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) {
		return new LuaBoolean(true);
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		return new LuaBoolean(false);
	}

	@Override
	public Object visitExpString(ExpString expString, Object arg) {
		return new LuaString(expString.v);
	}

	@Override
	public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) {
		//To be implemented
		return null;
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		LuaValue e0 = visitExp(expBin.e0, arg);
		LuaValue e1 = visitExp(expBin.e1, arg);
		Kind opKind = expBin.op;
		if((e0 instanceof LuaNil) && (e1 instanceof LuaNil)) {
			return e0;
			
		} else if((e0 instanceof LuaInt) && (e1 instanceof LuaInt)) {
			return executeOperations((LuaInt)e0, (LuaInt)e1, opKind);
			
		} else if((e0 instanceof LuaBoolean) && (e1 instanceof LuaBoolean)) {
			return executeOperations((LuaBoolean)e0, (LuaBoolean)e1, opKind);
			
		} else if((e0 instanceof LuaString) && (e1 instanceof LuaString)) {
			return executeOperations((LuaString)e0, (LuaString)e1, opKind);
			
		} else if((e0 instanceof LuaTable) && (e1 instanceof LuaTable)) {
			//to be implemented
		} else {
			throw new StaticSemanticException(expBin.firstToken, "Incompatible operands");
		}
		return new LuaNil();
	}

	@Override
	public LuaValue visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
		LuaTable luaTable = new LuaTable();
		for(Field f : expTableConstr.fields) {
			if(f instanceof FieldNameKey) {
				FieldNameKey fnk = (FieldNameKey)f;
				luaTable.put(fnk.name.name, visitExp(fnk.exp, arg));
				
			} else if(f instanceof FieldExpKey) {
				FieldExpKey fek = (FieldExpKey)f;
				luaTable.put(visitExp(fek.key, arg), visitExp(fek.value, arg));
				
			} else if(f instanceof FieldImplicitKey) {
				FieldImplicitKey fik = (FieldImplicitKey)f;
				luaTable.putImplicit(visitExp(fik.exp, arg));
				
			} else {
				throw new StaticSemanticException(expTableConstr.firstToken, "Invalid table constructor syntax");
			}
		}
		return luaTable;
	}



	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		Block b = statDo.b;
		List<LuaValue> retList = (List<LuaValue>) b.visit(this, arg);
		if((retList.size() != 0)) {
			if(retList.get(0) instanceof LuaBreak) {
				retList.remove(0);
			} 
		}
		return retList;
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		List<Exp> eList = statIf.es;
		List<Block> bList = statIf.bs;
		List<LuaValue> retList = new ArrayList<LuaValue>();
		int	count = 0;
		for(Exp exp : eList) {
			if((exp.visit(this, arg).equals(new LuaBoolean(true))) || (exp.visit(this, arg).equals(new LuaInt(0)))){
				Block block = bList.get(count);
				retList = (List<LuaValue>) block.visit(this, arg);
				if((retList.size() != 0)) {
					if(retList.get(0) instanceof LuaBreak) {
						//retList.remove(0);
					} 
				}
				return retList;
			}
			count++;
		}
		if(bList.size() > eList.size()) {
			Block block = bList.get(count);
			retList = (List<LuaValue>) block.visit(this, arg);
			if((retList.size() != 0)) {
				if(retList.get(0) instanceof LuaBreak) {
					//retList.remove(0);
				} 
			}
			return retList;
		}else{
			return retList;
		}
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		Exp exp = statWhile.e;
		List<LuaValue> list = new ArrayList<LuaValue>();
		
		while((exp.visit(this, arg).equals(new LuaBoolean(true))) || (exp.visit(this, arg).equals(new LuaInt(0)))) {
			Block block = statWhile.b;
			list = (List<LuaValue>) block.visit(this, arg);

			if((list.size() != 0)) {
				if(list.get(0) instanceof LuaBreak) {
					list.remove(0);
				}
				return list; 
			}
		}
		return list;
	}

	public LuaValue executeOperations(LuaString e0, LuaString e1, Kind opKind) {
		switch (opKind) {
		case OP_PLUS:
			return new LuaString(e0.value + e1.value);
		case OP_MINUS:
			break;
		case OP_TIMES:
			break;
		case OP_DIV:
			break;
		case OP_DIVDIV:
			break;
		case OP_POW:
			break;
		case OP_MOD:
			break;
		case BIT_AMP:
			//& to be implemented
			break;
		case BIT_XOR:
			//~ to be implemented
			break;
		case BIT_OR:
			// | to be implemented
			break;
		case BIT_SHIFTL:
			break;
		case BIT_SHIFTR:
			break;
		case DOTDOT:
			return new LuaString(e0.value + e1.value);
		case REL_LT:
			//to be implemented
			break;
		case REL_LE:
			//to be implemented
			break;
		case REL_GT:
			//to be implemented
			break;
		case REL_GE:
			//to be implemented
			break;
		case REL_EQEQ:
			if(e0.equals(e1))
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_NOTEQ:
			if(!e0.equals(e1))
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case KW_and:
			//to be implemented
			break;
		case KW_or:
			//to be implemented
			break;
		default:
			break;
		}
		return new LuaNil();
	}



	public LuaValue executeOperations(LuaBoolean e0, LuaBoolean e1, Kind opKind) {
		switch (opKind) {
		case OP_PLUS:
			break;
		case OP_MINUS:
			break;
		case OP_TIMES:
			break;
		case OP_DIV:
			break;
		case OP_DIVDIV:
			break;
		case OP_POW:
			break;
		case OP_MOD:
			break;
		case BIT_AMP:
			//& to be implemented
			break;
		case BIT_XOR:
			//~ to be implemented
			break;
		case BIT_OR:
			// | to be implemented
			break;
		case BIT_SHIFTL:
			break;
		case BIT_SHIFTR:
			break;
		case DOTDOT:
			break;
		case REL_LT:
			break;
		case REL_LE:
			break;
		case REL_GT:
			break;
		case REL_GE:
			break;
		case REL_EQEQ:
			if(e0.value == e1.value)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_NOTEQ:
			if(e0.value != e1.value)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case KW_and:
			return new LuaBoolean(e0.value && e1.value);
		case KW_or:
			return new LuaBoolean(e0.value || e1.value);
		default:
			break;
		}
		return new LuaNil();
	}



	public LuaValue executeOperations(LuaInt e0, LuaInt e1, Kind opKind) {
		switch (opKind) {
		case OP_PLUS:
			return new LuaInt(e0.v + e1.v);
		case OP_MINUS:
			return new LuaInt(e0.v - e1.v);
		case OP_TIMES:
			return new LuaInt(e0.v * e1.v);
		case OP_DIV:
			return new LuaInt(e0.v / e1.v);
		case OP_DIVDIV:
			return new LuaInt(Math.floorDiv(e0.v, e1.v));
		case OP_POW:
			return new LuaInt((int) Math.pow(e0.v, e1.v));
		case OP_MOD:
			return new LuaInt(e0.v % e1.v);
		case BIT_AMP:
			//& to be implemented
			break;
		case BIT_XOR:
			//~ to be implemented
			break;
		case BIT_OR:
			// | to be implemented
			break;
		case BIT_SHIFTL:
			return new LuaInt(e0.v << e1.v);
		case BIT_SHIFTR:
			return new LuaInt(e0.v >> e1.v);
		case DOTDOT:
			return new LuaString(String.valueOf(e0.v) + String.valueOf(e1.v));
		case REL_LT:
			if(e0.v < e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_LE:
			if(e0.v <= e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_GT:
			if(e0.v > e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_GE:
			if(e0.v >= e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_EQEQ:
			if(e0.v == e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case REL_NOTEQ:
			if(e0.v != e1.v)
				return new LuaBoolean(true);
			else
				return new LuaBoolean(false);
		case KW_and:
			//to be implemented
			break;
		case KW_or:
			//to be implemented
			break;
		default:
			break;
		}
		return new LuaNil();
	}

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFuncName(FuncName funcName, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object ar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFieldList(FieldList fieldList, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
