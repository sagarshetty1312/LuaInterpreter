/* *
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

import static cop5556fa19.Token.Kind.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

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
import cop5556fa19.AST.Expressions;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.ParList;
import cop5556fa19.ExpressionParser.SyntaxException;

class ExpressionParserTest {

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	
	// creates a scanner, parser, and parses the input.  
	Exp parseAndShow(String input) throws Exception {
		show("parser input:\n" + input); // Display the input
		Reader r = new StringReader(input);
		Scanner scanner = new Scanner(r, false); // Create a Scanner and initialize it
		ExpressionParser parser = new ExpressionParser(scanner);  // Create a parser
		Exp e = parser.exp(); // Parse and expression
		show("e=" + e);  //Show the resulting AST
		return e;
	}
	


	@Test
	void testIdent0() throws Exception {
		String input = "x";
		Exp e = parseAndShow(input);
		assertEquals(ExpName.class, e.getClass());
		assertEquals("x", ((ExpName) e).name);
	}

	@Test
	void testIdent1() throws Exception {
		String input = "(x)";
		Exp e = parseAndShow(input);
		assertEquals(ExpName.class, e.getClass());
		assertEquals("x", ((ExpName) e).name);
	}

	@Test
	void testString() throws Exception {
		String input = "\"string\"";
		Exp e = parseAndShow(input);
		assertEquals(ExpString.class, e.getClass());
		assertEquals("string", ((ExpString) e).v);
	}

	@Test
	void testBoolean0() throws Exception {
		String input = "true";
		Exp e = parseAndShow(input);
		assertEquals(ExpTrue.class, e.getClass());
	}

	@Test
	void testBoolean1() throws Exception {
		String input = "false";
		Exp e = parseAndShow(input);
		assertEquals(ExpFalse.class, e.getClass());
	}


	@Test
	void testBinary0() throws Exception {
		String input = "1 + 2";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(1,OP_PLUS,2);
		show("expected="+expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testUnary0() throws Exception {
		String input = "-2";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeExpUnary(OP_MINUS, 2);
		show("expected="+expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testUnary1() throws Exception {
		String input = "-*2\n";
		assertThrows(SyntaxException.class, () -> {
		Exp e = parseAndShow(input);
		});	
	}
	

	
	@Test
	void testRightAssoc() throws Exception {
		String input = "\"concat\" .. \"is\"..\"right associative\"";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(
				Expressions.makeExpString("concat")
				, DOTDOT
				, Expressions.makeBinary("is",DOTDOT,"right associative"));
		show("expected=" + expected);
		assertEquals(expected,e);
	}
	
	@Test
	void testLeftAssoc() throws Exception {
		String input = "\"minus\" - \"is\" - \"left associative\"";
		Exp e = parseAndShow(input);
		Exp expected = Expressions.makeBinary(
				Expressions.makeBinary(
						Expressions.makeExpString("minus")
				, OP_MINUS
				, Expressions.makeExpString("is")), OP_MINUS, 
				Expressions.makeExpString("left associative"));
		show("expected=" + expected);
		assertEquals(expected,e);
		
	}
	
	@Test
	void testFailed0() throws Exception {
		String input = "a+b*c+d";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpName [name=a, firstToken=Token [kind=NAME, text=a, pos=2, line=1]], op=OP_PLUS, e1=ExpBinary [e0=ExpName [name=b, firstToken=Token [kind=NAME, text=b, pos=4, line=1]], op=OP_TIMES, e1=ExpName [name=c, firstToken=Token [kind=NAME, text=c, pos=6, line=1]]]], op=OP_PLUS, e1=ExpName [name=d, firstToken=Token [kind=NAME, text=d, pos=8, line=1]]]";
		//System.out.println("TEST0:" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed1() throws Exception {
		String input = " nil ";
		Exp e = parseAndShow(input);
		String expected = "ExpNil [firstToken=Token [kind=KW_nil, text=nil, pos=5, line=1]]";
		//System.out.println("TEST1:" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed2() throws Exception {
		String input = "123 + (456 - 789) - 101112";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpInt [v=123, firstToken=Token [kind=INTLIT, text=123, pos=1, line=1]], op=OP_PLUS, e1=ExpBinary [e0=ExpInt [v=456, firstToken=Token [kind=INTLIT, text=456, pos=1, line=1]], op=OP_MINUS, e1=ExpInt [v=789, firstToken=Token [kind=INTLIT, text=789, pos=1, line=1]]]], op=OP_MINUS, e1=ExpInt [v=101112, firstToken=Token [kind=INTLIT, text=101112, pos=1, line=1]]]";
		//System.out.println("TEST2:" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed3() throws Exception {
		String input = "123 or 456 and 789 or 1011";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpInt [v=123, firstToken=Token [kind=INTLIT, text=123, pos=1, line=1]], op=KW_or, e1=ExpBinary [e0=ExpInt [v=456, firstToken=Token [kind=INTLIT, text=456, pos=1, line=1]], op=KW_and, e1=ExpInt [v=789, firstToken=Token [kind=INTLIT, text=789, pos=1, line=1]]]], op=KW_or, e1=ExpInt [v=1011, firstToken=Token [kind=INTLIT, text=1011, pos=1, line=1]]]";
		//System.out.println("TEST3:" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed4() throws Exception {
		String input = "1 ^ (4 + 2) * 3";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpInt [v=1, firstToken=Token [kind=INTLIT, text=1, pos=1, line=1]], op=OP_POW, e1=ExpBinary [e0=ExpInt [v=4, firstToken=Token [kind=INTLIT, text=4, pos=1, line=1]], op=OP_PLUS, e1=ExpInt [v=2, firstToken=Token [kind=INTLIT, text=2, pos=1, line=1]]]], op=OP_TIMES, e1=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=1, line=1]]]";
		//System.out.println("TEST4:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed5() throws Exception {
		String input = "1 + 2/3 >> 4^5";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpInt [v=1, firstToken=Token [kind=INTLIT, text=1, pos=1, line=1]], op=OP_PLUS, e1=ExpBinary [e0=ExpInt [v=2, firstToken=Token [kind=INTLIT, text=2, pos=1, line=1]], op=OP_DIV, e1=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=1, line=1]]]], op=BIT_SHIFTR, e1=ExpBinary [e0=ExpInt [v=4, firstToken=Token [kind=INTLIT, text=4, pos=1, line=1]], op=OP_POW, e1=ExpInt [v=5, firstToken=Token [kind=INTLIT, text=5, pos=1, line=1]]]]";
		//System.out.println("TEST5:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed6() throws Exception {
		String input = "1 ~ 2 | 3 & 4";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpInt [v=1, firstToken=Token [kind=INTLIT, text=1, pos=1, line=1]], op=BIT_XOR, e1=ExpInt [v=2, firstToken=Token [kind=INTLIT, text=2, pos=1, line=1]]], op=BIT_OR, e1=ExpBinary [e0=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=1, line=1]], op=BIT_AMP, e1=ExpInt [v=4, firstToken=Token [kind=INTLIT, text=4, pos=1, line=1]]]]";
		//System.out.println("TEST6:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	//@Test
	void testFailed7() throws Exception {
		String input = "(1 * 2) / (3 % 4) // 5";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpBinary [e0=ExpInt [v=1, firstToken=Token [kind=INTLIT, text=1, pos=1, line=1]], op=OP_TIMES, e1=ExpInt [v=2, firstToken=Token [kind=INTLIT, text=2, pos=1, line=1]]], op=OP_DIV, e1=ExpBinary [e0=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=1, line=1]], op=OP_MOD, e1=ExpInt [v=4, firstToken=Token [kind=INTLIT, text=4, pos=1, line=1]]]], op=OP_DIVDIV, e1=ExpInt [v=5, firstToken=Token [kind=INTLIT, text=5, pos=1, line=1]]]";
		System.out.println("TEST7:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed8() throws Exception {
		String input = "function (aa, b) end >> function(test, l, ...) end ";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=aa, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]], Name [name=b, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]]], hasVarArgs=false, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=KW_function, text=function, pos=9, line=1]], op=BIT_SHIFTR, e1=FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=test, firstToken=Token [kind=NAME, text=test, pos=38, line=1]], Name [name=l, firstToken=Token [kind=NAME, text=test, pos=38, line=1]]], hasVarArgs=true, firstToken=Token [kind=NAME, text=test, pos=38, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=33, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=33, line=1]], firstToken=Token [kind=KW_function, text=function, pos=33, line=1]]]";
		//System.out.println("TEST8:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed9() throws Exception {
		String input = "function (aa, b) end >> function(test, l, ...) end & function(...) end";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=aa, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]], Name [name=b, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]]], hasVarArgs=false, firstToken=Token [kind=NAME, text=aa, pos=13, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=KW_function, text=function, pos=9, line=1]], op=BIT_SHIFTR, e1=FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=test, firstToken=Token [kind=NAME, text=test, pos=38, line=1]], Name [name=l, firstToken=Token [kind=NAME, text=test, pos=38, line=1]]], hasVarArgs=true, firstToken=Token [kind=NAME, text=test, pos=38, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=33, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=33, line=1]], firstToken=Token [kind=KW_function, text=function, pos=33, line=1]]], op=BIT_AMP, e1=FuncDec [body=FuncBody [p=ParList [nameList=null, hasVarArgs=true, firstToken=Token [kind=DOTDOTDOT, text=..., pos=65, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=62, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=62, line=1]], firstToken=Token [kind=KW_function, text=function, pos=62, line=1]]]";
		//System.out.println("TEST9:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed10() throws Exception {
		String input = "{}";
		Exp e = parseAndShow(input);
		String expected = "ExpTable [fields=null, firstToken=Token [kind=LCURLY, text={, pos=1, line=1]]";
		//System.out.println("TEST10:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed11() throws Exception {
		String input = "{3}";
		Exp e = parseAndShow(input);
		String expected = "ExpTable [fields=[FieldImplicitKey [exp=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=3, line=1]], firstToken=Token [kind=INTLIT, text=3, pos=3, line=1]]], firstToken=Token [kind=LCURLY, text={, pos=1, line=1]]";
		//System.out.println("TEST11:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed12() throws Exception {
		String input = "{3, a}";
		assertThrows(SyntaxException.class, ()->{
			parseAndShow(input);
	        });
	}
	
	@Test
	void testFailed13() throws Exception {
		String input = "{[3]=a}";
		Exp e = parseAndShow(input);
		String expected = "ExpTable [fields=[FieldExpKey [key=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=4, line=1]], value=ExpName [name=a, firstToken=Token [kind=NAME, text=a, pos=7, line=1]], firstToken=Token [kind=LSQUARE, text=[, pos=2, line=1]]], firstToken=Token [kind=LCURLY, text={, pos=1, line=1]]";
		//System.out.println("TEST13:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed14() throws Exception {
		String input = "{[x + y] = xx * yy,}";
		Exp e = parseAndShow(input);
		String expected = "ExpBinary [e0=ExpBinary [e0=ExpBinary [e0=ExpInt [v=1, firstToken=Token [kind=INTLIT, text=1, pos=1, line=1]], op=OP_TIMES, e1=ExpInt [v=2, firstToken=Token [kind=INTLIT, text=2, pos=1, line=1]]], op=OP_DIV, e1=ExpBinary [e0=ExpInt [v=3, firstToken=Token [kind=INTLIT, text=3, pos=1, line=1]], op=OP_MOD, e1=ExpInt [v=4, firstToken=Token [kind=INTLIT, text=4, pos=1, line=1]]]], op=OP_DIVDIV, e1=ExpInt [v=5, firstToken=Token [kind=INTLIT, text=5, pos=1, line=1]]]";
		System.out.println("TEST14:\n" + e.toString());
		//assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed15() throws Exception {
		String input = "function (a,b) end";
		Exp e = parseAndShow(input);
		String expected = "FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=a, firstToken=Token [kind=NAME, text=a, pos=12, line=1]], Name [name=b, firstToken=Token [kind=NAME, text=a, pos=12, line=1]]], hasVarArgs=false, firstToken=Token [kind=NAME, text=a, pos=12, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=KW_function, text=function, pos=9, line=1]]";
		//System.out.println("TEST15:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed16() throws Exception {
		String input = "function (...) end";
		Exp e = parseAndShow(input);
		String expected = "FuncDec [body=FuncBody [p=ParList [nameList=null, hasVarArgs=true, firstToken=Token [kind=DOTDOTDOT, text=..., pos=13, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=KW_function, text=function, pos=9, line=1]]";
		//System.out.println("TEST16:\n" + e.toString());
		assertEquals(expected,e.toString());
	}
	
	@Test
	void testFailed17() throws Exception {
		String input = "function (xy,zy, ...) end";
		Exp e = parseAndShow(input);
		String expected = "FuncDec [body=FuncBody [p=ParList [nameList=[Name [name=xy, firstToken=Token [kind=NAME, text=xy, pos=13, line=1]], Name [name=zy, firstToken=Token [kind=NAME, text=xy, pos=13, line=1]]], hasVarArgs=true, firstToken=Token [kind=NAME, text=xy, pos=13, line=1]], b=Block [firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=LPAREN, text=(, pos=9, line=1]], firstToken=Token [kind=KW_function, text=function, pos=9, line=1]]";
		//System.out.println("TEST17:\n" + e.toString());
		assertEquals(expected,e.toString());
	}

}
