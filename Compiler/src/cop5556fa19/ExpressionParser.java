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

import cop5556fa19.AST.Block;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;
import static org.junit.Assert.assertNotNull;

public class ExpressionParser {

	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}

	final Scanner scanner;
	Token t;  //invariant:  this is the next token


	ExpressionParser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
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
		if(isKind(DOTDOT)) {
			Token op = null;
			Exp e1 = null;
			while (isKind(DOTDOT)) {
				op = consume();
				e1 = minusExp();
				e1 = new ExpBinary(first, e0, op, e1);
			}
			return new ExpBinary(first, e0, op, e1);
		}else {
			return e0;
		}
	}

	Exp minusExp() throws Exception {
		Token first = t;
		Exp e0 = plusExp();
		if(isKind(OP_MINUS)) {
			while (isKind(OP_MINUS)) {
				Token op = consume();
				Exp e1 = plusExp();
				e0 = new ExpBinary(first, e0, op, e1);
			}
		}else {
			//e0 = plusExp();
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
			e0 = powExp();
			e0 = new ExpUnary(first, op, e0);
		}else {
			e0 = powExp();
			
		}
		return e0;
	}

	Exp powExp() throws Exception {
		Token first = t;
		Exp e0 = factorExp();
		if(isKind(OP_POW)) {
			while (isKind(OP_POW)) {
				Token op = consume();
				Exp e1 = factorExp();
				e0 = new ExpBinary(first, e0, op, e1);
			}	
		}else {
			
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
		}else if(isKind(NAME)) {//prefix exp
			Token t = match(NAME);
			e0 = new ExpName(t);
		}else if(isKind(LPAREN)) {
			Token lp = consume();
			e0 = exp();
			Token rp = match(RPAREN);
			//Prefix Exp??
			
		}else if(isKind(LCURLY)) {
			/*Tableconstructor*/
			Token c1 = consume();
			List fieldList = fieldList();
			Token c2 = match(RCURLY);
			return new ExpTable(first, fieldList);
		}else {
			throw new SyntaxException(first, "");
		}
		return e0;
	}

	private List<Field> fieldList() throws Exception {
		Token first = t;
		List<Field> list = new ArrayList<Field>();
		list.add(field());
		while((isKind(COMMA)) || (isKind(SEMI))) {
			consume();
			Field field = field(); 
			list.add(field);
		}
		
		return list;
	}

	public Field field() throws Exception {
		Token first = t;
		if(isKind(LSQUARE)) {
			Token ls = consume();
			Exp key = exp();
			Token rs = match(RSQUARE);
			Token eq = match(ASSIGN);
			Exp value = exp();
			return new FieldExpKey(first, key, value);
			
		}else if(isKind(NAME)){
			Exp name = new ExpName(match(NAME));
			Token eq = match(ASSIGN);
			Exp value = exp();
			return new FieldExpKey(first, name, value);
		}else
			return new FieldImplicitKey(first, exp());
	}

	public FuncBody funcBodyExp() throws Exception {
		Token first = t;
		Token lp = match(LPAREN);
		ParList e0 = parListExp();
		Token rp = match(RPAREN);
		Token end = match(KW_end);
		FuncBody e1 = new FuncBody(first, e0, new Block(first));
		return e1;
	}

	public ParList parListExp() throws Exception {
		Token first = t;
		if(isKind(NAME)) {
			List e0 = nameList();
			boolean hasVarArgs = false;
			if (isKind(DOTDOTDOT)) {
				Token ddd = consume();
				hasVarArgs = true;
				return new ParList(first, e0, hasVarArgs);
			}else {
				return new ParList(first, e0, hasVarArgs);
			}
		}else if (isKind(DOTDOTDOT)) {
			Token ddd = consume();
			return new ParList(first, null, true);
		} else if (isKind(RPAREN)){
			return new ParList(first, null, false);
		} else {
			error(t.kind);
			return new ParList(first, null, false);
		}
	}

	public List nameList() throws Exception {
		Token first = t;
		List<Name> list = new ArrayList<Name>();
		if(isKind(NAME)) {
			Token op = consume();
			list.add(new Name(first, op.getName()));
			while(isKind(COMMA)) {
				match(COMMA);
				if(isKind(NAME)) {
					Token newName = consume();
					list.add(new Name(first, newName.getName()));
				}else if(isKind(DOTDOTDOT)){
					break;
				}else {
					error(t.kind);
				}
			}
		}
		return list;
	}

	private Exp exampleExp() throws Exception{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
	}
		


	

	private Block block() {
		return new Block(null);  //this is OK for Assignment 2
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
