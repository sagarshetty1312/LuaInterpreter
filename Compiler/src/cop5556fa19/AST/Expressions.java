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

package cop5556fa19.AST;

import java.util.List;

import cop5556fa19.Token;
import cop5556fa19.Token.Kind;

public class Expressions {
	
	static public ExpBinary makeBinary(int v0, Kind op, int v1) {
		Exp e0 = makeInt(v0);
		Token first = e0.firstToken;
		Token eop = new Token(op, op.toString(),0,0);
		Exp e1 = makeInt(v1);
		return new ExpBinary(first,e0,eop,e1);
	}

	static public ExpInt makeInt(int v0) {
		Token t = new Token(Kind.INTLIT, Integer.toString(v0),0,0);
		return new ExpInt(t);
	}

	static public ExpString makeExpString(String s) {
		Token t = new Token(Kind.STRINGLIT, '"' + s + '"', 0,0);
		return new ExpString(t);
	}

	public static Exp makeBinary(String s0, Kind op, String s1) {
		Exp es0 = makeExpString(s0);
		Exp es1 = makeExpString(s1);
		Token eop = new Token(op,op.toString(),0,0);
		return new ExpBinary(es0.firstToken,es0,eop,es1);
	}
	
	public static Exp makeBinary(Exp e0, Kind op, Exp e1) {
		Token eop = new Token(op,op.toString(),0,0);
		return new ExpBinary(e0.firstToken, e0, eop, e1);
	}
	
	public static ExpUnary makeExpUnary(Kind op, int i) {
		Token first = new Token(op,op.toString(), 0,0);
		Exp e = makeInt(i);
		return new ExpUnary(first,op,e);
	}

	
	public static ExpUnary makeExpUnary(Kind op, Exp e) {
		Token first = new Token(op,op.toString(), 0,0);
		return new ExpUnary(first,op,e);
	}

	public static Block makeBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object makeExpName(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<Exp> makeExpList(Object makeExpName) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<Exp> makeExpList(Object makeExpName, Object makeExpName2) {
		// TODO Auto-generated method stub
		return null;
	}

	public static StatAssign makeStatAssign(List<Exp> lhs, List<Exp> rhs) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Block makeBlock(StatAssign s) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Exp makeExpInt(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Exp makeExpFunCall(Object makeExpName, List<Exp> args, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Block makeBlock(Stat s) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Exp makeExpTableLookup(ExpName g, ExpString a) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Exp makeExpTableLookup(Exp gtable, ExpString b) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat makeStatAssign(Exp v, Exp e) {
		// TODO Auto-generated method stub
		return null;
	}

	public static StatLabel makeStatLabel(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat makeStatGoto(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static StatBreak makeStatBreak() {
		// TODO Auto-generated method stub
		return null;
	}

	public static StatDo makeStatDo(Stat s2, Stat s3, StatAssign s4) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Block makeBlock(Stat s0, StatLabel s1, StatDo statdo, StatBreak statBreak) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
