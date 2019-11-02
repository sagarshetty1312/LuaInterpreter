package cop5556fa19.AST;

import cop5556fa19.Token;
import java.util.List;

public class ExpFunctionCall extends Exp {
	
	public final Exp f;
	public final List<Exp> args;
	
	

	public ExpFunctionCall(Token firstToken, Exp f, List<Exp> args) {
		super(firstToken);
		this.f = f;
		this.args = args;
	}

	@Override
	public String toString() {
		return "ExpFunctionCall [f=" + f + ", args=" + args + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((f == null) ? 0 : f.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpFunctionCall other = (ExpFunctionCall) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (f == null) {
			if (other.f != null)
				return false;
		} else if (!f.equals(other.f))
			return false;
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitExpFunctionCall(this,arg);
	}

}
