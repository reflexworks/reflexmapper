package jp.reflexworks.atom.entry;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.msgpack.annotation.Index;

import jp.reflexworks.atom.AtomConst;
import jp.reflexworks.atom.wrapper.base.ConditionBase;

/**
 * Entryの親クラス.
 * <p>
 * ATOM形式のEntryです。<br>
 * このEntryが1件のデータになります。<br>
 * 各プロジェクトでこのクラスを継承し、カスタマイズしたEntryクラスを生成してください。<br>
 * </p>
 */
public abstract class EntryBase implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String KIND = "Entry";
	public static final String TOP = ":";

	/**
	 * デフォルトの名前空間
	 */
	@Index(0)
	public String _$xmlns;

	@Index(1)
	public String _$xml$lang;

	@Index(2)
	public String _$xml$base;

	/**
	 * 更新者.
	 * <p>
	 * urn:vte.cx:{created|updated|deleted}:{uid} の形式です。
	 * </p>
	 */
	@Index(3)
	public List<Author> author;

	@Index(4)
	public List<Category> category;

	/**
	 * コンテンツ.
	 * <p>
	 * HTMLなどのテキストコンテンツや、画像のリンク先を設定します。
	 * </p>
	 */
	@Index(5)
	public Content content;

	/**
	 * 認証・認可情報定義.
	 * <p>
	 * <b>WSSE指定</b><br>
	 * /_user/{username} をキーとするエントリーのｃontributorタグのuriタグに、以下の書式で認証情報を設定します。
	 * <ul>
	 * <li>urn:virtual-tech.net:auth:{username},{password}</li>
	 * </ul>
	 * <br>
	 * <b>ACL指定</b><br>
	 * uriタグに、以下の書式でACLを設定します。<br>
	 * <br>
	 * <ul>
	 * <li>urn:virtual-tech.net:acl:{username},{C|R|U|D|.|/|E}</li>
	 * </ul>
	 * <br>
	 * このACLは、配下のエントリーに対し有効です。<br>
	 * 配下のエントリーにACLの設定がある場合、上位階層で設定されたACLは全て無効となり、配下のACLのみ有効となります。<br>
	 * <br>
	 * <ul>
	 * <li><b>username</b><br>
	 * ログインユーザを指定します。先頭と末尾にワイルドカード(*)が指定できます。<br>
	 * *のみを指定した場合、ログインしていないユーザを含むすべてのユーザに対しACLが適用されます。<br>
	 * ?を指定した場合、ログインしているすべてのユーザに対しACLが適用されます。<br>
	 * <br>
	 * </li>
	 * <li><b>ACLの種類</b><br>
	 * 以下の種類があります。複数指定可能です。<br>
	 * <ul>
	 * <li>C : 登録処理を許可</li>
	 * <li>R : 検索処理を許可</li>
	 * <li>U : 更新処理を許可</li>
	 * <li>D : 削除処理を許可</li>
	 * <li>. : 指定したエントリーのみ設定が有効</li>
	 * <li>/ : 指定したエントリーの配下から設定が有効 (設定したエントリー自身には適用されない。)</li>
	 * <li>E : 外部サービス呼び出しからのみデータ操作可で、Reflexサービスから直接データ操作が不可。</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 */
	@Index(6)
	public List<Contributor> contributor;

	/**
	 * ID.
	 * <p>
	 * idの構成は、{キー},{リビジョン}です。<br>
	 * PUTの場合、idで楽観的排他チェックを行います。<br>
	 * DELETEの場合、idのリビジョンで楽観的排他チェックを行います。<br>
	 * </p>
	 */
	@Index(7)
	public String id;

	@Index(8)
	public String id_$xml$lang;

	@Index(9)
	public String id_$xml$base;

	/**
	 * Link.
	 * <p>
	 * linkタグは、rel属性により様々な役割を持ちます。<br>
	 * <br>
	 * <b>キー</b><br>
	 * このエントリーのキーを、rel="self"の、href属性に設定します。<br>
	 * <br>
	 * <b>エイリアス</b><br>
	 * rel="alternate"の、href属性に設定できます。複数指定可能です。<br>
	 * エイリアスで検索したり、ACLを適用させることができます。<br>
	 * エイリアスで検索した場合、キーであるrel="self"のhref属性にエイリアスの値が設定されます。ただしidタグは本体のキーが使用されます。<br>
	 * <br>
	 * <b>コンテンツ</b><br>
	 * rel="related"の場合、href属性に外部コンテンツのURLを指定します。<br>
	 * GETでURLにリダイレクトすることができます。<br>
	 * <br>
	 * <b>Blobstore　(GAE用)</b><br>
	 * rel="related"で、type="blobstore"の場合、Blobstoreのデータを表します。<br>
	 * href属性にBlobKeyの文字列、title属性に名前を指定できます。<br>
	 * <br>
	 * <b>WebHook</b><br>
	 * rel="via"、type="webhook"の場合、href属性に指定されたURLにリクエストします。<br>
	 * リクエストのタイミングは、エントリーの登録・更新後です。<br>
	 * title属性にGET、POST、PUT、DELETEが指定できます。この場合、
	 * 配下のエントリーが検索・登録・更新・削除された場合リクエストが実行されます。自身の登録・更新時には実行されません。<br>
	 * hrefのURLに?async={数字}パラメータが設定されている場合、{数字}秒後にリクエストを実行します。<br>
	 * <br>
	 * </p>
	 */
	@Index(10)
	public List<Link> link;

	/**
	 * 作成日時.
	 * <p>
	 * yyyy-MM-dd'T'hh:mm:ss.SSS+99:99 形式です。<br>
	 * </p>
	 */
	@Index(11)
	public String published;

	@Index(12)
	public String published_$xml$lang;

	@Index(13)
	public String published_$xml$base;

	@Index(14)
	public String rights;

	@Index(15)
	public String rights_$type;

	@Index(16)
	public String rights_$xml$lang;

	@Index(17)
	public String rights_$xml$base;

	@Index(18)
	public String summary;

	@Index(19)
	public String summary_$type;

	@Index(20)
	public String summary_$xml$lang;

	@Index(21)
	public String summary_$xml$base;

	@Index(22)
	public String title;

	@Index(23)
	public String title_$type;

	@Index(24)
	public String title_$xml$lang;

	@Index(25)
	public String title_$xml$base;

	@Index(26)
	public String subtitle;

	@Index(27)
	public String subtitle_$type;

	@Index(28)
	public String subtitle_$xml$lang;

	@Index(29)
	public String subtitle_$xml$base;

	/**
	 * 更新日時.
	 * <p>
	 * yyyy-MM-dd'T'hh:mm:ss.SSS+99:99 形式です。<br>
	 * Index項目です。
	 * </p>
	 */
	@Index(30)
	public String updated;

	@Index(31)
	public String updated_$xml$lang;

	@Index(32)
	public String updated_$xml$base;

	public String get$xmlns() {
		return _$xmlns;
	}

	public void set$xmlns(String _$xmlns) {
		this._$xmlns = _$xmlns;
	}

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

	public List<Author> getAuthor() {
		return author;
	}

	public void setAuthor(List<Author> author) {
		this.author = author;
	}

	public List<Category> getCategory() {
		return category;
	}

	public void setCategory(List<Category> category) {
		this.category = category;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public List<Contributor> getContributor() {
		return contributor;
	}

	public void setContributor(List<Contributor> contributor) {
		this.contributor = contributor;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_$xml$lang() {
		return id_$xml$lang;
	}

	public void setId_$xml$lang(String id_$xml$lang) {
		this.id_$xml$lang = id_$xml$lang;
	}

	public String getId_$xml$base() {
		return id_$xml$base;
	}

	public void setId_$xml$base(String id_$xml$base) {
		this.id_$xml$base = id_$xml$base;
	}

	public List<Link> getLink() {
		return link;
	}

	public void setLink(List<Link> link) {
		this.link = link;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getPublished_$xml$lang() {
		return published_$xml$lang;
	}

	public void setPublished_$xml$lang(String published_$xml$lang) {
		this.published_$xml$lang = published_$xml$lang;
	}

	public String getPublished_$xml$base() {
		return published_$xml$base;
	}

	public void setPublished_$xml$base(String published_$xml$base) {
		this.published_$xml$base = published_$xml$base;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getRights_$type() {
		return rights_$type;
	}

	public void setRights_$type(String rights_$type) {
		this.rights_$type = rights_$type;
	}

	public String getRights_$xml$lang() {
		return rights_$xml$lang;
	}

	public void setRights_$xml$lang(String rights_$xml$lang) {
		this.rights_$xml$lang = rights_$xml$lang;
	}

	public String getRights_$xml$base() {
		return rights_$xml$base;
	}

	public void setRights_$xml$base(String rights_$xml$base) {
		this.rights_$xml$base = rights_$xml$base;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummary_$type() {
		return summary_$type;
	}

	public void setSummary_$type(String summary_$type) {
		this.summary_$type = summary_$type;
	}

	public String getSummary_$xml$lang() {
		return summary_$xml$lang;
	}

	public void setSummary_$xml$lang(String summary_$xml$lang) {
		this.summary_$xml$lang = summary_$xml$lang;
	}

	public String getSummary_$xml$base() {
		return summary_$xml$base;
	}

	public void setSummary_$xml$base(String summary_$xml$base) {
		this.summary_$xml$base = summary_$xml$base;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle_$type() {
		return title_$type;
	}

	public void setTitle_$type(String title_$type) {
		this.title_$type = title_$type;
	}

	public String getTitle_$xml$lang() {
		return title_$xml$lang;
	}

	public void setTitle_$xml$lang(String title_$xml$lang) {
		this.title_$xml$lang = title_$xml$lang;
	}

	public String getTitle_$xml$base() {
		return title_$xml$base;
	}

	public void setTitle_$xml$base(String title_$xml$base) {
		this.title_$xml$base = title_$xml$base;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getSubtitle_$type() {
		return subtitle_$type;
	}

	public void setSubtitle_$type(String subtitle_$type) {
		this.subtitle_$type = subtitle_$type;
	}

	public String getSubtitle_$xml$lang() {
		return subtitle_$xml$lang;
	}

	public void setSubtitle_$xml$lang(String subtitle_$xml$lang) {
		this.subtitle_$xml$lang = subtitle_$xml$lang;
	}

	public String getSubtitle_$xml$base() {
		return subtitle_$xml$base;
	}

	public void setSubtitle_$xml$base(String subtitle_$xml$base) {
		this.subtitle_$xml$base = subtitle_$xml$base;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getUpdated_$xml$lang() {
		return updated_$xml$lang;
	}

	public void setUpdated_$xml$lang(String updated_$xml$lang) {
		this.updated_$xml$lang = updated_$xml$lang;
	}

	public String getUpdated_$xml$base() {
		return updated_$xml$base;
	}

	public void setUpdated_$xml$base(String updated_$xml$base) {
		this.updated_$xml$base = updated_$xml$base;
	}

	/**
	 * キーとなるURIを取得します.
	 * <p>
	 * <link rel="self">タグのhrefの値を返却します.
	 * </p>
	 * 
	 * @return キー
	 */
	public String getMyUri() {
		String myUri = null;
		if (link != null) {
			for (Link childLink : link) {
				if (Link.REL_SELF.equals(childLink._$rel)) {
					myUri = childLink._$href;
				}
			}
		}
		return myUri;
	}

	/**
	 * このエントリーにキーを設定します.
	 * <p>
	 * 引数の値を、<link rel="self">タグのhref属性に設定します.
	 * </p>
	 * 
	 * @param uri
	 *            キー
	 */
	public void setMyUri(String uri) {
		String tmpUri = uri;
		// uriをparentとselfidに分割
		if (uri != null && uri.length() > 0) {
			if ("/".equals(uri)) { // root layer
				// Do nothing.

			} else {
				if ("/".equals(tmpUri.substring(tmpUri.length() - 1))) {
					tmpUri = tmpUri.substring(0, tmpUri.length() - 1);
				}
			}

		} else {
			// root layer
			tmpUri = "/";
		}

		setLinkSelf(tmpUri);
	}

	public static String getMyUri(String uri) {
		String tmpUri = uri;
		if (uri != null && uri.length() > 0) {
			if ("/".equals(uri)) {
				// root layer

			} else if (TOP.equals(uri)) {
				// rootの親階層

			} else {
				if ("/".equals(tmpUri.substring(tmpUri.length() - 1))) {
					tmpUri = tmpUri.substring(0, tmpUri.length() - 1);
				}
			}

		} else {
			// root layer
			tmpUri = "/";
		}

		return tmpUri;
	}

	private void setLinkSelf(String uri) {
		if (uri == null || uri.trim().length() == 0) {
			return;
		}
		if (link == null) {
			link = new ArrayList<Link>();
		}
		for (Link childLink : link) {
			if (Link.REL_SELF.equals(childLink._$rel)) {
				childLink._$href = uri;
				return;
			}
		}
		Link childLink = new Link();
		childLink._$rel = Link.REL_SELF;
		childLink._$href = uri;
		link.add(childLink);
	}

	/**
	 * エイリアスに指定されたURLを追加します。
	 * 
	 * @param uri
	 *            エイリアス
	 */
	public void addAlternate(String uri) {
		if (isTop(uri)) {
			return;
		}
		if (link == null) {
			link = new ArrayList<Link>();
		}

		boolean isExist = false;
		for (Link li : link) {
			if (Link.REL_ALTERNATE.equals(li.get$rel())
					&& uri.equals(li.get$href())) {
				isExist = true;
			}
		}
		if (!isExist) {
			Link li = new Link();
			li.set$rel(Link.REL_ALTERNATE);
			li.set$href(uri);
			link.add(li);
		}
	}

	/**
	 * エイリアスから指定されたURLを削除します。
	 * @param uri エイリアス
	 */
	public void removeAlternate(String uri) {
		if (isTop(uri)) {
			return;
		}
		if (link == null) {
			return;
		}

		for (int i = 0; i < link.size(); i++) {
			Link li = link.get(i);
			if (Link.REL_ALTERNATE.equals(li.get$rel())
					&& uri.equals(li.get$href())) {
				link.remove(i);
				break;
			}
		}
	}

	/**
	 * エイリアス一覧を取得します.
	 */
	public List<String> getAlternate() {
		if (link == null) {
			return null;
		}
		List<String> aliases = new ArrayList<String>();
		for (Link li : link) {
			if (Link.REL_ALTERNATE.equals(li.get$rel()) && li._$href != null
					&& li._$href.length() > 0) {
				aliases.add(li.get$href());
			}
		}
		if (aliases.size() == 0) {
			return null;
		}
		return aliases;
	}

	/**
	 * Linkを追加します.
	 * @param ln Link
	 */
	public void addLink(Link ln) {
		if (ln == null) {
			return;
		}
		if (link == null) {
			link = new ArrayList<Link>();
		}
		link.add(ln);
	}

	/**
	 * Contributorを追加します.
	 * @param cont Contributor
	 */
	public void addContributor(Contributor cont) {
		if (cont == null) {
			return;
		}
		if (contributor == null) {
			contributor = new ArrayList<Contributor>();
		}
		contributor.add(cont);
	}
	
	/**
	 * Contentに文字列を設定します.
	 * @param text 文字列
	 */
	public void setContentText(String text) {
		if (content == null) {
			content = new Content();
		}
		content._$$text = text;
	}
	
	/**
	 * Contentの文字列を取得します.
	 * @return Contentの文字列
	 */
	public String getContentText() {
		if (content != null) {
			return content._$$text;
		}
		return null;
	}

	/**
	 * URIが最上位かどうかを判定します.
	 * @param uri URI
	 * @return URIが最上位の場合true
	 */
	public static boolean isTop(String uri) {
		if (uri == null || uri.length() == 0 || EntryBase.TOP.equals(uri)) {
			return true;
		}
		return false;
	}

	/**
	 * URIが/か""かnullかどうかを判定します.
	 * @param uri URI
	 * @return URIが最上位の場合true
	 */
	public static boolean isSlash(String uri) {
		if (uri == null || uri.length() == 0 || "/".equals(uri)) {
			return true;
		}
		return false;
	}

	/**
	 * IDからリビジョンを取得します.
	 * @param id ID
	 * @return リビジョン
	 */
	public static int getRevisionFromId(String id) {
		int rev = 0;
		String[] uriAndRev = getUriAndRevisionFromId(id);
		if (uriAndRev != null && uriAndRev.length >= 2) {
			try {
				rev = Integer.parseInt(uriAndRev[1]);
			} catch (Exception e) {}	// Do nothing.
		}
		return rev;
	}

	/**
	 * IDからURIを取得します.
	 * @param id ID
	 * @return IDから抽出したURI
	 */
	public static String getUriFromId(String id) {
		String url = null;
		String[] uriAndRev = getUriAndRevisionFromId(id);
		if (uriAndRev != null && uriAndRev.length >= 1) {
			url = uriAndRev[0];
		}
		return url;
	}
	
	/**
	 * IDからURIとリビジョンを取得します.
	 * @param id ID
	 * @return [0]URI、[1]リビジョン
	 */
	public static String[] getUriAndRevisionFromId(String id) {
		if (id != null) {
			String[] temp = id.split(",");
			try {
				if (temp != null && temp.length >= 2) {
					int idx = temp[1].indexOf("?");
					if (idx == -1) {
						idx = temp[1].length();
					}
					String rev = temp[1].substring(0, idx);
					return new String[]{temp[0], rev};
				}
			} catch (Exception e) {}
		}
		return null;

	}

	@Override
	public String toString() {
		return "Entry [myUri=" + getMyUri() + ", title=" + title + "]";
	}

	public String getMyself() {
		if (id != null) {
			String token[] = id.split("/");
			if (token.length > 2) { // /@{サービス名}/{uid}
				return token[2];
			}
		}
		return null;
	}

	public void addSvcname(String svcname) {
		if (svcname == null || svcname.length() == 0) {
			return;
		}
		if (id != null && !id.startsWith(AtomConst.SVC_PREFIX)) {
			//_id = "/@" + svcname + _id;
			StringBuilder buf = new StringBuilder();
			buf.append(AtomConst.SVC_PREFIX);
			buf.append(svcname);
			String[] uriAndRev = getUriAndRevisionFromId(id);
			if (!isSlash(uriAndRev[0])) {
				buf.append(id);
			} else {
				buf.append(",");
				buf.append(uriAndRev[1]);
			}
			id = buf.toString();
		}
		if (link != null) {
			for (Link l : link) {
				l.addSvcname(svcname);
			}
		}
		if (contributor != null) {
			for (Contributor cont : contributor) {
				cont.addSvcname(svcname);
			}
		}
	}

	public void cutSvcname(String svcname) {
		if (svcname == null || svcname.length() == 0) {
			return;
		}
		String serviceTopUri = AtomConst.SVC_PREFIX + svcname;
		if (id != null && id.startsWith(serviceTopUri)) {
			//_id = _id.substring(svcname.length() + 2);
			String[] uriAndRev = getUriAndRevisionFromId(id);
			if (!isTop(uriAndRev[0])) {
				if (id.indexOf(serviceTopUri+"/")<0) {
					id = "/"+id.substring(serviceTopUri.length());
				}else {
					id = id.substring(serviceTopUri.length());
				}
			} else {
				StringBuilder buf = new StringBuilder();
				buf.append(serviceTopUri);
				buf.append(",");
				buf.append(uriAndRev[1]);
				id = buf.toString();
			}
		}
		if (link != null) {
			for (Link l : link) {
				l.cutSvcname(svcname);
			}
		}
		if (contributor != null) {
			for (Contributor cont : contributor) {
				cont.cutSvcname(svcname);
			}
		}
	}

	public abstract Object getValue(String fieldname);

	public abstract void encrypt(Object cipher);

	public abstract void decrypt(Object cipher);

	public abstract boolean isMatch(ConditionBase[] conditions);

	/**
	 * 項目チェック.
	 * @param uid UID
	 * @param groups 参加グループリスト.<br>
	 *               nullの場合、項目ACLチェックを行いません。<br>
	 *               空リストの場合、項目ACLチェックを行います。
	 */
	public abstract boolean validate(String uid, List<String> groups)
			throws java.text.ParseException;

	public abstract void maskprop(String uid, List<String> groups);

}
