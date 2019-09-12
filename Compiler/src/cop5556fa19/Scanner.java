

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
		START, HAVE_EQ, IN_NUMLIT, IN_IDENT
	}
	Reader r;

	public Scanner(Reader r) throws IOException {
		this.r = r;
	}


	int currPos;
	int currLine;

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
		StringBuilder sb;//for token text
		int pos = -1;
		int line = -1;
		State state = State.START;
		//ch = r.read();
	

		while (t==null) {
			switch (state) {
			case START: {
				// skip white space
				//some sort of for loop
				pos = currPos;
				line = currLine;
				switch (ch) {
				case '+': {t = new Token(OP_PLUS, "+", pos, line);getChar();}break;
				case '*': {t = new Token(OP_TIMES, "*", pos, line);getChar();}break;        
				case '=': {state = State.HAVE_EQ; getChar();}break;
				case '0': {t = new Token(NUM_LIT,"0",pos,line);getChar();}break;
				case  -1: {t = new Token(EOF, "EOF", pos, line); break;}
				default: {
					if (Character.isDigit(ch)) {
						state = State.IN_DIGIT; 		
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
					else { error(….);  }
				}
				} // switch (ch)
			} break;      // case START
			case IN_NUMLIT: { ... }  break;
			case HAVE_EQ: {...} break;
			case IN_IDENT: {	
				if (Character.isJavaIdentifierPart(ch)) {
					sb.append((char)ch);
					getChar();
				} else {
					//we are done building the ident.  Create Token
					//if we had keywords, we would check for that here
					t = new Token(Ident,sb.toString(), pos, line));
				}
			}break;

			default error(...);
			}//switch(state)
		} //while
		//return t;


		//replace this code.  Just for illustration
		if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
		throw new LexicalException("Useful error message");
	}

}
