package jp.reflexworks.atom.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONException;
import org.junit.Test;

import com.carrotsearch.sizeof.ObjectTree;

import jp.sourceforge.reflex.util.DeflateUtil;
import jp.sourceforge.reflex.util.FileUtil;
import jp.reflexworks.atom.AtomConst;
import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.entry.Contributor;
import jp.reflexworks.atom.entry.Link;
import jp.reflexworks.atom.feed.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.reflexworks.atom.mapper.CipherUtil;
import jp.reflexworks.atom.wrapper.Condition;
import jp.reflexworks.test.model.Feed;

public class TestMsgpackMapper {

	public static String entitytempl[] = {
		// {}がMap, []がArray　, {} [] は末尾にどれか一つだけが付けられる。また、!を付けると必須項目となる
		"default{2}",        //  0行目はパッケージ名(service名)
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
		"  $$text"				// テキストノード
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
		"contributor=@+RW,/@testservice/$admin+RW",
		//"contributor=/@testservice/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/@testservice/$admin+RW"
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
		"  music[3]=^.{5}$",			// 配列(要素数max3)
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
		"  music[3]=^.{5}$",			// 配列(要素数max3)
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

	public static String entityAcls3[] = {
		"title:/*",
		"contributor=/@testservice/$admin+RW",
		"contributor.uri#",
		"rights#=@+RW,/@testservice/$admin+RW"
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
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);
				
		System.out.println("\n=== JSON Entry シリアライズ ===");
        String json2 = mp.toJSON(entry);
		System.out.println(json);
		System.out.println(json2);

		assertEquals(json, json2);
	}

	@Test
	public void testXMLEntry() throws ParseException, JSONException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl,entityAcls, 30, SECRETKEY);
		
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
		EntryBase entry = (EntryBase) mp.fromJSON(json);
		
		System.out.println("\n=== XML Entry シリアライズ ===");
        String xml = mp.toXML(entry);
		System.out.println(xml);

		System.out.println("\n=== XML Entry デシリアライズ ===");
		EntryBase entry2 = (EntryBase) mp.fromXML(xml);
		entry2._$xmlns = null;
		System.out.println(mp.toJSON(entry2));
		entry2._$xmlns = null;

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
		
		String json = "{\"entry\" : {\"id\" : \"/123/new,1\",\"content\" : {\"$$text\" : \"あああ\"},\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
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
        groups.add("/grp2");
        groups.add("/grp1");
        groups.add("1");
        groups.add("/_group/$content");	// contentに書込できるグループ
        System.out.println("Validtion:"+muserinfo.validate("123",groups));	

        System.out.println("Before Masked:"+mp.toJSON(muserinfo));	
        muserinfo.maskprop("123",groups);	
        System.out.println("After  Masked:"+mp.toJSON(muserinfo));	

		assertNotSame(json, mp.toJSON(muserinfo));
	}

	@Test
	public void testFeedWithValidate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		//DeflateUtil deflateUtil = new DeflateUtil();
		
		String json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/@svc/123/new,1\",\"link\" : [{\"$title\" : \"署名\",\"$href\" : \"/@svc/123/allA/759188985520\",\"$rel\" : \"alternate\"}],\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}},{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}]}}";

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
	}

	@Test
	public void testArrayEntry() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
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
		
		String json = "{ \"feed\" : {\"entry\" : [{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}},{\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}]}}";
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
	    String json = "{ \"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
		// 正常ケース
		EntryBase entry = (EntryBase) mp.fromJSON(json);
		//entry.validate();	// TODO validate実装する。

		// エラーケース（errorsの数が２個）
		json = "{ \"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"}],\"code\" : 101,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";
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

		String json = "{\"entry\" : {\"public\" : {\"int\" : \"予約語\"},\"subInfo\" : {\"hobby\" : [{\"$$text\" : \"テキストノード\"}]},\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}]}}";
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

		String json = "{\"feed\" : {\"entry\" : [{\"subInfo\" : {\"hobby\" : [{\"$$text\" : \"テキストノード\"}]},\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}]}]}}";
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
	
		FeedTemplateMapper mp = new FeedTemplateMapper(MODEL_PACKAGE, SECRETKEY);		
