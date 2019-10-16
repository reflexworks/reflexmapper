package jp.reflexworks.atom.api;

import java.util.Arrays;
import java.util.List;

/**
 * サービスごとに指定する設定
 */
public interface SettingConst {

	/** サービス設定 : エントリー最大数デフォルト設定 **/
	public static final String ENTRY_NUMBER_DEFAULT = "_entry.number.default";
	/** サービス設定 : 検索条件除外設定 **/
	public static final String IGNORE_CONDITION_PREFIX = "_ignore.condition.";
	/** サービス設定 : エラー画面表示URLパターン */
	public static final String ERRORPAGE_PREFIX = "_errorpage.";
	/** RXIDのカウント指定URLパターン */
	public static final String RXID_COUNTER_PREFIX = "_rxid.counter.";
	/** RXID有効時間(分)設定 **/
	public static final String RXID_MINUTE = "_rxid.minute";
	/** セッション有効時間(分)設定 **/
	public static final String SESSION_MINUTE = "_session.minute";
	/** サービス設定 : Amazon Web Service SNS mobile push : 更新者自身にpush通知を行うかどうか */
	public static final String AWS_SNS_PUSH_SELF = "_aws.sns.push.self";
	/** サービス設定 : EMail通知設定 : 更新者自身にメール通知を行うかどうか */
	public static final String EMAIL_SEND_ONESELF = "_email.send.oneself";
	/** サービス設定 : EMail情報 */
	public static final String MAIL_PREFIX = "_mail.";
	/** 登録反映(apply)処理後、元のデータを削除するまでの待ち時間(秒) */
	public static final String APPLY_DELETE_WAITSEC = "_apply.delete.waitsec";
	/** IPアドレスホワイトリスト設定(サービス管理者) **/
	public static final String WHITE_REMOTEADDR_PREFIX = "_white.remoteaddress.";
	/** ユーザキャッシュの有効期間(分) **/
	public static final String USERCACHE_MINUTE = "_usercache.minute";
	/** BigQueryのプロジェクトID */
	public static final String BIGQUERY_PROJECTID = "_bigquery.projectid";
	/** BigQueryのデータセット名 */
	public static final String BIGQUERY_DATASET = "_bigquery.dataset";
	/** BigQueryのロケーション */
	public static final String BIGQUERY_LOCATION = "_bigquery.location";
	/** JSON出力においてfeed.entryを省略するかどうか */
	public static final String JSON_STARTARRAYBRACKET = "_json.startarraybracket";
	/** バッチジョブ **/
	public static final String BATCHJOB_PREFIX = "_batchjob.";

	/**
	 * サービスの情報が存在する場合、システムの情報を無視する設定一覧.
	 * <p>
	 * 条件を複数取得する、前方一致指定の項目に有効。
	 * 追加があればString配列に項目を追加すること。
	 * </p>
	 */
	public static final List<String> IGNORE_SYSTEM_IF_EXIST_SERVICE_INFO =
			Arrays.asList(new String[]{MAIL_PREFIX, BIGQUERY_PROJECTID, BIGQUERY_DATASET,
					BIGQUERY_LOCATION});

	/** ユーザ初期エントリー設定 : ユーザ番号に置き換える記号 */
	public static final String SETTING_USERINIT_UID = "#";

}
