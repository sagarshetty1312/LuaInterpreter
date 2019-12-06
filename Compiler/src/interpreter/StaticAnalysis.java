package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cop5556fa19.Token.Kind;
import cop5556fa19.AST.ASTVisitor;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpList;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldList;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;

public class StaticAnalysis implements ASTVisitor {

	Map<String, List<Stat>> lMap = new HashMap<String, List<Stat>>();
	Map<Stack<Integer>,Map<String, List<Stat>>> symbolTable = new HashMap<Stack<Integer>, Map<String,List<Stat>>>();
	Stack<Integer> curstack = new Stack<Integer>();
	int blockCounter = 0;

	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		Block block = chunk.block;
		block.visit(this, arg);
		blockCounter = 0;
		curstack = new Stack<Integer>();
		block.visit(this, arg);
		//return lMap;
		return symbolTable;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		curstack.push(blockCounter ++);
		saveParentLabels();
		for(Stat stat : block.stats) {
			if(stat instanceof RetStat) {
				//RetStat retStat = (RetStat)stat;
				//return (List<LuaValue>) retStat.visit(this, arg);
				
			} else if(stat instanceof StatAssign) {
				//StatAssign statAssign = (StatAssign)stat;
				//visitStatAssign(statAssign, arg);
				
			} else if (stat instanceof StatLabel) {
				StatLabel statLabel = (StatLabel)stat;
				saveLabel(statLabel,block.stats);
				//to be implemented
			} else if (stat instanceof StatBreak) {
				//retList.add(new LuaBreak());
				//return retList;
				
			} else if(stat instanceof StatGoto) {
				//to be implemented
			} else if(stat instanceof StatDo) {
				StatDo statDo = (StatDo)stat;
				visitStatDo(statDo, arg);
				
			} else if(stat instanceof StatWhile) {
				StatWhile statWhile = (StatWhile)stat;
				visitStatWhile(statWhile, arg);
				
			} else if(stat instanceof StatRepeat) {
				StatRepeat statRepeat = (StatRepeat)stat;
				visitStatRepeat(statRepeat, arg);
				
			} else if(stat instanceof StatIf) {
				StatIf statIf = (StatIf)stat;
				visitStatIf(statIf, arg);
				
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
		}
		curstack.pop();
		return null;
	}

	public void saveParentLabels() {
		Stack<Integer> tempStack = (Stack<Integer>) curstack.clone();
		tempStack.pop();
		Map<String, List<Stat>> tempMap = new HashMap<String, List<Stat>>();
		if(!tempStack.isEmpty()) {
			if(symbolTable.get(tempStack)!=null) {
				tempMap.putAll(symbolTable.get(tempStack));
				Stack<Integer> insertStack = (Stack<Integer>) curstack.clone();
				symbolTable.put(insertStack, tempMap);
			}
		}
	}

