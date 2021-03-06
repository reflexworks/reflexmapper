package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.carrotsearch.sizeof.ObjectTree;

import jp.sourceforge.reflex.exception.JSONException;
import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.DeflateUtil;
import jp.sourceforge.reflex.util.FieldMapper;
import jp.sourceforge.reflex.util.FileUtil;
import jp.reflexworks.atom.api.AtomConst;
import jp.reflexworks.atom.api.Condition;
import jp.reflexworks.atom.api.EntryUtil;
import jp.reflexworks.atom.entry.Author;
import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.entry.Contributor;
import jp.reflexworks.atom.entry.FeedBase;
import jp.reflexworks.atom.entry.Link;
import jp.reflexworks.atom.mapper.BQJSONSerializer;
import jp.reflexworks.atom.mapper.CipherUtil;
import jp.reflexworks.atom.mapper.FeedTemplateConst;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;
import jp.reflexworks.atom.mapper.SizeLimitExceededException;
import jp.reflexworks.atom.util.SurrogateConverter;

public class TestMsgpackMapper {

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

	public static String entityAcls[] = {
		"title:/*",
		"Idx:/[0-9]+/(self|alias)",
		"error=@+RW,1+W,/grp1+RW,/grp3+RW",
		"subInfo.favorite.food#=@+W,1+W,/grp1+W,/grp3+RW,/grp4+R",
		"subInfo.favorite.music=@+W,1+W,/grp4+R,/grp1+W",
		"contributor=@+RW,/$admin+RW",
		"contributor.uri#",
		//		"contributor=@+RW",
		"rights#=@+RW,/$admin+RW"
	};

	public static String entityAcls2[] = {
		"title:/*",
		"Idx:/[0-9]+/(self|alias)",
		"error=@+RW,1+W,/grp1+RW,/grp3+RW",
		"subInfo.favorite.food#=@+W,1+W,/grp1+W,/grp3+RW,/grp4+R",
		"subInfo.favorite.music=@+W,1+W,/grp4+R,/grp1+W",
		"contributor=@+RW,/_group/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/_group/$admin+RW"
	};

	public static String entityAcls3[] = {
		"title:/*",
		"contributor=/@testservice/_group/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/@testservice/_group/$admin+RW"
	};

	public static String entityAcls4[] = {
		"title:/*",
		"Idx:/[0-9]+/(self|alias)",
		"error=@+RW,1+W,/grp1+RW,/grp3+RW",
		"subInfo.favorite.food#=@+W,1+W,/grp1+W,/grp3+RW,/grp4+R",
		"subInfo.favorite.music=@+W,1+W,/grp4+R,/grp1+W",
		"contributor=@+RW,/@/_group/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/@/_group/$admin+RW"
	};

	public static String entityAcls5[] = {
		"title:^/$|^/@XXXX$",
		"contributor=/_group/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/_group/$admin+RW",
		"info.category=/1/group/office+RW",
		"comment=7+RW",
		"info.name:^/index[^/]*$",
		"info.stock_int:^/index[^/]*$",
		"info.stock_long:^/index[^/]*$",
		"info.stock_float:^/index[^/]*$",
		"info.stock_double:^/index[^/]*$",
		"comment.secret#"
	};

	public static String entitytempl2[] = {
		// {}がMap, []がArray　, {} [] は末尾に一つだけ付けられる。*が必須項目
		"import{2}",        //  0行目はパッケージ名(service名)
		"Idx",			  
		"email",
		"verified_email(Boolean)",// Boolean型 他に（int,date,long,float,doubleがある。先小文字OK、省略時はString）
		"name",
		"given_name",
		"family_name",
		"error",
		" errors{1}",				// 多重度(n)、*がないと多重度(1)、繰り返し最大{1}
		"  domain",
		"  reason",
		"  message",
		"  locationType",
		"  location",
		" code(int){1~100}",			// 1~100の範囲			
		" message",
		" test",						// 追加項目
		"subInfo",
		" favorite",
		"  food!=^.{3}$",	// 必須項目、正規表現つき
		"  music=^.{5}$",			// 配列(要素数max3)
		" favorite2",
		"  food",
		"   food1",
		" favorite3",
		"  food",
		" hobby{}",
		"  $$text"				// テキストノード
	};

	public static String entitytemplp[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"default{2}",        //  0行目はパッケージ名(service名)
		"Idx",			  
		"public",
		" int",
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
		"  $$text"				// テキストノード
	};

	public static String entitytempl3[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"simple{100}",        //  0行目はパッケージ名(service名)
		"name",
		"brand",
		"size",
		"color",
		"price"
	};

	public static String entitytempl4[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"androidhello{100}",        //  0行目はパッケージ名(service名)
		"info",
		" name!",
		" category",
		" color",
		" size=^[a-zA-Z0-9]{1,2}$",
		" sale_boolean(boolean)",
		" stock_int(int)",
		" stock_long(long)",
		" stock_float(float)",
		//" stock_double(double){0~9999.99}",	// TODO issue #327 テスト用
		" stock_double(double)",
		" stock_string(string)",
		" stock_date(date)",
		//" stock_desc(desc)",
		" aaa(desc)",
		"comment{}",
		" $$text",
		" nickname",
		" secret",
		"deleteFlg",
	};

	public static String entitytempl4tmptype[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"mypackage{100}",        //  0行目はパッケージ名(service名)
		"info",
		" name!",
		" category",
		" color",
		" size=^[a-zA-Z0-9]{1,2}$",
		" sale_boolean(rdb_boolean)",
		" stock_int(rdb_int)",
		" stock_long(rdb_long)",
		" stock_float(rdb_float)",
		" stock_double(rdb_double)",
		" stock_string(rdb_string)",
		" stock_date(rdb_date)",
		" aaa(rdb_desc)",
		"comment{}",
		" $$text",
		" nickname",
		" secret",
		"deleteFlg",
	};

	public static String entitytempl5[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"mypackage{100}",        //  0行目はパッケージ名(service名)
		"info",
		" name!",
		" category",
		" color",
		" size=^[a-zA-Z0-9]{1,2}$",
		" sale_boolean(boolean)",
		" stock_int(int)",
		" stock_long(long)",
		" stock_float(float)",
		" stock_double(double)",
		" stock_string(string)",
		" stock_date(date)",
		" aaa(desc)",
		"comment{}",
		" $$text",
		" nickname",
		" secret",
		"deleteFlg",
		"layer_a1{}",
		" layer_a1_name",
		" layer_a2{}",
		"  layer_a2_name",
		"  layer_a2_value",
		"layer_b1_list",
		" layer_b1{}",
		"  layer_b1_name",
		"  layer_b2_list",
		"   layer_b2{}",
		"    layer_b2_name",
		"    layer_b2_value",
	};

	public static String entitytempl6[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"mypackage{100}",        //  0行目はパッケージ名(service名)
		"info",
		" item_1",
		" item_2",
		" item_3",
		"email",
		" before_message{}",
		"  message_no",
		"  before_message_text",
		" after_message{}",
		"  message_no",
		"  after_message_text",
		"addresss",
		" zip",
		" address"
	};

	public static String entitytemplf[] = {
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
			" code(float){1~100}",			// 1~100の範囲			
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

	private static boolean FEED = true;
	private static boolean ENTRY = false;
	private static String SECRETKEY = "testsecret123";

	/**
	 * 項目追加テスト用
	 * @param mp
	 * @param feed
	 */
	private static void editTestEntry(FeedTemplateMapper mp,Object feed)  {
		try {
			Field f = feed.getClass().getField("entry");
			List entrylist = (List) f.get(feed);	
			Object entry = entrylist.get(0);	

			f = entry.getClass().getField("error");
			Object error = f.get(entry);	

			f = error.getClass().getField("test");
			f.set(error, "<この項目が追加された>");		

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testJSONEntry() throws ParseException, JSONException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);

		System.out.println("JSON Entry デシリアライズ");
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);

		System.out.println("\n=== JSON Entry シリアライズ ===");
		String json2 = mp.toJSON(entry);
		System.out.println(json);
		System.out.println(json2);

		assertEquals(json, json2);
	}

