package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * カテゴリ.
 */
public class Category implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String _$term;
	public String _$scheme;
	public String _$label;

	public String get$xml$lang() {
		return _$xml$lang;
	}

	public void set$xml$lang(String _$xml$lang) {
		this._$xml$lang = _$xml$lang;
	}

	public String get$xml$base() {
		return _$xml$base;
	}

	public void set$xml$base(String _$xml$base) {
		this._$xml$base = _$xml$base;
	}

	public String get$term() {
		return _$term;
	}

	public void set$term(String term) {
		this._$term = term;
	}

	public String get$scheme() {
		return _$scheme;
	}

	public void set$scheme(String scheme) {
		this._$scheme = scheme;
	}

	public String get$label() {
		return _$label;
	}

	public void set$label(String label) {
		this._$label = label;
	}

	@Override
	public String toString() {
		return "Category [_$term=" + _$term + ", _$scheme=" + _$scheme
				+ ", _$label=" + _$label + "]";
	}

	public Object getValue(String fldname) {
		if (fldname.equals("category.$term")) return _$term;
		if (fldname.equals("category.$scheme")) return _$scheme;
		if (fldname.equals("category.$label")) return _$label;
		return null;
	}

	public void encrypt(String id, Object cipher, String secretkey) {}
	public void decrypt(String id, Object cipher, String secretkey) {}
	
	public void isMatch(ConditionContext context) {
		if (_$term != null) {
			context.fldname = "category.$term";
			context.type = "String";
			context.obj = _$term;
			ConditionContext.checkCondition(context);
		}
		if (_$scheme != null) {
			context.fldname = "category.$scheme";
			context.type = "String";
			context.obj = _$scheme;
			ConditionContext.checkCondition(context);
		}
		if (_$label != null) {
			context.fldname = "category.$label";
			context.type = "String";
			context.obj = _$label;
			ConditionContext.checkCondition(context);
		}
	}

	public boolean validate(String uid, List<String> groups, String myself) 
			throws java.text.ParseException {return true;}

	public void maskprop(String uid, List<String> groups, String myself) {}

}
