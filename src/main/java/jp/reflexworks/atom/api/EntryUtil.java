package jp.reflexworks.atom.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.sourceforge.reflex.util.StringUtils;
import jp.reflexworks.atom.entry.Contributor;
import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.entry.FeedBase;
import jp.reflexworks.atom.entry.Link;
import jp.reflexworks.atom.mapper.FeedTemplateConst;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;

public class EntryUtil {

	private static Logger logger = Logger.getLogger(EntryUtil.class.getName());

	/**
	 * キーを親階層と自身の階層に分割します
	 * @param uri キー
	 * @return 親階層と自身の階層
	 */
	public static UriPair getUriPair(String uri) {
		String parent = null;
		String selfid = null;
		String tmpUri = uri;
		// uriをparentとselfidに分割
		if (uri != null && uri.length() > 0) {
			if ("/".equals(uri)) {	// root layer
				parent = EntryBase.TOP;
				selfid = "/";
				
			} else {
				if ("/".equals(tmpUri.substring(tmpUri.length() - 1))) {
					tmpUri = tmpUri.substring(0, tmpUri.length() - 1);
				}
	
				int slash = tmpUri.lastIndexOf("/");
	
				if (slash == -1) {
					parent = "/";
					selfid = tmpUri;
				} else {
					parent = tmpUri.substring(0, slash + 1);
					selfid = tmpUri.substring(slash + 1);
				}
			}

		} else {
			// root layer
			parent = EntryBase.TOP;
			selfid = "/";
		}
		
		return new UriPair(parent, selfid);
	}
	
	/**
	 * 階層を/で分割した配列を返却します.
	 * <p>
	 * 最上位の階層は"/"のため、0番目は""が設定されます.<br>
	 * 例) /aaa/bbb/ccc の実行結果
	 * <ul>
	 * <li>0番目 : ""</li>
	 * <li>1番目 : "aaa"</li>
	 * <li>2番目 : "bbb"</li>
	 * <li>3番目 : "ccc"</li>
	 * </ul>
	 * </p>
	 * @param uri URI
	 * @return 階層を/で分割した配列
	 */
	public static String[] getUriParts(String uri) {
		if (uri != null) {
			return uri.split("/");
		}
		return null;
	}

	/**
	 * 親階層と自身の階層を持つクラス
	 */
	public static class UriPair {
		/** 親階層 */
		public String parent;
		/** 自身の階層 */
		public String selfid;

		public UriPair(String parent, String selfid) {
			this.parent = parent;
			this.selfid = selfid;
		}
		
		@Override
		public String toString() {
			return "parent=" + parent + ", selfid=" + selfid;
		}
	}
	
