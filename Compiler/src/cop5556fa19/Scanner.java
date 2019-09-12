

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
		START, HAVE_EQ, HAVE_REL_GT, HAVE_REL_LT, IN_NUMLIT, IN_IDENT
	}
	Reader r;

	public Scanner(Reader r) throws IOException {
		this.r = r;
	}


	int currPos;
	int currLine;
	int nextchar = -2;
	int ch;

	void getChar() throws IOException {
		//read next char
		//update currPos and currLine
	}

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}



	public Token getNext() throws Exception {
		Token t = null;
		StringBuilder sb = null;//for token text
		int pos = -1;
		int line = -1;
		State state = State.START;
		if(nextchar == -2) {
			ch = r.read();			
		}
		else {
			ch = nextchar;
			nextchar = -2;
		}


		while (t==null) {
			switch (state) {
			case START: {
				// skip white space
				//some sort of for loop
				pos = currPos;
				line = currLine;
				switch (ch) {
				case '+': {t = new Token(OP_PLUS, "+", pos, line);getChar();}break;
				case '-': {t = new Token(OP_MINUS, "-", pos, line);getChar();}break;
				case '*': {t = new Token(OP_TIMES, "*", pos, line);getChar();}break;
				//case '/': {state = State.HAVE_EQ; getChar();}break; //OP_DIV
				case '%': {t = new Token(OP_MOD, "%", pos, line);getChar();}break;
				case '^': {t = new Token(OP_POW, "^", pos, line);getChar();}break;
				case '#': {t = new Token(OP_HASH, "#", pos, line);getChar();}break;
				case '&': {t = new Token(BIT_AMP, "&", pos, line);getChar();}break;
				//case '~': {state = State.HAVE_EQ; getChar();}break; //BIT_XOR
				case '|': {t = new Token(BIT_OR, "|", pos, line);getChar();}break;
				//case '<': {state = State.HAVE_EQ; getChar();}break; //REL_LT
				//case '>': {state = State.HAVE_EQ; getChar();}break; //REL_GT
				//case '=': {state = State.HAVE_EQ; getChar();}break;
				case '(': {t = new Token(LPAREN, "(", pos, line);getChar();}break;
				case ')': {t = new Token(RPAREN, ")", pos, line);getChar();}break;
				case '{': {t = new Token(LCURLY, "{", pos, line);getChar();}break;
				case '}': {t = new Token(RCURLY, "}", pos, line);getChar();}break;
				case '[': {t = new Token(LSQUARE, "[", pos, line);getChar();}break;
				case ']': {t = new Token(RSQUARE, "]", pos, line);getChar();}break;
				//case ':': {state = State.HAVE_EQ; getChar();}break;COLON
				case ';': {t = new Token(SEMI, ";", pos, line);getChar();}break;
				case ',': {t = new Token(COMMA, ",", pos, line);getChar();}break;
				//case '.': {state = State.HAVE_EQ; getChar();}break;//DOT

				case '0': {t = new Token(INTLIT,"0",pos,line);getChar();}break;
				case  -1: {t = new Token(EOF, "EOF", pos, line); 
				//throw new LexicalException("Useful error message");
				break;
				}
				default: {
					if (Character.isDigit(ch)) {
						state = State.IN_NUMLIT;
						sb = new StringBuilder();
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

			case HAVE_EQ: {

			} break;
			case IN_NUMLIT: {  }  break;
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


		//replace this code.  Just for illustration
		/*
		 * if (r.read() == -1) { return new Token(EOF,"eof",0,0);} throw new
		 * LexicalException("Useful error message");
		 */
	}

}
