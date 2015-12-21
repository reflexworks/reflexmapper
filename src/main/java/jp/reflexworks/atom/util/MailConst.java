package jp.reflexworks.atom.util;

public interface MailConst {

	public static final String PROP_FROM = "mail.from";
	public static final String PROP_FROM_PERSONAL = "mail.from.personal";
	public static final String PROP_USER = "mail.user";
	public static final String PROP_PASSWORD = "mail.password";
	public static final String PROP_HOST = "mail.host";
	public static final String PROP_SMTP_HOST = "mail.smtp.host";
	public static final String PROP_SMTP_PORT = "mail.smtp.port";
	public static final String PROP_SMTP_STARTTLS = "mail.smtp.starttls.enable";
	public static final String PROP_TRANSPORT_PROTOCOL = "mail.transport.protocol";
	public static final String PROP_SMTP_AUTH = "mail.smtp.auth";
	public static final String PROP_DEBUG = "mail.debug";

	public static final String SMTP = "smtp";
	public static final String SMTPS = "smtps";

	public static final String CHARSET = "ISO-2022-JP";
	public static final String CONTENT_TYPE = "text/plain; charset=" + CHARSET;
	public static final String ENCODING = "B";	// B or Q

}
