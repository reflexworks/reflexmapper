package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.CipherUtil;
import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * 認証・認可情報定義.
 * <p>
 * <b>WSSE指定</b><br><br>
 * /_user/{username} をキーとするエントリーのｃontributorタグのuriタグに、以下の書式でACLを設定します。
 * <ul>
 * <li>urn:virtual-tech.net:wsse:{username},{password}</li>
 * </ul>
 * <br>
 * <b>ACL指定</b><br><br>
 * uriタグに、以下の書式でACLを設定します。<br>
 * <ul>
 * <li>urn:virtual-tech.net:acl:{username},{C|R|U|D|A|E}</li>
 * </ul><br>
 * このACLは、配下のエントリーに対し有効です。<br>
 * 配下のエントリーにACLの設定がある場合、上位階層で設定されたACLは全て無効となり、配下のACLのみ有効となります。<br>
 * <ul>
 * <li><b>username</b><br><br>
 * ログインユーザを指定します。先頭と末尾にワイルドカード(*)が指定できます。<br>
 * *のみを指定した場合、ログインしていないユーザを含むすべてのユーザに対しACLが適用されます。<br>
 * ?を指定した場合、ログインしているすべてのユーザに対しACLが適用されます。<br>
 * <br></li>
 * <li><b>ACLの種類</b><br><br>
 * 以下の種類があります。複数指定可能です。<br>
 * <ul>
 * <li>C : 登録処理を許可</li>
 * <li>R : 検索処理を許可</li>
 * <li>U : 更新処理を許可</li>
 * <li>D : 削除処理を許可</li>
 * <li>A : 管理者 (CRUD権限に加え、権限の付与および参照が可能)</li>
 * <li>E : 外部サービス呼び出しからのみデータ操作可で、Reflexサービスから直接データ操作が不可。</li>
 * </ul>
 * </li>
 * </p>
 */
public class Contributor implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public static final String URI_SECRETKEY = "$201311131503";

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
		return "Contributor [uri=" + _uri + ", email=" + _email + ", name=" + _name + "]";
	}
	
	public Object getValue(String fldname) {
		if (fldname.equals("contributor.name")) return _name;
		if (fldname.equals("contributor.uri")) return _uri;
		if (fldname.equals("contributor.email")) return _email;
		return null;
	}

	public void encrypt(String id, Object cipher) {
		if (_uri != null) _uri = (String)CipherUtil.doEncrypt("" + _uri, URI_SECRETKEY + id, cipher);
	}
	public void decrypt(String id, Object cipher) {
		if (_uri != null) _uri = (String)CipherUtil.doDecrypt("" + _uri, URI_SECRETKEY + id, cipher);
	}
	
	public void isMatch(ConditionContext context) {
		if (_name != null) {
			context.fldname = "contributor.name";
			context.type = "String";
			context.obj = _name;
			ConditionContext.checkCondition(context);
		}
		if (_uri != null) {
			context.fldname = "contributor.uri";
			context.type = "String";
			context.obj = _uri;
			ConditionContext.checkCondition(context);
		}
		if (_email != null) {
			context.fldname = "contributor.email";
			context.type = "String";
			context.obj = _email;
			ConditionContext.checkCondition(context);
		}
	}

	public boolean validate(String ucode, List<String> groups, String myself) 
			throws java.text.ParseException {return true;}

	public void maskprop(String ucode, List<String> groups, String myself) {}

	public void addSvcname(String svcname) {
		if (_uri != null && svcname != null && svcname.length() > 0) {
			int s = _uri.indexOf(":acl:/");
			if (s >= 0) {
				s += 5;
				String r = _uri.substring(s);
				//_uri = l + ":/@" + svcname + r;
				if (!r.startsWith(EntryBase.SVC_PREFIX)) {
					String l = _uri.substring(0, s);
					StringBuilder buf = new StringBuilder();
					buf.append(l);
					buf.append(EntryBase.SVC_PREFIX);
					buf.append(svcname);
					buf.append(r);
					_uri = buf.toString();
				}
			}
		}
	}

	public void cutSvcname(String svcname) {
		if (_uri != null && svcname != null && svcname.length() > 0) {
			String serviceName = EntryBase.SVC_PREFIX + svcname;
			int s = _uri.indexOf(":acl:" + serviceName);
			if (s >= 0) {
				s += s + 5;
				String l = _uri.substring(0, s);
				String r = _uri.substring(s + serviceName.length());
				_uri = l + r;
				//int s2 = r.indexOf("/");
				//String svc = r.substring(1, s2);
				//if (svc.equals(svcname) && s2 >= 0) { // svcnameが一致していたら省略
				//	String r2 = r.substring(s2);
				//	_uri = l + ":" + r2;
				//}
			}
		}
	}

}
