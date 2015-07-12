package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.util.BQSchemaConverter;
import jp.sourceforge.reflex.exception.JSONException;

import org.junit.Test;

public class TestBQSchemaConverter {

	public static String entitytempl[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"default{2}",        //  0行目はパッケージ名(service名)
		"Idx 	",			  
		"email(string){5~30}",	// 5文字~30文字の範囲
		"verified_email(Boolean)",// Boolean型 他に（int,date,long,float,doubleがある。先小文字OK、省略時はString）
		"name",
		"given_name",
		"family_name",
		"error",
		" errors{}",				// 多重度(n)、*がないと多重度(1)、繰り返し最大{1}
		"  domain",
		"  reason",
		"  message",
		"  locationType",
		"  location",
		" code(int){1~100}",			// 1~100の範囲			
		" message",
		"subInfo",
		" favorite",
		"  food!=^.{3}$",	// 必須項目、正規表現つき
		"  music=^.{5}$",			// 配列(要素数max3)
		" favorite2",
		"  food",
		"   food1",
		" favorite3",
		"  food",
		"  updated(date)",
		" hobby{}",
		"  $$text",				// テキストノード
		"seq(desc)"
	};

	private static String SECRETKEY = "testsecret123";

	@Test
	public void testBQSchemaConverter() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		
		BQSchemaConverter converter = new BQSchemaConverter();
		String schema = converter.convert(mp.getMetalist("service_name"));
		System.out.println("\n=== BigQuery Schema ===");
		System.out.println(schema);
		
	}

}
