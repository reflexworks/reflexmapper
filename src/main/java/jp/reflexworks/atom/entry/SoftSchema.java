package jp.reflexworks.atom.entry;

import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;

public interface SoftSchema {
	
	public Object getValue(String fldname);
	public void encrypt(String id, Object cipher);
	public void decrypt(String id, Object cipher);
	public void isMatch(ConditionContext context);
	public boolean validate(String ucode, List<String> groups, String myself) 
			throws java.text.ParseException;
	public void maskprop(String ucode, List<String> groups, String myself);

}