	/**
	 * Feedにデータが存在する場合trueを返却します.
	 * @param feed Feed
	 * @return Feedにデータが存在する場合true
	 */
	public static boolean isExistData(FeedBase feed) {
		if (feed != null && feed.entry != null && !feed.entry.isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * フィードの最初のエントリーを返却します.
	 * <p>
	 * フィードが空の場合、またエントリーが空の場合はnullを返却します.
	 * </p>
	 * @param feed フィード
	 * @return 最初のエントリー
	 */
	public static EntryBase getFirstEntry(FeedBase feed) {
		if (feed == null || feed.entry == null || feed.entry.isEmpty()) {
			return null;
		}
		return feed.entry.get(0);
	}

	/**
	 * IDからリビジョンを取得します.
	 * @param id ID
	 * @return リビジョン
	 */
	public static int getRevisionById(String id) {
		return EntryBase.getRevisionById(id);
	}

	/**
	 * IDからURIを取得します.
	 * @param id ID
	 * @return IDから抽出したURI
	 */
	public static String getUriById(String id) {
		return EntryBase.getUriById(id);
	}

	/**
	 * IDからURIとリビジョンを取得します.
	 * @param id ID
	 * @return [0]URI、[1]リビジョン
	 */
	public static String[] getUriAndRevisionById(String id) {
		return EntryBase.getUriAndRevisionById(id);
	}
	
	/**
	 * 親階層、自身の階層からキーを取得します.
	 * @param parent 親階層
	 * @param selfid 自身の階層
	 * @return キー
	 */
	public static String getUri(String parent, String selfid) {
		String uri = null;
		if (!isTop(parent)) {
			if (selfid != null) {
				uri = editSlash(parent) + selfid;
			} else {
				uri = parent;
			}
		} else {
			if (selfid != null) {
				uri = selfid;
			}
		}
		return uri;
	}

	/**
	 * キーが最上位かどうかを判定します.
	 * @param uri キー
	 * @return 最上位の場合true
	 */
	public static boolean isTop(String uri) {
		return EntryBase.isTop(uri);
	}

	/**
	 * キーの末尾にスラッシュを設定します.
	 * @param myUri キー
	 * @return キーの末尾にスラッシュをつけた文字列
	 */
	public static String editSlash(String myUri) {
		if (myUri != null && myUri.length() > 0) {
			if (myUri.lastIndexOf("/") < myUri.length() - 1) {
				myUri = myUri + "/";
			}
		} else {
			myUri = "/";
		}
		return myUri;
	}
	
	/**
	 * 最後にスラッシュが設定されている場合に除去します.
	 * @param myUri URI
	 * @return 最後のスラッシュを除去したURI
	 */
	public static String removeLastSlash(String myUri) {
		if (myUri == null || myUri.length() < 2) {
			return myUri;
		}
		// 最後にスラッシュが2個以上設定されている場合はそのまま返す。
		if (myUri.endsWith("//")) {
			return myUri;
		}
		if (myUri.endsWith("/")) {
			// スラッシュを除去する。
			return myUri.substring(0, myUri.length() - 1);
		}
		return myUri;
	}

	/**
	 * 親階層を返却します.
	 * <p>
	 * 親階層は末尾にスラッシュが付きます.
	 * <ul>
	 * <li>uriが"/"の場合、親はnull、子は"/"です。親がnullの場合は":"を返却します。</li>
	 * <li>uriが"/aaa"の場合、親は"/"、子は"aaa"のため、"/"を返却します。</li>
	 * <li>uriが"/aaa/bbb"の場合、親は"/aaa/"、子は"bbb"のため、"/aaa/"を返却します。</li>
	 * </ul>
	 * </p>
	 * @param pMyUri キー
	 * @return キーの親階層
	 */
	public static String getParentUri(String pMyUri) {
		String parentUri = null;
		String myUri = editSlash(pMyUri);
		if ("/".equals(myUri)) {
			return EntryBase.TOP;	// root layer
		}
		if (myUri != null && myUri.length() > 1) {
			int index = myUri.lastIndexOf("/", myUri.length() - 2);
			if (index > -1) {
				parentUri = myUri.substring(0, index + 1);
			}
		}
		return parentUri;
	}

	/**
	 * 自分の階層を返却します.
	 * <p>
	 * <ul>
	 * <li>uriが"/"の場合、親はnull、子は"/"のため、"/"を返却します。</li>
	 * <li>uriが"/aaa"の場合、親は"/"、子は"aaa"のため、"aaa"を返却します。</li>
	 * <li>uriが"/aaa/bbb"の場合、親は"/aaa/"、子は"bbb"のため、"bbb"を返却します。</li>
	 * </ul>
	 * </p>
	 * @param pMyUri キー
	 * @return キーの自階層
	 */
	public static String getSelfidUri(String pMyUri) {
		String selfidUri = null;
		String myUri = editSlash(pMyUri);
		if ("/".equals(myUri)) {
			return "/";	// root layer
		}
		myUri = myUri.substring(0, myUri.length() - 1);
		if (myUri != null) {
			int index = myUri.lastIndexOf("/");
			if (index > -1) {
				selfidUri = myUri.substring(index + 1);
			} else {
				selfidUri = myUri;
			}
		}
		return selfidUri;
	}

	/**
	 * Entryクラスのインスタンスを生成します。
	 * @return Entryオブジェクト
	 */
	public static EntryBase createEntry(FeedTemplateMapper mapper) {
		if (mapper != null) {
			try {
		        EntryBase emptyEntry = (EntryBase)mapper.fromMessagePack(
		        		FeedTemplateConst.MSGPACK_BYTES_ENTRY, AtomConst.MSGPACK_ENTRY);
		        return emptyEntry;

			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, e.getClass().getName(), e);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getClass().getName(), e);
			}
		}
		return null;
	}

