package jp.reflexworks.atom;

import java.util.Map;

import jp.sourceforge.reflex.util.BinaryUtil;

public interface AtomConst {
	
	/** エンコード */
	public static final String ENCODING = "UTF-8";

	/** ATOM : namespace */
	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	/** ATOM : Feed package */
	public static final String ATOM_PACKAGE_FEED = "jp.reflexworks.atom.feed";
	/** ATOM : Entry package */
	public static final String ATOM_PACKAGE_ENTRY = "jp.reflexworks.atom.entry";
	
	/** ATOM : Source package */
	//public static final String ATOM_PACKAGE_SOURCE = "jp.reflexworks.atom.source";
	/** ATOM : Package map */
	//public static final Map<String, String> ATOM_PACKAGE;
	//static {
	//	ATOM_PACKAGE = new HashMap<String, String>();
	//	ATOM_PACKAGE.put(ATOM_PACKAGE_FEED, "");
	//	ATOM_PACKAGE.put(ATOM_PACKAGE_ENTRY, "");
	//}
	public static final Map<String, String> ATOM_PACKAGE = 
			AtomConstSupporter.createModelPackage();
	
	/** MessagePack byte配列 最初の1バイト */
	public static final byte MSGPACK_PREFIX = -36;
	/** MessagePack Entry byte配列 16進数表記 */
	public static final String MSGPACK_BYTES_HEX_ENTRY = 
			"DC0020C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";
	/** MessagePack Entry byte配列 */
	public static final byte[] MSGPACK_BYTES_ENTRY = 
			BinaryUtil.hex2bin(MSGPACK_BYTES_HEX_ENTRY);
	/** MessagePack Feed byte配列 16進数表記 */
	public static final String MSGPACK_BYTES_HEX_FEED = 
			"DC0011C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";
	/** MessagePack Feed byte配列 */
	public static final byte[] MSGPACK_BYTES_FEED = 
			BinaryUtil.hex2bin(MSGPACK_BYTES_HEX_FEED);
	
	/** MessaaePack Entry class */
	public static final boolean MSGPACK_ENTRY = false;
	/** MessaaePack Feed class */
	public static final boolean MSGPACK_FEED = true;
	/** MessagePack template default name */
	public static final String TEMPLATE_DEFAULT = "_";
	/** MessagePack template(rights) Field ACL start */
	public static final String TEMPLATE_FIELD_ACL_START = "=";
	/** MessagePack template(rights) Index start */
	public static final String TEMPLATE_INDEX_START = ":";
	/** MessagePack template(rights) Encryption start */
	public static final String TEMPLATE_ENCRYPTION_START = "#";

	/** Field ACL self */
	public static final String FIELD_ACL_MYSELF = "@";
	/** Field ACL all */
	public static final String FIELD_ACL_ALL = "/*";
	/** Field ACL Read */
	public static final String FIELD_ACL_READ = "R";
	/** Field ACL Write */
	public static final String FIELD_ACL_WRITE = "W";
	/** Field ACL Read Write */
	public static final String FIELD_ACL_RW = FIELD_ACL_READ + FIELD_ACL_WRITE;
	/** Field ACL give */
	public static final String FIELD_ACL_REQUIRED = "+";
	/** Field ACL Delimiter */
	public static final String FIELD_ACL_DELIMITER = ",";
	/** デフォルトテンプレート */
	public static final String[] TEMPLATE_DEFAULT_ARRAY = new String[]{TEMPLATE_DEFAULT};

	/** URN : 接頭辞 */
	public static final String URN_PREFIX = "urn:vte.cx:";
	/** URN : created */
	public static final String URN_PREFIX_CREATED = URN_PREFIX + "created:";
	/** URN : updated */
	public static final String URN_PREFIX_UPDATED = URN_PREFIX + "updated:";
	/** URN : deleted */
	public static final String URN_PREFIX_DELETED = URN_PREFIX + "deleted:";
	/** URN : username */
	public static final String URN_PREFIX_SERVICE = URN_PREFIX + "username:";
	/** URN : acl */
	public static final String URN_PREFIX_ACL = URN_PREFIX + "acl:";
	/** URN : auth */
	public static final String URN_PREFIX_AUTH = URN_PREFIX + "auth:";
	/** URN : usersecret */
	public static final String URN_PREFIX_USERSECRET = URN_PREFIX + "usersecret:";
	
	/** 登録権限 */
	public static final String ACL_TYPE_CREATE = "C";
	/** 参照権限 */
	public static final String ACL_TYPE_RETRIEVE = "R";
	/** 更新権限 */
	public static final String ACL_TYPE_UPDATE = "U";
	/** 削除権限 */
	public static final String ACL_TYPE_DELETE = "D";
	/** CRUD権限 */
	public static final String ACL_TYPE_CRUD = ACL_TYPE_CREATE + ACL_TYPE_RETRIEVE + 
			ACL_TYPE_UPDATE + ACL_TYPE_DELETE;
	/** サービスからのみアクセス可能な権限 */
	public static final String ACL_TYPE_EXTERNAL = "E";
	/** 配下のエントリーより有効である権限 */
	public static final String ACL_TYPE_LOW = "/";
	/** 指定されたエントリーのみ有効である権限 */
	public static final String ACL_TYPE_OWN = ".";
	/** 指定されたエントリーとその配下のエントリーが有効である権限 */
	public static final String ACL_TYPE_OWN_AND_LOW = ACL_TYPE_OWN + ACL_TYPE_LOW;
	/** 任意の文字列 */
	public static final String ACL_USER_ANY = "*";
	/** ログインユーザ */
	public static final String ACL_USER_LOGGEDIN = "+";
	/** selfまたはエイリアスのユーザトップエントリーのuidがログイン情報のuidと等しい */
	public static final String ACL_USER_SELFALIAS = "-";
	
	/** サービスエントリーの先頭文字の値 */
	public static final String SVC_PREFIX_VAL = "@";
	/** サービスエントリーの先頭 */
	public static final String SVC_PREFIX = "/" + SVC_PREFIX_VAL;

	/** サービスステータス : 新規作成中 */
	public static final String SERVICE_STATUS_CREATING = "creating";
	/** サービスステータス : 非公開 */
	public static final String SERVICE_STATUS_INACTIVE = "inactive";
	/** サービスステータス : 公開中 */
	public static final String SERVICE_STATUS_RUNNING = "running";
	/** サービスステータス : 強制停止 */
	public static final String SERVICE_STATUS_BLOCKED = "blocked";

}