//		FeedTemplateMapper mp = new FeedTemplateMapper(new String[]{"jp.reflexworks.test.model"});		
		//DeflateUtil deflateUtil = new DeflateUtil();
		DeflateUtil deflateUtil = new DeflateUtil(Deflater.BEST_SPEED, true);

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
		
		assertTrue(true);
	}

	@Test
	public void testStaticPackages2() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
	
		Map<String, String> MODEL_PACKAGE = new HashMap<String, String>();
		MODEL_PACKAGE.putAll(AtomConst.ATOM_PACKAGE);
		MODEL_PACKAGE.put("jp.reflexworks.test2.model", "test2=http://jp.reflexworks/test2");
		FeedTemplateMapper mp = new FeedTemplateMapper(MODEL_PACKAGE, SECRETKEY);
		
		jp.reflexworks.test2.model.Feed feed = createTest2Feed();
		String xml = mp.toXML(feed);
		System.out.println("--- testStaticPackages2 (XML) ---");
		System.out.println(xml);
		
		String json = mp.toJSON(feed);
		System.out.println("--- testStaticPackages2 (JSON) ---");
		System.out.println(json);

		byte[] msgData = mp.toMessagePack(feed);
		System.out.println("--- testStaticPackages2 (MessagePack) ---");
		System.out.println(msgData);
		
		System.out.println("------");

		int idx = xml.indexOf("<test2:");
		assertTrue(idx == -1);
	}
	
	private jp.reflexworks.test2.model.Feed createTest2Feed() {
		jp.reflexworks.test2.model.Feed feed = new jp.reflexworks.test2.model.Feed();
		feed._entry = new ArrayList<EntryBase>();
		
		String code = "100001";
		jp.reflexworks.test2.model.Entry entry = createTest2Entry(code);
		feed._entry.add(entry);
		code = "100002";
		entry = createTest2Entry(code);
		feed._entry.add(entry);
		
		return feed;
	}
	
	private jp.reflexworks.test2.model.Entry createTest2Entry(String code) {
		jp.reflexworks.test2.model.Entry entry = new jp.reflexworks.test2.model.Entry();
		entry.setMyUri("/1/item/" + code);
		entry.setTitle("商品" + code);
		entry._deleteFlg = "0";

		jp.reflexworks.test2.model.Info info = new jp.reflexworks.test2.model.Info();
		info.name = "えんぴつ";
		info.color = "緑";
		info.size = "15cm";
		info.category = "文房具";
		entry._info = info;
		
		List<jp.reflexworks.test2.model.Comment> comments = new ArrayList<jp.reflexworks.test2.model.Comment>();
		jp.reflexworks.test2.model.Comment comment = new jp.reflexworks.test2.model.Comment();
		comment._nickname = "なまえ1";
		comment._$$text = "普通のえんぴつです。";
		comments.add(comment);
		comment = new jp.reflexworks.test2.model.Comment();
		comment._nickname = "なまえ2";
		comment._$$text = "良い感じのえんぴつです。";
		comments.add(comment);
		entry._comment = comments;
		
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
        	title = defentry2._title;
        }
		System.out.println("\n=== [default] title=" + title);
        
		// template
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);
		String json = "{\"entry\" : {\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : {\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]}}}}";

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
        	title = defentry2._title;
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
//		String json = "{ \"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

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
        
		assertTrue(true);
	}

	@Test
	public void testArrayFeed2() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_