	/**
	 * Feedクラスのインスタンスを生成します。
	 * @return Feedオブジェクト
	 */
	public static FeedBase createFeed(FeedTemplateMapper mapper) {
		try {
			FeedBase emptyFeed = (FeedBase)mapper.fromMessagePack(
					FeedTemplateConst.MSGPACK_BYTES_FEED, AtomConst.MSGPACK_FEED);
			return emptyFeed;
			
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING, e.getClass().getName(), e);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getClass().getName(), e);
		}
		return null;
	}
	
	/**
	 * データがMessagePack形式かどうか判定します.
	 * <p>
	 * 最初の1バイトが「-36」であればMessagePackとみなします。
	 * </p>
	 * @param data データ
	 * @return データがMessagePack形式の場合true
	 */
	public static boolean isMessagePack(byte[] data) {
		if (data != null && data.length > 1 && data[0] == FeedTemplateConst.MSGPACK_PREFIX) {
			return true;
		}
		return false;
	}
	
	/**
	 * エントリーから指定されたURIの署名({revision},{署名})を取得します.
	 */
	public static String getSignature(EntryBase entry, String uri) {
		if (!StringUtils.isBlank(uri) && entry != null && entry.link != null) {
			String idUri = getUriById(entry.id);
			if (uri.equals(idUri)) {
				for (Link link : entry.link) {
					if (Link.REL_SELF.equals(link._$rel)) {
						return link._$title;
					}
				}
			} else {
				for (Link link : entry.link) {
					if (Link.REL_ALTERNATE.equals(link._$rel) &&
							uri.equals(link._$href)) {
						return link._$title;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Feedのtitleを返却します.
	 * @param feed フィード
	 * @return titleの内容
	 */
	public static String getTitle(FeedBase feed) {
		if (feed != null) {
			return feed.title;
		}
		return null;
	}

	/**
	 * ユーザに指定した権限を付与するACL情報の文字列を作成します
	 * @param user ユーザ名
	 * @param aclType 権限情報
	 * @return ACL情報の文字列
	 */
	public static String getAclUrn(String user, String aclType) {
		if (aclType != null && aclType.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(AtomConst.URN_PREFIX_ACL);
			sb.append(user);
			sb.append(",");
			sb.append(aclType);
			return sb.toString();
		}
		return null;
	}
	
	/**
	 * ACL情報を設定したContributorを作成します.
	 * @param user ユーザ名
	 * @param aclType 権限情報
	 * @return ACL情報を設定したContributor
	 */
	public static Contributor getAclContributor(String user, String aclType) {
		String urn = getAclUrn(user, aclType);
		Contributor contributor = new Contributor();
		contributor.uri = urn;
		return contributor;
	}

	/**
	 * エントリーに認可情報を設定します。
	 * @param entry 編集対象エントリー
	 * @param user ACLを設定する対象(ユーザ(UID)、グループ)
	 * @param aclType ACLタイプ
	 */
	public static void addAclToEntry(EntryBase entry, String user, 
			String aclType) {
		if (entry != null) {
			Contributor contributor = new Contributor();
			contributor.uri = getAclUrn(user, aclType);
			List<Contributor> contributors = entry.getContributor();
			if (contributors == null) {
				contributors = new ArrayList<Contributor>();
				entry.setContributor(contributors);
			}
			contributors.add(contributor);
			entry.setContributor(contributors);
		}
	}
	
	/**
	 * Metalistから指定された項目名のmeta情報を取得します.
	 * @param metalist フィールド情報リスト
	 * @param name フィールド名
	 * @return フィールド情報
	 */
	public static Meta getMeta(List<Meta> metalist, String name) {
		if (metalist != null && !StringUtils.isBlank(name)) {
			for (Meta meta : metalist) {
				if (name.equals(meta.name)) {
					return meta;
				}
			}
		}
		return null;
	}

}
