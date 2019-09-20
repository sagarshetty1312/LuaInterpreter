

/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */
package cop5556fa19;


import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;


import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;


public class Scanner {

	public enum State {
		START, HAVE_MINUS, HAVE_EQ, HAVE_DIV, HAVE_XOR, HAVE_REL_LT, HAVE_REL_GT,
		HAVE_COLON, HAVE_DOT, IN_DOUBLE_QUOTES, IN_SINGLE_QUOTES, IN_NUMLIT, IN_IDENT, IN_COMMENT
	}
	Reader r;

	public Scanner(Reader r) throws IOException {
		this.r = r;
		currPos = -1;
		currLine = 0;
		ch = -1;
		prev = -1;
		getChar();
	}

	public Scanner(Reader r2, boolean b) throws IOException {
		this.r = r;
		currPos = -1;
		currLine = 0;
		ch = -1;
		prev = -1;
		getChar();
	}

	int currPos;
	int currLine;
	int ch;
	int prev;
	static HashMap<String, Kind> tokenMap = new HashMap<String, Kind>();
	static {
		tokenMap.put("and", KW_and);
		tokenMap.put("break", KW_break);
		tokenMap.put("do", KW_do);
		tokenMap.put("else", KW_else);
		tokenMap.put("elseif", KW_elseif);
		tokenMap.put("end", Kind.KW_end);
		tokenMap.put("false", KW_false);
		tokenMap.put("for", KW_for);
		tokenMap.put("function", KW_function);
		tokenMap.put("goto", KW_goto);
		tokenMap.put("if", KW_if);
		tokenMap.put("in", KW_in);
		tokenMap.put("local", KW_local);
		tokenMap.put("nil", KW_nil);
		tokenMap.put("not", KW_not);
		tokenMap.put("or", KW_or);
		tokenMap.put("repeat", KW_repeat);
		tokenMap.put("return", KW_return);
		tokenMap.put("then", KW_then);
		tokenMap.put("true", KW_true);
		tokenMap.put("until", KW_until);
		tokenMap.put("while", KW_while);


	}
	/*
	Line feed ASCII Characters:
	LF ("\n"): 10
	CR ("\r"): 13
	CR LF ("\r\n"): 13 10
	Whitespace ASCII Character:
	SP (" "): 32
	HT ("\t"): 9
	FF ("\f"): 12
	 */
	void getChar() throws IOException {
		if((prev == 10)||(prev == 13))
			currPos = -1;
		prev = ch;
		ch = r.read();
		currPos++;
		if((ch == 10)||(ch == 13)){
			currLine++;
		}
		if((ch == 10) && (prev == 13)) {
			currLine--;
		}
	}

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}

	void skipBlanks()  throws IOException {
		//Skipping whitespace and line terminators
		while ((ch == 10) || (ch == 13) || (ch == 9) || (ch == 12) || (ch == 32)) {
			getChar();
		}
	}

	public Token getNext() throws Exception {
		Token t = null;
		StringBuilder sb = new StringBuilder("");//for token text
		int pos = -1;
		int line = -1;
		State state = State.START;


		while (t==null) {
			pos = currPos;
			line = currLine;

			switch (state) {
			case START: {

				skipBlanks();

				switch (ch) {
				case '+': {t = new Token(OP_PLUS, "+", pos, line);getChar();}break;
				case '-': {state = State.HAVE_MINUS; sb.append((char)ch); getChar();}break;
				case '*': {t = new Token(OP_TIMES, "*", pos, line);getChar();}break;
				case '/': {state = State.HAVE_DIV; getChar();}break;
				case '%': {t = new Token(OP_MOD, "%", pos, line);getChar();}break;
				case '^': {t = new Token(OP_POW, "^", pos, line);getChar();}break;
				case '#': {t = new Token(OP_HASH, "#", pos, line);getChar();}break;
				case '&': {t = new Token(BIT_AMP, "&", pos, line);getChar();}break;
				case '~': {state = State.HAVE_XOR; getChar();}break;
				case '|': {t = new Token(BIT_OR, "|", pos, line);getChar();}break;
				case '<': {state = State.HAVE_REL_LT; getChar();}break;
				case '>': {state = State.HAVE_REL_GT; getChar();}break;
				case '=': {state = State.HAVE_EQ; getChar();}break;
				case '(': {t = new Token(LPAREN, "(", pos, line);getChar();}break;
				case ')': {t = new Token(RPAREN, ")", pos, line);getChar();}break;
				case '{': {t = new Token(LCURLY, "{", pos, line);getChar();}break;
				case '}': {t = new Token(RCURLY, "}", pos, line);getChar();}break;
				case '[': {t = new Token(LSQUARE, "[", pos, line);getChar();}break;
				case ']': {t = new Token(RSQUARE, "]", pos, line);getChar();}break;
				case ':': {state = State.HAVE_COLON; getChar();}break;
				case ';': {t = new Token(SEMI, ";", pos, line);getChar();}break;
				case ',': {t = new Token(COMMA, ",", pos, line);getChar();}break;
				case '.': {state = State.HAVE_DOT; sb.append((char)ch); getChar();}break;
				case '"': {state = State.IN_DOUBLE_QUOTES; sb.append((char)ch); getChar();}break;
				case '\'': {state = State.IN_SINGLE_QUOTES; sb.append((char)ch); getChar();}break;
				case '0': {t = new Token(INTLIT,"0",pos,line);getChar();}break;
				case  -1: {t = new Token(EOF, "EOF", pos, line);break;
				}
				default: {
					if (Character.isDigit(ch)) {
						state = State.IN_NUMLIT;
						sb.append((char)ch);
						getChar();
					}
					else if (Character.isJavaIdentifierStart(ch)) {
						//Will tell us if it is a lower case letter, upper case letter, dollar sign or underscore
						state = State.IN_IDENT; //Corresponding part is Java part.
						sb = new StringBuilder();
						sb.append((char)ch);
						getChar();
					}
					else {if((ch == 10) || (ch == 13)) line--; throw new LexicalException("Invalid character at Line: "+line+" Pos: "+pos);  }
				}
				} // switch(ch)
			} break;      // case START

			case HAVE_MINUS: {
				if(ch == '-') {
					state = State.IN_COMMENT;
					sb.append((char)ch);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(OP_MINUS, "-", pos, line);
					sb = new StringBuilder("");
				}
			} break;

			case IN_COMMENT: {
				if((ch == -1) || (ch == 10) || (ch == 13)) {
					sb = new StringBuilder("");
					getChar();
					t = getNext();
				}else {
					sb.append((char)ch);
					getChar();
				}
			} break;

			case HAVE_DIV: {
				if(ch == '/') {
					t = new Token(OP_DIVDIV, "//", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(OP_DIV, "/", pos, line);
				}
			} break;

			case HAVE_XOR: {
				if(ch == '=') {
					t = new Token(REL_NOTEQ, "~=", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(BIT_XOR, "~", pos, line);
				}
			} break;

			case HAVE_REL_LT: {
				if(ch == '<') {
					t = new Token(BIT_SHIFTL, "<<", pos, line);
					getChar();
				}else if(ch == '=') {
					t = new Token(REL_LE, "<=", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(REL_LT, "<", pos, line);
				}
			} break;

			case HAVE_REL_GT: {
				if(ch == '>') {
					t = new Token(BIT_SHIFTR, ">>", pos, line);
					getChar();
				}else if(ch == '=') {
					t = new Token(REL_GE, ">=", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(REL_GT, ">", pos, line);
				}
			} break;

			case HAVE_EQ: {
				if(ch == '=') {
					t = new Token(REL_EQEQ, "==", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(ASSIGN, "=", pos, line);
				}
			} break;

			case HAVE_COLON: {
				if(ch == ':') {
					t = new Token(COLONCOLON, "::", pos, line);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					t = new Token(COLON, ":", pos, line);
				}
			} break;

			case HAVE_DOT: {
				if(ch == '.') {
					sb.append((char)ch);
					if(sb.toString().equals("...")) {t = new Token(DOTDOTDOT, "...", pos, line); sb = new StringBuilder("");}
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					switch (sb.toString()) {
					case "...":{t = new Token(DOTDOTDOT, "...", pos, line); sb = new StringBuilder("");} break;
					case "..": {t = new Token(DOTDOT, "..", pos, line); sb = new StringBuilder("");} break;
					default: {t = new Token(DOT, ".", pos, line); sb = new StringBuilder("");} break;
					}
				}
			} break;

			case IN_DOUBLE_QUOTES: {
				if((ch == 10) || (ch == 13)) line--;
				switch (ch) {
				case -1: throw new LexicalException("Could not find matching quote for \'\"\' at Line: "+line+" Pos: "+(pos-sb.length()));
				case 10: throw new LexicalException("Could not find matching quote for \'\"\' at Line: "+line+" Pos: "+(pos-sb.length()));
				case 13: throw new LexicalException("Could not find matching quote for \'\"\' at Line: "+line+" Pos: "+(pos-sb.length()));
				case '\\': {
					getChar();
					switch (ch) {
					case 'a': {sb.append((char)7); getChar();} break;
					case 'b': {sb.append((char)8); getChar();} break;
					case 'f': {sb.append((char)12); getChar();} break;
					case 'n': {sb.append((char)10); getChar();} break;
					case 'r': {sb.append((char)13); getChar();} break;
					case 't': {sb.append((char)9); getChar();} break;
					case 'v':  {sb.append((char)11); getChar();} break;
					case '\\': {sb.append((char)92); getChar();} break;
					case '\"': {sb.append((char)ch); getChar();} break;
					case '\'': {sb.append((char)ch); getChar();} break;
					default: throw new LexicalException("Invalid escape sequence at Line: "+line+" Pos: "+pos+
							" (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\'  \\\\ )");
					}
				} break;
				case '"': {
					sb.append((char)ch);
					t = new Token(STRINGLIT, sb.toString(), pos, line);
					sb = new StringBuilder("");
					getChar();
				} break;
				default: {sb.append((char)ch); getChar();} break;
				}
			} break; //case IN_DOUBLE_QUOTES

			case IN_SINGLE_QUOTES: {
				if((ch == 10) || (ch == 13)) line--;
				switch (ch) {
				case -1: throw new LexicalException("Could not find matching quote for \"\'\" at Line: "+line+" Pos: "+(pos-sb.length()));
				case 10: throw new LexicalException("Could not find matching quote for \"\'\" at Line: "+line+" Pos: "+(pos-sb.length()));
				case 13: throw new LexicalException("Could not find matching quote for \"\'\" at Line: "+line+" Pos: "+(pos-sb.length()));
				case '\\': { getChar();
				switch (ch) {
				case 'a': {sb.append((char)7); getChar();} break;
				case 'b': {sb.append((char)8); getChar();} break;
				case 'f': {sb.append((char)12); getChar();} break;
				case 'n': {sb.append((char)10); getChar();} break;
				case 'r': {sb.append((char)13); getChar();} break;
				case 't': {sb.append((char)9); getChar();} break;
				case 'v':  {sb.append((char)11); getChar();} break;
				case '\\': {sb.append((char)92); getChar();} break;
				case '\"': {sb.append((char)ch); getChar();} break;
				case '\'': {sb.append((char)ch); getChar();} break;
				default: throw new LexicalException("Invalid escape sequence at Line: "+line+" Pos: "+pos+
						" (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\'  \\\\ )");
				}
				} break;
				case '\'': {
					sb.append((char)ch);
					t = new Token(STRINGLIT, sb.toString(), pos, line);
					sb = new StringBuilder("");
					getChar();
				} break;
				default: {sb.append((char)ch); getChar();} break;
				}
			} break; //case IN_SINGLE_QUOTES

			case IN_NUMLIT: {
				if (Character.isDigit(ch)) {
					sb.append((char)ch);
					getChar();
				}else {
					if((ch == 10) || (ch == 13)) line--;
					try {
						Integer.parseInt(sb.toString());
						t = new Token(INTLIT, sb.toString(), pos, line); sb = new StringBuilder("");
					} catch (NumberFormatException e) {
						throw new LexicalException("Numlit out of range at Line: "+line+" Pos: "+(pos-sb.length()));
					}
				}
			}  break;

			case IN_IDENT: {
				if (Character.isJavaIdentifierPart(ch)) {
					sb.append((char)ch);
					getChar();
				} else {
					if((ch == 10) || (ch == 13)) line--;
					if(tokenMap.containsKey(sb.toString())) {
						t = new Token(tokenMap.get(sb.toString()),sb.toString(), pos, line);
					}else {
						t = new Token(NAME,sb.toString(), pos, line);
						sb = new StringBuilder("");
					}
				}
			}break;

			//default;
			}//switch(state)
		} //while
		return t;
	}

}
