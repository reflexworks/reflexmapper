package jp.reflexworks.atom.test;

import jp.reflexworks.atom.util.BQJsonConverter;
import jp.sourceforge.reflex.IResourceMapper;
import jp.sourceforge.reflex.core.ResourceMapper;

import org.junit.Test;

public class TestBQJsonConverter {

	@Test
	public void test() {
		
		IResourceMapper mapper = new ResourceMapper("jp.reflexworks.test2.model");
		jp.reflexworks.test2.model.Entry entry = new jp.reflexworks.test2.model.Entry();
		
		entry.id="/aaa/bbb,1";
		entry.title ="Hello";
		
		String result = BQJsonConverter.toJSON(mapper, entry);
		
		System.out.println(result);
		
	}

}
