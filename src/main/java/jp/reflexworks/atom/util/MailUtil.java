package jp.reflexworks.atom.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;

import jp.reflexworks.atom.api.MailConst;
import jp.sourceforge.reflex.util.StringUtils;

public class MailUtil implements MailConst {

	public static final Map<String, String> HEADERS = new HashMap<String, String>();
	static {
		HEADERS.put("Content-Transfer-Encoding", "7bit");
	}

	private static Logger logger = Logger.getLogger(MailUtil.class.getName());

	public void send(String title, String message, String to, String from, 
			String user, String password, String host, String port, String protocol,
			boolean isStarttls, boolean debug) 
	throws IOException {
		send(title, message, to, null, from, null, user, password, host, port, protocol, 
				isStarttls, debug);
	}

	public void send(String title, String message, String to, String toPersonal,
			String from, String fromPersonal, String user, String password, 
			String host, String port, String protocol, boolean isStarttls, boolean debug) 
	throws IOException {
		Properties props = new Properties();
		props.put(PROP_SMTP_HOST, host);
		props.put(PROP_HOST, host);
		props.put(PROP_FROM, from);
		props.put(PROP_SMTP_PORT, port);  // サブミッションポート
		props.put(PROP_SMTP_AUTH, "true");   // SMTP 認証を行う
		props.put(PROP_SMTP_STARTTLS, "true");   // STARTTLS
		if (debug) {
			props.put(PROP_DEBUG, "true");
		}

		send(title, message, to, toPersonal, from, fromPersonal, user, password, props, 
				protocol, debug);
	}

	public void send(String title, String message, String to, String from, String user, 
			String password, Properties props, String protocol, boolean debug) 
	throws IOException {
		send(title, message, to, null, from, null, user, password, props, protocol, debug);
	}

	public void send(String title, String message, String to, String toPersonal,
			String from, String fromPersonal, String user, String password, Properties props, 
			String protocol, boolean debug) 
	throws IOException {
		Session session = Session.getInstance(props);
		session.setDebug(debug);

		Transport transport = null;
		try {
			InternetAddress iaTo = new InternetAddress(to, toPersonal, CHARSET);
			InternetAddress iaFrom = new InternetAddress(from, fromPersonal, CHARSET);

			MimeMessage msg = new MimeMessage(session);

			// From
			msg.setFrom(iaFrom);
			// To
			msg.setRecipient(Message.RecipientType.TO, iaTo);
			// Subject
			msg.setSubject(title, CHARSET);
			// Date
			msg.setSentDate(new Date());
			// 本文
			/*
			//msg.setContent(message, CONTENT_TYPE);
			String encodeMessage = MimeUtility.encodeText(message, CHARSET, ENCODING);
			msg.setContent(encodeMessage, CONTENT_TYPE);
			 */

			MimeMultipart multipart = new MimeMultipart();
			msg.setContent(multipart);
			MimeBodyPart body = new MimeBodyPart();
			//body.setContent(message, CONTENT_TYPE);

			if (!StringUtils.isBlank(protocol)) {
				// 通常のメール送信
				String jisMsg = convertJIS(message);
				body.setContent(jisMsg, CONTENT_TYPE);
			} else {
				// GAE
				body.setText(message);
			}

			multipart.addBodyPart(body);

			// ヘッダ
			for (Map.Entry<String, String> mapEntry : HEADERS.entrySet()) {
				msg.setHeader(mapEntry.getKey(), mapEntry.getValue());
			}

			if (!StringUtils.isBlank(protocol)) {
				// protocolが設定されている場合、認証コネクト
				transport = session.getTransport(protocol);
				if (StringUtils.isBlank(user)) {
					user = from;
				}
				transport.connect(user, password);
				transport.sendMessage(msg, msg.getAllRecipients());

			} else {
				// protocolが設定されていない場合、そのままsendする。(GAE)
				Transport.send(msg);
			}

			//logger.info("send mail.");

		} catch (MessagingException e) {
			IOException ie = new IOException(e.getMessage());
			ie.initCause(e);
			throw ie;

		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException mex) {
					logger.warning("MessagingException: " + mex.getMessage());
				}
			}
		}
	}

	public String convertJIS(String msg) {
		try {
			byte[] convBytes = msg.getBytes(CHARSET);
			return new String(convBytes);
		} catch (UnsupportedEncodingException e) {
			logger.warning("UnsupportedEncodingException: " + e.getMessage());
		}
		return null;
	}

}