	public void saveLabel(StatLabel statLabel, List<Stat> stats) {
		List<Stat> listNew = new ArrayList<Stat>();
		boolean countStart = false;
		for(Stat s : stats) {
			if(countStart) {
				listNew.add(s);
			}
			if(s instanceof StatLabel && ((StatLabel)s).label.equals(statLabel.label)) {
				countStart = true;
			}
		}
		//if(lMap.get(statLabel.label.name) == null)
			//lMap.put(statLabel.label.name, listNew);

		Stack<Integer> tempStack = (Stack<Integer>) curstack.clone();
		Map<String, List<Stat>> tempMap = new HashMap<String, List<Stat>>();
		if(symbolTable.get(tempStack) == null) {
			tempMap = new HashMap<String, List<Stat>>();
		} else {
			tempMap.putAll(symbolTable.get(tempStack));
		}
		tempMap.put(statLabel.label.name, listNew);
		symbolTable.put(tempStack, tempMap);
		
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
			return (LuaValue) visitExpNil(expNil, arg);
			
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
			ExpTableLookup etl = (ExpTableLookup)exp;
			return (LuaValue)etl.visit(this, arg);
			
		} else if (exp instanceof ExpFunctionCall) {
			ExpFunctionCall efc = (ExpFunctionCall)exp;
			return (LuaValue)efc.visit(this, arg);
			//to be implemented
		} else if (exp instanceof ExpTable) {
			ExpTable expTable = (ExpTable) exp;
			return (LuaTable) expTable.visit(this, arg);
			
		} else if (exp instanceof ExpBinary) {
			ExpBinary expBin = (ExpBinary)exp;
			return (LuaValue) expBin.visit(this, arg);
		} else if (exp instanceof ExpUnary) {
			ExpUnary expUn = (ExpUnary)exp;
			return (LuaValue) expUn.visit(this, arg);
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
			List<LuaValue> expList = visitExpList(statAssign.expList, arg);
			int count = 0;
			for(Exp var : statAssign.varList) {
				if(var instanceof ExpName) {
					ExpName expName = (ExpName)var;

					//symTable.put(new LuaString(expName.name), expList.get(count++));
					
				} else if(var instanceof ExpTableLookup) {
					ExpTableLookup expTableLookup = (ExpTableLookup)var;
					LuaTable table = (LuaTable)visitExp(expTableLookup.table, arg);
					if(table == null)
						throw new StaticSemanticException(expTableLookup.firstToken, "Invalid table for lookup.");
					
					if(table instanceof LuaTable) {
						LuaTable ltable = (LuaTable)table;
						ltable.put(visitExp(expTableLookup.key, arg), expList.get(count++));
						//symTable.put((LuaValue)visitExp(expTableLookup.table, arg), ltable);
					}
				} else {
					throw new StaticSemanticException(var.firstToken, "Expected variable.");
				}
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
		//LuaValue luaValue = symTable.get(luaString);
		//if(luaValue.equals(new LuaNil())) {
			throw new StaticSemanticException(expName.firstToken, "Variable not initalized");
		//}
		//return luaValue;
	}

	@Override
	public LuaValue visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		try{
			//LuaTable table = (LuaTable) symTable.get((LuaValue)visitExp(expTableLookup.table, arg));
			//if(table == null)
				throw new StaticSemanticException(expTableLookup.firstToken, "Invalid table for lookup.");

			//return table.get(visitExp(expTableLookup.key, arg));
		} catch (ClassCastException e) {
			throw new StaticSemanticException(expTableLookup.firstToken, "Invalid lookup");
		}
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
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		LuaValue e = visitExp(unExp.e, arg);
		Kind opKind = unExp.op;
		if(e instanceof LuaInt) {
			LuaInt luaInt = (LuaInt)e;
			if(opKind == Kind.OP_MINUS)
				return new LuaInt(luaInt.v*(-1));
			else
				throw new StaticSemanticException(unExp.firstToken, "Invalid operand");
			
		} else if(e instanceof LuaBoolean) {
			LuaBoolean luaBoolean = (LuaBoolean)e;
			if(opKind == Kind.KW_not)
				return new LuaBoolean(!luaBoolean.value);
			else if(opKind == Kind.BIT_XOR)
				return new LuaBoolean(!luaBoolean.value);
			else
				throw new StaticSemanticException(unExp.firstToken, "Invalid operand");
			
		} else if(opKind == Kind.OP_HASH) {
			if(e instanceof LuaString) {
				LuaString luaS = (LuaString)e;
				return new LuaInt(luaS.value.length());
			
			} else if(e instanceof LuaTable) {
				LuaTable luaS = (LuaTable)e;
				return new LuaInt(luaS.arraySize);
			
			}
			else
				throw new StaticSemanticException(unExp.firstToken, "Invalid operand");
		}
		return null;
	}

	@Override
	public LuaTable visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
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
		visitBlock(b, arg);
		return null;
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		List<Block> bList = statIf.bs;
		for(Block b : bList) {
			visitBlock(b, arg);
		}
		return null;
	}

	@Override
	public List<LuaValue> visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		Block block = statWhile.b;
		visitBlock(block, arg);
		return null;
	}

	@Override
	public List<LuaValue> visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		Block block = statRepeat.b;
		visitBlock(block, arg);
		return null;
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		List<LuaValue> list = new ArrayList<LuaValue>();
		for(Exp e : expFunctionCall.args) {
			list.add((LuaValue) e.visit(this, arg));
		}
		ExpName expName = (ExpName) expFunctionCall.f;
		if(expName.name.equals("print")) {
			print p = new print();
			return p.call(list);
		} else if(expName.name.equals("println")) {
			println p = new println();
			return p.call(list);
		} else if(expName.name.equals("toNumber")) {
			toNumber p = new toNumber();
			return p.call(list);
		} else {
			throw new StaticSemanticException(expFunctionCall.firstToken, "Function not defined");
		}
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