	@Test
	public void testJSONEntryFloat() throws ParseException, JSONException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplf, entityAcls, 30, SECRETKEY);

		System.out.println("JSON Entry デシリアライズ");
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);

		System.out.println("\n=== JSON Entry シリアライズ ===");
		String json2 = mp.toJSON(entry);
		System.out.println(json);
		System.out.println(json2);

	}

	@Test
	public void testXMLEntry() throws ParseException, JSONException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl,entityAcls, 30, SECRETKEY);

		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);

		System.out.println("\n=== XML Entry シリアライズ ===");
		String xml = mp.toXML(entry);
		System.out.println(xml);

		System.out.println("\n=== XML Entry デシリアライズ ===");
		EntryBase entry2 = (EntryBase) mp.fromXML(xml);
		System.out.println(mp.toJSON(entry2));

		//		System.out.println("object1:"+ObjectTree.dump(entry));
		//		System.out.println("object2:"+ObjectTree.dump(entry2));

		assertEquals(json, mp.toJSON(entry2));
	}
	/*
	@Test
	public void testStaticGeneratedFeed() throws ParseException {
		// Generate
//		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl,entityAcls,30,"/Users/stakezaki/git/taggingservicecore/src/test/resources");		
		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		MODEL_PACKAGE.put("jp.reflexworks.atom.feed", "http://www.w3.org/2005/Atom");
		MODEL_PACKAGE.put("jp.reflexworks.atom.entry", "http://www.w3.org/2005/Atom");
		MODEL_PACKAGE.put("jp.reflexworks.atom.source", "http://www.w3.org/2005/Atom");
		MODEL_PACKAGE.put("_default", "vt=http://invoice.reflexworks.co.jp/vt/1.0");

		// Parse
		FeedTemplateMapper mp = new FeedTemplateMapper(MODEL_PACKAGE);

		Entry entry2 = new Entry();
		entry2._id = "xxx";
		entry2._family_name = "aaaa";
		entry2._link = new ArrayList();
		Link link = new Link();
		link._$href = "bbb";
		entry2._link.add(link);
		entry2._subInfo = new SubInfo();
		entry2._subInfo._favorite = new Favorite();
		entry2._subInfo._favorite._food = "xxx";

		String xml =  mp.toXML(entry2);
		System.out.println(xml);
	}
	 */
	@Test
	public void testMsgPackEntryWithDeflateAndValidate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		//DeflateUtil deflateUtil = new DeflateUtil();
		DeflateUtil deflateUtil = new DeflateUtil(Deflater.BEST_COMPRESSION, true);
		try {

			String json = "{\"entry\" : {\"id\" : \"/123/new,1\",\"content\" : {\"$type\" : \"image/gif\",\"$$text\" : \"あああ\"},\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
			//String json = "{\"entry\" : {\"id\" : \"/123/new,1\",\"rights\" : \"暗号化される\",\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"content\" : {\"$$text\" : \"あああ\"},\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
			EntryBase entry = (EntryBase) mp.fromJSON(json);
	
			/*
			Entry entry2 = new Entry();
			entry2._id = "xxx";
			entry2._family_name = "aaaa";
			entry2._link = new ArrayList();
			Link link = new Link();
			entry2._link.add(link);
			entry2._subInfo = new SubInfo();
			entry2._subInfo._favorite = new Favorite();
			entry2._subInfo._favorite._food = "xxx";
	
			String xml =  mp.toXML(entry2);
			System.out.println(xml);
			 */
	
			// MessagePack test
			System.out.println("\n=== MessagePack Entry シリアライズ ===");
			byte[] mbytes = mp.toMessagePack(entry);
			System.out.println("len:"+mbytes.length);
			for(int i=0;i<mbytes.length;i++) { 
				System.out.print(Integer.toHexString(mbytes[i]& 0xff)+" "); 
			} 
			System.out.println("\n=== MessagePack Entry deflate圧縮 ===");
			byte[] de = deflateUtil.deflate(mbytes);
			System.out.println("len:"+de.length+" 圧縮率："+(de.length*100/mbytes.length)+"%");
			for(int i=0;i<de.length;i++) { 
				System.out.print(Integer.toHexString(de[i]& 0xff)+" "); 
			} 
	
			System.out.println("\n=== MessagePack Entry infrate解凍 ===");
			byte[] in = deflateUtil.inflate(de);
			System.out.println("len:"+in.length);
			for(int i=0;i<in.length;i++) { 
				System.out.print(Integer.toHexString(in[i]& 0xff)+" "); 
			} 
	
			System.out.println("\n=== MessagePack Entry デシリアライズ ===");
	
			EntryBase  muserinfo = (EntryBase) mp.fromMessagePack(in,ENTRY);	// false でEntryをデシリアライズ
			List groups = new ArrayList<String>();
			groups.add("/@hoge/grp2");
			groups.add("/@hoge/grp1");
			groups.add("1");
			//        groups.add("/_group/$content");	// contentに書込できるグループ
			System.out.println("Validtion:"+muserinfo.validate("123",groups));	
	
			System.out.println("Before Masked:"+mp.toJSON(muserinfo));	
			muserinfo.maskprop("123",groups);	
			System.out.println("After  Masked:"+mp.toJSON(muserinfo));	
	
			assertNotSame(json, mp.toJSON(muserinfo));
		
		} finally {
			deflateUtil.end();
		}
	}

	@Test
	public void testFeedWithValidate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		//DeflateUtil deflateUtil = new DeflateUtil();

		// desc(降順プロパティ付き）
		String json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/@svc/123/new,1\",\"link\" : [{\"$title\" : \"署名\",\"$href\" : \"/@svc/123/allA/759188985520\",\"$rel\" : \"alternate\"}],\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}},{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}},\"seq\" :\"1\"}]}}";

		FeedBase feed = (FeedBase) mp.fromJSON(json);
		List groups = new ArrayList<String>();
		groups.add("/grp2");
		groups.add("/grp1");
		groups.add("1");
		groups.add("/$content");	// contentに書込できるグループ
		System.out.println("Validtion:"+feed.validate("123",groups));	

		System.out.println("Before Masked:"+mp.toJSON(feed));	
		//        feed.maskprop("123",groups);	
		System.out.println("After  Masked:"+mp.toJSON(feed));	

		assertNotSame(json, mp.toJSON(feed));
		
		// descにLongを超える文字を入れる
		boolean isParseException = false;
		json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/@svc/123/new,1\",\"link\" : [{\"$title\" : \"署名\",\"$href\" : \"/@svc/123/allA/759188985520\",\"$rel\" : \"alternate\"}],\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}},{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}},\"seq\" :\"9223372036854775904\"}]}}";
		feed = (FeedBase) mp.fromJSON(json);
		try {
			System.out.println("Validtion:"+feed.validate("123",groups));
		} catch (ParseException e) {
			isParseException = true;
		}
		assertTrue(isParseException);
	}

	@Test
	public void testArrayEntry() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);

		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);

		// MessagePack test
		System.out.println("\n=== Array Entry シリアライズ ===");

		// 項目名を省略した配列形式でもシリアライズ/デシリアライズ可能 (null は省略できない）
		// 一旦、toMessagaPack()でrawにした後、toArray()する
		byte[] mbytes = mp.toMessagePack(entry);
		String array = mp.toArray(mbytes).toString();

		System.out.println(array);
		EntryBase entity2 = (EntryBase) mp.fromArray(array,ENTRY);  // Entry

		System.out.println("\n=== Array Entry デシリアライズ ===");

		assertEquals(json, mp.toJSON(entity2));
	}

	@Test
	public void testChangeTemplateFeed() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		// 変更前
		FeedTemplateMapper mp2 = new FeedTemplateMapper(entitytempl2, SECRETKEY);	// 項目追加後	

		String json = "{ \"feed\" : {\"entry\" : [{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}},{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}]}}";
		FeedBase entry = (FeedBase) mp.fromJSON(json);
		byte[] mbytes = mp.toMessagePack(entry);	// mbytesは変更前のrawデータ

		// MessagePack test
		System.out.println("\n=== Array Feed(クラス変更後) シリアライズ ===");
		FeedBase entry2 = (FeedBase) mp2.fromMessagePack(mbytes,FEED);		
		editTestEntry(mp2,entry2);

		byte[] msgpack = mp2.toMessagePack(entry2);
		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 
		System.out.println();
		System.out.println(mp2.toArray(msgpack));

		System.out.println("\n=== XML Feed(クラス変更後) シリアライズ ===");
		String xml = mp2.toXML(entry2);
		System.out.println(xml);

		System.out.println("\n=== JSON Feed(クラス変更後) シリアライズ ===");
		String json2 = mp2.toJSON(entry2);
		System.out.println(json2);

		assertNotSame(json, json2);
	}

	@Test
	public void testMapEntry() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		
		String json = "{ \"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		// 正常ケース
		EntryBase entry = (EntryBase) mp.fromJSON(json);
		//entry.validate();	// TODO validate実装する。

		// エラーケース（errorsの数が２個）
		json = "{ \"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 101,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";
		entry = (EntryBase) mp.fromJSON(json);

		/*
		try {
			entry.validate();	// TODO validate実装する。
		} catch(Exception e) {
			// validateに失敗するとParseExceptionがスローされる
			System.out.println(e.getMessage());
		}
		 */
	}

	@Test
	public void testBooleanEntry() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);

		String json = "{\"entry\" : {\"verified_email\" : false}}";
		// 正常ケース
		EntryBase entry = (EntryBase) mp.fromJSON(json);
		json = json.replace("false", "true");
		entry = (EntryBase) mp.fromJSON(json);

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(entry);
		System.out.println(xml);

		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp.toMessagePack(entry);
		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 

		// 異常ケース
		try {
			json = json.replace("true", "\"true\"");
			entry = (EntryBase) mp.fromJSON(json);
		}catch (JSONException je) {
			System.out.println("\n=== test error === \n"+je.getMessage());
		}
		assertTrue(true);
	}

	@Test
	public void testTextNodeEntry() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, SECRETKEY);		// 変更前
		
		String json = "{\"entry\" : {\"public\" : {\"int\" : \"予約語\"},\"subInfo\" : {\"hobby\" : [{\"______text\" : \"テキストノード\\\"\\n\"}]},\"link\" : [{\"___href\" : \"/0762678511-/allA/759188985520\",\"___rel\" : \"self\"},{\"___href\" : \"/transferring/all/0762678511-/759188985520\",\"___rel\" : \"alternate\"},{\"___href\" : \"/0762678511-/@/spool/759188985520\",\"___rel\" : \"alternate\"},{\"___href\" : \"/0762678511-/historyA/759188985520\",\"___rel\" : \"alternate\"}]}}";
		//		String json = "{\"entry\" : {\"subInfo\" : {\"hobby\" : [{\"_$$text\" : \"テキストノード\"}]},\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}]}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(entry);
		System.out.println(xml);
		System.out.println("\n=== XML Entry(テキストノード+Link2) シリアライズ ===");
		System.out.println(mp.toJSON(mp.fromXML(xml)));

		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp.toMessagePack(entry);
		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 

		System.out.println(xml);
		System.out.println("====");
		System.out.println(mp.toJSON(mp.fromXML(xml)));
		System.out.println("====");
		System.out.println(json);
		
		assertEquals(json, mp.toJSON(mp.fromXML(xml)));
	}

	@Test
	public void testTextNodeFeed() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		// 変更前
		//		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl,null,30,"/Users/stakezaki/git/taggingservicecore/src/test/resources");		// 変更前

		// for static class test
		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		String NAMESPACE_VT = "vt=http://reflexworks.jp/test/1.0";
		String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
		MODEL_PACKAGE.put("jp.reflexworks.atom.feed", NAMESPACE_ATOM);
		MODEL_PACKAGE.put("jp.reflexworks.atom.entry", NAMESPACE_ATOM);
		MODEL_PACKAGE.put("_default", NAMESPACE_VT);
		//		FeedTemplateMapper mp = new FeedTemplateMapper(MODEL_PACKAGE);		

		String json = "{\"feed\" : {\"entry\" : [{\"subInfo\" : {\"hobby\" : [{\"______text\" : \"テキストノード\"}]},\"link\" : [{\"___href\" : \"/0762678511-/allA/759188985520\",\"___rel\" : \"self\"},{\"___href\" : \"/transferring/all/0762678511-/759188985520\",\"___rel\" : \"alternate\"},{\"___href\" : \"/0762678511-/@/spool/759188985520\",\"___rel\" : \"alternate\"},{\"___href\" : \"/0762678511-/historyA/759188985520\",\"___rel\" : \"alternate\"}]}]}}";
		FeedBase feed = (FeedBase) mp.fromJSON(json);

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(feed);
		System.out.println(xml);

		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp.toMessagePack(feed);
		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 


		assertEquals(json, mp.toJSON(mp.fromXML(xml)));
	}

	@Test
	public void testStaticPackages() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		String NAMESPACE_VT = "vt=http://reflexworks.jp/test/1.0";
		String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";

		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		MODEL_PACKAGE.put("jp.reflexworks.atom.feed", NAMESPACE_ATOM);
		MODEL_PACKAGE.put("jp.reflexworks.atom.entry", NAMESPACE_ATOM);
		//MODEL_PACKAGE.put("jp.reflexworks.atom.source", NAMESPACE_ATOM);
		MODEL_PACKAGE.put("jp.reflexworks.test.model", NAMESPACE_VT);

		FeedTemplateMapper mp = new FeedTemplateMapper(null, SECRETKEY, MODEL_PACKAGE);
		//		FeedTemplateMapper mp = new FeedTemplateMapper(new String[]{"jp.reflexworks.test.model"});		
		//DeflateUtil deflateUtil = new DeflateUtil();
		DeflateUtil deflateUtil = new DeflateUtil(Deflater.BEST_SPEED, true);

		try {
			String dataXmlFile = FileUtil.getResourceFilename("feed_test.txt");
			FileReader fi = new FileReader(dataXmlFile);
	
			// XMLにシリアライズ
			Date d1 = new Date();
			Object obj = mp.fromXML(fi);
	
			FeedBase feedobj = null;
			if (obj instanceof FeedBase) {
				feedobj = (FeedBase)obj;
			} else {
				System.out.println("The text is not feed : " + obj.getClass().getName());
			}
	
			Date d2 = new Date();
	
			Date d3 = new Date();
			String xml = mp.toXML(feedobj);
			Date d4 = new Date();
			System.out.println("\n=== XML Feed シリアライズ ===");
			System.out.println("xml size:"+xml.length()+" time:"+(d4.getTime()-d3.getTime()));
			System.out.println("\n=== XML Feed デシリアライズ ===");
			System.out.println("time:"+(d2.getTime()-d1.getTime()));
			System.out.println(xml);
	
			Date d5 = new Date();
			String json = mp.toJSON(feedobj);
			Date d6 = new Date();
			System.out.println("\n=== JSON Feed シリアライズ ===");
			System.out.println("json size:"+json.length()+" time:"+(d6.getTime()-d5.getTime()));
			System.out.println(json);
			Date d7 = new Date();
			Object json2 = mp.fromJSON(json);
			Date d8 = new Date();
			System.out.println("\n=== JSON Feed デシリアライズ ===");
			System.out.println("time:"+(d8.getTime()-d7.getTime()));
	
			//		System.out.println("object size:"+ObjectTree.dump(feedobj));
	
			System.out.println("\n=== Messagepack Feed シリアライズ ===");
			Date d9 = new Date();
			byte[] msgpack = mp.toMessagePack(feedobj);
			Date d10 = new Date();
			//        for(int i=0;i<msgpack.length;i++) { 
			//        	System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
			//        } 
			System.out.println("\nmsgpack size:"+msgpack.length+" time:"+(d10.getTime()-d9.getTime()));
			Date d11 = new Date();
			FeedBase msgpack2 = (FeedBase) mp.fromMessagePack(msgpack,true);
			Date d12 = new Date();
			System.out.println("\n=== Messagepack Feed デシリアライズ ===");
			System.out.println("time:"+(d12.getTime()-d11.getTime()));
	
			System.out.println("\n=== MessagePack Entry deflate圧縮 ===");
			Date d13 = new Date();
			byte[] de = deflateUtil.deflate(msgpack);
			Date d14 = new Date();
			System.out.println("defleted size:"+de.length+" 圧縮率(対msgpack)："+(de.length*100/msgpack.length)+"% 圧縮率(対json)："+(de.length*100/json.length())+"% 圧縮率(対xml)："+(de.length*100/xml.length())+"%");
			//        for(int i=0;i<de.length;i++) { 
			//        	System.out.print(Integer.toHexString(de[i]& 0xff)+" "); 
			//        } 
			System.out.println("time:"+(d14.getTime()-d13.getTime()));
			
		} finally {
			deflateUtil.end();
		}

		assertTrue(true);
	}

	@Test
	public void testStaticPackages2() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		MODEL_PACKAGE.putAll(AtomConst.ATOM_PACKAGE);
		// 名前空間の指定なし
		MODEL_PACKAGE.put("jp.reflexworks.test2.model", "");
		FeedTemplateMapper mp = new FeedTemplateMapper(null, SECRETKEY, MODEL_PACKAGE);

		jp.reflexworks.test2.model.Feed feed = createTest2Feed();
		String xml = mp.toXML(feed);
		System.out.println("--- testStaticPackages2 (to XML) ---");
		System.out.println(xml);
		FeedBase retFeed = (FeedBase)mp.fromXML(xml);
		System.out.println("--- testStaticPackages2 (from XML) ---");
		System.out.println(retFeed);

		String json = mp.toJSON(feed);
		System.out.println("--- testStaticPackages2 (to JSON) ---");
		System.out.println(json);
		retFeed = (FeedBase)mp.fromJSON(json);
		System.out.println("--- testStaticPackages2 (from JSON) ---");
		System.out.println(retFeed);

		byte[] msgData = mp.toMessagePack(feed);
		System.out.println("--- testStaticPackages2 (to MessagePack) ---");
		System.out.println(msgData);
		retFeed = (FeedBase)mp.fromMessagePack(msgData);
		System.out.println("--- testStaticPackages2 (from MessagePack) ---");
		System.out.println(retFeed);

		System.out.println("------");

		int idx = xml.indexOf("<test2:");
		assertTrue(idx == -1);
	}

	@Test
	public void testStaticPackages3() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		String PACKAGE_VT = "jp.reflexworks.testvt.model";
		String NAMESPACE_VT = "vt=http://reflexworks.jp/test/1.0";

		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		MODEL_PACKAGE.put(AtomConst.ATOM_PACKAGE_ENTRY, AtomConst.ATOM_NAMESPACE);
		MODEL_PACKAGE.put(PACKAGE_VT, NAMESPACE_VT);

		FeedTemplateMapper mp = new FeedTemplateMapper(null, SECRETKEY, MODEL_PACKAGE);

		jp.reflexworks.testvt.model.Column column = new jp.reflexworks.testvt.model.Column();
		column.vt$color = "red";
		column.vt$size = "M";
		column.vt$memo = "名前空間テスト";
		jp.reflexworks.testvt.model.Entry entry = new jp.reflexworks.testvt.model.Entry();
		entry.column = column;
		entry.vt$item1 = "Tシャツ";
		entry.vt$item2 = "半ズボン";
		Author author = new Author();
		author.uri = "urn:vte.cx:created:12";
		entry.author = new ArrayList<Author>();
		entry.author.add(author);
		entry.id = "/12/item/001,1";
		Link link = new Link();
		link._$rel = Link.REL_SELF;
		link._$href = "/12/item/001";
		entry.addLink(link);
		
		jp.reflexworks.testvt.model.Feed feed = new jp.reflexworks.testvt.model.Feed();
		feed.addEntry(entry);
		
		System.out.println("[testStaticPackages3] 名前空間テスト start");
		System.out.println("[testStaticPackages3] 名前空間テスト Entry XML 出力");
		System.out.println(mp.toXML(entry));
		System.out.println("[testStaticPackages3] 名前空間テスト Feed XML 出力");
		System.out.println(mp.toXML(feed));
		System.out.println("[testStaticPackages3] 名前空間テスト end");

	}

	private jp.reflexworks.test2.model.Feed createTest2Feed() {
		jp.reflexworks.test2.model.Feed feed = new jp.reflexworks.test2.model.Feed();
		feed.entry = new ArrayList<EntryBase>();

		String code = "100001";
		jp.reflexworks.test2.model.Entry entry = createTest2Entry(code);
		feed.entry.add(entry);
		code = "100002";
		entry = createTest2Entry(code);
		feed.entry.add(entry);

		return feed;
	}

	private jp.reflexworks.test2.model.Entry createTest2Entry(String code) {
		jp.reflexworks.test2.model.Entry entry = new jp.reflexworks.test2.model.Entry();
		entry.setMyUri("/1/item/" + code);
		entry.setTitle("商品" + code);
		entry.deleteFlg = "0";

		jp.reflexworks.test2.model.Info info = new jp.reflexworks.test2.model.Info();
		info.name = "えんぴつ";
		info.color = "緑";
		info.size = "15cm";
		info.category = "文房具";
		entry.info = info;

		List<jp.reflexworks.test2.model.Comment> comments = new ArrayList<jp.reflexworks.test2.model.Comment>();
		jp.reflexworks.test2.model.Comment comment = new jp.reflexworks.test2.model.Comment();
		comment.nickname = "なまえ1";
		comment._$$text = "普通のえんぴつです。";
		comments.add(comment);
		comment = new jp.reflexworks.test2.model.Comment();
		comment.nickname = "なまえ2";
		comment._$$text = "良い感じのえんぴつです。";
		comments.add(comment);
		entry.comment = comments;

		return entry;
	}


	@Test
	public void testDefaultAtom() 
			throws ParseException, JSONException, IOException, ClassNotFoundException {

		// default
		FeedTemplateMapper defmp = new FeedTemplateMapper(new String[]{"default"}, SECRETKEY);
		//String defjson = "{\"entry\" : {\"title\" : \"Titleテスト\",\"subtitle\" : \"Subtitleテスト\"}}";
		String defjson = "{\"entry\" : {\"updated\" : null}}";
		String defjsonFeed = "{ \"feed\" : {\"entry\" : [null]}}";

		EntryBase defentry = (EntryBase) defmp.fromJSON(defjson);
		FeedBase deffeed = (FeedBase) defmp.fromJSON(defjsonFeed);

		System.out.println("\n=== [default] MessagePack Entry シリアライズ ===");
		byte[] defbytes = defmp.toMessagePack(defentry);
		for(int i=0;i<defbytes.length;i++) { 
			//System.out.print(Integer.toHexString(defbytes[i]& 0xff)+" "); 
			//System.out.print(Integer.toHexString(defbytes[i])+" ");	// 符号(先頭ビット)が1の場合、上位ビットも1になる。
			System.out.print(String.format("%02X", defbytes[i])); 
		} 

		System.out.println("\n=== [default] MessagePack Feed シリアライズ ===");
		byte[] defbytesfeed = defmp.toMessagePack(deffeed);
		for(int i=0;i<defbytesfeed.length;i++) { 
			//System.out.print(Integer.toHexString(defbytes[i]& 0xff)+" "); 
			//System.out.print(Integer.toHexString(defbytes[i])+" ");	// 符号(先頭ビット)が1の場合、上位ビットも1になる。
			System.out.print(String.format("%02X", defbytesfeed[i])); 
		} 

		System.out.println("\n=== [default] MessagePack Entry デシリアライズ ===");
		EntryBase defentry2 = (EntryBase) defmp.fromMessagePack(defbytes, ENTRY);	// false でEntryをデシリアライズ
		String title = null;
		if (defentry2 != null) {
			title = defentry2.title;
		}
		System.out.println("\n=== [default] title=" + title);

		// template
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"}}}}";

		EntryBase entry = (EntryBase) mp.fromJSON(json);
		System.out.println("\n=== [template] xml=\n" + defmp.toXML(entry));

		System.out.println("\n=== [template] MessagePack Entry シリアライズ ===");
		byte[] mbytes = mp.toMessagePack(entry);
		for(int i=0;i<mbytes.length;i++) { 
			System.out.print(Integer.toHexString(mbytes[i]& 0xff)+" "); 
		} 

		System.out.println("\n=== [template] MessagePack Entry デシリアライズ ===");
		EntryBase muserinfo = (EntryBase) mp.fromMessagePack(mbytes, ENTRY);	// false でEntryをデシリアライズ
		System.out.println("\n=== [template] muserinfo=" + mp.toJSON(muserinfo));

		System.out.println("\n=== [template] MessagePack default Entry デシリアライズ ===");
		EntryBase defmuserinfo = (EntryBase) mp.fromMessagePack(defbytes, ENTRY);	// false でEntryをデシリアライズ
		System.out.println("\n=== [template] muserinfo=" + mp.toJSON(defmuserinfo));

		// default (2回目)
		defentry = (EntryBase) defmp.fromJSON(defjson);

		System.out.println("\n=== [default (2回目)] MessagePack Entry シリアライズ ===");
		defbytes = defmp.toMessagePack(defentry);

		System.out.println("\n=== [default (2回目)] MessagePack Entry デシリアライズ ===");
		defentry2 = (EntryBase) defmp.fromMessagePack(defbytes, ENTRY);	// false でEntryをデシリアライズ
		if (defentry2 != null) {
			title = defentry2.title;
		}
		System.out.println("\n=== [default (2回目)] title=" + title);

		// template (空オブジェクト)
		String msgEntry16 = "DC0020C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";
		String msgFeed16 = "DC0011C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";

		byte[] emptyBytes = DatatypeConverter.parseHexBinary(msgEntry16);
		System.out.println("\n=== [template] MessagePack empty Entry デシリアライズ ===");
		for(int i=0;i<emptyBytes.length;i++) { 
			System.out.print(String.format("%02X", emptyBytes[i])); 
		} 
		EntryBase emptyEntry = (EntryBase) mp.fromMessagePack(emptyBytes, ENTRY);	// false でEntryをデシリアライズ
		System.out.println("\n=== [template] emptyEntry=" + emptyEntry);

		emptyBytes = DatatypeConverter.parseHexBinary(msgFeed16);

		byte MSGPACK_PREFIX = -36;

		System.out.println("\n=== [template] MessagePack empty Feed デシリアライズ ===");
		for(int i=0;i<emptyBytes.length;i++) { 
			System.out.print(String.format("%02X", emptyBytes[i])); 
		} 
		FeedBase emptyFeed = (FeedBase) mp.fromMessagePack(emptyBytes, FEED);	// true でFeedをデシリアライズ
		System.out.println("\n=== [template] emptyFeed=" + emptyFeed);

		assertEquals(MSGPACK_PREFIX, emptyBytes[0]);
	}

	@Test
	public void testBasicFeed() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(new String[] {"_"}, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_

		//		String json = "{\"feed\" : {\"entry\" : [{\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"}]}]}}";
		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"\"}]}}";
		//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
		//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
		//		String json = "{ \"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

		FeedBase feed = (FeedBase) mp.fromJSON(json);
		//		feed._$xmlns = "1";
		//		feed._$xmlns$rx = "2";
		//		feed.author = new ArrayList<jp.reflexworks.atom.feed.Author>();

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(feed);
		System.out.println(xml);
		
		feed.entry.get(0).title=null;
		
		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp.toMessagePack(feed);

		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 
		
		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		msgpack = mp.toMessagePack(feed.entry.get(0));

		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 

		System.out.println("\n=== Array シリアライズ ===");
		String array = (String) mp.toArray(msgpack).toString();
		System.out.println(array);

//		FeedBase feed2 = (FeedBase) mp.fromMessagePack(msgpack,FEED);
		FeedBase feed2 = (FeedBase) mp.fromMessagePack(FeedTemplateConst.MSGPACK_BYTES_FEED,FEED);
		EntryBase entry2 = (EntryBase) mp.fromMessagePack(FeedTemplateConst.MSGPACK_BYTES_ENTRY,ENTRY);

		System.out.println(mp.toJSON(mp.fromXML(xml)));
		System.out.println(json);

		assertTrue(true);
	}

	@Test
	public void testArrayFeed2() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_

		//		String json = "{\"feed\" : {\"entry\" : [{\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"}]}]}}";
		//		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"test\"}]}}";
		//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
		//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
		//		String json = "{ \"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

		//		String json = "{\"feed\" : {\"entry\" : [{ \"title\" : \"hello\", \"subInfo\" : { \"favorite2\": { \"food\" : { \"food1\" : \"ラーメン\"}}},\"link\" : [{\"href\":\"xxx\",\"title\":\"yyy\"},{\"href\":\"aaa\",\"title\":\"bbb\"},{\"href\":\"ccc\",\"title\":\"ddd\"}] }]}}";
		String json = "{\"feed\" : {\"entry\" : [{ \"family_name\" : \"f\",\"Idx\" : \"1\",\"title\" : \"hello\", \"subInfo\" : { \"favorite2\": { \"food\" : { \"food1\" : \"ラーメン\"}}},\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}] }]}}";

		FeedBase feed = (FeedBase) mp.fromJSON(json);
		//		feed._$xmlns = "1";
		//		feed._$xmlns$rx = "2";
		//		feed.author = new ArrayList<jp.reflexworks.atom.feed.Author>();

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(feed);
		System.out.println(xml);


		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp.toMessagePack(feed);

		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 
		System.out.println("\n=== Array シリアライズ ===");
		String array = (String) mp.toArray(msgpack).toString();
		System.out.println(array);

		FeedBase feed2 = (FeedBase) mp.fromMessagePack(msgpack,FEED);

		System.out.println(mp.toJSON(mp.fromXML(xml)));
		System.out.println(json);

		System.out.println(mp.toArray(msgpack));

		assertTrue(true);
	}

	@Test
	public void testIgnoreCustomTag() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp0 = new FeedTemplateMapper(new String[] {"_"}, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_
		FeedTemplateMapper mp1 = new FeedTemplateMapper(entitytempl, SECRETKEY);		// 変更前

		String json1 = "{\"entry\" : {\"subInfo\" : {\"hobby\" : [{\"$$text\" : \"テキストノード\"}]},\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}],\"updated\" : \"2013-10-22T10:50:30+09:00\"}}";
		EntryBase entry = (EntryBase) mp1.fromJSON(json1);

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp1.toXML(entry);
		System.out.println(xml);

		System.out.println("\n=== Messagepack Entry シリアライズ ===");
		byte[] msgpack = mp1.toMessagePack(entry);
		for(int i=0;i<msgpack.length;i++) { 
			System.out.print(Integer.toHexString(msgpack[i]& 0xff)+" "); 
		} 
		System.out.print("\n"+Integer.toHexString(msgpack[22]& 0xff)+" "); 

		// 2番目に0x27(本来は0x2e)を入れることでATOM標準Feedとしてデシリアライズできる
		//msgpack[2] = 0x27;
		msgpack[2] = 0x10;

		EntryBase entry2 = (EntryBase) mp0.fromMessagePack(msgpack,ENTRY);
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml2 = mp0.toXML(entry2);
		System.out.println(xml2);


	}

	@Test
	public void testGetSetvalue() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException, SizeLimitExceededException {
		//		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl);		// ATOM Feed/Entryのみ。パッケージは_
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls, 30, SECRETKEY);

		//String json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/1/new,1\",\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"2014/10/03\"}],\"category\" : [{\"$term\":\"term1\"},{\"$scheme\":\"scheme1\"},{\"$label\":\"label1\"}],\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"public\" : {\"int\":\"email1\"},\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";
		String json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/1/new,1\",\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"2014/10/03\"}],\"category\" : [{\"$term\":\"term1\"},{\"$scheme\":\"scheme1\"},{\"$label\":\"label1\"}],\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\",\"$type\" : \"index1\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\",\"$type\" : \"index3\"}],\"title\" : \"タイトル\",\"public\" : {\"int\":\"email1\"},\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : \"ポップス1\"},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

		//		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"123\"}]}}";
		FeedBase feed = (FeedBase) mp.fromJSON(json);

		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(feed);
