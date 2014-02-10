package jp.reflexworks.atom.wrapper.base;

import jp.reflexworks.servlet.util.URLDecoderPlus;
import jp.sourceforge.reflex.util.StringUtils;

public abstract class ConditionBase {
	
	public static final String EQUAL = "eq";
	public static final String GREATER_THAN = "gt";
	public static final String GREATER_THAN_OR_EQUAL = "ge";
	public static final String LESS_THAN = "lt";
	public static final String LESS_THAN_OR_EQUAL = "le";
	public static final String NOT_EQUAL = "ne";
	public static final String REGEX = "rg";
	public static final String DELIMITER = "-";
	
	public static final String PREFIX_MATCHING  = "*";
	public static final String PREFIX_MATCHING_END = "\ufffd";
	public static final String PREFIX_MATCHING_SLASH  = PREFIX_MATCHING + "/";
	
	// 項目名
	protected String prop;
	
	// 等・不等式
	protected String equations;
	
	// 値 (デコード済)
	protected String value;
	
	// デコード前の入力値
	protected String originalValue;
	
	// 前方一致かどうか
	protected boolean isPrefixMatching;

	// isPrefixMatching(前方一致かどうか)は、後ろのlike検索。
	// 前にlike指定されているかどうかのフラグ
	protected boolean isLikeForward;

	public ConditionBase(String cond) {
		setCondition(cond);
	}

	public ConditionBase(String prop, String value) {
		setCondition(prop, value);
	}
	
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
			this.originalValue = "";
			this.value = "";
		} else {
			String value = cond.substring(idxFilter);
			if (value.endsWith(PREFIX_MATCHING)) {
				if (EQUAL.equals(this.equations)) {
					this.equations = GREATER_THAN_OR_EQUAL;
				}
				this.originalValue = value.substring(0, value.length() - 1);
				this.isPrefixMatching = true;
			} else {
				this.originalValue = value;
			}
			setDecodeValue();
		}
	}
	
	protected void setCondition(String prop, String value) {
		if (value == null || "".equals(value)) {
			setCondition(prop);
		} else {
			this.prop = prop;
			if (value.endsWith(PREFIX_MATCHING)) {
				this.equations = GREATER_THAN_OR_EQUAL;
				this.originalValue = value.substring(0, value.length() - 1);
				this.isPrefixMatching = true;
			} else {
				this.equations = EQUAL;
				this.originalValue = value;
			}
			setDecodeValue();
		}
	}
	
	private void setDecodeValue() {
		if (this.originalValue != null) {
			this.value = URLDecoderPlus.urlDecode(this.originalValue);
		}
	}

	public String getProp() {
		return prop;
	}

	public String getEquations() {
		return equations;
	}

	public String getValue() {
		return value;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public boolean isPrefixMatching() {
		return isPrefixMatching;
	}

	public boolean isLikeForward() {
		return isLikeForward;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(prop);
		buf.append(" ");
		buf.append(equations);
		buf.append(" ");
		buf.append(value);
		buf.append(" (original = ");
		buf.append(originalValue);
		buf.append(")");
		buf.append(", isPrefixMatching = ");
		buf.append(isPrefixMatching);
		buf.append(", isLikeForward = ");
		buf.append(isLikeForward);
		return buf.toString();
	}

}
