package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * 更新者.
 * <p>
 * Reflex内で登録・更新時、uriに更新者情報を設定します。<br>
 * urn:virtual-tech.net:{created|updated|deleted}:{username} の形式です。
 * </p>
 */
public class Author implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String _name;
	public String _uri;
	public String _email;

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

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getUri() {
		return _uri;
	}

	public void setUri(String uri) {
		this._uri = uri;
	}

	public String getEmail() {
		return _email;
	}

	public void setEmail(String email) {
		this._email = email;
	}

	@Override
	public String toString() {
		return "Author [uri=" + _uri + ", email=" + _email + ", name=" + _name + "]";
	}
	
	public Object getValue(String fldname) {
		if (fldname.equals("author.name")) return _name;
		if (fldname.equals("author.uri")) return _uri;
		if (fldname.equals("author.email")) return _email;
		return null;
	}

	public void encrypt(String id, Object cipher) {}
	public void decrypt(String id, Object cipher) {}
	
	public void isMatch(ConditionContext context) {
		if (_name != null) {
			context.fldname = "author.name";
			context.type = "String";
			context.obj = _name;
			ConditionContext.checkCondition(context);
		}
		if (_uri != null) {
			context.fldname = "author.uri";
			context.type = "String";
			context.obj = _uri;
			ConditionContext.checkCondition(context);
		}
		if (_email != null) {
			context.fldname = "author.email";
			context.type = "String";
			context.obj = _email;
			ConditionContext.checkCondition(context);
		}
	}

	public boolean validate(String ucode, List<String> groups, String myself) 
	throws java.text.ParseException {return true;}

	public void maskprop(String ucode, List<String> groups, String myself) {}

}
