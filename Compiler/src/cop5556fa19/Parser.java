/**
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa19.AST.ASTNode;
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
import cop5556fa19.AST.Var;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;
import static org.junit.Assert.assertNotNull;

public class Parser {

	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}

	final Scanner scanner;
	Token t;  //invariant:  this is the next token
	Exp varCaseExp = null;

	Parser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
	}


	public Chunk parse() throws Exception {
		Token first = t;
		return new Chunk(first, block());
	}
	
	public Block block() throws Exception {
		Token first = t;
		List<Stat> list = new ArrayList<Stat>();
		while(checkIfStatFirst(t)) {
			list.add(stat());
		}
		if(isKind(KW_return)) {
			list.add(retstat());
		}
		return new Block(first, list);
	}


	public RetStat retstat() throws Exception {
		Token first = t;
		List<Exp> list = new ArrayList<Exp>();
		match(KW_return);
		if(checkIfExpFirst(this.t)) {
			list.add(exp());
			while(isKind(COMMA)) {
				consume();
				list.add(exp());
			}
		}
		if(isKind(SEMI)) {
			consume();
		}
		return new RetStat(first, list);
	}


	public Stat stat() throws Exception {
		Token first = t;
		Stat e0 = null;
		
		if(isKind(SEMI)) {
			consume();
			if(checkIfStatFirst(first)) {
				e0 = stat();
			}
		}else if(checkIfVarFirst(first)) {
			List varList = new ArrayList<Var>();
			List expList = new ArrayList<Exp>();
			varList.add(var());
			while (isKind(COMMA)) {
				consume();
				varList.add(var());
			}
			match(ASSIGN);
			expList.add(exp());
			while(isKind(COMMA)) {
				consume();
				expList.add(exp());
			}
			e0 =  new StatAssign(first, varList, expList);
			
		}else if(isKind(COLONCOLON)) {
			//label
			match(COLONCOLON);
			Token name = null;
			if(isKind(NAME)) {
				name = consume();
			}
			match(COLONCOLON);
			e0 =  new StatLabel(first, new Name(first, name.text));
			
		}else if(isKind(KW_break)) {
			consume();
			e0 =  new StatBreak(first);
			
		}else if(isKind(KW_goto)) {
			consume();
			Token name = match(NAME);
			e0 =  new StatGoto(first, new Name(first, name.text));
			
		}else if(isKind(KW_do)) {
			consume();
			Block b = block();
			match(Kind.KW_end);
			e0 =  new StatDo(first, b);
			
		}else if(isKind(KW_while)) {
			consume();
			Exp e = exp();
			match(KW_do);
			Block b = block();
			match(KW_end);
			e0 =  new StatWhile(first, e, b);
			
		}else if(isKind(KW_repeat)) {
			consume();
			Block b = block();
			match(KW_until);
			e0 =  new StatRepeat(first, b, exp());
			
		}else if(isKind(KW_if)) {
			List blockList = new ArrayList<Block>();
			List expList = new ArrayList<Exp>();
			consume();//if
			expList.add(exp());
			match(KW_then);
			blockList.add(block());
			while(isKind(KW_elseif)) {
				consume();
				expList.add(exp());
				match(KW_then);
				blockList.add(block());
			}
			if(isKind(KW_else)) {
				consume();
				blockList.add(block());
			}
			match(KW_end);
			e0 =  new StatIf(first, expList, blockList);
			
		}else if(isKind(KW_for)) {
			consume();
			Token name = match(NAME);
			if(isKind(ASSIGN)) {//StatFor
				Exp einc = null;
				Exp ebeg = exp();
				match(COMMA);
				Exp eend = exp();
				if(isKind(COMMA)) {
					einc = exp();
				}
				match(KW_do);
				Block block = block();
				match(KW_end);
				e0 =  new StatFor(first, new ExpName(name.text), ebeg, eend, einc, block);
				
			}else {//StatForEach
				List nameList = new ArrayList<ExpName>();
				nameList.add(new ExpName(name.text));
				while(isKind(COMMA)) {
					consume();
					name = match(NAME);
					nameList.add(new ExpName(name.text));
				}
				match(KW_in);
				List explist = expList();
				match(KW_do);
				Block block = block();
				match(KW_end);
				e0 =  new StatForEach(first, nameList, explist, block);
			}
			
		}else if(isKind(KW_function)) {
			consume();
			e0 =  new StatFunction(first, funcName(), funcBodyExp());
			
		}else if(isKind(KW_local)) {
			consume();
			if(isKind(KW_function)) {
				//StatLocalFunc
				consume();
				Token name = match(NAME);
				e0 =  new StatLocalFunc(first, new FuncName(first, new ExpName(name.text)), funcBodyExp());
			}else {
				//StatLocalAssign
				List nameList = new ArrayList<ExpName>();
				List expList = new ArrayList<Exp>();
				Token name = match(NAME);
				nameList.add(new ExpName(name.text));
				while(isKind(COMMA)) {
					consume();
					name = match(NAME);
					nameList.add(new ExpName(name.text));
				}
				if(isKind(ASSIGN)) {
					consume();
					expList.add(exp());
					while(isKind(COMMA)) {
						consume();
						expList.add(exp());
					}
				}
				e0 =  new StatLocalAssign(first, nameList, expList);
			}
		}
		return e0;
	}


	public Exp var() throws Exception {
		Token first = t;
		Token name = null;
		Exp prefixExp = null;
		//Special case for name
		if(isKind(NAME)) {
			name = consume();
			if(checkIfPrefixTailFirst()) {
				prefixExp = prefixExpForVarWithFirst(new ExpName(name));
			}else {
				return new ExpName(name.text);
			}
		} else if(isKind(LPAREN)) {
			prefixExp = prefixExpForVar();
		} else {
			error(NAME, LPAREN);
		}
		
		if(varCaseExp != null) {
			return new ExpTableLookup(first, prefixExp, varCaseExp);
		} else {
			error(LSQUARE,DOT);
			return null;
		}
	}


	public Exp prefixExpForVar() throws Exception {
		Token first = t;
		if(isKind(LPAREN)) {
			consume();
			Exp e0 = exp();
			match(RPAREN);
			return prefixExpForVarWithFirst(e0);
		} else if(isKind(NAME)) {
			return prefixExpForVarWithFirst(new ExpName(first));
		}
		
		return null;
	}


	public Exp prefixExpForVarWithFirst(Exp exp) throws Exception {
		Token first = t;
		Exp last = null;
		varCaseExp = last;
		Exp e0 = exp;
		boolean tableLookupFlag = false;
		
		while(checkIfPrefixTailFirst()) {
			if(tableLookupFlag) {
				e0 = new ExpTableLookup(first, e0, last);
			}
			
			if(isKind(LSQUARE)) {
				consume();
				tableLookupFlag = true;
				last = exp();
				match(RSQUARE);
				
			} else if(isKind(DOT)) {
				consume();
				tableLookupFlag = true;
				//last = new ExpName(match(NAME));
				last = new ExpString(match(NAME));
				
			} else if((isKind(LPAREN) || isKind(LCURLY) || isKind(STRINGLIT))) {
				tableLookupFlag = false;
				e0 = new ExpFunctionCall(first, e0, args());
				
			} else if(isKind(COLON)) {
				consume();
				tableLookupFlag = false;
				List<Exp> list = new ArrayList<Exp>();
				Token name1 = match(NAME);
				list.add(new ExpName(name1));
				list.addAll(args());
				e0 = new ExpFunctionCall(first, e0, list);
				
			}
		}

		if(tableLookupFlag) {
			varCaseExp = last;
		}
		return e0;
	}


	public List args() throws Exception {
		Token first = t;
		List<Exp> expList = new ArrayList<Exp>();
		if(isKind(LPAREN)) {
			consume();
			if(checkIfExpFirst(t)) {
				expList.add(exp());
				while(isKind(COMMA)) {
					consume();
					expList.add(exp());
				}
				match(RPAREN);
			}
			
		}else if(isKind(LCURLY)) {
			expList.add(expTableConstructor()); 
		}else {
			consume();
			expList.add(new ExpString(first));
		}
		return expList;
	}


	public boolean checkIfPrefixTailFirst() {
		switch (this.t.kind) {
		case LSQUARE:
		case DOT:
		case COLON:
			return true;
		}
		
		if(checkIfArgsFirst())
			return true;
		
		return false;
	}


	public boolean checkIfArgsFirst() {
		switch (this.t.kind) {
		case LPAREN:
		case LCURLY:
		case STRINGLIT:
			return true;
		}
		
		return false;
	}


	public FuncName funcName() throws Exception {
		Token first = t;
		List nameList = new ArrayList<ExpName>();
		Token name = match(NAME);
		nameList.add(new ExpName(name.text));
		if((isKind(DOT)||(isKind(COLON)))){
			while(isKind(DOT)) {
				consume();
				name = match(NAME);
				nameList.add(new ExpName(name.text));
			}
			if(isKind(COLON)) {
				consume();
				name = match(NAME);
				return new FuncName(first, nameList, new ExpName(name.text));
			}else {
				return new FuncName(first, nameList, null);
			}
			
		}
		return new FuncName(first, new ExpName(name.text));
	}


	public List expList() throws Exception {
		List list = new ArrayList<Exp>();
		list.add(exp());
		while(isKind(COMMA)) {
			list.add(exp());
		}
		return list;
	}


	public boolean checkIfStatFirst(Token token) {
		switch (token.kind) {
		case SEMI:
		//case functionCall: to implement
		case COLONCOLON:
		case KW_break:
		case KW_goto:
		case DOTDOTDOT:
		case KW_do:
		case KW_while:
		case KW_repeat:
		case KW_if:
		case KW_for:
		case KW_function:
		case KW_local:
			return true;
		}
		
		if(checkIfVarFirst(token))
			return true;
		
		return false;
	}

	public boolean checkIfVarFirst(Token token) {
		switch (token.kind) {
		case NAME:
		case LPAREN:
			return true;
		}
		
		return false;
	}


	Exp exp() throws Exception {
		Token first = t;
		Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp andExp() throws Exception {
		Token first = t;
		Exp e0 = relationExp();
		while (isKind(KW_and)) {
			Token op = consume();
			Exp e1 = relationExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp relationExp() throws Exception {
		Token first = t;
		Exp e0 = bitOrExp();
		while ((isKind(REL_LT)) || (isKind(REL_GT)) || (isKind(REL_LE)) || (isKind(REL_GE)) || (isKind(REL_NOTEQ)) || (isKind(REL_EQEQ))) {
			Token op = consume();
			Exp e1 = bitOrExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp bitOrExp() throws Exception {
		Token first = t;
		Exp e0 = bitXorExp();
		while (isKind(BIT_OR)) {
			Token op = consume();
			Exp e1 = bitXorExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp bitXorExp() throws Exception {
		Token first = t;
		Exp e0 = bitAmpExp();
		while (isKind(BIT_XOR)) {
			Token op = consume();
			Exp e1 = bitAmpExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp bitAmpExp() throws Exception {
		Token first = t;
		Exp e0 = bitShiftExp();
		while (isKind(BIT_AMP)) {
			Token op = consume();
			Exp e1 = bitShiftExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp bitShiftExp() throws Exception {
		Token first = t;
		Exp e0 = dotdotExp();
		while ((isKind(BIT_SHIFTL)) || (isKind(BIT_SHIFTR))) {
			Token op = consume();
			Exp e1 = dotdotExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp dotdotExp() throws Exception {
		Token first = t;
		Exp e0 = minusExp();
		while (isKind(DOTDOT)) {
			Token op = consume();
			Exp e1 = dotdotExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}

	Exp minusExp() throws Exception {
		Token first = t;
		Exp e0 = plusExp();
		while (isKind(OP_MINUS)) {
			Token op = consume();
			Exp e1 = plusExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp plusExp() throws Exception {
		Token first = t;
		Exp e0 = timesDivExp();
		while (isKind(OP_PLUS)) {
			Token op = consume();
			Exp e1 = timesDivExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp timesDivExp() throws Exception {
		Token first = t;
		Exp e0 = unaryExp();
		while ((isKind(OP_TIMES)) || (isKind(OP_DIV)) || (isKind(OP_DIVDIV)) || (isKind(OP_MOD))) {
			Token op = consume();
			Exp e1 = unaryExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp unaryExp() throws Exception {
		Token first = t;
		Exp e0 = null;
		if((isKind(KW_not)) || (isKind(OP_HASH)) || (isKind(OP_MINUS)) || (isKind(BIT_XOR))) {
			Token op = consume();
			e0 = unaryExp();
			e0 = new ExpUnary(first, op.kind, e0);
		}else {
			e0 = powExp();
			
		}
		return e0;
	}

	Exp powExp() throws Exception {
		Token first = t;
		Exp e0 = factorExp();
		while (isKind(OP_POW)) {
			Token op = consume();
			Exp e1 = powExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Exp factorExp() throws Exception {
		Token first = t;
		Exp e0 = null;
		if(isKind(KW_function)) {
			Token op = consume();
			FuncBody e1 = funcBodyExp();
			e0 = new ExpFunction(first, e1);
		}
		else if(isKind(KW_nil)) {
			Token t = match(KW_nil);
			e0 = new ExpNil(t);
		}else if(isKind(KW_true)) {
			Token t = match(KW_true);
			e0 = new ExpTrue(t);
		}else if(isKind(KW_false)) {
			Token t = match(KW_false);
			e0 = new ExpFalse(t);
		}else if(isKind(INTLIT)) {
			Token t = match(INTLIT);
			e0 = new ExpInt(t);
		}else if(isKind(STRINGLIT)) {
			Token t = match(STRINGLIT);
			e0 = new ExpString(t);
		}else if(isKind(DOTDOTDOT)) {
			Token t = match(DOTDOTDOT);
			e0 = new ExpVarArgs(t);
		}else if((isKind(NAME)) || (isKind(LPAREN))) {
			e0 = prefixExp();
		}else if(isKind(LCURLY)) {
			/*Tableconstructor*/
			e0 = expTableConstructor();
		}else {
			throw new SyntaxException(first, "");
		}
		return e0;
	}


	public Exp prefixExp() throws Exception {
		Token first = t;
		Exp e0 = null;
		Exp last = null;
		
		if(isKind(NAME)) {
			e0 = new ExpName(match(NAME));
		} else if(isKind(LPAREN)) {
			consume();
			e0 = exp();
			match(RPAREN);
		}
		
		while(checkIfPrefixTailFirst()) {
			if(isKind(LSQUARE)) {
				consume();
				last = exp();
				match(RSQUARE);
				e0 = new ExpTableLookup(first, e0, last);
				
			} else if(isKind(DOT)) {
				consume();
				//last = new ExpName(match(NAME));
				last = new ExpString(match(NAME));
				e0 = new ExpTableLookup(first, e0, last);
				
			} else if((isKind(LPAREN) || isKind(LCURLY) || isKind(STRINGLIT))) {
				e0 = new ExpFunctionCall(first, e0, args());
				
			} else if(isKind(COLON)) {
				consume();
				List<Exp> list = new ArrayList<Exp>();
				Token name1 = match(NAME);
				list.add(new ExpName(name1));
				list.addAll(args());
				e0 = new ExpFunctionCall(first, e0, list);
				
			}
		}
		return e0;
	}


	public Exp expTableConstructor() throws Exception {
		Token first = t;
		Exp e0;
		List fieldList = new ArrayList();
		consume();
		if(isKind(RCURLY)) {
			e0 =  new ExpTable(first, fieldList);
		}else {
			fieldList = fieldList();
			Token c2 = match(RCURLY);
			e0 =  new ExpTable(first, fieldList);
		}
		return e0;
	}

	public List<Field> fieldList() throws Exception {
		Token first = t;
		List<Field> list = new ArrayList<Field>();
		list.add(field());
		while((isKind(COMMA)) || (isKind(SEMI))) {
			consume();
			if(checkIfFieldFirst(this.t)) {
				Field field = field(); 
				list.add(field);	
			}else {
				break;
			}
		}
		
		return list;
	}

	public boolean checkIfFieldFirst(Token token) {
		switch (token.kind) {
		case NAME:
		case LSQUARE:
			return true;
		}
		if(checkIfExpFirst(token))
			return true;
		return false;
	}

	public boolean checkIfExpFirst(Token token) {
		switch (token.kind) {
		case KW_nil:
		case KW_false:
		case KW_true:
		case INTLIT:
		case STRINGLIT:
		case DOTDOTDOT:
		case KW_function:
		case NAME:
		case LPAREN:
		case LCURLY:
			return true;
		}
		return false;
	}

	public Field field() throws Exception {
		Token first = t;
		if(isKind(LSQUARE)) {
			Token ls = consume();
			Exp key = null;
			if(checkIfExpFirst(this.t)) {
				key = exp();
			}else {
				error(t.kind);
				return null;
			}
			Token rs = match(RSQUARE);
			Token eq = match(ASSIGN);
			Exp value = null;
			if(checkIfExpFirst(this.t)) {
				value = exp();
			}else {
				error(t.kind);
				return null;
			}
			return new FieldExpKey(first, key, value);
			
		}else if(isKind(NAME)){
			/*Token t = match(NAME);
			e0 = new ExpName(t);*/
			Exp name = new ExpName(match(NAME));
			if(isKind(ASSIGN)) {
				Token eq = match(ASSIGN);
				Exp value = null;
				if(checkIfExpFirst(this.t)) {
					value = exp();
				}else {
					error(t.kind);
				}
				return new FieldExpKey(first, name, value);
			} else {
				return new FieldImplicitKey(first, name);
			}
		}else if(checkIfExpFirst(this.t)) {
				return new FieldImplicitKey(first, exp());
		}else {
			error(t.kind);
			return null;
		}
	}

	public FuncBody funcBodyExp() throws Exception {
		Token first = t;
		match(LPAREN);
		ParList listPar = parListExp();
		match(RPAREN);
		Block block = block();
		Token end = match(KW_end);
		return new FuncBody(first, listPar, block);
	}

	public ParList parListExp() throws Exception {
		Token first = t;
		List<Name> list = new ArrayList<Name>();
		if(isKind(NAME)) {
			list = nameList();
			boolean hasVarArgs = false;
			if (isKind(DOTDOTDOT)) {
				Token ddd = consume();
				hasVarArgs = true;
				return new ParList(first, list, hasVarArgs);
			}else {
				return new ParList(first, list, hasVarArgs);
			}
		}else if (isKind(DOTDOTDOT)) {
			Token ddd = consume();
			return new ParList(first, list, true);
		} else {
			return new ParList(first, list, false);
		}
	}

	public List nameList() throws Exception {
		Token first = t;
		List<Name> list = new ArrayList<Name>();
		Token op = match(NAME);
		list.add(new Name(first, op.getName()));
		while(isKind(COMMA)) {
			consume();
			if(isKind(NAME)) {
				Token newName = consume();
				list.add(new Name(first, newName.getName()));
			}else if(isKind(DOTDOTDOT)){
				break;
			}else {
				error(t.kind);
			}
		}
		return list;
	}

	private Exp exampleExp() throws Exception{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
	}



	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind kind) throws Exception {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(kind);
		return null; // unreachable
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind... kinds) throws Exception {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		error(kinds);
		return null; // unreachable
	}

	Token consume() throws Exception {
		Token tmp = t;
		t = scanner.getNext();
		return tmp;
	}

	void error(Kind... expectedKinds) throws SyntaxException {
		String kinds = Arrays.toString(expectedKinds);
		String message;
		if (expectedKinds.length == 1) {
			message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
		} else {
			message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
		}
		throw new SyntaxException(t, message);
	}

	void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line + ":" + t.pos;
		throw new SyntaxException(t, message);
	}



}
