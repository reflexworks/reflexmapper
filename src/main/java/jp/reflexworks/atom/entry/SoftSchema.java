package jp.reflexworks.atom.entry;

import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;
import jp.reflexworks.atom.mapper.CipherContext;

public interface SoftSchema {
	
	/**
	 * オブジェクトから項目の値を取得
	 * 
	 * @param fldname
	 * @return 項目の値
	 */
	public Object getValue(String fldname);
	
	/**
	 * 暗号化対象の項目を暗号化
	 * 
	 * @param id
	 * @param cipher
	 * @param secretkey
	 */
//	public void encrypt(String id, Object cipher, String secretkey);
	public void encrypt(CipherContext context);
	
	/**
	 * 暗号化対象の項目を複合
	 * 
	 * @param id
	 * @param cipher
	 * @param secretkey
	 */
//	public void decrypt(String id, Object cipher, String secretkey);
	public void decrypt(CipherContext context);
	
	/**
	 * 検索条件に合致するか調べる
	 * 
	 * @param context
	 */
	public void isMatch(ConditionContext context);
	
	/**
	 * 項目のバリデーションを行う
	 * 
	 * @param uid
	 * @param groups
	 * @param myself
	 * @return validであればtrue
	 * @throws java.text.ParseException
	 */
	public boolean validate(String uid, List<String> groups, String myself) 
			throws java.text.ParseException;
	
	/**
	 * 項目の値をマスクする
	 * 
	 * @param uid
	 * @param groups
	 * @param myself
	 */
	public void maskprop(String uid, List<String> groups, String myself);

}
