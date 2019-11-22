package interpreter;

import java.util.ArrayList;
import java.util.List;

public class LuaList extends LuaValue {
	ArrayList<LuaValue> list = new ArrayList<LuaValue>();
	public LuaList(List<LuaValue> list) {
		this.list = (ArrayList<LuaValue>) list;
	}
	@Override
	public LuaValue copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
