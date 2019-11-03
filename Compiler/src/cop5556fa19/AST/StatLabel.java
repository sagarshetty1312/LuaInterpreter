package cop5556fa19.AST;

import cop5556fa19.Token;

public class StatLabel extends Stat {
	
	Name label;
	
	public StatLabel(Token firstToken, Name label) {
		super(firstToken);
		this.label = label;
	}

	@Override
	public String toString() {
		return "StatLabel [label=" + label + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		StatLabel other = (StatLabel) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitLabel(this,arg);
	}

}