//		System.out.println(xml);

		FeedBase feed2 = (FeedBase) mp.fromXML(xml);
		List groups = new ArrayList<String>();
		groups.add("/$admin");
		groups.add("/@default/_group/$content");
		groups.add("1");
		System.out.println("Validtion:"+feed2.validate("123",groups));	

		EntryBase entry = feed.entry.get(0);
		System.out.println("\n==== getValue test ====");
		System.out.println("email value="+entry.getValue("email"));
		System.out.println("verified_email value="+entry.getValue("verified_email"));
		System.out.println("error.errors.domain value="+entry.getValue("error.errors.domain"));
		System.out.println("subInfo.favorite.food value="+entry.getValue("subInfo.favorite.food"));
		System.out.println("element value="+entry.getValue("element")+" <= Arrayは取れなくてよいか");
		System.out.println("subInfo.favorite3.food value="+entry.getValue("subInfo.favorite3.food"));
		System.out.println("subInfo.favorite3.updated value="+entry.getValue("subInfo.favorite3.updated"));
		System.out.println("title(ATOM Entry) value="+entry.getValue("title"));
		System.out.println("link(ATOM Entry) $href value="+entry.getValue("link.$href"));
		System.out.println("link(ATOM Entry) $type value="+entry.getValue("link.$type"));
		System.out.println("link(ATOM Entry) email value="+entry.getValue("contributor.email"));
		System.out.println("link(ATOM Entry) uri value="+entry.getValue("contributor.uri"));
		System.out.println("link(ATOM Entry) name value="+entry.getValue("contributor.name"));

		Object objLinkHref = entry.getValue("link.$href");
		System.out.println("[EntryBase#getValue] link.$href class = " + objLinkHref.getClass().getName());
		Object objLink = entry.getValue("link");
		if (objLink != null) {
			System.out.println("[EntryBase#getValue] link class = " + objLink.getClass().getName());
		} else {
			System.out.println("[EntryBase#getValue] link object is null.");
		}
		
		System.out.println("entry size="+entry.getsize());
		String bqjson = BQJSONSerializer.toJSON(mp, entry);
