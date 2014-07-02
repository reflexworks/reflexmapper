package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * コンテンツ.
 * <p>
 * HTMLなどのテキストコンテンツや、画像などのリンク先を設定します。<br>
 * テキストコンテンツを設定する場合、_$$text項目に設定してください。(XMLにシリアライズした際、contentタグの値となります。)<br>
 * 画像などのリンク先を設定する場合、_$srcにURLを設定してください。<br>
 * </p>
 */
public class Content implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String _$src;
	public String _$type;
	public String _$$text;

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

	public String get$src() {
		return _$src;
	}

	public void set$src(String _$src) {
		this._$src = _$src;
	}

	public String get$type() {
		return _$type;
	}

	public void set$type(String _$type) {
		this._$type = _$type;
	}

	public String get$$text() {
		return _$$text;
	}

	public void set$$text(String _$$text) {
		this._$$text = _$$text;
	}
	
	@Override
	public String toString() {
		return "Content [" + _$$text + "]";
	}
	
	public boolean validate(String ucode, List<String> groups, String myself) 
	throws java.text.ParseException {
		
		if (this._$$text != null || this._$type != null || this._$src != null) {
			if (groups != null && groups.size() > 0) {
				boolean ex = false;
				for (int i = 0; i < groups.size(); i++) {
					// $contentグループでなければ更新できない -> /@{サービス名}/_group/$content
					if (groups.get(i).equals("/_group/$content")) ex=true;
				}
				if (_$type.equals("image/jpeg")||_$type.equals("image/png")||_$type.equals("image/gif")) ex=true;
				if (!ex) throw new java.text.ParseException(
						"Property 'content' is not writeable.", 0);
			}
		}
		
		return true;
	}

	public Object getValue(String fldname) {
		if (fldname.equals("content.$$text")) return _$$text;
		if (fldname.equals("content.$type")) return _$type;
		if (fldname.equals("content.$src")) return _$src;
		return null;
	}

	public void encrypt(String id, Object cipher, String secretkey) {}
	public void decrypt(String id, Object cipher, String secretkey) {}
	
	public void isMatch(ConditionContext context) {
		if (_$$text != null) {
			context.fldname = "content.$$text";
			context.type = "String";
			context.obj = _$$text;
			ConditionContext.checkCondition(context);
		}
		if (_$type != null) {
			context.fldname = "content.$type";
			context.type = "String";
			context.obj = _$type;
			ConditionContext.checkCondition(context);
		}
		if (_$src != null) {
			context.fldname = "content.$src";
			context.type = "String";
			context.obj = _$src;
			ConditionContext.checkCondition(context);
		}
	}

	public void maskprop(String ucode, List<String> groups, String myself) {}

}
