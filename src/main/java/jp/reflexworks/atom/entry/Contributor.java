package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.mapper.CipherUtil;
import jp.reflexworks.atom.mapper.ConditionContext;

/**
 * 認証・認可情報定義.
 * <p>
 * <b>WSSE指定</b><br><br>
 * <ul>
 * <li>urn:vte.cx:auth:{username},{password}</li>
 * </ul>
 * <br>
 * <b>ACL指定</b><br><br>
 * uriタグに、以下の書式でACLを設定します。<br>
 * <ul>
 * <li>urn:vte.cx:acl:{UID},{C|R|U|D|E|.|/}</li>
 * </ul><br>
 * このACLは、配下のエントリーに対し有効です。<br>
 * 配下のエントリーにACLの設定がある場合、上位階層で設定されたACLは全て無効となり、配下のACLのみ有効となります。<br>
 * <ul>
 * <li><b>UID</b><br><br>
 * ログインユーザのUIDを指定します。<br>
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
 * <li>E : 外部サービス呼び出しからのみデータ操作可で、Reflexサービスから直接データ操作が不可。</li>
 * </ul>
 * </li>
 * </p>
 */
public class Contributor implements Serializable, Cloneable, SoftSchema {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String name;
	public String uri;
	public String email;

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
		return "Contributor [uri=" + uri + ", email=" + email + ", name=" + name + "]";
	}
	
	public Object getValue(String fldname) {
		if (fldname.equals("contributor.name")) return name;
		if (fldname.equals("contributor.uri")) return uri;
		if (fldname.equals("contributor.email")) return email;
		return null;
	}

	public void encrypt(String id, Object cipher, String secretkey) {
		if (uri != null) uri = (String)CipherUtil.doEncrypt("" + uri, secretkey + id, cipher);
	}
	public void decrypt(String id, Object cipher, String secretkey) {
		if (uri != null) uri = (String)CipherUtil.doDecrypt("" + uri, secretkey + id, cipher);
	}
	
	public void isMatch(ConditionContext context) {
		if (name != null) {
			context.fldname = "contributor.name";
			context.type = "String";
			context.obj = name;
			ConditionContext.checkCondition(context);
		}
		if (uri != null) {
			context.fldname = "contributor.uri";
			context.type = "String";
			context.obj = uri;
			ConditionContext.checkCondition(context);
		}
		if (email != null) {
			context.fldname = "contributor.email";
			context.type = "String";
			context.obj = email;
			ConditionContext.checkCondition(context);
		}
	}

	public boolean validate(String uid, List<String> groups, String myself) 
			throws java.text.ParseException {return true;}

	public void maskprop(String uid, List<String> groups, String myself) {}

	public void addSvcname(String svcname) {
		if (uri != null && svcname != null && svcname.length() > 0) {
			int s = uri.indexOf(":acl:/");
			if (s >= 0) {
				s += 5;
				String r = uri.substring(s);
				//_uri = l + ":/@" + svcname + r;
				if (!r.startsWith(EntryBase.SVC_PREFIX)) {
					String l = uri.substring(0, s);
					StringBuilder buf = new StringBuilder();
					buf.append(l);
					buf.append(EntryBase.SVC_PREFIX);
					buf.append(svcname);
					if (r.indexOf("/,")>=0) {
						buf.append(r.substring(1));
					}else {
						buf.append(r);
					}
					uri = buf.toString();
				}
			}
		}
	}

	public void cutSvcname(String svcname) {
		if (uri != null && svcname != null && svcname.length() > 0) {
			String serviceName = EntryBase.SVC_PREFIX + svcname;

			String oldstr = ":acl:" + serviceName;
			String newstr = ":acl:";
			
			// servicenameだけの場合、cut後のservicenameは/
			if (uri.indexOf(oldstr+"/")<0) {
				newstr += "/";
			}
			uri = uri.replace(oldstr, newstr);
		}
	}

}
