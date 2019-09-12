

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


import static cop5556fa19.Token.Kind.*;


import java.io.IOException;
import java.io.Reader;

public class Scanner {

	public enum State {
		START, HAVE_MINUS, HAVE_EQ, HAVE_DIV, HAVE_XOR, HAVE_REL_LT, HAVE_REL_GT, HAVE_COLON, HAVE_DOT, IN_DOUBLE_QUOTES, IN_SINGLE_QUOTES, IN_NUMLIT, IN_IDENT, IN_COMMENT
	}
	Reader r;

	public Scanner(Reader r) throws IOException {
		this.r = r;
		getChar();
	}


	int currPos;
	int currLine;
	int nextchar = -2;
	int ch;

	void getChar() throws IOException {
		ch = r.read();
		currPos++;
		currLine++;
		//TBD!
		//update currPos and currLine after checking for line end
		//File completed
	}

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}



	public Token getNext() throws Exception {
		Token t = null;
		StringBuilder sb = new StringBuilder("");//for token text
		int pos = -1;
		int line = -1;
		State state = State.START;


		while (t==null) {
			pos = currPos; //Line and column calculation
			line = currLine;
			
			switch (state) {
			case START: {
				// skip white space
				//some sort of for loop
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
				case '.': {state = State.HAVE_DOT; sb.append("."); getChar();}break;
				case '"': {state = State.IN_DOUBLE_QUOTES; getChar();}break;//String literal
				case '\'': {state = State.IN_SINGLE_QUOTES; getChar();}break;//String literal

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
					else {throw new LexicalException("Invalid character at Line: "+line+" Pos: "+pos);  }
				}
				} // switch (ch)
			} break;      // case START
			
			case HAVE_MINUS: {
				if(ch == '-') {
					state = State.IN_COMMENT;
					sb.append((char)ch);
					getChar();
				}else {
					t = new Token(OP_MINUS, "-", pos, line);
					sb = new StringBuilder("");
				}
			} break;
			
			case IN_COMMENT: {
				if(ch == -1) {
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
					t = new Token(OP_DIV, "/", pos, line);
				}
			} break;
			
			case HAVE_XOR: {
				if(ch == '=') {
					t = new Token(REL_NOTEQ, "=~", pos, line);
					getChar();
				}else {
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
					t = new Token(REL_GT, ">", pos, line);
				}
			} break;
			
			case HAVE_EQ: {
				if(ch == '=') {
					t = new Token(REL_EQEQ, "==", pos, line);
					getChar();
				}else {
					t = new Token(ASSIGN, "=", pos, line);
				}
			} break;
			
			case HAVE_COLON: {
				if(ch == ':') {
					t = new Token(COLONCOLON, "::", pos, line);
					getChar();
				}else {
					t = new Token(COLON, ":", pos, line);
				}
			} break;
			
			case HAVE_DOT: {
				if(ch == '.') {
					sb.append((char)ch);
					getChar();
				}else {
					switch (sb.toString()) {
						case "...":{t = new Token(DOTDOTDOT, "...", pos, line); sb = new StringBuilder("");} break;
						case "..": {t = new Token(DOTDOT, "..", pos, line); sb = new StringBuilder("");} break;
						default: {t = new Token(DOT, ".", pos, line); sb = new StringBuilder("");} break;
					}
				}
			} break;
			
			case IN_DOUBLE_QUOTES: {
				switch (ch) {
					case -1: throw new LexicalException("Could not find matching quote for \'\"\' at Line: "+line+" Pos: "+pos);
					case '\\': { getChar();
						switch (ch) {
							case 'a': {sb.append((char)ch); getChar();} break;
							case 'b': {sb.append((char)ch); getChar();} break;
							case 'f': {sb.append((char)ch); getChar();} break;
							case 'n': {sb.append((char)ch); getChar();} break;
							case 'r': {sb.append((char)ch); getChar();} break;
							case 't': {sb.append((char)ch); getChar();} break;
							case 'v':  {sb.append((char)ch); getChar();} break;
							case '\\': {sb.append((char)ch); getChar();} break;
							case '"': {sb.append((char)ch); getChar();} break;
							case '\'': {sb.append((char)ch); getChar();} break;
							default: throw new LexicalException("Invalid escape sequence at Line: "+line+" Pos: "+pos+" (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\'  \\\\ )");
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
				switch (ch) {
					case -1: throw new LexicalException("Could not find matching quote for \"\'\" at Line: "+line+" Pos: "+(pos-sb.length()));
					case '\\': { getChar();
						switch (ch) {
							case 'a': {sb.append((char)ch); getChar();} break;
							case 'b': {sb.append((char)ch); getChar();} break;
							case 'f': {sb.append((char)ch); getChar();} break;
							case 'n': {sb.append((char)ch); getChar();} break;
							case 'r': {sb.append((char)ch); getChar();} break;
							case 't': {sb.append((char)ch); getChar();} break;
							case 'v':  {sb.append((char)ch); getChar();} break;
							case '\\': {sb.append((char)ch); getChar();} break;
							case '"': {sb.append((char)ch); getChar();} break;
							case '\'': {sb.append((char)ch); getChar();} break;
							default: throw new LexicalException("Invalid escape sequence at Line: "+line+" Pos: "+pos+" (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\'  \\\\ )");
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
				}else /*if(ch == -1)*/{
					t = new Token(INTLIT, sb.toString(), pos, line); sb = new StringBuilder("");
				} /*
					 * else { throw new
					 * LexicalException("Invalid number at Line: "+line+" Pos: "+pos); }
					 */
			}  break;
			
			case IN_IDENT: {
				if (Character.isJavaIdentifierPart(ch)) {
					sb.append((char)ch);
					getChar();
				} else {
					//we are done building the ident.  Create Token
					//if we had keywords, we would check for that here
					t = new Token(NAME,sb.toString(), pos, line);
				}
			}break;

			//default error(...);
			}//switch(state)
		} //while
		return t;
	}

}
