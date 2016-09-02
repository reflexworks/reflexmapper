package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.entry.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.util.MailReceiver;
import jp.reflexworks.atom.util.SurrogateConverter;

import org.junit.Test;

public class SurrogateConverterTest {
	
	@Test
	public void test() {
		// basic
	    String src = "\\"+"uD840\\"+"uDC0B";         
        String tgt = new SurrogateConverter(src).convertUcs();
        System.out.println(tgt);

		// mix1
	    src = "あ"+"\\"+"uD840\\"+"uDC0Bお";         
        tgt = new SurrogateConverter(src).convertUcs();
        System.out.println(tgt);

		// mix2(codepoint+surrogate pair)
	    src = "あいうえ\\"+"u3042"+"お"+"\\"+"uD840\\"+"uDC0B";         
        tgt = new SurrogateConverter(src).convertUcs();
        System.out.println(tgt);

		// plain
	    src = "あいうえ";         
        tgt = new SurrogateConverter(src).convertUcs();
        System.out.println(tgt);

		// blank
	    src = "";         
        tgt = new SurrogateConverter(src).convertUcs();
        System.out.println(tgt);

		assertTrue(true);
	}
	
}
