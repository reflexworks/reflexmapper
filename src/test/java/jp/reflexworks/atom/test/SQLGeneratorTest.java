package jp.reflexworks.atom.test;

import java.text.ParseException;

import jp.reflexworks.atom.util.BQSchemaConverter;
import jp.reflexworks.atom.util.SQLGenerator;
import jp.sourceforge.reflex.exception.JSONException;

import org.junit.Test;

public class SQLGeneratorTest {

	public static String entitytempl[] = {
		"user_parent{}",
		" parent_text",
		" user_child{}",
		"  child_text",
		"user_pine{}",
		" pine_text",
		" user_bamboo{}",
		"  bamboo_text",
		"  user_plum{}",
		"   plum_text"
	};

	@Test
	public void testR2BConverter() throws ParseException, JSONException {
		
		SQLGenerator r2b = new SQLGenerator();
		System.out.println(r2b.convert(entitytempl));

	}

}
