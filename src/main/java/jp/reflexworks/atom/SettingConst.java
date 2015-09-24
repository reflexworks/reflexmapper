package jp.reflexworks.atom;

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
	/** サービス設定 : Amazon Web Service SNS mobile push : 更新者自身にpush通知を行うかどうか */
	public static final String AWS_SNS_PUSH_SELF = "_aws.sns.push.self";
	/** サービス設定 : EMail通知設定 : 更新者自身にメール通知を行うかどうか */
	public static final String EMAIL_SEND_ONESELF = "_email.send.oneself";
	/** サービス設定 : EMailの送信元名 */
	public static final String MAIL_FROM_PERSONAL = "_mail.from.personal";
	
	/** ユーザ初期エントリー設定 : ユーザ番号に置き換える記号 */
	public static final String SETTING_USERINIT_UID = "#";

}
