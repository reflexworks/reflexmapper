package jp.reflexworks.atom.wrapper;

import jp.reflexworks.atom.wrapper.base.ConditionBase;

public class Condition extends ConditionBase {
	
	public boolean result;
	
	public Condition(String cond) {
		super(cond);
//		throw new IllegalStateException();
	}
	
	public Condition(String prop, String value) {
		super(prop, value);
		result = false;
//		throw new IllegalStateException();
	}

	public String getCondition() {
		throw new IllegalStateException();
	}

}