//		String json = "{\"feed\" : {\"entry\" : [{\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"}]}]}}";
//		String json = "{\"feed\" : {\"entry\" : [{\"title\" : \"test\"}]}}";
//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
//		String json = "{\"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\"}]}}";
//		String json = "{ \"feed\" : {\"entry\" : [{\"content\" : {\"_$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"_$term\":\"term1\"},{\"_$scheme\":\"scheme1\"},{\"_$label\":\"label1\"}],\"link\" : [{\"_$href\" : \"/0762678511-/allA/759188985520\",\"_$rel\" : \"self\"},{\"_$href\" : \"/transferring/all/0762678511-/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/@/spool/759188985520\",\"_$rel\" : \"alternate\"},{\"_$href\" : \"/0762678511-/historyA/759188985520\",\"_$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"email\" : \"email1\",\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

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
    	msgpack[2] = 0x21;
        
    	EntryBase entry2 = (EntryBase) mp0.fromMessagePack(msgpack,ENTRY);
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
        String xml2 = mp0.toXML(entry2);
		System.out.println(xml2);


	}

	@Test
	public void testGetSetvalue() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
//		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl);		// ATOM Feed/Entryのみ。パッケージは_
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls, 30, SECRETKEY);

		String json = "{ \"feed\" : {\"entry\" : [{\"id\" : \"/1/new,1\",\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}],\"category\" : [{\"$term\":\"term1\"},{\"$scheme\":\"scheme1\"},{\"$label\":\"label1\"}],\"link\" : [{\"$href\" : \"/0762678511-/allA/759188985520\",\"$rel\" : \"self\"},{\"$href\" : \"/transferring/all/0762678511-/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/@/spool/759188985520\",\"$rel\" : \"alternate\"},{\"$href\" : \"/0762678511-/historyA/759188985520\",\"$rel\" : \"alternate\"}],\"title\" : \"タイトル\",\"public\" : {\"int\":\"email1\"},\"verified_email\" : false,\"name\" : \"管理者\",\"given_name\" : \"X\",\"family_name\" : \"管理者Y\",\"error\" : { \"errors\" : [{\"domain\": \"com.google.auth\",\"reason\": \"invalidAuthentication\",\"message\": \"invalid header\",\"locationType\": \"header\",\"location\": \"Authorization\"},{\"domain\": \"com.google.auth2\",\"reason\": \"invalidAuthentication2\",\"message\": \"invalid header2\",\"locationType\": \"header2\",\"location\": \"Authorization2\"}],\"code\" : 100,\"message\" : \"Syntax Error\"},\"subInfo\" : {\"favorite\" : {\"food\" : \"カレー\",\"music\" : [\"ポップス1\",\"ポップス2\",\"ポップス3\"]},\"favorite3\" : {\"food\" : \"うどん\",\"updated\" : \"2013-09-30T14:06:30+09:00\"}}}]}}";

