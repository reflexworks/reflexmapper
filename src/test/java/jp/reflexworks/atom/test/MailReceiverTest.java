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

	String[] propstr = {"mail.pop3.host=pop.gmail.com","mail.pop3.port=995","mail.pop3.connectiontimeout=60000",
			"mail.pop3.socketFactory.class=javax.net.ssl.SSLSocketFactory","mail.pop3.socketFactory.fallback=false"
			,"mail.pop3.socketFactory.port=995","username=XXXXX@gmail.com","password=XXXX"};
	
	@Test
	public void test() {
		System.out.println("メール受信: 開始");
		try {
			MailReceiver mr = new MailReceiver();
			FeedTemplateMapper mapper = new FeedTemplateMapper(
					new String[] { "default" }, "");
		
			FeedBase feed = mr.doReceive(mapper, propstr);

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
			e.printStackTrace();
			// Do nothing
		}
		assertTrue(true);
	}
	
}
