package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.util.BQSQLGenerator;
import jp.sourceforge.reflex.exception.JSONException;

import org.junit.Test;

public class TestBQSQLGenerator {

	public static String entitytemplbq[] = {
		"bqtest{}",
		"user_parent{}",
		" parent_text",
		" user_child{}",
		"  child_text",
		"user_pine{}",
		" pine_text",
		" user_bamboo{}",
		"  bamboo_text",
		"  user_plum{}",
		"   plum_text",
		"user_paintset",
		" paintset_text",
		" user_palette",
		"  palette_text",
		"  palette_size",
		" user_paintbrush",
		"  paintbrush_text",
		"  user_measure",
		"   length_text",
		"   weight_text",
		" user_colors{}",
		"  colors_text"
	};

	private static String SECRETKEY = "testsecret123";

	@Test
	public void testBQSQLGenerator() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplbq, SECRETKEY);		
		BQSQLGenerator generator = new BQSQLGenerator();
		String schema = generator.generate(mp.getMetalist("service_name"),"test_dataset.test_listnum_table");
		System.out.println("\n=== BigQuery SQL ===");
		System.out.println(schema);
		
	}

}
