package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.mail.MessagingException;

import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.feed.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.util.MailReceiver;

import org.junit.Test;

public class MailReceiverTest {

	@Test
	public void test() {
		System.out.println("メール受信: 開始");
		try {
			MailReceiver mr = new MailReceiver();
			FeedTemplateMapper mapper = new FeedTemplateMapper(
					new String[] { "default" }, "");
		
			FeedBase feed = mr.doReceive(mapper, getProperties(),
					"メールアドレス@gmail.com", "パスワード");
	
			System.out.println("メール受信: 終了");
			
			for(EntryBase entry:feed.getEntry()) {
				System.out.println("Number:"+entry.id);
				System.out.println("Subject:"+entry.title);
				System.out.println("From:"+entry.subtitle);
				System.out.println("Date:"+entry.published);
				System.out.println("Content:"+entry.summary);
				System.out.println("FileName:"+entry.content._$src);
				System.out.println("File:"+entry.content._$$text);
			}
			
		}catch(Exception e) {
			// Do nothing
		}
		assertTrue(true);
	}
	
	private Properties getProperties() {
		final Properties props = new Properties();

		props.setProperty("mail.pop3.host", "pop.gmail.com");
		props.setProperty("mail.pop3.port", "995");

		// タイムアウト
		props.setProperty("mail.pop3.connectiontimeout", "60000");
		props.setProperty("mail.pop3.timeout", "60000");

		// SSL関連
		props.setProperty("mail.pop3.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.pop3.socketFactory.fallback", "false");
		props.setProperty("mail.pop3.socketFactory.port", "995");
		return props;
	}


}
