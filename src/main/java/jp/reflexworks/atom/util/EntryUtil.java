package jp.reflexworks.atom.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.reflexworks.atom.AtomConst;
import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.feed.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;

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
		if (feed != null && feed.entry != null && feed.entry.size() > 0) {
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
		if (feed == null || feed.entry == null || feed.entry.size() == 0) {
			return null;
		}
		return feed.entry.get(0);
	}

	/**
	 * IDからリビジョンを取得します.
	 * @param id ID
	 * @return リビジョン
	 */
	public static int getRevisionFromId(String id) {
		return EntryBase.getRevisionFromId(id);
	}

	/**
	 * IDからURIを取得します.
	 * @param id ID
	 * @return IDから抽出したURI
	 */
	public static String getUriFromId(String id) {
		return EntryBase.getUriFromId(id);
	}

	/**
	 * IDからURIとリビジョンを取得します.
	 * @param id ID
	 * @return [0]URI、[1]リビジョン
	 */
	public static String[] getUriAndRevisionFromId(String id) {
		return EntryBase.getUriAndRevisionFromId(id);
	}
	
	/**
	 * Entryクラスのインスタンスを生成します。
	 * @return Entryオブジェクト
	 */
	public static EntryBase createEntry(FeedTemplateMapper mapper) {
		if (mapper != null) {
			try {
		        EntryBase emptyEntry = (EntryBase)mapper.fromMessagePack(
		        		AtomConst.MSGPACK_BYTES_ENTRY, AtomConst.MSGPACK_ENTRY);
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
							AtomConst.MSGPACK_BYTES_FEED, AtomConst.MSGPACK_FEED);
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
		if (data != null && data.length > 1 && data[0] == AtomConst.MSGPACK_PREFIX) {
			return true;
		}
		return false;
	}

}
