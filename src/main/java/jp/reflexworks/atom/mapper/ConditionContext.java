package jp.reflexworks.atom.mapper;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.reflexworks.atom.wrapper.base.ConditionBase;
import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.StringUtils;

/**
 * エンティティオブジェクトが検索条件に合致するか調べるために使われるコンテキスト
 * 
 */
public class ConditionContext {

	public ConditionBase[] conditions;
	public String fldname;
	public String type;
	public Object obj;
	public Map<String,Boolean> isMatchs;
	private Boolean[] isFetchs;		// 項目名が一致したか
	
	public ConditionContext(ConditionBase[] conditions) {
		this.conditions = conditions;
		this.isMatchs = new HashMap<String, Boolean>();
		this.isFetchs = new Boolean[conditions.length];
	}
	
	/**
	 * 検索条件に合致していたかについてのチェック結果の判定
	 * 
	 * @return 合致していればtrue
	 */
	public boolean isMatch() {
		for (Boolean value : isFetchs) {
			if (value==null||!value) return false;
		}

		for (Boolean value : isMatchs.values()) {
			if (!value) return false;
		}
		return true;
	}

	/**
	 * すべての項目の検索条件について合致しているかどうかチェックする
	 * 
	 * @param context
	 */
	public static void checkCondition(ConditionContext context) {
		for (int i=0;i<context.conditions.length;i++) {
			ConditionBase cond = context.conditions[i];
			if (cond.getProp().equals(context.fldname)) { // 項目名が一致するかどうか
				context.isFetchs[i] = true;
				Boolean value = context.isMatchs.get(context.fldname);
				if (value == null || !value) {
					context.isMatchs.put(context.fldname, 
							checkCondition(context.obj, cond, context.type));
				}
			}
		}
	}

	/**
	 * 個々の項目について検索条件に合致しているかどうかチェックする
	 * 
	 * @param obj
	 * @param cond
	 * @param type
	 * @return 合致していればtrue
	 */
	private static boolean checkCondition(Object obj, ConditionBase cond, String type) {
		String equal = cond.getEquations();

		if (obj==null) return false;
		if (type.equals("String")) {
			String src = (String) obj;
			String value = cond.getValue();
			if (ConditionBase.REGEX.equals(equal)) {
				Pattern pattern = Pattern.compile(value);
				Matcher matcher = pattern.matcher(src);
				if (!matcher.find()) {
					return false;
				}
			}
			else 
			if (!cond.isPrefixMatching() && !cond.isLikeForward()) {
				int compare = src.compareTo(value);
				
				if (ConditionBase.EQUAL.equals(equal) && compare != 0) {
					return false;
				} else if (ConditionBase.NOT_EQUAL.equals(equal) && compare == 0) {
					return false;
				} else if (ConditionBase.GREATER_THAN.equals(equal) && compare <= 0) {
					return false;
				} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && compare < 0) {
					return false;
				} else if (ConditionBase.LESS_THAN.equals(equal) && compare >= 0) {
					return false;
				} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && compare > 0) {
					return false;
				}

			} else if (cond.isPrefixMatching() && cond.isLikeForward()) {
				// あいまい検索
				if (src.indexOf(value) < 0) {
					return false;
				}

			} else if (cond.isPrefixMatching() && !cond.isLikeForward()) {
				// 前方一致検索
				if (!src.startsWith(value)) {
					return false;
				}

			} else {
				// 後方一致検索
				if (!src.endsWith(value)) {
					return false;
				}
			}

		} else if (type.equals("Integer")) {
			int src = (Integer)obj;
			int value = StringUtils.intValue(cond.getValue());
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} else if (ConditionBase.GREATER_THAN.equals(equal) && src <= value) {
				return false;
			} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && src < value) {
				return false;
			} else if (ConditionBase.LESS_THAN.equals(equal) && src >= value) {
				return false;
			} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && src > value) {
				return false;
			}

		} else if (type.equals("Long")) {
			long src = (Long)obj;
			long value = StringUtils.longValue(cond.getValue());
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} else if (ConditionBase.GREATER_THAN.equals(equal) && src <= value) {
				return false;
			} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && src < value) {
				return false;
			} else if (ConditionBase.LESS_THAN.equals(equal) && src >= value) {
				return false;
			} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && src > value) {
				return false;
			}

		} else if (type.equals("Float")) {
			float src = (Float)obj;
			float value = StringUtils.floatValue(cond.getValue());
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} else if (ConditionBase.GREATER_THAN.equals(equal) && src <= value) {
				return false;
			} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && src < value) {
				return false;
			} else if (ConditionBase.LESS_THAN.equals(equal) && src >= value) {
				return false;
			} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && src > value) {
				return false;
			}

		} else if (type.equals("Double")) {
			double src = (Double)obj;
			double value = StringUtils.doubleValue(cond.getValue());
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} else if (ConditionBase.GREATER_THAN.equals(equal) && src <= value) {
				return false;
			} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && src < value) {
				return false;
			} else if (ConditionBase.LESS_THAN.equals(equal) && src >= value) {
				return false;
			} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && src > value) {
				return false;
			}
		
		} else if (type.equals("Date")) {
			long src = ((Date)obj).getTime();
			long value;
			try {
				value = DateUtil.getDate(cond.getValue()).getTime();
			} catch (ParseException e) {
				value = 0;
			}
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} else if (ConditionBase.GREATER_THAN.equals(equal) && src <= value) {
				return false;
			} else if (ConditionBase.GREATER_THAN_OR_EQUAL.equals(equal) && src < value) {
				return false;
			} else if (ConditionBase.LESS_THAN.equals(equal) && src >= value) {
				return false;
			} else if (ConditionBase.LESS_THAN_OR_EQUAL.equals(equal) && src > value) {
				return false;
			}

		} else if (type.equals("Boolean")) {
			boolean src = (Boolean) obj;
			boolean value = cond.getValue().equals("true"); 
			
			if (ConditionBase.EQUAL.equals(equal) && src != value) {
				return false;
			} else if (ConditionBase.NOT_EQUAL.equals(equal) && src == value) {
				return false;
			} 		
		}
		
		return true;
	}
	
}
