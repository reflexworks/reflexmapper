package jp.reflexworks.atom.test;

import java.text.ParseException;

import jp.reflexworks.atom.util.R2BConverter;
import jp.sourceforge.reflex.exception.JSONException;

import org.junit.Test;

public class R2BConverterTest {

	public static String entitytempl[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"Idx 	",			  
		"email",
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
		"  music[3]=^.{5}$",			// 配列(要素数max3)
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

	@Test
	public void testR2BConverter() throws ParseException, JSONException {
		
		R2BConverter r2b = new R2BConverter();
		System.out.println(r2b.convert(entitytempl));

	}

}