//		String bqjson = mp.toJSON(entry);
		System.out.println(bqjson);
		System.out.println("entry json size="+bqjson.length());
		
		// TODO contributor
		Contributor contributor = new Contributor();
		entry.contributor.add(contributor);
		contributor.uri = getAclUrn("888", "CRUD");
		contributor.name = "テストネーム";
		contributor = new Contributor();
		entry.contributor.add(contributor);
		contributor.uri = getAclUrn("889", "CRUD");
		contributor.name = "てすと";

		Cipher cipher = (new CipherUtil()).getInstance();
		System.out.println("---(before encrypted)---");
		System.out.println(mp.toXML(entry));
		System.out.println("--------------");
		entry.encrypt(cipher);
		System.out.println("Encrypted subInfo.favorite.food value="+entry.getValue("subInfo.favorite.food"));
		System.out.println("---(after encrypted)---");
		System.out.println(mp.toXML(entry));
		System.out.println("--------------");
		entry.decrypt(cipher);
		System.out.println("Decrypted subInfo.favorite.food value="+entry.getValue("subInfo.favorite.food"));
		System.out.println("---(after decrypted)---");
		System.out.println(mp.toXML(entry));
		System.out.println("--------------");

		Condition[] conditions = new Condition[21];

		conditions[0] = new Condition("subInfo.favorite.food", "カレー");
		conditions[1] = new Condition("subInfo.favorite3.food", "うどん");
		conditions[2] = new Condition("verified_email", "false");		// boolean
		conditions[3] = new Condition("subInfo.favorite3.updated", "2013-09-30 14:06:30");	// date(Tや+09:00は省略可能)
		conditions[4] = new Condition("error.errors.domain", "com.google.auth2");	// List検索
		conditions[5] = new Condition("error.errors.domain", "com.google.auth");	// List検索(全て合致すればtrue)
		conditions[6] = new Condition("title-rg-^タイトル$");							// 正規表現検索
		conditions[7] = new Condition("content.$$text", "あああ");
		conditions[8] = new Condition("contributor.email", "abc@def");
		conditions[9] = new Condition("contributor.uri", "http://abc");
		conditions[10] = new Condition("contributor.name", "hoge");
		conditions[11] = new Condition("category.$term", "term1");
		conditions[12] = new Condition("category.$scheme", "scheme1");
		conditions[13] = new Condition("category.$label", "label1");
		conditions[14] = new Condition("link.$href", "/0762678511-/@/spool/759188985520");	// ATOM標準Entry List検索
		conditions[15] = new Condition("author.email", "xyz@def");
		conditions[16] = new Condition("author.uri", "http://xyz");
		conditions[17] = new Condition("author.name-le-2014/10/04");
		conditions[18] = new Condition("public.int", "email1");	// java予約語項目
		conditions[19] = new Condition("subInfo.favorite.music", "ポップス1");	// java予約語項目
		conditions[20] = new Condition("author.name-ge-2014/10/03");

		boolean ismatch = entry.isMatch(conditions);
		System.out.println("isMatch="+ismatch);

		assertTrue(ismatch);
	}

	@Test
	public void testPrecheckTemplate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		String entitytempl_new[] = {
				// {}がMap, #がIndex , []がArray　, {} # [] は末尾に一つだけ付けられる。*が必須項目
				"default{2}",        //  0行目はパッケージ名(service名)
				"Idx",			  // Index
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
				"    test4",		// 子要素の追加はOK
				"   test5",			// 同じ階層の最後尾に追加はOK
				" favorite3",
				"  food",
				"   test6",
				"  test3(date)",	// 元と同じタイプであればOK
				"  updated(date)",
				"  test2",
				" hobby",			//{}を外すのはOK
				"  $$text",				// テキストノード
				"seq(desc)",
				"test1"				// 最後尾に追加はOK
		};

		FeedTemplateMapper mp0 = new FeedTemplateMapper(new String[] {"_"}, SECRETKEY);	
		boolean precheck = mp0.precheckTemplate(entitytempl, entitytempl_new);
		System.out.println("precheck:"+precheck);

		// 全く同じ内容のテスト
		String[] tmpl1 = new String[]{"androidservice{100}", "info", " name", " category", " color", " size", "comment{}", " $$text", " nickname", "deleteFlg", "deleteFlg2"};
		String[] tmpl2 = new String[]{"androidservice{100}", "info", " name", " category", " color", " size", "comment{}", " $$text", " nickname", "deleteFlg", "deleteFlg2"};

		precheck = mp0.precheckTemplate(tmpl1, tmpl2);

		assertTrue(precheck);
	}

	@Test
	public void testMsgPackFeedWithDeflate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		//DeflateUtil deflateUtil = new DeflateUtil();
		DeflateUtil deflateUtil = new DeflateUtil(Deflater.BEST_SPEED, true);

		try {
			String json = "{\"feed\" : {\"entry\" : [{ \"verified_email\": false,\"family_name\" : \"f\",\"Idx\" : \"1\",\"title\" : \"hello\", \"subInfo\" : { \"favorite2\": { \"food\" : { \"food1\" : \"ラーメン\"}}},\"link\" : [{\"$href\" : \"/test/1\",\"$rel\" : \"self\"}] }]}}";
	
			FeedBase feed = (FeedBase) mp.fromJSON(json);
	
			// MessagePack test
			System.out.println("\n=== MessagePack Feed シリアライズ ===");
			byte[] mbytes = mp.toMessagePack(feed);
			System.out.println("len:"+mbytes.length);
			
			
			byte[] testBytes = FeedTemplateConst.MSGPACK_BYTES_FEED;
			assertTrue(EntryUtil.isMessagePack(testBytes));
			
			System.out.println("array:"+ mp.toArray(mbytes));
			for(int i=0;i<mbytes.length;i++) { 
				System.out.print(Integer.toHexString(mbytes[i]& 0xff)+" "); 
			} 
			System.out.println("\n=== MessagePack Feed deflate圧縮 ===");
			byte[] de = deflateUtil.deflate(mbytes);
			System.out.println("len:"+de.length+" 圧縮率："+(de.length*100/mbytes.length)+"%");
			for(int i=0;i<de.length;i++) { 
				System.out.print(Integer.toHexString(de[i]& 0xff)+" "); 
			} 
	
			System.out.println("\n=== MessagePack Feed infrate解凍 ===");
			byte[] in = deflateUtil.inflate(de);
			System.out.println("len:"+in.length);
			for(int i=0;i<in.length;i++) { 
				System.out.print(Integer.toHexString(in[i]& 0xff)+" "); 
			} 

			FeedBase muserinfo = (FeedBase) mp.fromMessagePack(in,FEED);	// false でEntryをデシリアライズ

			System.out.println("\n=== MessagePack Feed infrate解凍 Feedオブジェクトに戻す ===");
			System.out.println(muserinfo);

		} finally {
			deflateUtil.end();
		}
	}

	@Test
	public void testMaskprop() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls2, 30, SECRETKEY);

		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}]}]}}";

		FeedBase feed = (FeedBase)mp.fromJSON(json);
		String xml = null;

		// maskprop test
		//String uid = "6";
		String uid = "7";
		List<String> groups = new ArrayList<String>();

		// グループ参加なし
		feed.maskprop(uid, groups);
		System.out.println("\n=== maskprop (グループ参加なし) ===");
		xml = mp.toXML(feed);
		System.out.println(xml);

		// 結果判定
		boolean isMatch = false;
		if (feed != null && feed.entry != null && feed.entry.size() > 0) {
			EntryBase entry0 = feed.entry.get(0);
			//if (entry0.contributor == null) {
			if (entry0.contributor != null) {
				isMatch = true;
			}
		}

		// 別グループに参加
		feed = (FeedBase)mp.fromJSON(json);
		groups.add("/othergroup");
		feed.maskprop(uid, groups);
		System.out.println("\n=== maskprop (別グループに参加) ===");
		xml = mp.toXML(feed);
		System.out.println(xml);

		// /$admin グループ
		feed = (FeedBase)mp.fromJSON(json);
		groups.add("/@testservice/_group/$admin");
		feed.maskprop(uid, groups);
		System.out.println("\n=== maskprop (/@testservice/$admin グループ) ===");
		xml = mp.toXML(feed);
		System.out.println(xml);

		System.out.println("entry size="+feed.entry.get(0).getsize());
		System.out.println("entry json size="+mp.toJSON(feed.entry.get(0)).length());

		assertTrue(isMatch);
	}

	@Test
	public void testMaskprop2() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls4, 30, SECRETKEY);
		//FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls2, 30, SECRETKEY);

		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@/_system/admin,2\",\"link\" : [{\"$href\" : \"/@/_system/admin\",\"$rel\" : \"self\"}],\"rights\" : \"key=value\nprop=暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}]}]}}";
		//String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/1/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/1/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}]}]}}";

		FeedBase feed = (FeedBase)mp.fromJSON(json);
		String xml = null;

		// maskprop test
		String uid = "0";
		//String uid = "1";
		//String uid = "6";
		List<String> groups = new ArrayList<String>();
		
		// 管理者権限
		groups.add("/@/_group/$admin");
		groups.add("/@/_group/$content");
		//groups.add("/@testservice/_group/$admin");
		//groups.add("/@testservice/_group/$content");

		feed.maskprop(uid, groups);
		System.out.println("\n=== maskprop (管理者権限) ===");
		xml = mp.toXML(feed);
		System.out.println(xml);

		// 結果判定
		boolean isMatch = false;
		if (feed != null && feed.entry != null && feed.entry.size() > 0) {
			EntryBase entry0 = feed.entry.get(0);
			//if (entry0.contributor == null) {
			if (entry0.rights != null) {
				isMatch = true;
			}
		}

		assertTrue(isMatch);
	}

	@Test
	public void testXmlFormat() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp3 = new FeedTemplateMapper(entitytempl3, entityAcls3, 30, SECRETKEY);
		FeedTemplateMapper mp0 = new FeedTemplateMapper(new String[] {"simple"}, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_

		//String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}]}]}}";
		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"POST\",\"subtitle\" : \"201\",\"summary\" : \"Registered.\"}]}}";

		FeedBase feed0 = (FeedBase)mp0.fromJSON(json);
		String xml = null;

		System.out.println("\n=== デフォルトMapperで作成したオブジェクトを、Template mapperでシリアライズ ===");
		xml = mp3.toXML(feed0);
		System.out.println(xml);

		boolean isMatch = false;
		if (xml.indexOf("_") == -1) {
			isMatch = true;
		}

		assertTrue(isMatch);
	}

	@Test
	public void testFieldMapper() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls3, 30, SECRETKEY);

		String json = createJsonTempl4_1();
		FeedBase feed1 = (FeedBase)mp4.fromJSON(json);
		json = createJsonTempl4_2();
		FeedBase feed2 = (FeedBase)mp4.fromJSON(json);

		// feed1の先頭エントリー(target)と、feed2の先頭エントリー(source)の内容を比較し、
		// 異なっていればtargetにsourceの内容をセットする。
		EntryBase targetEntry = feed1.entry.get(0);
		EntryBase sourceEntry = feed2.entry.get(0);

		// バックアップ
		String sDeleteFlg = (String)sourceEntry.getValue("deleteFlg");
		String sInfoName = (String)sourceEntry.getValue("info.name");
		List<String> sCommentText = (List<String>)sourceEntry.getValue("comment.$$text");
		String tDeleteFlg = (String)targetEntry.getValue("deleteFlg");
		String tInfoName = (String)targetEntry.getValue("info.name");
		List<String> tCommentText = (List<String>)targetEntry.getValue("comment.$$text");

		System.out.println("--- 実行前 ---");
		System.out.println("sDeleteFlg = " + sDeleteFlg);
		System.out.println("sInfoName = " + sInfoName);
		System.out.println("sCommentText = " + sCommentText);
		System.out.println("tDeleteFlg = " + tDeleteFlg);
		System.out.println("tInfoName = " + tInfoName);
		System.out.println("tCommentText = " + tCommentText);

		// 項目移送
		FieldMapper fieldMapper = new FieldMapper(true);
		fieldMapper.setValue(sourceEntry, targetEntry);

		// 結果チェック
		System.out.println("--- 実行後 ---");
		tDeleteFlg = (String)targetEntry.getValue("deleteFlg");
		tInfoName = (String)targetEntry.getValue("info.name");
		tCommentText = (List<String>)targetEntry.getValue("comment.$$text");

		System.out.println("tDeleteFlg = " + tDeleteFlg);
		System.out.println("tInfoName = " + tInfoName);
		System.out.println("tCommentText = " + tCommentText);

		// DeleteFlgとInfoNameをチェック
		assertTrue(sDeleteFlg.equals(tDeleteFlg) && sInfoName.equals(tInfoName));
		
		// コメントをチェック
		assertArrayEquals(sCommentText.toArray(), tCommentText.toArray());

	}

	private String createJsonTempl4_1() {
		StringBuilder buf = new StringBuilder();
		buf.append("{\"feed\" : {");
		buf.append("\"entry\" : [");
		buf.append("{");
		buf.append("\"comment\" : [");
		buf.append("{\"$$text\" : \"普通のえんぴつです。\",\"nickname\" : \"なまえ1\"},");
		buf.append("{\"$$text\" : \"良い感じのえんぴつです。\",\"nickname\" : \"なまえ2\"}],");
		buf.append("\"deleteFlg\" : \"0\",");
		buf.append("\"info\" : {");
		buf.append("\"category\" : \"文房具\",");
		buf.append("\"color\" : \"緑\",");
		buf.append("\"name\" : \"えんぴつ\",");
		buf.append("\"size\" : \"15cm\"},");
		buf.append("\"link\" : [{\"$href\" : \"/1/item/100001\",\"$rel\" : \"self\"}],");
		buf.append("\"title\" : \"商品100001\"");
		buf.append("},");
		buf.append("{");
		buf.append("\"comment\" : [");
		buf.append("{\"$$text\" : \"普通のえんぴつです。\",\"nickname\" : \"なまえ1\"},");
		buf.append("{\"$$text\" : \"良い感じのえんぴつです。\",\"nickname\" : \"なまえ2\"}],");
		buf.append("\"deleteFlg\" : \"0\",");
		buf.append("\"info\" : {");
		buf.append("\"category\" : \"文房具\",");
		buf.append("\"color\" : \"緑\",");
		buf.append("\"name\" : \"えんぴつ\",");
		buf.append("\"size\" : \"15cm\"},");
		buf.append("\"link\" : [{\"$href\" : \"/1/item/100002\",\"$rel\" : \"self\"}],");
		buf.append("\"title\" : \"商品100002\"");
		buf.append("}");
		buf.append("]}}");
		return buf.toString();
	}

	private String createJsonTempl4_2() {
		StringBuilder buf = new StringBuilder();
		buf.append("{\"feed\" : {");
		buf.append("\"entry\" : [");
		buf.append("{");
		buf.append("\"comment\" : [");
		buf.append("{\"$$text\" : \"コメント修正。\",\"nickname\" : \"なまえ修正\"},");
		buf.append("{\"$$text\" : \"良い感じのえんぴつです。\",\"nickname\" : \"なまえ2\"}],");
		buf.append("\"deleteFlg\" : \"delete update\",");
		buf.append("\"info\" : {");
		buf.append("\"category\" : \"カテゴリ修正\",");
		buf.append("\"color\" : \"色修正\",");
		buf.append("\"name\" : \"名前修正\",");
		buf.append("\"size\" : \"15cm\"},");
		buf.append("\"link\" : [{\"$href\" : \"/1/item/100001\",\"$rel\" : \"self\"}],");
		buf.append("\"title\" : \"商品100001\"");
		buf.append("},");
		buf.append("{");
		buf.append("\"comment\" : [");
		buf.append("{\"$$text\" : \"普通のえんぴつです。\",\"nickname\" : \"なまえ1\"},");
		buf.append("{\"$$text\" : \"良い感じのえんぴつです。\",\"nickname\" : \"なまえ2\"}],");
		buf.append("\"deleteFlg\" : \"0\",");
		buf.append("\"info\" : {");
		buf.append("\"category\" : \"文房具\",");
		buf.append("\"color\" : \"緑\",");
		buf.append("\"name\" : \"えんぴつ\",");
		buf.append("\"size\" : \"15cm\"},");
		buf.append("\"link\" : [{\"$href\" : \"/1/item/100002\",\"$rel\" : \"self\"}],");
		buf.append("\"title\" : \"商品100002\"");
		buf.append("}");
		buf.append("]}}");
		return buf.toString();
	}

	@Test
	public void testValidate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		//String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"info\" : {\"name\" : \"商品1\",\"category\" : \"Tops\",\"color\" : \"red\",\"size\" : \"MMM\"}}]}}";

		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY);

		String uid = "6";	// 別ユーザ
		List<String> groups = new ArrayList<String>();

		// validate test (1) : validateエラーあり
		System.out.println("testValidate (1) validate start.");
		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\"}}]}}";
		FeedBase feed = (FeedBase)mp4.fromJSON(json);
		
		boolean errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// validateエラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (1) validate OK.");

		// validate test (2) : validateエラー無し、content追加
		System.out.println("testValidate (2) content start.");
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"content\" : {\"$$text\":\"あああ\"},\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"M\"}}]}}";
		feed = (FeedBase)mp4.fromJSON(json);

		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// content編集エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (2) content OK.");

		// validate test (3) : validateエラー無し、content追加、content ACL追加
		System.out.println("testValidate (3) content start.");
		groups.add("/@testservice/_group/$content");
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (3) content OK.");

		// validate test (4) : validateエラー無し、right追加
		System.out.println("testValidate (4) admin right (Another user) start.");
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"M\"}}]}}";
		feed = (FeedBase)mp4.fromJSON(json);
		errorFlg = false;
		try {
			// groupsはcontentが入った状態
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// adminエラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (4) admin right (Another user) OK.");

		// validate test (5) : validateエラー無し、right追加
		System.out.println("testValidate (5) admin right (Another user) start.");
		// groupsをnullにする。
		groups = null;
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// groups=null は項目ACLチェックしないため正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (5) admin right (Another user) OK.");

		// validate test (6) : validateエラー無し、right追加
		System.out.println("testValidate (6) admin right (Another user) start.");
		// groupsを空リストにする。
		groups = new ArrayList<String>();
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// adminエラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (6) admin right (Another user) OK.");

		// validate test (7) : validateエラー無し、right追加
		System.out.println("testValidate (7) admin right (Another user) start.");
		// adminグループに参加
		groups.add("/@testservice/_group/$admin");
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 項目ACLが合致するため正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (7) admin right (Another user) OK.");

		// validate test (8) : validateエラー無し、right追加
		System.out.println("testValidate (8) admin right (Own user) start.");
		// groupsを空にする。
		groups = new ArrayList<String>();
		uid = "7";
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// UIDが合致するため正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (8) admin right (Own user) OK.");

		// validate test (9) : validateエラー無し、contributor追加
		System.out.println("testValidate (9) admin contributor start.");
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"contributor\" : [{\"email\":\"abc@example.com\"},{\"uri\":\"urn:vte.cx:abc@example.com,CRUD\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"M\"}}]}}";
		feed = (FeedBase)mp4.fromJSON(json);
		// groupsをnullにする。
		groups = null;
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// チェックしないので正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (9) admin contributor OK.");

		// validate test (10) : validateエラー無し、contributor追加
		System.out.println("testValidate (10) admin contributor start.");
		// groupsを空にする。
		groups = new ArrayList<String>();
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// adminエラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (10) admin contributor OK.");

		// validate test (11) : validateエラー無し、contributor追加
		System.out.println("testValidate (11) admin contributor start.");
		// 別のグループに参加
		groups.add("/@testservice/_group/$content");
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (11) admin contributor OK.");

		// validate test (12) : validateエラー無し、contributor追加
		System.out.println("testValidate (12) admin contributor start.");
		// adminグループに参加
		groups.add("/@testservice/_group/$admin");
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (12) admin contributor OK.");

		// validate test (13) : validateエラー無し、info.category追加
		System.out.println("testValidate (13) admin info.category start.");
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"category\" : \"Tops\",\"color\" : \"red\",\"size\" : \"M\"}}]}}";
		feed = (FeedBase)mp4.fromJSON(json);
		// groupsはcontentとadminに参加中。
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (13) admin info.category OK.");

		// validate test (14) : validateエラー無し、info.category追加
		System.out.println("testValidate (14) admin info.category start.");
		// groupsはnull。
		groups = null;
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (14) admin info.category OK.");

		// validate test (15) : validateエラー無し、info.category追加
		System.out.println("testValidate (15) admin info.category start.");
		// groupsは空。
		groups = new ArrayList<String>();
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (15) admin info.category OK.");

		// validate test (16) : validateエラー無し、info.category追加
		System.out.println("testValidate (16) admin info.category start.");
		// 権限のあるグループに参加。
		groups.add("/@testservice/1/group/office");
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (16) admin info.category OK.");

		// validate test (17) : validateエラー無し、項目ACLにUID設定
		System.out.println("testValidate (17) field acl uid start.");
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"category\" : \"Tops\",\"color\" : \"red\",\"size\" : \"M\"},\"comment\" : [{\"nickname\" : \"foo\",\"$$text\" : \"コメント。\"},{\"nickname\" : \"aaa\",\"$$text\" : \"あいうえお\"}]}]}}";
		feed = (FeedBase)mp4.fromJSON(json);
		// info権限のグループを設定
		groups = new ArrayList<String>();
		groups.add("/@testservice/1/group/office");
		// uidを6にする。
		uid = "6";
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// commentはuid=7のみ参照・編集可のため、エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		
		// groupsが変更されていないこと
		assertTrue(groups.size() == 1);
		
		System.out.println("testValidate (17) field acl uid OK.");

		// validate test (18) : validateエラー無し、項目ACLにUID設定
		System.out.println("testValidate (18) field acl uid start.");
		// uidを""にする。
		uid = "";
		errorFlg = false;
		try {
			feed.validate(uid, groups);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			// commentはuid=7のみ参照・編集可のため、エラーでOK
			errorFlg = true;
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (18) field acl uid OK.");

		// validate test (19) : validateエラー無し、項目ACLにUID設定
		System.out.println("testValidate (19) field acl uid start.");
		// uidを7にする。
		uid = "7";
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// 正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (19) field acl uid OK.");

		// validate test (20) : validateエラー無し、項目ACLにUID設定
		System.out.println("testValidate (20) field acl uid start.");
		// uidをnullにする。
		uid = null;
		errorFlg = false;
		try {
			feed.validate(uid, groups);
			// チェックしないため正常でOK
			errorFlg = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertTrue(errorFlg);
		System.out.println("testValidate (20) field acl uid OK.");
		
	}

	/**
	 * ユーザに指定した権限を付与するACL情報の文字列を作成します
	 * @param user ユーザ名
	 * @param aclType 権限情報
	 * @return ACL情報の文字列
	 */
	private String getAclUrn(String user, String aclType) {
		return "urn:vte.cx:acl:" + user + "," + aclType;
	}

	@Test
	public void testAddSvcname() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls3, 30, SECRETKEY);

		String json = createJsonTempl4_1();
		FeedBase feed = (FeedBase)mp4.fromJSON(json);
		EntryBase entry = feed.getEntry().get(0);
		String serviceName = "service1";

		String myUri = "/1/folders/001";
		entry = addSvcname(entry, serviceName, myUri);

		myUri = "/";
		entry = addSvcname(entry, serviceName, myUri);

		myUri = null;
		entry = addSvcname(entry, serviceName, myUri);

		myUri = "";
		entry = addSvcname(entry, serviceName, myUri);

		assertTrue(!"/".equals(entry.getMyUri().substring(entry.getMyUri().length() - 1)));
	}

	private EntryBase addSvcname(EntryBase entry, String serviceName, String myUri) {
		entry.setMyUri(myUri);
		entry.setId(myUri + ",1");
		Contributor contributor = new Contributor();
		contributor.setUri("urn:vte.cx:acl:" + myUri + ",R");
		entry.contributor = new ArrayList<Contributor>();
		entry.contributor.add(contributor);
		entry.addSvcname(serviceName);
		System.out.println("[before uri]" + myUri + ", [add servicename uri]" + entry.getMyUri() + ", [id]" + entry.getId() + ", [contributor]" + entry.contributor.get(0).getUri());
		return entry;
	}

	@Test
	public void testCutSvcname() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls3, 30, SECRETKEY);

		String json = createJsonTempl4_1();
		FeedBase feed = (FeedBase)mp4.fromJSON(json);
		EntryBase entry = feed.getEntry().get(0);
		String serviceName = "service1";
		String atServiceName = "/@" + serviceName;

		String myUri = atServiceName + "/1/folders/001";
		entry = cutSvcname(entry, serviceName, myUri);

		myUri = atServiceName;
		entry = cutSvcname(entry, serviceName, myUri);

		assertTrue("/,1".equals(entry.getId()));
	}

	private EntryBase cutSvcname(EntryBase entry, String serviceName, String myUri) {
		entry.setMyUri(myUri);
		entry.setId(myUri + ",1");
		Contributor contributor = new Contributor();
		contributor.setUri("urn:vte.cx:acl:" + myUri + ",R");
		entry.contributor = new ArrayList<Contributor>();
		entry.contributor.add(contributor);
		entry.cutSvcname(serviceName);
		System.out.println("[before uri]" + myUri + ", [cut servicename uri]" + entry.getMyUri() + ", [id]" + entry.getId() + ", [contributor]" + entry.contributor.get(0).getUri());
		//System.out.println("[before uri]" + myUri + ", [cut servicename uri]" + entry.getMyUri() + ", [id]" + entry.getId());
		return entry;
	}

	@Test
	public void testNull() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		// コンストラクタにnull。NullPointerExceptionがスローされなければOK。
		FeedTemplateMapper mp1 = new FeedTemplateMapper(null, null, 0, null);
		
		// 正しくMapperを生成
		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls3, 30, SECRETKEY);

		String dummyStr = null;
		byte[] dummyBytes = null;
		Object obj = null;

		// fromMessagePack
		obj = mp4.fromMessagePack(dummyBytes);
		assertTrue(obj == null);
		// fromXML
		obj = mp4.fromXML(dummyStr);
		assertTrue(obj == null);
		// fromJson
		obj = mp4.fromJSON(dummyStr);
		assertTrue(obj == null);

		InputStream in = null;
		Reader reader = null;
		// fromMessagePack
		obj = mp4.fromMessagePack(in, true);
		assertTrue(obj == null);
		// fromXML
		obj = mp4.fromXML(reader);
		assertTrue(obj == null);
		// fromJson
		obj = mp4.fromJSON(reader);
		assertTrue(obj == null);
		
		FeedBase feed = null;
		// toMessagePack
		obj = mp4.toMessagePack(feed);
		assertTrue(obj == null);
		// toJSON
		obj = mp4.toJSON(feed);
		assertTrue(obj == null);
		// toXML
		obj = mp4.toXML(feed);
		assertTrue(obj == null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StringWriter writer = new StringWriter();
		// toMessagePack
		mp4.toMessagePack(feed, out);
		byte[] msg = out.toByteArray();
		assertTrue(msg.length == 0);
		// toJSON
		mp4.toJSON(feed, writer);
		String json = writer.toString();
		assertTrue(json.equals(""));
		// toXML
		mp4.toXML(feed, writer);
		String xml = writer.toString();
		assertTrue(xml.equals(""));

		out = null;
		writer = null;
		// toMessagePack
		mp4.toMessagePack(feed, out);
		// toJSON
		mp4.toJSON(feed, writer);
		// toXML
		mp4.toXML(feed, writer);

	}

	@Test
	public void testMetalist() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		//String serviceName = "testservice";
		//String serviceName = null;
		String serviceName = "";

		System.out.println("--- Template Metalist ---");
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY);
		List<Meta> metalist = mp.getMetalist(serviceName);
		boolean existsMetalist = printMetalist(metalist);
		System.out.println("--- printMetalist2 ---");
		existsMetalist = printMetalist2(metalist);
		assertTrue(existsMetalist);

		System.out.println("--- Package Metalist ---");
		Map<String, String> modelPackage = new HashMap<String, String>();
		modelPackage.putAll(AtomConst.ATOM_PACKAGE);
		modelPackage.put("jp.reflexworks.test2.model", "");

		//mp = new FeedTemplateMapper(modelPackage, entityAcls5, 30, SECRETKEY);
		mp = new FeedTemplateMapper(null, entityAcls5, 30, SECRETKEY, modelPackage);
		metalist = mp.getMetalist(serviceName);
		existsMetalist = printMetalist(metalist);
		assertTrue(existsMetalist);
		
		System.out.println("--- Package Metalist (template & package) ---");
		mp = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY, modelPackage);
		metalist = mp.getMetalist(serviceName);
		existsMetalist = printMetalist(metalist);
		assertTrue(existsMetalist);

	}
	
	private boolean printMetalist(List<Meta> metalist) {
		boolean existsMetalist = false;
		StringBuilder prn = new StringBuilder();
		if (metalist != null && metalist.size() > 0) {
			existsMetalist = true;
			for (Meta meta : metalist) {
				prn.append(meta);
				prn.append("(");
				prn.append(meta.type);
				prn.append(")");
				if (meta.index != null) {
					prn.append(" : index=");
					prn.append(meta.index);
				}
				if (meta.privatekey != null) {
					prn.append(", privatekey=");
					prn.append(meta.privatekey);
				}
				prn.append("\n");
			}
		} else {
			prn.append("metalist is nothing : " + metalist);
		}
		System.out.println(prn.toString());
		return existsMetalist;
	}
	
	private boolean printMetalist2(List<Meta> metalist) {
		boolean existsMetalist = false;
		StringBuilder prn = new StringBuilder();
		if (metalist != null && metalist.size() > 0) {
			existsMetalist = true;
			prn.append("[name]\t[level]\t[type]\t[parent]\t[self]\t[index]\t[repeated]\t[isrecord]\n");
			for (Meta meta : metalist) {
				prn.append(meta);
				prn.append("\t");
				prn.append(meta.level);
				prn.append("\t");
				prn.append(meta.type);
				prn.append("\t");
				prn.append(meta.parent);
				prn.append("\t");
				prn.append(meta.self);
				prn.append("\t");
				prn.append(meta.index);
				prn.append("\t");
				prn.append(meta.repeated);
				prn.append("\t");
				prn.append(meta.isrecord);
				prn.append("\n");
			}
		}
		System.out.println(prn.toString());
		return existsMetalist;
	}

	@Test
	public void testGetMetalist() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY);
		
		String serviceName = "service1";
		String testName = "info.name";
		String sourceValue = null;
		String targetValue = null;
		
		System.out.println("[Index]");
		for (String acl : entityAcls5) {
			int idx = acl.indexOf(":");
			if (idx > 0) {
				System.out.println("** " + acl);
				if (testName.equals(acl.substring(0, idx))) {
					sourceValue = acl.substring(idx + 1);
				}
			}
		}
		
		List<Meta> metalist = mp4.getMetalist(serviceName);
		System.out.println("[Metalist Index]");
		for (Meta meta : metalist) {
			if (meta.index != null && meta.index.length() > 0) {
				System.out.println("** " + meta.name + ": " + meta.index);
				if (testName.equals(meta.name)) {
					targetValue = meta.index;
				}
			}
		}
		
		String testLayer = "/index";
		String sourceLayer = sourceValue.substring(sourceValue.indexOf(testLayer));
		String targetLayer = targetValue.substring(targetValue.indexOf(testLayer));
		
		System.out.println("sourceLayer: " + sourceLayer);
		System.out.println("targetLayer: " + targetLayer);
		
		assertEquals(sourceLayer, targetLayer);

	}

	@Test
	public void testDate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY);

		String printFormat = "yyyy-MM-dd HH:mm:ss.SSSZ";
		
		String date = "2015-12-04 09:53:34";
		String json = getDateJson(date);
		FeedBase feed = (FeedBase)mp4.fromJSON(json);
		EntryBase entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04 09:53:34.333";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04 09:53:34+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "2015-12-04 09:53:34+08:00";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04 09:53:34.333+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "2015-12-04T09:53:34";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04T09:53:34.333";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "2015-12-04T09:53:34+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04T09:53:34+08:00";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "2015-12-04T09:53:34.333+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "2015/12/04 09:53:34.333";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "2015/12/04 09:53:34.333+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		date = "20151204095334333";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));
		
		date = "20151204095334333+0800";
		json = getDateJson(date);
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);
		System.out.println("[testDate] before = " + date + " | after = " + DateUtil.getDateTimeFormat((Date)entry.getValue("info.stock_date"), printFormat));

		System.out.println("[testDate] json = " + mp4.toJSON(feed));
		System.out.println("[testDate] xml = " + mp4.toXML(feed));
		
	}
	
	private String getDateJson(String date) {
		return "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_date\" : \"" + date + "\"}}]}}";
	}

	@Test
	public void testIsMatch() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp4 = new FeedTemplateMapper(entitytempl4, entityAcls5, 30, SECRETKEY);

		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		// stock_int をテスト
		// 数値が正数の場合
		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : 1000,\"stock_long\" : 1000000,\"stock_float\" : 222.333,\"stock_double\" : 4444.5555}}]}}";
		//String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : 1000,\"stock_long\" : 1000000,\"stock_float\" : 222.333}}]}}";
		FeedBase feed = (FeedBase)mp4.fromJSON(json);
		EntryBase entry = feed.entry.get(0);
		
		checkPositiveInteger(entry);
		checkPositiveLong(entry);
		checkPositiveFloat(entry);
		checkPositiveDouble(entry);

		// 数値が負数の場合
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : -1000,\"stock_long\" : -1000000,\"stock_float\" : -222.333,\"stock_double\" : -4444.5555}}]}}";
		//json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : -1000,\"stock_long\" : -1000000,\"stock_float\" : -222.333}}]}}";
		feed = (FeedBase)mp4.fromJSON(json);
		entry = feed.entry.get(0);

		checkNegativeInteger(entry);
		checkNegativeLong(entry);
		checkNegativeFloat(entry);
		checkNegativeDouble(entry);

	}
	
	private String checkResult(boolean target, boolean source) {
		if (target == source) {
			return " (◯) ";
		}
		return " (☓) ";
	}
	
	// 正数 Integer の isMatchテスト
	private void checkPositiveInteger(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		System.out.println("info.stock_int = " + entry.getValue("info.stock_int"));
		// Integer桁数内の数値
		conditions[0] = new Condition("info.stock_int-gt-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-gt-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_int-eq-1000");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Integer桁数を超える数値
		conditions[0] = new Condition("info.stock_int-gt-999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	// 正数 Long の isMatchテスト
	private void checkPositiveLong(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		System.out.println("info.stock_long = " + entry.getValue("info.stock_long"));
		// Long桁数内の数値
		conditions[0] = new Condition("info.stock_long-gt-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-gt-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_long-eq-1000000");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Long桁数を超える数値
		conditions[0] = new Condition("info.stock_long-gt-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}
	
	// 正数 Float の isMatchテスト
	private void checkPositiveFloat(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		// 222.333
		System.out.println("info.stock_float = " + entry.getValue("info.stock_float"));
		// Float桁数内の数値
		conditions[0] = new Condition("info.stock_float-gt-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-gt-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_float-eq-222.333");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Float桁数を超える数値
		conditions[0] = new Condition("info.stock_float-gt-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}
	
	// 正数 Double の isMatchテスト
	private void checkPositiveDouble(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		// 4444.5555
		System.out.println("info.stock_double = " + entry.getValue("info.stock_double"));
		// Double桁数内の数値
		conditions[0] = new Condition("info.stock_double-gt-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-gt-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_double-eq-4444.5555");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Double桁数を超える数値
		conditions[0] = new Condition("info.stock_double-gt-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	// 負数 Integer の isMatchテスト
	private void checkNegativeInteger(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		System.out.println("info.stock_int = " + entry.getValue("info.stock_int"));
		// Integer桁数内の数値
		conditions[0] = new Condition("info.stock_int-gt-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le-99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-gt-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le-999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_int-eq--99999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_int-eq--999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_int-eq--1000");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Integer桁数を超える数値
		conditions[0] = new Condition("info.stock_int-gt-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-ge-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-lt-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-le-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_int-eq-9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_int-gt--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_int-ge--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_int-lt--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_int-le--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	
		conditions[0] = new Condition("info.stock_int-eq--9999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	// 負数 Long の isMatchテスト
	private void checkNegativeLong(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		System.out.println("info.stock_long = " + entry.getValue("info.stock_long"));
		// Integer桁数内の数値
		conditions[0] = new Condition("info.stock_long-gt-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le-99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-gt-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le-999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_long-eq--99999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_long-eq--999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_long-eq--1000000");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Long桁数を超える数値
		conditions[0] = new Condition("info.stock_long-gt-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-ge-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-lt-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-le-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_long-eq-99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_long-gt--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_long-ge--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_long-lt--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_long-le--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	
		conditions[0] = new Condition("info.stock_long-eq--99999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	// 負数 Float の isMatchテスト
	private void checkNegativeFloat(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		// 222.333
		System.out.println("info.stock_float = " + entry.getValue("info.stock_float"));
		// Float桁数内の数値
		conditions[0] = new Condition("info.stock_float-gt-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le-999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-gt-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le-11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_float-eq--999.99");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_float-eq--11.222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_float-eq--222.333");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Float桁数を超える数値
		conditions[0] = new Condition("info.stock_float-gt-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-ge-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-lt-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-le-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_float-eq-999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_float-gt--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_float-ge--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_float-lt--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_float-le--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	
		conditions[0] = new Condition("info.stock_float-eq--999.9999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	// 負数 Double の isMatchテスト
	private void checkNegativeDouble(EntryBase entry) {
		Condition[] conditions = new Condition[1];
		boolean isMatch = false;

		// 4444.5555
		System.out.println("info.stock_double = " + entry.getValue("info.stock_double"));
		// Double桁数内の数値
		conditions[0] = new Condition("info.stock_double-gt-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le-999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-gt-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le-111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_double-eq--999999.999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_double-eq--111.22222222");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		
		conditions[0] = new Condition("info.stock_double-eq--4444.5555");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		// Double桁数を超える数値
		conditions[0] = new Condition("info.stock_double-gt-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-ge-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-lt-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-le-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		
		conditions[0] = new Condition("info.stock_double-eq-9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);

		conditions[0] = new Condition("info.stock_double-gt--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);
		conditions[0] = new Condition("info.stock_double-ge--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, true) + conditions[0]);
		assertTrue(isMatch);

		conditions[0] = new Condition("info.stock_double-lt--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
		conditions[0] = new Condition("info.stock_double-le--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	
		conditions[0] = new Condition("info.stock_double-eq--9999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999");
		isMatch = entry.isMatch(conditions);
		System.out.println("** isMatch = " + isMatch + checkResult(isMatch, false) + conditions[0]);
		assertFalse(isMatch);
	}

	@Test
	public void testCreateObject() 
	throws ParseException, ClassNotFoundException, IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl5, entityAcls5, 30, SECRETKEY);
		
		EntryBase entry = EntryUtil.createEntry(mapper);

		// 内部のクラスを生成したい。
		String pkg = entitytempl5[0].substring(0, entitytempl5[0].indexOf("{"));
		String clsName = "_" + pkg + "." + "Info";
		
		try {
			Class cls = Class.forName(clsName);
			System.out.println("Class.forName - OK : " + cls.getName());
			assertTrue(false);
			
		} catch (ClassNotFoundException e) {
			System.out.println("Class.forName - ClassNotFoundException : " + e.getMessage());
		}

		// javassistのクラスローダーからクラスを取得
		Class cls = mapper.getClass(clsName);
		
		// インスタンス生成
		Object obj = cls.newInstance();
		
		// setterで値を設定
		String field = "name";
		String value = "名前1";
		//Class type = String.class;
		String typeName = "java.lang.String";
		Class type = Class.forName(typeName);

		String setter = FieldMapper.getSetter(field, type);
		Method method = cls.getMethod(setter, type);
		method.invoke(obj, value);
		
		System.out.println(mapper.toXML(obj));
	}
	
	@Test
	public void testMaskprop3() throws ParseException {
		// パッケージ指定mapperの動作方法
		// 1. TestMsgpackGenerterFilesクラスでエンティティクラスを生成する。
		// 2. 生成されたパッケージフォルダを、/target/test-classes 配下にコピーする。
		// 3. 実行する。
		Map<String, String> packages = new HashMap<String, String>();
		packages.putAll(AtomConst.ATOM_PACKAGE);
		packages.put("_mypackage", null);
		//FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl6, entityAcls3, 30, SECRETKEY, packages);
		
		// テンプレート指定
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl6, entityAcls3, 30, SECRETKEY);
		
		StringBuilder sb = new StringBuilder();
		sb.append("{\"feed\": {\"entry\": [");
		sb.append("{\"info\": {\"item_1\": \"51000007\"},");
		sb.append("\"email\": {");
		sb.append("\"before_message\": [");
		sb.append("{\"message_no\": \"2\",\"before_message_text\": \"予定Eメール2\"},");
		sb.append("{\"message_no\": \"3\",\"before_message_text\": \"予定Eメール3\"}],");
		sb.append("\"after_message\": [");
		sb.append("{\"message_no\": \"4\",\"after_message_text\": \"完了Eメール4\"},");
		sb.append("{\"message_no\": \"5\",\"after_message_text\": \"完了Eメール5\"}]},");
		sb.append("\"id\": \"/11000001/send/51000007,1\",");
		sb.append("\"link\": [{\"___href\": \"/11000001/send/51000007\",\"___rel\": \"self\"}]");
		sb.append("}]}}");
		
		String json = sb.toString();
		System.out.println(json);
		
		FeedBase feed = (FeedBase)mapper.fromJSON(json);
		
		// maskprop
		String uid = "11";
		List<String> groups = new ArrayList<String>();
		groups.add("/@mypackage/_group/$admin");
		groups.add("/@mypackage/_group/$content");
		groups.add("/@mypackage/_group/$useradmin");
		
		EntryBase entry = feed.entry.get(0);
		entry.maskprop(uid, groups);
		
		String name = "info";
		Object obj = entry.getValue(name);
		System.out.println("[getValue] " + name + " : " + obj);
		
		name = "info.item_1";
		obj = entry.getValue(name);
		System.out.println("[getValue] " + name + " : " + obj);

		name = "email";
		obj = entry.getValue(name);
		System.out.println("[getValue] " + name + " : " + obj);

		name = "email.before_message";
		obj = entry.getValue(name);
		System.out.println("[getValue] " + name + " : " + obj);

		name = "email.before_message.before_message_text";
		obj = entry.getValue(name);
		System.out.println("[getValue] " + name + " : " + obj);

		System.out.println("testMaskprop3 end");
	}

	@Test
	public void testType() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl4tmptype, entityAcls5, 30, SECRETKEY);

		List<Meta> metalist = mp.getMetalist("myservice");
		
		System.out.println("[testType] print type : ");
		for (Meta meta : metalist) {
			System.out.println("  name = " + meta.name + ", type = " + meta.type + ", typesrc = " + meta.typesrc);
		}

		// stock_int をテスト
		// 数値が正数の場合
		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : 1000,\"stock_long\" : 1000000,\"stock_float\" : 222.333,\"stock_double\" : 4444.5555}}]}}";
		//String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : 1000,\"stock_long\" : 1000000,\"stock_float\" : 222.333}}]}}";
		FeedBase feed = (FeedBase)mp.fromJSON(json);
		EntryBase entry = feed.entry.get(0);
		

		/*
		// 数値が負数の場合
		json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : -1000,\"stock_long\" : -1000000,\"stock_float\" : -222.333,\"stock_double\" : -4444.5555}}]}}";
		//json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"info\" : {\"name\" : \"商品1\",\"color\" : \"red\",\"size\" : \"MMM\",\"stock_int\" : -1000,\"stock_long\" : -1000000,\"stock_float\" : -222.333}}]}}";
		feed = (FeedBase)mp.fromJSON(json);
		entry = feed.entry.get(0);

		checkNegativeInteger(entry);
		checkNegativeLong(entry);
		checkNegativeFloat(entry);
		checkNegativeDouble(entry);
		*/

	}
	
	@Test
	public void testSurrogate() throws ParseException, IOException, ClassNotFoundException {
		// 第三水準・第四水準文字（サロゲートペア）のテスト
		Map<String, String> packages = new HashMap<String, String>();
		packages.putAll(AtomConst.ATOM_PACKAGE);
		packages.put("_mypackage", null);
		
		// 文字列
		String testStr = "あ𠀋あ";
		
		// テンプレート指定
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl6, entityAcls3, 30, SECRETKEY);
		
		// JSON
		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"" + testStr + "\"}]}}";
		System.out.println(json);
		
		FeedBase feed = (FeedBase)mapper.fromJSON(json);
		
		System.out.println("[testSurrogate] (fromJSON) title = " + feed.entry.get(0).title);
		assertTrue(testStr.equals(feed.entry.get(0).title));
		System.out.println("[testSurrogate] (fromJSON) title = " + new SurrogateConverter(feed.entry.get(0).title).convertUcs());
		
		String message1 = "";
		byte[] bytes = feed.entry.get(0).title.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			message1 += " "+Integer.toHexString(bytes[i] & 0xff);
		}
		System.out.println(message1);

		// XML
		String xml = "<feed><entry><title>" + testStr + "</title></entry></feed>";
		System.out.println(xml);
		
		feed = (FeedBase)mapper.fromXML(xml);

		bytes = feed.entry.get(0).title.getBytes();

		message1 = "";
		for (int i = 0; i < bytes.length; i++) {
			message1 += " "+Integer.toHexString(bytes[i] & 0xff);
		}
		System.out.println(message1);

		System.out.println("[testSurrogate] (fromXML) title = " + feed.entry.get(0).title);
		assertTrue(testStr.equals(feed.entry.get(0).title));

		byte[] msg = mapper.toMessagePack(feed);
		FeedBase feed2 = (FeedBase)mapper.fromMessagePack(msg);

		System.out.println("[testSurrogate] (fromMessagePck) title = " + feed2.entry.get(0).title);

	}
	
	@Test
	public void testBackSlash() throws ParseException {
		// \のテスト
		Map<String, String> packages = new HashMap<String, String>();
		packages.putAll(AtomConst.ATOM_PACKAGE);
		packages.put("_mypackage", null);
		
		// テンプレート指定
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl6, entityAcls3, 30, SECRETKEY);
		
		// JSON
		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"\\\\\"}]}}";
		System.out.println("[testBackSlash] (fromJSON) " + json);
		
		FeedBase feed = (FeedBase)mapper.fromJSON(json);
		
		System.out.println("[testBackSlash] (fromJSON) title = " + feed.entry.get(0).title);
		
		feed.entry.get(0).title = "\\";
		String toJson = mapper.toJSON(feed);
		
		System.out.println("[testBackSlash] (toJSON) title = " + feed.entry.get(0).title);
		System.out.println("[testBackSlash] (toJSON) " + toJson);
		
		// XML
		String xml = "<feed><entry><title>\\</title></entry></feed>";
		System.out.println(xml);
		
		feed = (FeedBase)mapper.fromXML(xml);
		
		System.out.println("[testBackSlash] (fromXML) title = " + feed.entry.get(0).title);

	}

	@Test
	public void testaddsvcname() throws ParseException {
		Map<String, String> packages = new HashMap<String, String>();
		packages.putAll(AtomConst.ATOM_PACKAGE);
		packages.put("_mypackage", null);
		
		// テンプレート指定
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl6, entityAcls3, 30, SECRETKEY);		
		EntryBase entry = EntryUtil.createEntry(mapper);
		// id正常
		entry.setId("/abc,1");
		System.out.println("[addsvcname] 1 id before : " + entry.id);
		entry.addSvcname("xx");
		System.out.println("[addsvcname] 1 id after : " + entry.id);
		entry.cutSvcname("xx");
		System.out.println("[cutsvcname] 1 id after : " + entry.id);
		// id不正1
		entry.setId("abc,1");
		System.out.println("[addsvcname] 2 id before : " + entry.id);
		entry.addSvcname("xx");
		System.out.println("[addsvcname] 2 id after : " + entry.id);
		entry.cutSvcname("xx");
		System.out.println("[cutsvcname] 2 id after : " + entry.id);
		// id不正2
		entry.setId("abc");
		System.out.println("[addsvcname] 3 id before : " + entry.id);
		entry.addSvcname("xx");
		System.out.println("[addsvcname] 3 id after : " + entry.id);
		entry.cutSvcname("xx");
		System.out.println("[cutsvcname] 3 id after : " + entry.id);
		
	}
	
	/**
	 * 改行・タブ文字テスト
	 */
	@Test
	public void testControlCharactor() throws ParseException, IOException, ClassNotFoundException {
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl3, entityAcls3, 30, SECRETKEY);
		EntryBase entry = EntryUtil.createEntry(mapper);
		
		String xml = null;
		String json = null;
		byte[] msgData = null;
		EntryBase entryXml = null;
		EntryBase entryJson = null;
		EntryBase entryMsgpack = null;

		System.out.println("1. 本物リターンコード(\\n)");
		entry.title = "return\ncode";
		xml = mapper.toXML(entry);
		entryXml = (EntryBase)mapper.fromXML(xml);
		json = mapper.toJSON(entry);
		entryJson = (EntryBase)mapper.fromJSON(json);
		msgData = mapper.toMessagePack(entry);
		entryMsgpack = (EntryBase)mapper.fromMessagePack(msgData, false);
		System.out.println(xml);
		System.out.println(json);
		System.out.println("*      before : " + entry.title);
		System.out.println("*  (xml)after : " + entryXml.title);
		System.out.println("* (json)after : " + entryJson.title);
		System.out.println("*  (msg)after : " + entryMsgpack.title);
		assertEquals(entry.title, entryXml.title);
		assertEquals(entry.title, entryJson.title);
		assertEquals(entry.title, entryMsgpack.title);

		System.out.println("2. リターンコード文字列(\\\\n)");
		entry.title = "return\\\\ncode";
		xml = mapper.toXML(entry);
		entryXml = (EntryBase)mapper.fromXML(xml);
		json = mapper.toJSON(entry);
		entryJson = (EntryBase)mapper.fromJSON(json);
		msgData = mapper.toMessagePack(entry);
		entryMsgpack = (EntryBase)mapper.fromMessagePack(msgData, false);
		System.out.println(xml);
		System.out.println(json);
		System.out.println("*      before : " + entry.title);
		System.out.println("*  (xml)after : " + entryXml.title);
		System.out.println("* (json)after : " + entryJson.title);
		System.out.println("*  (msg)after : " + entryMsgpack.title);
		assertEquals(entry.title, entryXml.title);
		assertEquals(entry.title, entryJson.title);
		assertEquals(entry.title, entryMsgpack.title);

		System.out.println("3. 本物タブ(\\t)");
		entry.title = "return\tcode";
		xml = mapper.toXML(entry);
		entryXml = (EntryBase)mapper.fromXML(xml);
		json = mapper.toJSON(entry);
		entryJson = (EntryBase)mapper.fromJSON(json);
		msgData = mapper.toMessagePack(entry);
		entryMsgpack = (EntryBase)mapper.fromMessagePack(msgData, false);
		System.out.println(xml);
		System.out.println(json);
		System.out.println("*      before : " + entry.title);
		System.out.println("*  (xml)after : " + entryXml.title);
		System.out.println("* (json)after : " + entryJson.title);
		System.out.println("*  (msg)after : " + entryMsgpack.title);
		assertEquals(entry.title, entryXml.title);
		assertEquals(entry.title, entryJson.title);
		assertEquals(entry.title, entryMsgpack.title);

		System.out.println("4. タブ文字列(\\\\t)");
		entry.title = "return\\tcode";
		xml = mapper.toXML(entry);
		entryXml = (EntryBase)mapper.fromXML(xml);
		json = mapper.toJSON(entry);
		entryJson = (EntryBase)mapper.fromJSON(json);
		msgData = mapper.toMessagePack(entry);
		entryMsgpack = (EntryBase)mapper.fromMessagePack(msgData, false);
		System.out.println(xml);
		System.out.println(json);
		System.out.println("*      before : " + entry.title);
		System.out.println("*  (xml)after : " + entryXml.title);
		System.out.println("* (json)after : " + entryJson.title);
		System.out.println("*  (msg)after : " + entryMsgpack.title);
		assertEquals(entry.title, entryXml.title);
		assertEquals(entry.title, entryJson.title);
		assertEquals(entry.title, entryMsgpack.title);

	}
	
	/**
	 * 制御文字テスト
	 */
	@Test
	public void testControlCharactor2() throws ParseException, IOException, ClassNotFoundException {
		FeedTemplateMapper mapper = new FeedTemplateMapper(entitytempl3, entityAcls3, 30, SECRETKEY);
		
		String xml = null;
		String json = null;
		byte[] msgData = null;
		EntryBase entryXml = null;
		EntryBase entryJson = null;
		EntryBase entryMsgpack = null;
		String startStr = "a";
		String endStr = "b";
		String str = null;
		
		String jsonStart = "{\"entry\" : {\"title\" : \"";
		String jsonEnd = "\"}}";
		
		String xmlStart = "<entry><title>";
		String xmlEnd = "</title></entry>";

		String prefixJson = "[json] ";
		String prefixXml = " [xml] ";
		int num = 0;
		char code = 1;
		
		System.out.println("--- testControlCharactor2 start ---");

		// No.1〜33 (0x01〜0x20) (制御コードは0x1Fまで。0x20は!)
		for (int i = 0; i < 33; i++) {
			num++;
			if (i > 0) {
				code++;
			}
			System.out.println(num + ". [code] " + Integer.toHexString(code));
			
			str = startStr + code + endStr;
			json = jsonStart + str + jsonEnd;
			xml = xmlStart + str + xmlEnd;
			
			System.out.println(prefixJson + num + ". start = " + str);
			entryJson = (EntryBase)mapper.fromJSON(json);
			System.out.println(prefixJson + num + ".   ret = " + entryJson.title);
			
			System.out.println(prefixXml + num + ". start = " + str);
			entryXml = (EntryBase)mapper.fromXML(xml);
			System.out.println(prefixXml + num + ".   ret = " + entryXml.title);
			
			if (num==13) {
				String hexString = DatatypeConverter.printHexBinary(entryJson.title.getBytes());
				assertEquals(hexString,"610D62");
				// 0DはXMLでは0Aに変換されてしまう
				hexString = DatatypeConverter.printHexBinary(entryXml.title.getBytes());
				assertEquals(hexString,"610A62");				
			}else {
				assertEquals(entryJson.title, entryXml.title);				
			}
		}

		// No.34〜67 (0x7F〜0xA0)
		code = 0x7E;
		for (int i = 0; i < 34; i++) {
			num++;
			code++;
			System.out.println(num + ". [code] " + Integer.toHexString(code));
			
			str = startStr + code + endStr;
			json = jsonStart + str + jsonEnd;
			xml = xmlStart + str + xmlEnd;
			
			System.out.println(prefixJson + num + ". start = " + str);
			entryJson = (EntryBase)mapper.fromJSON(json);
			System.out.println(prefixJson + num + ".   ret = " + entryJson.title);
			
			System.out.println(prefixXml + num + ". start = " + str);
			entryXml = (EntryBase)mapper.fromXML(xml);
			System.out.println(prefixXml + num + ".   ret = " + entryXml.title);
			
			assertEquals(entryJson.title, entryXml.title);
		}
		
		// No.68 (0xAD)
		num++;
		code = 0xAD;
		System.out.println(num + ". [code] " + Integer.toHexString(code));
		
		str = startStr + code + endStr;
		json = jsonStart + str + jsonEnd;
		xml = xmlStart + str + xmlEnd;
		
		System.out.println(prefixJson + num + ". start = " + str);
		entryJson = (EntryBase)mapper.fromJSON(json);
		System.out.println(prefixJson + num + ".   ret = " + entryJson.title);
		
		System.out.println(prefixXml + num + ". start = " + str);
		entryXml = (EntryBase)mapper.fromXML(xml);
		System.out.println(prefixXml + num + ".   ret = " + entryXml.title);

		assertEquals(entryJson.title, entryXml.title);

		System.out.println("--- testControlCharactor2 end ---");
		
	}

}
