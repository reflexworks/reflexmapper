package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * 更新者.
 * <p>
 * Reflex内で登録・更新時、uriに更新者情報を設定します。<br>
 * urn:vte.cx:{created|updated|deleted}:{username} の形式です。
 * </p>
 */
public class Author implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String name;
	public String uri;
	public String email;

	public String get_$xml$lang() {
		return _$xml$lang;
	}

	public void set_$xml$lang(String _$xml$lang) {
		this._$xml$lang = _$xml$lang;
	}

	public String get_$xml$base() {
		return _$xml$base;
	}

	public void set_$xml$base(String _$xml$base) {
		this._$xml$base = _$xml$base;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Author [uri=" + uri + ", email=" + email + ", name=" + name + "]";
	}
	
	public Object getValue(String fldname) {
		if (fldname.equals("author.name")) return name;
		if (fldname.equals("author.uri")) return uri;
		if (fldname.equals("author.email")) return email;
		return null;
	}

	public void encrypt(String id, Object cipher, String secretkey) {}
	public void decrypt(String id, Object cipher, String secretkey) {}
	
	public void isMatch(ConditionContext context) {
		if (name != null) {
			context.fldname = "author.name";
			context.type = "String";
			context.obj = name;
			ConditionContext.checkCondition(context);
		}
		if (uri != null) {
			context.fldname = "author.uri";
			context.type = "String";
			context.obj = uri;
			ConditionContext.checkCondition(context);
		}
		if (email != null) {
			context.fldname = "author.email";
			context.type = "String";
			context.obj = email;
			ConditionContext.checkCondition(context);
		}
	}

	public boolean validate(String ucode, List<String> groups, String myself) 
	throws java.text.ParseException {return true;}

	public void maskprop(String ucode, List<String> groups, String myself) {}

}