//		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"123\"}]}}";
		FeedBase feed = (FeedBase) mp.fromJSON(json);
		
		// MessagePack test
		System.out.println("\n=== XML Entry(テキストノード+Link) シリアライズ ===");
		String xml = mp.toXML(feed);
		System.out.println(xml);

		EntryBase entry = feed._entry.get(0);
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
		System.out.println("link(ATOM Entry) email value="+entry.getValue("contributor.email"));
		System.out.println("link(ATOM Entry) uri value="+entry.getValue("contributor.uri"));
		System.out.println("link(ATOM Entry) name value="+entry.getValue("contributor.name"));
		
		// TODO contributor
		Contributor contributor = new Contributor();
		entry._contributor.add(contributor);
		contributor._uri = getAclUrn("888", "CRUD");
		contributor._name = "テストネーム";
		contributor = new Contributor();
		entry._contributor.add(contributor);
		contributor._uri = getAclUrn("889", "CRUD");
		contributor._name = "てすと";

		Cipher cipher = CipherUtil.getInstance();
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

		Condition[] conditions = new Condition[19];
		
		conditions[0] = new Condition("subInfo.favorite.food", "カレー");
		conditions[1] = new Condition("subInfo.favorite3.food", "うどん");
		conditions[2] = new Condition("verified_email", "false");		// boolean
		conditions[3] = new Condition("subInfo.favorite3.updated", "2013-09-30 14:06:30");	// date(Tや+09:00は省略可能)
		conditions[4] = new Condition("error.errors.domain", "com.google.auth");	// List検索
		conditions[5] = new Condition("error.errors.domain", "com.google.auth2");	// List検索
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
		conditions[17] = new Condition("author.name", "fuga");	
		conditions[18] = new Condition("public.int", "email1");	// java予約語項目
		
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
			"test1"				// 最後尾に追加はOK
		};

		FeedTemplateMapper mp0 = new FeedTemplateMapper(new String[] {"_"}, SECRETKEY);	
		boolean precheck = mp0.precheckTemplate(entitytempl, entitytempl_new);
		System.out.println("precheck:"+precheck);
		assertTrue(precheck);

	}

	@Test
	public void testMsgPackFeedWithDeflate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		FeedTemplateMapper mp = new FeedTemplateMapper(entitytempl, entityAcls, 30, SECRETKEY);
		//DeflateUtil deflateUtil = new DeflateUtil();
		DeflateUtil deflateUtil = new DeflateUtil(Deflater.BEST_SPEED, true);

		String json = "{\"feed\" : {\"entry\" : [{ \"verified_email\": false,\"family_name\" : \"f\",\"Idx\" : \"1\",\"title\" : \"hello\", \"subInfo\" : { \"favorite2\": { \"food\" : { \"food1\" : \"ラーメン\"}}},\"link\" : [{\"$href\" : \"/test/1\",\"$rel\" : \"self\"}] }]}}";

		FeedBase feed = (FeedBase) mp.fromJSON(json);
		
		// MessagePack test
		System.out.println("\n=== MessagePack Entry シリアライズ ===");
        byte[] mbytes = mp.toMessagePack(feed);
		System.out.println("len:"+mbytes.length);
		System.out.println("array:"+ mp.toArray(mbytes));
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
		if (feed != null && feed._entry != null && feed._entry.size() > 0) {
			EntryBase entry0 = feed._entry.get(0);
			//if (entry0._contributor == null) {
			if (entry0._contributor != null) {
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
		groups.add("/@testservice/$admin");
		feed.maskprop(uid, groups);
		System.out.println("\n=== maskprop (/@testservice/$admin グループ) ===");
		xml = mp.toXML(feed);
		System.out.println(xml);

		assertTrue(isMatch);
	}

	@Test
	public void testXmlFormat() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {
		FeedTemplateMapper mp3 = new FeedTemplateMapper(entitytempl3, entityAcls3, 30, SECRETKEY);
		FeedTemplateMapper mp0 = new FeedTemplateMapper(new String[] {"_"}, SECRETKEY);		// ATOM Feed/Entryのみ。パッケージは_

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
	public void testValidate() throws ParseException, JSONException, IOException, DataFormatException, ClassNotFoundException {

		/*
		// TODO Contributor#validate 実装後に要確認
		
		FeedTemplateMapper mp = new FeedTemplateMapper(entitytemplp, entityAcls2, 30);

		String json = "{\"feed\" : {\"entry\" : [{\"id\" : \"/@testservice/7/folders,2\",\"link\" : [{\"$href\" : \"/@testservice/7/folders\",\"$rel\" : \"self\"}],\"rights\" : \"暗号化される\",\"content\" : {\"$$text\":\"あああ\"},\"contributor\" : [{\"email\":\"abc@def\"},{\"uri\":\"http://abc\"},{\"name\":\"hoge\"}],\"author\" : [{\"email\":\"xyz@def\"},{\"uri\":\"http://xyz\"},{\"name\":\"fuga\"}]}]}}";

		FeedBase feed = (FeedBase)mp.fromJSON(json);
		String xml = null;
	
//		String uid = "6";
		String uid = "7";
		List<String> groups = new ArrayList<String>();

		// validate test
		feed.validate(uid, groups);
		*/

		assertTrue(true);
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

}
