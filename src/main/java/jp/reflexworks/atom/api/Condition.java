package jp.reflexworks.atom.api;

import jp.sourceforge.reflex.util.StringUtils;

/**
 * 検索条件クラス
 */
public class Condition {
	
	/** 演算子 : equal (=) */
	public static final String EQUAL = "eq";
	/** 演算子 : greater than (>) */
	public static final String GREATER_THAN = "gt";
	/** 演算子 : greater than or equal (>=) */
	public static final String GREATER_THAN_OR_EQUAL = "ge";
	/** 演算子 : less than (<) */
	public static final String LESS_THAN = "lt";
	/** 演算子 : less than or equal (<=) */
	public static final String LESS_THAN_OR_EQUAL = "le";
	/** 演算子 : not equal (!=) */
	public static final String NOT_EQUAL = "ne";
	/** 演算子 : 正規表現 */
	public static final String REGEX = "rg";
	/** 演算子 : 前方一致 */
	public static final String FOWARD_MATCH = "fm";
	/** 演算子 : 後方一致 */
	public static final String BACKWARD_MATCH = "bm";

	// 互換性のためfinalにしない。(継承クラスで編集)
	/** 演算子の接続文字 */
	public static String DELIMITER = "-";
	
	//public static final String PREFIX_MATCHING  = "*";
	//public static final String PREFIX_MATCHING_END = "\ufffd";
	//public static final String PREFIX_MATCHING_SLASH  = PREFIX_MATCHING + "/";
	
	/** 項目名 */
	protected String prop;
	
	/** 等・不等式 */
	protected String equations;
	
	/** 値 (デコード済) */
	protected String value;
	
	// デコード前の入力値
	//protected String originalValue;
	
	// 前方一致かどうか
	//protected boolean isPrefixMatching;

	// isPrefixMatching(前方一致かどうか)は、後ろのlike検索。
	// 前にlike指定されているかどうかのフラグ
	//protected boolean isLikeForward;

	/**
	 * コンストラクタ
	 * @param cond 条件
	 */
	public Condition(String cond) {
		setCondition(cond);
	}

	/**
	 * コンストラクタ
	 * @param prop 項目
	 * @param value 値
	 */
	public Condition(String prop, String value) {
		setCondition(prop, value);
	}
	
	/**
	 * 条件の解析
	 * @param cond 条件
	 */
	protected void setCondition(String cond) {
		if (StringUtils.isBlank(cond)) {
			return;
		}
		
		int condLen = cond.length();
		int idxProp = cond.indexOf(DELIMITER);
		int idxFilter = -1;
		if (idxProp == 0) {
			return;	// 項目名が指定されていないので条件としない
		}
		if (idxProp == -1) {
			this.prop = cond;
			idxProp = condLen;
		} else {
			this.prop = cond.substring(0, idxProp);
			idxProp++;
		}
		
		if (idxProp >= condLen) {
			this.equations = EQUAL;
			idxFilter = condLen;
		} else {
			idxFilter = cond.indexOf(DELIMITER, idxProp);
			if (idxFilter == -1) {
				idxFilter = condLen;
			}
			this.equations = cond.substring(idxProp, idxFilter);
			idxFilter++;
		}
		
		if (idxFilter >= condLen) {
			//this.originalValue = "";
			this.value = "";
		} else {
			//String value = cond.substring(idxFilter);
			//if (value.endsWith(PREFIX_MATCHING)) {
			//	if (EQUAL.equals(this.equations)) {
			//		this.equations = GREATER_THAN_OR_EQUAL;
			//	}
			//	this.originalValue = value.substring(0, value.length() - 1);
			//	this.isPrefixMatching = true;
			//} else {
			//	this.originalValue = value;
			//}
			//setDecodeValue();
			this.value = cond.substring(idxFilter);
		}
	}
	
	protected void setCondition(String prop, String value) {
		if (value == null || "".equals(value)) {
			setCondition(prop);
		} else {
			this.prop = prop;
			//if (value.endsWith(PREFIX_MATCHING)) {
			//	this.equations = GREATER_THAN_OR_EQUAL;
			//	this.originalValue = value.substring(0, value.length() - 1);
			//	this.isPrefixMatching = true;
			//} else {
			//	this.equations = EQUAL;
			//	this.originalValue = value;
			//}
			//setDecodeValue();
			this.equations = EQUAL;
			this.value = value;
		}
	}
	
	//private void setDecodeValue() {
	//	if (this.originalValue != null) {
	//		this.value = URLDecoderPlus.urlDecode(this.originalValue);
	//	}
	//}

	public String getProp() {
		return prop;
	}

	public String getEquations() {
		return equations;
	}

	public String getValue() {
		return value;
	}

	//public String getOriginalValue() {
	//	return originalValue;
	//}

	//public boolean isPrefixMatching() {
	//	return isPrefixMatching;
	//}

	//public boolean isLikeForward() {
	//	return isLikeForward;
	//}

	/**
	 * 文字列表現
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(prop);
		buf.append(" ");
		buf.append(equations);
		buf.append(" ");
		buf.append(value);
		return buf.toString();
	}

}
