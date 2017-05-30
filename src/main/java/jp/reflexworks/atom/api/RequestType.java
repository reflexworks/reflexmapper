package jp.reflexworks.atom.api;

public interface RequestType {

	/** Servlet path */
	public static final String SERVLET_PATH = "/d";

	/** URLパラメータ : 戻り値の形式をXMLに指定 (デフォルトはJSON) */
	public static final String PARAM_XML = "x";
	/** URLパラメータ : 戻り値の形式をMessagePackに指定 */
	public static final String PARAM_MESSAGEPACK = "m";
	/** URLパラメータ : 最大件数を指定 */
	public static final String PARAM_LIMIT = "l";
	/** URLパラメータ : 次ページ取得のカーソルを指定 */
	public static final String PARAM_NEXT = "p";
	/** URLパラメータ : ページ番号を指定 */
	public static final String PARAM_NUMBER = "n";
	/** URLパラメータ : リビジョンを指定 */
	public static final String PARAM_REVISION = "r";
	/** URLパラメータ : 件数取得 */
	public static final String PARAM_COUNT = "c";
	/** URLパラメータ : Entry取得 */
	public static final String PARAM_ENTRY = "e"; 
	/** URLパラメータ : Feed検索、削除 */
	public static final String PARAM_FEED = "f";
	/** URLパラメータ : contentの中身を取得 */
	public static final String PARAM_CONTENT = "_content";
	/** URLパラメータ : contentの中身無しで返す */
	public static final String PARAM_NOCONTENT = "_nocontent";
	/** URLパラメータ : 配下のエントリー削除処理 (DELETEメソッドで使用) */
	public static final String PARAM_RF = "_rf";
	/** URLパラメータ : ログイン */
	public static final String PARAM_LOGIN = "_login";
	/** URLパラメータ : ログアウト */
	public static final String PARAM_LOGOUT = "_logout";
	/** URLパラメータ : 認証後にディスパッチする */
	//public static final String PARAM_LOGIN_DISPATCH = "_logindispatch";
	/** URLパラメータ : 強制ログイン */
	public static final String PARAM_LOGINF = "_loginf";
	/** URLパラメータ : BlobstoreへのアップロードURLを取得 */
	public static final String PARAM_UPLOADURL = "_uploadurl";
	/** URLパラメータ : link rel="related"の情報を取得 */
	//public static final String PARAM_RELATED = "_related";
	/** URLパラメータ : Blobstoreの情報をリダイレクトで取得する場合付加 */
	//public static final String PARAM_REDIRECT = "_redirect";
	/** URLパラメータ : 自動採番 */
	public static final String PARAM_ALLOCIDS = "_allocids";
	/** URLパラメータ : 自動採番の加算 */
	public static final String PARAM_ADDIDS = "_addids";
	/** URLパラメータ : 自動採番の値設定 */
	public static final String PARAM_SETIDS = "_setids";
	/** URLパラメータ : 自動採番の枠設定 */
	public static final String PARAM_RANGEIDS = "_rangeids";
	/** URLパラメータ : 認証チェックサービス */
	public static final String PARAM_AUTHCHECK = "_authcheck";
	/** URLパラメータ : 認証チェックとユーザ名・ニックネーム・UID取得サービス */
	public static final String PARAM_WHOAMI = "_whoami";
	/** URLパラメータ : Dataスキームで取得 */
	public static final String PARAM_DATASCHEME = "64";
	/** URLパラメータ : RXID */
	public static final String PARAM_RXID = "_RXID";
	/** URLパラメータ : 更新時にrevisionをカウントアップしないオプション */
	public static final String PARAM_SILENT = "_silent";
	/** URLパラメータ : 現在時刻返却サービス */
	public static final String PARAM_GETDATETIME = "_getdatetime";
	/** URLパラメータ : 現在時刻返却サービス */
	public static final String PARAM_NOW = "_now";
	/** URLパラメータ : 処理継続 */
	public static final String PARAM_CONTINUE = "_continue";
	/** URLパラメータ : 複数エントリー検索 */
	public static final String PARAM_GETMULTI = "_getmulti";
	/** URLパラメータ : ユーザ番号取得 */
	public static final String PARAM_UID = "_uid";
	/** URLパラメータ : 署名 */
	public static final String PARAM_SIGNATURE = "_signature";
	/** URLパラメータ : 指定されたページ数分のカーソル一覧を返却するオプション */
	public static final String PARAM_POINTERS = "_pointers";
	/** URLパラメータ : カーソル一覧取得で最初のページの件数を指定するオプション */
	public static final String PARAM_FIRST = "_first";
	/** URLパラメータ : 指定されたページ数分のカーソル一覧をサーバ内部に取得するオプション */
	public static final String PARAM_PAGINATION = "_pagination";
	/** URLパラメータ : 配下のエントリーを全て抽出するオプション */
	public static final String PARAM_LOWER = "_lower";
	/** URLパラメータ : ノード名を指定するオプション (_lowerオプションと合わせて使用) */
	public static final String PARAM_NODE = "_node";
	/** URLパラメータ : アクセスキー変更オプション */
	public static final String PARAM_ACCESSKEY = "_accesskey";
	/** URLパラメータ : アクセストークン発行オプション */
	public static final String PARAM_ACCESSTOKEN = "_accesstoken";
	/** URLパラメータ : リンクトークン発行オプション */
	public static final String PARAM_LINKTOKEN = "_linktoken";
	/** URLパラメータ : リンクトークン指定オプション */
	public static final String PARAM_TOKEN = "_token";
	/** URLパラメータ : ユーザ登録メール送信オプション */
	public static final String PARAM_ADDUSER = "_adduser";
	/** URLパラメータ : パスワード変更メール送信オプション */
	public static final String PARAM_PASSRESET = "_passreset";
	/** URLパラメータ : パスワード変更オプション */
	public static final String PARAM_CHANGEPASS = "_changephash";
	/** URLパラメータ : 管理者によるユーザ登録オプション */
	public static final String PARAM_ADDUSER_BYADMIN = "_adduserByAdmin";
	/** URLパラメータ : サービス作成 */
	public static final String PARAM_CREATESERVICE = "_createservice";
	/** URLパラメータ : サービス削除 */
	public static final String PARAM_DELETESERVICE = "_deleteservice";
	/** URLパラメータ : サービスリセット */
	public static final String PARAM_RESETSERVICE = "_resetservice";
	/** URLパラメータ : ログインサービスへのリダイレクト時にサービス名を引き渡すために使用するパラメータ */
	public static final String PARAM_INVOKER = "_invoker";
	/** URLパラメータ : サービス */
	public static final String PARAM_SERVICE = "_service";
	/** URLパラメータ : サービスのステータス取得・更新 */
	public static final String PARAM_SERVICESTATUS = "_servicestatus";
	/** URLパラメータ : ユーザステータス取得 */
	public static final String PARAM_USERSTATUS = "_userstatus";
	/** URLパラメータ : ユーザを無効にする */
	public static final String PARAM_REVOKEUSER = "_revokeuser";
	/** URLパラメータ : ユーザを有効にする */
	public static final String PARAM_ACTIVATEUSER = "_activateuser";
	/** URLパラメータ : RDB登録反映 */
	//public static final String PARAM_APPLYRDB = "_applyrdb";
	/** URLパラメータ : RDB */
	public static final String PARAM_RDB = "_rdb";
	/** URLパラメータ : キャッシュ */
	public static final String PARAM_CACHE = "_cache";
	/** URLパラメータ : SID */
	public static final String PARAM_SID = "_sid";

	/** URLパラメータ : relatedパラメータと同様の意味を持つ（キャッシュ対応） */
	//public static final String SIGN_RELATED = "+";
	/** URLパラメータ : redirectパラメータと同様の意味を持つ（キャッシュ対応） */
	//public static final String SIGN_REDIRECT = "!";
	
	/** URLパラメータ : ワイルドカード */
	public static final String WILDCARD = "*";

}
