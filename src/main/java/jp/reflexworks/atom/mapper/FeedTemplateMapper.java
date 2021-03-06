package jp.reflexworks.atom.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Loader;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.template.builder.ReflectionTemplateBuilder;
import org.msgpack.type.Value;
import org.msgpack.util.json.JSONBufferUnpacker;

//import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;





import jp.reflexworks.atom.api.AtomConst;
import jp.reflexworks.atom.entry.Element;
import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.util.SurrogateConverter;
import jp.sourceforge.reflex.core.ResourceMapper;
import jp.sourceforge.reflex.exception.JSONException;
import jp.sourceforge.reflex.util.DateUtil;
import jp.sourceforge.reflex.util.FieldMapper;
import jp.sourceforge.reflex.util.StringUtils;

/**
 * FeedTemplateMapper
 * <p>
 * テンプレートから動的にエンティティクラスを作り、オブジェクトをXML/JSON/MessgaePackにシリアライズを行います。またその逆のデシリアライズを行います。
 * <ul>
 * <li>テンプレートにユーザ項目を記述することでATOMを拡張できます。</li>
 * <li>項目ごとにValidationやマスク化、暗号化、ACLなどを設定できます</li>
 * <li>静的なエンティティクラスがあればそれが優先して使われます</li>
 * <li>動的に生成したクラスは静的なクラスとして保存できます</li>
 * <li>new FeedTemplateMapper(new String[] {"パッケージ名"}); とするとユーザ定義項目はなくATOM Feed/Entryのみとなります</li>
 * </ul>
 * 
 */
public class FeedTemplateMapper extends ResourceMapper {

	private static Logger logger = Logger.getLogger(FeedTemplateMapper.class.getName());
	public static final String FIELDPATTERN = "^( *)([a-zA-Z_$][0-9a-zA-Z_$]{0,127})(\\(([a-zA-Z_]+)\\))?((?:\\[([0-9]+)?\\]|\\{([\\-0-9]*)~?([\\-0-9]+)?\\})?)(\\!?)(?:=(.+))?(?:[ \\t]*)$";

	private static final String MANDATORY = "!";
	private static final String ARRAY = "[";

	// atom クラス（順番は重要）
	public static final String[] ATOMCLASSES = { 
		"jp.reflexworks.atom.entry.Author",
		"jp.reflexworks.atom.entry.Category",
		"jp.reflexworks.atom.entry.Content",
		"jp.reflexworks.atom.entry.Contributor",
		"jp.reflexworks.atom.entry.Link",
		"jp.reflexworks.atom.entry.Element",		// Elementは本来はATOMクラスではないがここに必要
		"jp.reflexworks.atom.entry.Generator"
	};

	public static final String[] ATOMENTRYTMPL = {
		"author{}",
		" name",
		" uri",
		" email",
		"category{}",
		" $term",
		" $scheme",
		" $label",
		"content",
		" $src",				// 下に同じ
		" $type",				// この項目はContentクラスのvalidate(group)において$contentグループに属しているかのチェックをしている
		" $$text",				// 同上
		"contributor{}",
		" name",
		" uri",
		" email",
		"id",
		"link{}",
		" $href",
		" $rel",
		" $type",
		" $title",
		" $length", 
		"published",
		"rights",
		"rights_$type",
		"summary",
		"summary_$type",
		"title",
		"title_$type",
		"subtitle",
		"subtitle_$type",
		"updated",
	};	

	private static final String SERIALIZABLE = "java.io.Serializable";
	private static final String ENTRYBASE = "jp.reflexworks.atom.entry.EntryBase";
	private static final String FEEDBASE = "jp.reflexworks.atom.entry.FeedBase";
	private static final String SOFTSCHEMA = "jp.reflexworks.atom.entry.SoftSchema";
	private static final String CONDITIONCONTEXT = "jp.reflexworks.atom.mapper.ConditionContext";
	private static final String CIPHERCONTEXT = "jp.reflexworks.atom.mapper.CipherContext";
	private static final String MASKPROPCONTEXT = "jp.reflexworks.atom.mapper.MaskpropContext";
	private static final String SIZECONTEXT = "jp.reflexworks.atom.mapper.SizeContext";
	private static final String CONDITION = "jp.reflexworks.atom.api.Condition";
	private static final String CIPHERUTIL = "jp.reflexworks.atom.mapper.CipherUtil";
	private static final String ATOMCONST = "jp.reflexworks.atom.AtomConst";

	private static final int AUTHOR = 0;
	private static final int CATEGORY = 1;
	private static final int CONTENT = 2;
	private static final int CONTRIBUTOR = 3;
	private static final int ENTRYLINK = 4;

	// Arrayの要素クラス
	public static final String ELEMENTCLASS = "jp.reflexworks.atom.entry.Element";
	private static final String ELEMENTSIG = "Ljava/util/List<Ljp/reflexworks/atom/entry/Element;>;";

	/** ATOM : Package map */
	public static Map<String, String> ATOM_PACKAGE = AtomConst.ATOM_PACKAGE;

	private MessagePack msgpack = new MessagePack();
	private List<Meta> metalist;
	private TemplateRegistry registry;
	private ReflectionTemplateBuilder builder;
	private ClassPool pool;
	private Loader loader;
	private String packagename;
	private boolean isDefaultTemplate;
	private String folderpath;
	private String secretkey;

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String secretkey) 
	throws ParseException {
		this(template, null, 0, null, false, false, false, null, secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param secretkey
	 * @param jo_packages 既存クラスパッケージ指定
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String secretkey, Object jo_packages) 
	throws ParseException {
		this(template, null, 0, jo_packages, false, false, false, null, secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param compatible
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, boolean compatible, String secretkey) 
	throws ParseException {
		this(template, null, 0, null, false, false, compatible, null, secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			String secretkey) 
	throws ParseException {
		this(template, propAcls, indexmax, null, false, false, false, null, secretkey);
	}

	/**
	 * コンストラクタ.
	 * 既存クラスのパッケージを指定する場合はこのコンストラクタを使用してください。
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			String secretkey, Object jo_package) 
	throws ParseException {
		this(template, propAcls, indexmax, jo_package, false, false, false, null, 
				secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param compatible
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			boolean compatible, String secretkey) 
	throws ParseException {
		this(template, propAcls, indexmax, null, false, false, compatible, null, 
				secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param compatible
	 * @param folderpath
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			boolean compatible, String folderpath, String secretkey) 
	throws ParseException {
		this(template, propAcls, indexmax, null, false, false, compatible, folderpath, 
				secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param isCamel
	 * @param compatible
	 * @param folderpath
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			boolean isCamel, boolean compatible, String folderpath, String secretkey) 
	throws ParseException {
		this(template, propAcls, indexmax, null, isCamel, false, compatible, folderpath, 
				secretkey);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param template
	 * @param propAcls
	 * @param indexmax
	 * @param jo_packages
	 * @param isCamel
	 * @param useSingleQuote
	 * @param compatible
	 * @param reflectionProvider
	 * @param folderpath
	 * @param secretkey
	 * @throws ParseException
	 */
	public FeedTemplateMapper(String[] template, String[] propAcls, int indexmax, 
			Object jo_packages, boolean isCamel, boolean useSingleQuote, 
			boolean compatible, String folderpath, String secretkey) 
	throws ParseException {
		super(getJo_packages(template, jo_packages), isCamel, useSingleQuote, compatible);

		this.folderpath = folderpath;
		this.secretkey = secretkey;

		this.pool = new ClassPool();
		this.pool.appendClassPath(new ClassClassPath(EntryBase.class));
		this.loader = new Loader(Thread.currentThread().getContextClassLoader(), this.pool);

		loader.delegateLoadingOf(CONDITIONCONTEXT);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(CIPHERCONTEXT);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(MASKPROPCONTEXT);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(SIZECONTEXT);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(CONDITION);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(SOFTSCHEMA);			// 既存classは先に読めるようにする
		loader.delegateLoadingOf(CIPHERUTIL);
		loader.delegateLoadingOf(ATOMCONST);
		loader.delegateLoadingOf(ENTRYBASE);
		loader.delegateLoadingOf(FEEDBASE);

		// XMLデシリアライザのRXMapperのClassloaderにセットする。サーブレットでのメモリ増加に注意
		((jp.sourceforge.reflex.core.RXMapper)this.getClassMapper()).wrapped = new com.thoughtworks.xstream.mapper.DefaultMapper(this.loader); 

		registry = new TemplateRegistry(null);
		builder = new ReflectionTemplateBuilder(registry); // msgpack準備(Javassistで動的に作成したクラスはReflectionTemplateBuilderを使わないとエラーになる)

		// Entityテンプレートからメタ情報を作成する
		if (template != null) {
			metalist = getMetalist(mergeAtomEntry(template));
			if (propAcls != null) {
				addPropAcls(propAcls, indexmax);
			}
		} else {
			metalist = getMetalistFromPropAcls(propAcls);
		}

		if (jo_packages == null && template != null) {
			// テンプレートからクラスを生成
			registerClasses();
			if (template.length == 1) {
				isDefaultTemplate = true;
			}

		} else if (jo_packages instanceof Map | jo_packages instanceof String) {
			// package名からregistClass
			// パッケージ名からクラス一覧を取得
			Set<String> classnames = null;  
			if (jo_packages != null) {
				try {
					if (jo_packages instanceof String) {
						packagename = (String)jo_packages;			// 業務アプリのEntityクラス
						classnames = new LinkedHashSet<String>();
						classnames.addAll(new ArrayList(Arrays.asList(ATOMCLASSES)));
						classnames.addAll(getClassNamesFromPackage((String)jo_packages));	// ATOM Classesは先に読む必要がある
					} else if (jo_packages instanceof Map) {
						classnames = new LinkedHashSet<String>();
						classnames.addAll(new ArrayList(Arrays.asList(ATOMCLASSES))); // ATOM Classesは先に読む必要がある

						for (String key : ((Map<String,String>)jo_packages).keySet()) {
							if (key.indexOf(".atom.") < 0) {
								packagename = key;			// 業務アプリのEntityクラス
								classnames.addAll(getClassNamesFromPackage(key));
							}
						}
					}
				} catch (ClassNotFoundException e) {
					logger.log(Level.WARNING, e.getClass().getName(), e);
					ParseException pe = new ParseException("ClassNotFoundException : "+e.getMessage(), 0);
					pe.initCause(e);
					throw pe;
				} 
			}

			// MessagePackにクラスを登録
			if (classnames != null) {
				Set<Class<?>> registSet = new HashSet<Class<?>>();
				for (String classname : classnames) {
					try {
						loader.delegateLoadingOf(classname);			// 既存classは先に読めるようにする
						registerClass(classname);
					} catch (CannotCompileException e) {
						logger.warning("ClassNotFoundException : " + e.getMessage());
						ParseException pe = new ParseException("ClassNotFoundException : "+e.getMessage(), 0);
						pe.initCause(e);
						throw pe;
					} 
				}
			}

		}
	}

	private List<Meta> getMetalistFromPropAcls(String[] propAcls) {
		
		List<Meta> result = new ArrayList<Meta>();
		if (propAcls==null) return result;
		
		for (String propacl : propAcls) {
			String token[] = propacl.split("=");
			String token2[] = token[0].split(":");	// Index項目
			if (token2.length > 1) {
				Meta meta = new Meta();
				meta.name = token2[0]; // key
//				meta.index = convertIndex(token2[1],svc); // index
				meta.index = token2[1]; // index
				result.add(meta);
			}
			
		}
		return result;
	}
	
	/**
	 * Metalistを取得.
	 * 指定されたサービス名を先頭に付加する。
	 * @param svc サービス名
	 * @return 編集したMetalist
	 */
	public List<Meta> getMetalist(String svc) {
		for (Meta meta:metalist) {
			meta.index = convertIndex(meta.index, svc);
		}
		return metalist;
	}
	
	/**
	 * Metalistを取得
	 * @return Metalist
	 */
	public List<Meta> getMetalist() {
		return metalist;
	}

	private String convertIndex(String propAcl,String svc) {
		if (propAcl==null) return null;
		String token[] = propAcl.split("\\|");
		StringBuilder result = new StringBuilder();
		
		for (int i=0;i<token.length;i++) {
			// サービス名が指定されている場合はそのまま
			if (token[i].indexOf("/@")>=0||token[i].indexOf("^/$")>=0||svc==null) {
				result.append(token[i]);
			}else {
				int p = token[i].indexOf("/");
				result.append(token[i].substring(0,p)+"/@"+svc+token[i].substring(p));
			}
			if (i+1<token.length) {
				result.append("|");
			}
		}
		return result.toString();
	}

	private List<String> getClassNamesFromPackage(String packagename) throws ClassNotFoundException {
		List<String> result = getClasses(packagename + ".Entry");
		List<String> resultlist = new ArrayList<String>(result);
		Collections.reverse(resultlist);
		resultlist.add(packagename + ".Entry");
		resultlist.add(packagename + ".Feed");
		return resultlist;
	}

	private List<String> getClasses(String entry) throws ClassNotFoundException {
		int idx = entry.lastIndexOf(".");
		String packagename = entry.substring(0, idx + 1);
		Class entryclass = Class.forName(entry);
		List<String> result = new ArrayList<String>();
		Field fields[] = entryclass.getDeclaredFields();
		for (Field field : fields) {
			if (isClass(field)) {
				idx = field.getType().getName().indexOf("$");
				String classname = field.getType().getName().substring(idx + 1);
				if (classname.equals("java.util.List")) {
					int i = field.getGenericType().toString().indexOf("<");
					if (i>0) {
						classname = field.getGenericType().toString().substring(i+1,field.getGenericType().toString().length()-1);
					}else {
						classname = packagename+toCamelcase(field.getName().substring(idx + 2));
					}
				}
				result.add(classname);
				List<String> child = getClasses(classname);
				result.addAll(child);
			}
		}
		return result;

	}

	private boolean isClass(Field field) {
		if (!Modifier.isPublic(field.getModifiers())) return false;
		if (field.getType().isPrimitive()) return false;
		if (field.getType().getName().equals("java.lang.String")) return false; 
		if (field.getType().getName().equals("java.lang.Integer")) return false;
		if (field.getType().getName().equals("java.lang.Float")) return false;
		if (field.getType().getName().equals("java.lang.Long")) return false;
		if (field.getType().getName().equals("java.lang.Double")) return false;
		if (field.getType().getName().equals("java.lang.Boolean")) return false;
		if (field.getType().getName().equals("java.util.Date")) return false;
		return true;	
	}

	private String[] mergeAtomEntry(String[] jo_packages){
		if (jo_packages.length == 1) {
			// ATOM Feed/Entryだけのテンプレート。パッケージ名は1行目、
			jo_packages = new String[]{((String[]) jo_packages)[0] + "{}"};
		}

		int l = ((String[]) jo_packages).length;
		String[] template = new String[ATOMENTRYTMPL.length+l];
		template[0] = ((String[]) jo_packages)[0];
		System.arraycopy(ATOMENTRYTMPL, 0, template, 1, ATOMENTRYTMPL.length);
		System.arraycopy((String[]) jo_packages, 1, template, ATOMENTRYTMPL.length + 1, l - 1);

		return template;
	}

	private static final String aclpattern = "([/0-9a-zA-Z_$*@]+\\+(?:R|W|RW),?)+";
	private static final String STRMAXLENGTH = "1048576";	// 1MB
	
	private void addPropAcls(String[] propAcls, int indexmax) throws ParseException {

		Pattern patternp = Pattern.compile(aclpattern);
		List<String> keys = new ArrayList<String>();

		int i = 0;
		for (String propacl : propAcls) {
			String k[] = propacl.split("=");
			int k2 = k[0].indexOf("#");	// 暗号化項目
			String k3[] = k[0].split(":");	// Index項目
			String key = null;
			String privatekey = null;
			String index = null;
			if (k2 >= 0 && k3.length > 1) throw new ParseException("Only one of these(:,#) to be specified.'" + k[0] + "'",0);
			key = k[0];
			if (k2 >= 0) {
				key = k[0].substring(0, k2);
				privatekey = secretkey;
				if (k[0].length() != k2 + 1) throw new ParseException("Illegal format.'" + k[0] + "'",0);
			}
			if (k3.length > 1) {
				key = k3[0];
				index = k3[1];
				i++;
				if (i >= indexmax) throw new ParseException("Custom property index limit exceeded.'" + k[0] + "'",0);
			}
			if (keys.contains(key)) throw new ParseException("Already specified.'" + k[0] + "'",0);
			keys.add(key);

			List<String> aclR = null;
			List<String> aclW = null;

			if (k.length > 1) {
				aclR = new ArrayList<String>();
				aclW = new ArrayList<String>();
				Matcher matcherp = patternp.matcher(k[1]);
				if (!matcherp.find()) throw new ParseException("Unexpected property ACL format. '" + k[0] + "'="+k[1],0);

				String[] strAry = k[1].split(",");
				for (String token : strAry) {
					if (token.contains("R")) {
						if (aclR == null) {
							aclR = new ArrayList<String>();
						}
						int p = token.indexOf("+");
						if (p < 0) throw new ParseException("Unexpected property ACL format. '" + k[0] + "'="+k[1],0);
						aclR.add(token.substring(0, p));
					}
					if (token.contains("W")) {
						if (aclW == null) {
							aclW = new ArrayList<String>();
						}
						int p = token.indexOf("+");
						if (p < 0) throw new ParseException("Unexpected property ACL format. '" + k[0] + "'="+k[1],0);
						aclW.add(token.substring(0, p));
					}
				}
			}
			boolean isExist = false;
			for (Meta meta : metalist) {
				if (meta.name.equals(key)) {
					meta.aclR = aclR;
					meta.aclW = aclW;
					isExist = true;
					if (privatekey != null) {
						if (meta.isArray || meta.isMap) throw new ParseException("Can't specify encription to '" + key + "'.",0);
						if (meta.index != null) throw new ParseException("Can't specify encription for the index property.'" + k[0] + "'",0);
						meta.privatekey = privatekey;
						isExist = true;
					}
					if (index != null) {
						if (meta.isArray || meta.isMap) throw new ParseException("Can't specify index to '" + key + "'.",0);
						if (meta.privatekey != null) throw new ParseException("Can't specify index for the encription property.'" + k[0] + "'",0);
						meta.index = index;
						isExist = true;
					}
				}
			}
			if (!isExist) throw new ParseException("Not found property '" + k[0] + "' in the template.",0);
		}
	}

	private boolean isSkip(Class<?> type) {
		if (type.isPrimitive()) {
			// プリミティブ型はスキップする。
			return true;
		}
		if (type.getName().startsWith("java.")) {
			if (!FieldMapper.isCollection(type)) {
				return true;
			}
		}
		if (isBaseclass(type.getName())) {
			return true;
		}

		return false;
	}

	// プリミティブ型、java〜パッケージは除く。
	// Listの場合、ジェネリックタイプも調べる。

	private boolean isBaseclass(String name) {
		if (name == null) return false;
		return name.indexOf(ENTRYBASE) >= 0 || name.indexOf(FEEDBASE) >= 0;
	}

	/*
	 * root entry
	 */
	private String getRootEntry(boolean isFeed) {
		return getRootEntry(packagename, isFeed);
	}

	/*
	 * root entry
	 */
	private static String getRootEntry(String packagename, boolean isFeed) {
		return isFeed ? packagename + ".Feed":packagename + ".Entry";
	}

	private boolean isEntry(String classname) {
		int dot = classname.lastIndexOf(".");
		if (dot > 0) {
			String token = classname.substring(dot);
			return token.equals(".Entry");
		}
		return false;
	}

	private boolean isFeed(String classname) {
		int dot = classname.lastIndexOf(".");
		if (dot > 0) {
			String token = classname.substring(dot);
			return token.equals(".Feed");
		}
		return false;
	}

	private static String toCamelcase(String name) {
		return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
	}

	/**
	 * ATOM Packageとユーザ Packageを取得する
	 * @param jo_packages
	 * @return
	 */
	private static Map<String, String> getJo_packages(String[] template, 
			Object jo_packages) {
		if (jo_packages != null) {
			if (jo_packages instanceof Map) {
				return (Map)jo_packages;
			} else if (jo_packages instanceof String) {
				Map result_packages = new LinkedHashMap<String,String>();
				result_packages.put((String)jo_packages, "");
				return result_packages;
			}
		}
		Map result_packages = new LinkedHashMap<String,String>();
		result_packages.putAll(ATOM_PACKAGE);
		if (template != null) {
			result_packages.put(parseLine0((template)[0]), "");
		}
		return result_packages;
	}

	/**
	 * Entityメタ情報インナークラス
	 *
	 */
	public static class Meta {

		/**
		 * 階層のレベル
		 */
		public int level;

		/**
		 * タイプ
		 */
		public String type;

		/**
		 * 属しているクラス
		 */
		public String parent;

		/**
		 * 項目名
		 */
		public String self;

		/**
		 * 暗号化のための秘密鍵
		 */
		public String privatekey;

		/**
		 * インデックス
		 */
		public String index; 

		/**
		 * 必須項目
		 */
		public boolean isMandatory; 

		/**
		 * 降順
		 */
		public boolean isDesc; 

		/**
		 * バリーデーション用正規表現
		 */
		public String regex; 

		/**
		 * ACL(READ）
		 */
		public List<String> aclR;

		/**
		 * ACL(WRITE)
		 */
		public List<String> aclW;

		/**
		 * 配列フラグ
		 */
		public boolean isArray;

		/**
		 * Mapフラグ
		 */
		public boolean isMap;

		/**
		 * 最小
		 */
		public String min;

		/**
		 * 最大
		 */
		public String max;	

		/**
		 * 名前
		 */
		public String name;

		/**
		 * XMLの属性
		 */
		public boolean canattr;
		
		/**
		 * 繰り返し項目
		 */
		public boolean repeated;

		/**
		 * レコード項目(BigQuery)
		 */
		public boolean isrecord;

		/**
		 * BigQueryの型
		 */
		public String bigquerytype;

		public String typesrc;

		
		/**
		 * Camelケースで名前を返す
		 * @return Camelcaseの名前
		 */
		public String getSelf() {
			if (self == null) return null;
			if (self.startsWith("_")) return toCamelcase(self.substring(1));
			return toCamelcase(self);
		}

		/**
		 * 子要素を持っているか
		 * @return 子要素を持っている場合true
		 */
		public boolean hasChild() {
			if (type==null) return false;
			return type.indexOf(getSelf()) > 0;
		}

		/**
		 * 型が数値（int,long,float,double）かどうか
		 * @return 数値の場合true
		 */
		public boolean isNumeric() {
			if (type.equals("Integer") || type.equals("Long") || type.equals("Float") || type.equals("Double")) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public Class getClass(String classname) throws ClassNotFoundException {
		// ATOMの優先すべき項目名の場合はATOMクラスを読む
		int dot = classname.lastIndexOf(".");
		if (dot > 0) {
			String token = classname.substring(dot);
			if (token.equals(".Link")) {
				classname = ATOMCLASSES[ENTRYLINK];
			} else if (token.equals(".Contributor")) {
				classname = ATOMCLASSES[CONTRIBUTOR];
			} else if (token.equals(".Content")) {
				classname = ATOMCLASSES[CONTENT];
			} else if (token.equals(".Author")) {
				classname = ATOMCLASSES[AUTHOR];
			} else if (token.equals(".Category")) {
				classname = ATOMCLASSES[CATEGORY];
			}
		}
		return loader.loadClass(classname);
	}

	/**
	 * ClassPoolを返す
	 * @return pool
	 */
	public ClassPool getPool() {
		return pool;
	}

	/* (非 Javadoc)
	 * @see jp.sourceforge.reflex.core.ResourceMapper#toMessagePack(java.lang.Object)
	 */
	public byte[] toMessagePack(Object entity) throws IOException {
		if (entity==null) return null;
		else return msgpack.write(entity);
	}

	/* (非 Javadoc)
	 * @see jp.sourceforge.reflex.core.ResourceMapper#toMessagePack(java.lang.Object, java.io.OutputStream)
	 */
	public void toMessagePack(Object entity, OutputStream out)
			throws IOException {
		if (entity==null) return;
		else msgpack.write(out, entity);
	}

	/**
	 * MessagePackからオブジェクトにデシリアライズする.
	 * <p>
	 * Feed形式のデータを設定してください。
	 * </p>
	 * @param msg MessagePackデータ
	 * @return オブジェクト
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object fromMessagePack(byte[] msg) 
			throws IOException, ClassNotFoundException  {
		return fromMessagePack(msg, true);
	}

	/**
	 * MessagePackからオブジェクトにデシリアライズする
	 * @param msg
	 * @param isFeed
	 * @return オブジェクト
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object fromMessagePack(byte[] msg, boolean isFeed) 
			throws IOException, ClassNotFoundException  {
		if (msg==null) return null;
		else return msgpack.read(msg, loader.loadClass(getRootEntry(isFeed)));
	}

	/* (非 Javadoc)
	 * @see jp.sourceforge.reflex.core.ResourceMapper#toArray(byte[])
	 */
	public Object toArray(byte[] msg) 
			throws IOException, ClassNotFoundException {
		return msgpack.read(msg);
	}

	/**
	 * MessagePackからオブジェクトにデシリアライズする
	 * @param msg
	 * @param isFeed
	 * @return オブジェクト
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object fromMessagePack(InputStream msg, boolean isFeed) 
			throws IOException, ClassNotFoundException {
		if (msg==null) return null;
		else return msgpack.read(msg, loader.loadClass(getRootEntry(isFeed)));
	}

	/* (非 Javadoc)
	 * @see jp.sourceforge.reflex.core.ResourceMapper#fromJSON(java.lang.String)
	 */
	public Object fromJSON(String json) throws JSONException {
		if (json==null) return null;
		JSONBufferUnpacker u = new JSONBufferUnpacker(msgpack).wrap(json.getBytes());
		Value v;
		try {
			v = u.readValue();

		} catch (IOException e) {
			throw new JSONException(e);
		}
		return parseValue("", v);
	}

	public Object fromJSON(Reader json) throws JSONException {

		if (json==null) return null;
		BufferedReader br = new BufferedReader(json);
		try {
			String jsonstring = "";
			for (;;) {
				String line = br.readLine();
				if (line == null)
					break;
				jsonstring = jsonstring + line;
			}
			return fromJSON(jsonstring);
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
	
	/* (非 Javadoc)
	 * @see jp.sourceforge.reflex.core.ResourceMapper#fromArray(java.lang.String, boolean)
	 */
	public Object fromArray(String array, boolean isFeed) throws JSONException {
		JSONBufferUnpacker u = new JSONBufferUnpacker(msgpack).wrap(array.getBytes());
		try {
			return msgpack.convert(u.readValue(), loader.loadClass(getRootEntry(isFeed)));
		} catch(Exception e) {
			throw new JSONException(e);
		}
	}

	private String getSignature(String classname) {
		String signature = "Ljava/util/List<L" + classname.replace(".", "/") + ";>;";
		return signature;
	}

	/**
	 * メタ情報から動的クラスを生成する
	 * 
	 * @return 生成したクラス名のSet
	 * @throws CannotCompileException
	 */
	private Set<String> generateClass()
			throws CannotCompileException {

		pool.importPackage("java.util.Date");

		Set<String> classnames = getClassnames();

		CtClass[] ci;
		try {
			ci = new CtClass[] { pool.get(SOFTSCHEMA), pool.get(SERIALIZABLE) };
		} catch (NotFoundException e1) {
			throw new CannotCompileException(e1);
		} 

		for (String classname : classnames) {
			CtClass cc;
			try {
				// ATOM classなど既に登録されていたらそれを使う
				cc = pool.get(classname);
			} catch (NotFoundException ne1) {
				cc = pool.makeClass(classname);
				cc.setInterfaces(ci);
				if (isEntry(classname)) {
					CtClass cs;
					try {
						loader.delegateLoadingOf(ENTRYBASE);			// 既存classは先に読めるようにする
						cs = pool.get(ENTRYBASE);
						cc.setSuperclass(cs); // superclassの定義
					} catch (NotFoundException e) {
						throw new CannotCompileException(e);
					}
				} else if (isFeed(classname)) {
					CtClass cs;
					try {
						loader.delegateLoadingOf(FEEDBASE);			// 既存classは先に読めるようにする
						cs = pool.get(FEEDBASE);
						cc.setSuperclass(cs); // superclassの定義

					} catch (NotFoundException e) {
						throw new CannotCompileException(e);
					}
				} 
			}

			StringBuilder getvalue = new StringBuilder();
			getvalue.append(getvalueFuncS);
			StringBuilder encrypt = new StringBuilder();
			StringBuilder decrypt = new StringBuilder();
			StringBuilder ismatch = new StringBuilder();
			StringBuilder validation = new StringBuilder();
			StringBuilder maskprop = new StringBuilder();
			StringBuilder getsize = new StringBuilder();
			if (isEntry(classname)) {
				ismatch.append(ismatchFuncS2);
				maskprop.append(maskpropFuncS2);
				validation.append(validateFuncS2);
				encrypt.append(encryptFuncS2+",\""+this.secretkey+"\");");
				decrypt.append(decryptFuncS2+",\""+this.secretkey+"\");");
				getsize.append(getsizeFuncS2);
			} else {
				if (!isFeed(classname)) {
					ismatch.append(ismatchFuncS+setparent(classname));
					maskprop.append(maskpropFuncS+setparent(classname));
					validation.append(validateFuncS);
					encrypt.append(encryptFuncS+setparent(classname));
					decrypt.append(decryptFuncS+setparent(classname));
					getsize.append(getsizeFuncS+setparent(classname));
				} else {
					maskprop.append(maskpropFuncS3);
					validation.append(validateFuncS3);
					encrypt.append(encryptFuncS4);
					decrypt.append(decryptFuncS4);
					getsize.append(getsizeFuncS3);
				}
			}

			for (int i = 0; i < matches(classname); i++) {

				Meta meta = getMetaByLevel(classname, i);
				String type = "public " + meta.type + " ";
				String field = meta.self + ";";
				try {
					// ATOM classなど既に登録されていたらそれを使う
					CtField f = cc.getField(meta.self);

				} catch (NotFoundException ne2) {

					// for Array
					if (meta.isArray) {
						try {
							CtClass objClass = pool.get("java.util.List");
							CtField arrayfld = new CtField(objClass, meta.self, cc); 
							arrayfld.setModifiers(Modifier.PUBLIC);
							SignatureAttribute.ObjectType st = SignatureAttribute.toFieldSignature(ELEMENTSIG);
							arrayfld.setGenericSignature(st.encode());    // <T:Ljava/lang/Object;>Ljava/lang/Object;

							// create the annotation
							ConstPool constpool = cc.getClassFile().getConstPool();
							AnnotationsAttribute attr = new AnnotationsAttribute(constpool,AnnotationsAttribute.visibleTag);
							Annotation annot = new Annotation("org.msgpack.annotation.Index", constpool);
							annot.addMemberValue("value", new IntegerMemberValue(constpool,i));
							attr.addAnnotation(annot);
							// add the annotation 
							arrayfld.getFieldInfo().addAttribute(attr);

							cc.addField(arrayfld);
							
							// getter/setterはTaggingServiceの更新処理で使用
							CtMethod m = CtNewMethod.make("public java.util.List get" + meta.getSelf()
									+ "() {" + "  return " + meta.self + "; }", cc);
							cc.addMethod(m);
							m = CtNewMethod.make("public void set" + meta.getSelf()
									+ "(java.util.List " + meta.self + ") { this."
									+ meta.self + "=" + meta.self + ";}", cc);
							cc.addMethod(m);

						} catch (NotFoundException e) {
							throw new CannotCompileException(e);
						} catch (BadBytecode e) {
							throw new CannotCompileException(e);
						}

					} else if (meta.isMap) {

						try {
							CtClass objClass = pool.get("java.util.List");
							CtField arrayfld = new CtField(objClass, meta.self, cc); 
							arrayfld.setModifiers(Modifier.PUBLIC);
							SignatureAttribute.ObjectType st = SignatureAttribute.toFieldSignature(getSignature(packagename+"."+meta.getSelf()));
							arrayfld.setGenericSignature(st.encode());    // <T:Ljava/lang/Object;>Ljava/lang/Object;
							
							// create the annotation
							ConstPool constpool = cc.getClassFile().getConstPool();
							AnnotationsAttribute attr = new AnnotationsAttribute(constpool,AnnotationsAttribute.visibleTag);
							Annotation annot = new Annotation("org.msgpack.annotation.Index", constpool);
							annot.addMemberValue("value", new IntegerMemberValue(constpool,i));
							attr.addAnnotation(annot);
							// add the annotation 
							arrayfld.getFieldInfo().addAttribute(attr);

							cc.addField(arrayfld);

							// getter/setterはTaggingServiceの更新処理で使用
							CtMethod m = CtNewMethod.make("public java.util.List get" + meta.getSelf()
									+ "() {" + "  return " + meta.self + "; }", cc);
							cc.addMethod(m);
							m = CtNewMethod.make("public void set" + meta.getSelf()
									+ "(java.util.List " + meta.self + ") { this."
									+ meta.self + "=" + meta.self + ";}", cc);
							cc.addMethod(m);

						} catch (NotFoundException e) {
							throw new CannotCompileException(e);
						} catch (BadBytecode e) {
							throw new CannotCompileException(e);
						}

					} else {
						CtField f2 = CtField.make(type + field, cc); // フィールドの定義

						// create the annotation
						ConstPool constpool = cc.getClassFile().getConstPool();
						AnnotationsAttribute attr = new AnnotationsAttribute(constpool,AnnotationsAttribute.visibleTag);
						Annotation annot = new Annotation("org.msgpack.annotation.Index", constpool);
						annot.addMemberValue("value", new IntegerMemberValue(constpool,i));
						attr.addAnnotation(annot);
						// add the annotation 
						f2.getFieldInfo().addAttribute(attr);

						cc.addField(f2);

						// getter/setterはTaggingServiceの更新処理で使用
						CtMethod m = CtNewMethod.make(type + "get" + meta.getSelf()
								+ "() {" + "  return " + meta.self + "; }", cc);
						cc.addMethod(m);
						m = CtNewMethod.make("public void set" + meta.getSelf()
								+ "(" + meta.type + " " + meta.self + ") { this."
								+ meta.self + "=" + meta.self + ";}", cc);
						cc.addMethod(m);
					}

				}

				// 暗号化
				if (meta.privatekey != null) {
					encrypt.append("if (" + meta.self + "!=null) if ((context.parent==null)||(context.parent!=null)&&(\"" + meta.name + "\".indexOf(context.parent)>=0))" + meta.self + "=(" + meta.type + ") jp.reflexworks.atom.mapper.CipherUtil.doEncrypt(\"\"+" + meta.self + ", \"" + meta.privatekey + "\"+context.id, context.cipher);");
					decrypt.append("if (" + meta.self + "!=null) if ((context.parent==null)||(context.parent!=null)&&(\"" + meta.name + "\".indexOf(context.parent)>=0))" + meta.self + "=(" + meta.type + ") jp.reflexworks.atom.mapper.CipherUtil.doDecrypt(\"\"+" + meta.self + ", \"" + meta.privatekey + "\"+context.id, context.cipher);");
				}

				// 子要素のgetValue/setValue
				if (meta.hasChild()) {
					if (meta.isMap) {
						getvalue.append("if (fldname.indexOf(\"" + meta.name + ".\")>=0&&" + meta.self + "!=null) { java.util.List result = new java.util.ArrayList(); for (int i=0;i<" + meta.self + ".size();i++) { Object value =((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).getValue(fldname);result.add(value);} if (result.size()>0) return result;}"); 
						if (!isFeed(classname)) {
							encrypt.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).encrypt(context);}"); 
							decrypt.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).decrypt(context);}"); 
							ismatch.append("if (" + meta.self + "!=null) {for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).isMatch(context);}}"); 
							maskprop.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).maskprop(context);}"); 
							getsize.append("if (" + meta.self + "!=null) {for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).getsize(context);context.arraycount++;}context.count++;context.mapcount++;context.keysize+=\"+meta.self+\".length();}"); 
						} else {
							encrypt.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.EntryBase)" + meta.self + ".get(i)).encrypt(cipher);}"); 
							decrypt.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.EntryBase)" + meta.self + ".get(i)).decrypt(cipher);}"); 
							maskprop.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.EntryBase)" + meta.self + ".get(i)).maskprop(uid,groups);}"); 
							getsize.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.EntryBase)" + meta.self + ".get(i)).getsize();}"); 
						}
					} else {
						getvalue.append("if (fldname.indexOf(\"" + meta.name + ".\")>=0&&" + meta.self + "!=null) { Object value=" + meta.self + ".getValue(fldname);if (value!=null) return value;}");
						encrypt.append("if (" + meta.self + "!=null) " + meta.self + ".encrypt(context);");
						decrypt.append("if (" + meta.self + "!=null) " + meta.self + ".decrypt(context);");
						ismatch.append("if (" + meta.self + "!=null) " + meta.self + ".isMatch(context);");
						getsize.append("if (" + meta.self + "!=null) {" + meta.self + ".getsize(context);context.count++;context.keysize+=\"+meta.self+\".length();}");
						if (!isFeed(classname)) {
							maskprop.append("if (" + meta.self + "!=null) " + meta.self + ".maskprop(context);");
						} else {
							maskprop.append("if ("+meta.self+"!=null) "+ meta.self+".maskprop(uid,groups);");
						}
					}
				} else {
					// getValueで返せるようにする
					getvalue.append("if (fldname.equals(\"" + meta.name + "\")) return " + meta.self + ";");
					ismatch.append("if (" + meta.self + "!=null) {context.fldname=\"" + meta.name + "\";context.type=\"" + meta.type + "\";context.obj=" + meta.self + ";");
					ismatch.append("if (context.parent==null) jp.reflexworks.atom.mapper.ConditionContext.checkCondition(context);");
					ismatch.append("else if (\"" + meta.name + "\".indexOf(context.parent)>=0)");
					ismatch.append("jp.reflexworks.atom.mapper.ConditionContext.checkCondition(context);}");
					getsize.append("if (" + meta.self + "!=null) if ((context.parent==null)||(context.parent!=null)&&(\"" + meta.name + "\".indexOf(context.parent)>=0)) {context.size+="+getSizeStr(meta.type,meta.self)+";context.count++;context.keysize+=\"+meta.self+\".length();}");
				}

				// バリデーションチェック
				if (meta.isArray) {
					validation.append(getValidatorLogicArray(meta));
				} else {
					validation.append(getValidatorLogic(meta));

					// 子要素のValidation
					if (meta.hasChild()) {
						if (meta.isMap) {
							if (!isFeed(classname)) {
								validation.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.SoftSchema)" + meta.self + ".get(i)).validate(uid,groups,myself);}"); 
							} else {
								validation.append("if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) { ((jp.reflexworks.atom.entry.EntryBase)" + meta.self + ".get(i)).validate(uid,groups);}"); 
							}
						} else {
							if (!isFeed(classname)) {
								validation.append("if (" + meta.self + "!=null) " + meta.self + ".validate(uid,groups,myself);");
							} else {
								validation.append("if (" + meta.self + "!=null) " + meta.self + ".validate(uid,groups);");
							}
						}
					}
				}
				// 項目ACL(W)
				validation.append(getValidatorPropW(meta));
				// 項目ACL(R)
				maskprop.append(getValidatorPropR(meta));
			}
			// Validation Method追加
			validation.append(validateFuncE);
			CtMethod m = CtNewMethod.make(validation.toString(), cc);
			cc.addMethod(m);

			getvalue.append(getvalueFuncE);
			CtMethod m2 = CtNewMethod.make(getvalue.toString(), cc);
			cc.addMethod(m2);

			if (!isFeed(classname)) {
				if (isEntry(classname)) {
					ismatch.append(ismatchFuncE2);
					encrypt.append(endFuncE);
					decrypt.append(endFuncE);
					maskprop.append(endFuncE);
					getsize.append(getsizeFuncE);
				} else {
					ismatch.append("context.parent=parent;"+endFuncE);
					encrypt.append("context.parent=parent;"+endFuncE);
					decrypt.append("context.parent=parent;"+endFuncE);
					maskprop.append("context.parent=parent;"+endFuncE);
					getsize.append("context.parent=parent;"+endFuncE);
				}
				CtMethod m5 = CtNewMethod.make(ismatch.toString(), cc);
				cc.addMethod(m5);
				CtMethod m3 = CtNewMethod.make(encrypt.toString(), cc);
				cc.addMethod(m3);
				CtMethod m4 = CtNewMethod.make(decrypt.toString(), cc);
				cc.addMethod(m4);
				CtMethod m7 = CtNewMethod.make(getsize.toString(), cc);
				cc.addMethod(m7);
			} else {
				maskprop.append(endFuncE);
			}
			CtMethod m6 = CtNewMethod.make(maskprop.toString(), cc);
			cc.addMethod(m6);

			/* 静的classFile作成 */
			if (folderpath != null && !cc.getName().equals("Author")
					&& !cc.getName().equals("Category") 
					&& !cc.getName().equals("Content") 
					&& !cc.getName().equals("Link") 
					&& !cc.getName().equals("Element") 
					&& !cc.getName().equals("Contributor")) {
				try {
					cc.writeFile(folderpath);
				} catch (IOException e) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, e.getClass().getName(), e);
					}
				}
			}

		}

		return classnames;
	}

	private String getSizeStr(String type, String self) {
		if (type.equals("String")) {
			return self+".length()";
		}else {
			return "(\"\"+"+self+").length()";
		}
	}

	private String setparent(String classname) {
		return "String parent=context.parent;if (context.parent==null) context.parent=\""+cutPackagename(classname) + "\";else context.parent=context.parent+\"." + cutPackagename(classname) + "\";";
	}
	
	private String cutPackagename(String classname) {
		String result = classname.substring(classname.lastIndexOf(".")+1);
		return (""+result.charAt(0)).toLowerCase(Locale.ENGLISH)+result.substring(1);
	}

	private final String getvalueFuncS = "public Object getValue(String fldname) {";
	private final String getvalueFuncE = "return null;}";
	private final String encryptFuncS = "public void encrypt(jp.reflexworks.atom.mapper.CipherContext context) {";
	private final String encryptFuncS2 = "public void encrypt(Object cipher) { jp.reflexworks.atom.mapper.CipherContext context= new jp.reflexworks.atom.mapper.CipherContext(cipher,this.id";
	private final String encryptFuncS4 = "public void encrypt(Object cipher) {";
	
	private final String decryptFuncS = "public void decrypt(jp.reflexworks.atom.mapper.CipherContext context) {";
	private final String decryptFuncS2 = "public void decrypt(Object cipher) { jp.reflexworks.atom.mapper.CipherContext context= new jp.reflexworks.atom.mapper.CipherContext(cipher,this.id";
	private final String endFuncE = "}";
	private final String decryptFuncS4 = "public void decrypt(Object cipher) {";

	private final String ismatchFuncS = "public void isMatch(jp.reflexworks.atom.mapper.ConditionContext context) {";
	private final String ismatchFuncS2 = "public boolean isMatch(jp.reflexworks.atom.api.Condition[] conditions) {" +
			"jp.reflexworks.atom.mapper.ConditionContext context = new jp.reflexworks.atom.mapper.ConditionContext(conditions);";
	private final String ismatchFuncE2 = "return context.isMatch();}";

	private final String validateFuncS = "public boolean validate(String uid, java.util.List groups, String myself) throws java.text.ParseException {";
	private final String validateFuncS2 = "public boolean validate(String uid, java.util.List groups) throws java.text.ParseException {String myself = getMyself();";
	private final String validateFuncS3 = "public boolean validate(String uid, java.util.List groups) throws java.text.ParseException {String myself = null;";

	private final String validateFuncE = "return true;}";
	private final String maskpropFuncS = "public void maskprop(jp.reflexworks.atom.mapper.MaskpropContext context) {";
	private final String maskpropFuncS2 = "public void maskprop(String uid, java.util.List groups) {jp.reflexworks.atom.mapper.MaskpropContext context= new jp.reflexworks.atom.mapper.MaskpropContext(uid,groups,getMyself());";
	private final String maskpropFuncS3 = "public void maskprop(String uid, java.util.List groups) {";

	private final String getsizeFuncS = "public void getsize(jp.reflexworks.atom.mapper.SizeContext context) {";
	private final String getsizeFuncS2 = "public int getsize() { jp.reflexworks.atom.mapper.SizeContext context= new jp.reflexworks.atom.mapper.SizeContext();";
	private final String getsizeFuncS3 = "public void getsize() {";
	private final String getsizeFuncE = "return context.size+context.keysize+(context.count+context.mapcount)*8+context.arraycount*10+100;}";
//	private final String getsizeFuncE = "return context.arraycount;}";

	/**
	 * バリデーションロジック（必須チェックと正規表現チェック）
	 * @param meta
	 * @return ロジック
	 */
	private String getValidatorPropW(Meta meta) {
		String line = ""; 
		if (meta.aclW!=null) {
			// ACLが設定されていて項目に値が存在している
			line += "if (uid!=null&&groups!=null&&groups.size()>=0&&"+ meta.self + "!=null) {";
			// 自分の属するグループが存在しなければエラー
			line += "boolean ex=false;";
			line += "java.util.ArrayList groups2 = new java.util.ArrayList(groups);";
			line += "groups2.add(\"\"+uid);";
			for(String aclw:meta.aclW) {
				if (aclw.equals("@")) line += "if (uid != null && uid.equals(myself)) ex=true;";
			}
			line += "for(int i=0;i<groups2.size();i++) {";
			for(String aclw:meta.aclW) {
				if (aclw.equals("/*")) line += "ex=true;";
				else if (aclw.startsWith("/")) {
					line += "java.util.regex.Pattern p = java.util.regex.Pattern.compile(\"^/@[^/]*"+aclw.replace("$", "\\\\$") +"$|^"+aclw.replace("$", "\\\\$")+"$\");";
					line += "java.util.regex.Matcher m = p.matcher(\"\"+groups2.get(i));";
					line += "if (m.find()) ex=true;";
				}else 
					line += "if (groups2.get(i).equals(\""+aclw+"\")) ex=true;";
			}
			line += "}";
			line += "if (!ex) throw new java.text.ParseException(\"Property '" + meta.name + "' is not writeable.\",0);";
			line += "}";
		}
		return line;
	}

	private String getValidatorPropR(Meta meta) {
		String line = ""; 
		if (meta.isDesc) {
			// desc項目は表示しない
			line += meta.self +"=null;";
		}
		if (meta.aclR != null) {
			// ACLが設定されていて項目に値が存在している
			line += "if ("+ meta.self + "!=null) {";
			// 自分の属するグループが存在しなければ値をnullにする
			line += "boolean ex=false;";
			line += "if (context.groups==null) context.groups = new java.util.ArrayList();";
			line += "java.util.ArrayList groups2 = new java.util.ArrayList(context.groups);";
			line += "groups2.add(\"\"+context.uid);";
			for(String aclr:meta.aclR) {
				if (aclr.equals("@")) line += "if (context.uid != null && context.uid.equals(context.myself)) ex=true;";
			}
			line += "for(int i=0;i<groups2.size();i++) {";
			for(String aclr:meta.aclR) {
				if (aclr.equals("/*")) line += "ex=true;";
				else if (aclr.startsWith("/")) {
					line += "java.util.regex.Pattern p = java.util.regex.Pattern.compile(\"^/@[^/]*"+aclr.replace("$", "\\\\$") +"$|^"+aclr.replace("$", "\\\\$")+"$\");";
					line += "java.util.regex.Matcher m = p.matcher(\"\"+groups2.get(i));";
					line += "if (m.find()) ex=true;";
				}
				else line += "if (groups2.get(i).equals(\""+aclr+"\")) ex=true;";
			}
			line += "}";
			line += "if ((context.parent==null)||(context.parent!=null)&&(\"" + meta.name + "\".indexOf(context.parent)>=0)) {";
			line += "if (!ex) "+ meta.self +"=null;";
			line += "}";
			line += "}";
		}
		return line;
	}


	private String getValidatorLogic(Meta meta) {
		String line = "";
		String metaorg = meta.self.replace("_desc", "");
		if (meta.isDesc) {
			line = "if (" + metaorg + "!=null) { try{ String d = \"0000000000000000000\"+new java.lang.Long(java.lang.Long.MAX_VALUE-Long.parseLong(" + metaorg + ".replaceAll(\"\\\\.|/|,|-\", \"\")));"+ meta.self +"=d.substring(d.length()-19);}catch(java.lang.NumberFormatException ne) {throw new java.text.ParseException(\"Property '" + metaorg + "' is not valid.(NumberFormatException, value=\"+" + metaorg + "+\")\",0);};}"; 
		}

		if (meta.isMandatory) {
			line = "if (" + meta.self + "==null) throw new java.text.ParseException(\"Required property '" + meta.self + "' not specified.\",0);";
		}
		if (meta.regex != null && !meta.regex.isEmpty()) {
			line += "if ("+meta.self+"!=null&&"+meta.self+".length()>0) {";
			line += "java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(\"" + meta.regex + "\");";
			line += "java.util.regex.Matcher matcher = pattern.matcher(\"\"+" + meta.self + ");";
			line += "if (!matcher.find()) throw new java.text.ParseException(\"Property '" + meta.self + "' is not valid.(regex=" + meta.regex + ", value=\"+" + meta.self + "+\")\",0);";
			line += "}";			
		}
		line += getMinmax(meta);

		return line;
	}

	private String getValidatorLogicArray(Meta meta) {
		String line = "";
		if (meta.isMandatory) {
			line = "if (" + meta.self + "==null) throw new java.text.ParseException(\"Required property '" + meta.self + "' not specified.\",0);";
		}
		if (meta.regex != null && !meta.regex.isEmpty()) {
			line += "if (" + meta.self + "!=null) for (int i=0;i<" + meta.self + ".size();i++) {";
			line += "java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(\"" + meta.regex + "\");";
			line += "String val=((" + ELEMENTCLASS + ")" + meta.self + ".get(i))._$$text;";
			line += "java.util.regex.Matcher matcher = pattern.matcher(val);";
			line += "if (!matcher.find()&&val!=null&&val.length()>0) throw new java.text.ParseException(\"Property '" + meta.self + "' is not valid.(regex=" + meta.regex + ", value=\"+val+\")\",0);";
			line += "}";
		}

		line += getMinmax(meta);
		return line;
	}

	private String getMinmax(Meta meta) {
		String line = "";
		if (meta.min != null && !meta.min.isEmpty()) {
			long max;
			long min = Long.parseLong(meta.min);
			if (meta.max != null && !meta.max.isEmpty()) {
				// min~maxチェック
				max = Long.parseLong(meta.max);
				if (meta.isNumeric()) {
					line += "if (" + meta.self + "!=null&&" + meta.self + ".longValue()<" + min + ") throw new java.text.ParseException(\"Minimum number of '" + meta.self + "' not met.\",0);";
					line += "if (" + meta.self + "!=null&&" + max +"<" + meta.self + ".longValue()) throw new java.text.ParseException(\"Maximum number of '" + meta.self + "' exceeded.\",0);";
				} else if (meta.isArray||meta.hasChild()) {
					line += "if (" + meta.self + "!=null&&" + meta.self + ".size()<" + min + ") throw new java.text.ParseException(\"Minimum number of '" + meta.self + "' not met.\",0);";
					line += "if (" + meta.self + "!=null&&" + max + "<" + meta.self + ".size()) throw new java.text.ParseException(\"Maximum number of '" + meta.self + "' exceeded.\",0);";
				} else if (meta.type.equals("String")) {
					line += "if (" + meta.self + "!=null&&" + meta.self + ".length()<" + min + ") throw new java.text.ParseException(\"Minimum length of '" + meta.self + "' not met.\",0);";
					line += "if (" + meta.self + "!=null&&" + max + "<" + meta.self + ".length()) throw new java.text.ParseException(\"Maximum length of '" + meta.self + "' exceeded.\",0);";
				}
			} else {
				// maxチェックのみ
				max = min;
				if (meta.isNumeric()) {
					line += "if (" + meta.self + "!=null&&" + max + "<" + meta.self + ".longValue()) throw new java.text.ParseException(\"Maximum number of '" + meta.self + "' exceeded.\",0);";
				} else if (meta.isArray || meta.hasChild()) {
					line += "if (" + meta.self + "!=null&&" + max + "<" + meta.self + ".size()) throw new java.text.ParseException(\"Maximum number of '" + meta.self + "' exceeded.\",0);";
				} else if (meta.type.equals("String")) {
					line += "if (" + meta.self + "!=null&&" + max + "<" + meta.self + ".length()) throw new java.text.ParseException(\"Maximum length of '" + meta.self + "' exceeded.\",0);";					
				}
			}
		}
		
		if (!meta.isArray && meta.type.equals("String")) {
			line += "if (" + meta.self + "!=null&&" + STRMAXLENGTH + "<" + meta.self + ".length()) throw new java.text.ParseException(\"Maximum length of '" + meta.self + "' exceeded.\",0);";					
		}
		
		return line;
	}

	private static String parseLine0(String line) {
		Pattern patternf = Pattern.compile(FIELDPATTERN);
		Matcher matcherf = patternf.matcher(line);

		if (matcherf.find()) {
			return "_" + matcherf.group(2);  // アンダーバーを強制的に付けることでjava予約語などとの重複をなくす
		} else {
			return "";
		}
	}
	
	private List<Meta> getMetalist(String[] entitytmpl) 
	throws ParseException {
		// 先頭のパッケージ名を退避してentryに置き換える
		this.packagename = parseLine0(entitytmpl[0]);
		return getMetalist(entitytmpl, packagename);
	}

	/**
	 * Entity Templateからメタ情報を作成する
	 * 
	 * @param entitytmpl
	 * @return メタ情報
	 * @throws ParseException
	 */
	public static List<Meta> getMetalist(String[] entitytmpl, String packagename) 
	throws ParseException {

		List<Meta> metalist = new ArrayList<Meta>();

		Pattern patternf = Pattern.compile(FIELDPATTERN);

		Meta meta = new Meta();
		Matcher matcherf;
		Stack<String> stack = new Stack<String>();
		String classname = getRootEntry(packagename, true);
		stack.push(classname);
		int level = 0;
		String fldname = "";
		boolean canattr = true;

		for (int l = 0; l < entitytmpl.length; l++) {
			String line = entitytmpl[l];
			if (l>0) line = " " + line;		// ２行目以降一段下げる
			matcherf = patternf.matcher(line);

			if (matcherf.find()) {
				if (meta.level != matcherf.group(1).length()) {
					level = matcherf.group(1).length();
					meta.isrecord = false;
					if (meta.level < level) {
						//２段階下がるとエラー
						if (meta.level + 1 < level) {
							throw new ParseException("Wrong Indentation:" + line, 0);
						}
						meta.isrecord = true;
						classname = packagename + "." + meta.getSelf();
						stack.push(classname);
						if (!meta.type.equals("String")) throw new ParseException("Can't specify (Type) for Map type:" + line, 0);
						meta.type = classname; // 子要素を持っている場合にタイプを自分にする

						String metaself = meta.self;
						if (meta.self.startsWith("_")) {
							metaself = metaself.substring(1);
						}

						if (meta.level == 0) {
							fldname = "";
						} else if (fldname.equals("")) {
							fldname = metaself;
						} else {
							fldname = fldname + "." + metaself;
						}
					} else {
						for (int i = 0; i < meta.level - level; i++) {
							stack.pop();
							classname = stack.peek();
							int p = fldname.lastIndexOf(".");
							if (p >= 0) {
								fldname = fldname.substring(0,fldname.lastIndexOf("."));
							} else {
								fldname = "";
							}
						}
					}
				}
				if (meta.self != null) {
					if (meta.regex != null && meta.regex.length() > 0 && meta.hasChild()) {
						throw new ParseException("Syntax error(illegal character in property or regex uses in the parent object):" + meta.self, 0);
					}
					metalist.add(meta);
				}

				if (l <= 1) {
					canattr = true;
				}
				else {
					if (meta.self.startsWith("_$")) {
						if (!meta.canattr) throw new ParseException("Attribute($) must be the first line.:" + meta.name, 0);
						if (meta.hasChild()) throw new ParseException("Can't specify attribute($) to.:" + meta.name, 0);
					}
					if (!meta.self.startsWith("_$")) {
						if (meta.level < level) {
							canattr = true;
						} else {
							canattr = false;
						}
					} else {
						canattr = true;
					}
				}

				meta = new Meta();
				meta.canattr = canattr;
				meta.level = level;
				meta.parent = classname;
				meta.privatekey = null;
				meta.index = null;
				meta.isMandatory = matcherf.group(9).equals(MANDATORY);
				meta.aclR = null;
				meta.aclW = null;
				
				// for BugQuery Schema
				if (matcherf.group(5) != null && !matcherf.group(5).equals("")) {
					meta.repeated = true;
				} else {
					meta.repeated = false;
				}
				
				meta.bigquerytype = "STRING";
				if (matcherf.group(4) != null) {
					switch(matcherf.group(4)) {
						case "int" :
						case "long" :
							meta.bigquerytype = "INTEGER";
							break;
						case "Boolean" :
							meta.bigquerytype = "BOOLEAN";
							break;
						case "Float" :
						case "double" :
							meta.bigquerytype = "FLOAT";
							break;
						case "date" :
							meta.bigquerytype = "TIMESTAMP";
							break;
						default:
							meta.bigquerytype = "STRING";
					}
				}

				meta.regex = matcherf.group(10);
				if (l == 0) {
					meta.self = "entry";
				} else {
					meta.self = matcherf.group(2);
				}
				meta.min = matcherf.group(7);
				meta.max = matcherf.group(8);
				if (fldname.equals("")) {
					meta.name = meta.self;
				} else {
					meta.name = fldname + "." + meta.self;
				}
				if (exists(metalist, meta.name)) throw new ParseException("Dupricated properties in Entry:" + meta.name, 0);
				if (meta.self.startsWith("_") && meta.level == 1) 
					throw new ParseException("Can't use '_' as prefix.:" + meta.name, 0);

				if (meta.self.length() < 2) throw new ParseException("Property name is too short.:" + meta.name, 0);

				//				if (folderpath!=null) {
				// 静的クラスを作成する場合はデフォルトが_なし
				meta.self = addUnderscore(meta.self);
				//				}else {
				//					meta.self = "_"+meta.self;
				//				}

				meta.isDesc = false;
				if (matcherf.group(4) != null) {
					String typestr = matcherf.group(4).toLowerCase(Locale.ENGLISH);
					meta.typesrc = typestr;
					if (typestr.equals("date")) {
						meta.type = "Date";
					} else if (typestr.equals("int")) {
						meta.type = "Integer";
					} else if (typestr.equals("long")) {
						meta.type = "Long";
					} else if (typestr.equals("float")) {
						meta.type = "Float";
					} else if (typestr.equals("double")) {
						meta.type = "Double";
					} else if (typestr.equals("boolean")) {
						meta.type = "Boolean";
						if (meta.min != null) throw new ParseException("Can't specify (Type) for Boolean type:" + line, 0);
					} else if (typestr.equals("desc")) {
						meta.type = "String";
						meta.isDesc = true;
					} else {
						meta.type = "String"; // その他
					}
				} 
				
				if (meta.min != null && meta.type==null) {
					meta.isMap = true;
				} else {
					if (matcherf.group(5).indexOf(ARRAY) >= 0) {
						// for Array(廃止)
//						meta.isArray = true;
						meta.min = matcherf.group(6);	// maxの要素数をminに入れる
					}
				}

				if (meta.type == null) {
					meta.type = "String"; // 省略時
				}

			} else {
				throw new ParseException("Unexpected Format:" + line, 0);
			}
		}
		metalist.add(meta);
		return metalist;
	}

	private static boolean exists(List<Meta> metalist, String name) {
		for (int i = 0; i < metalist.size(); i++) {
			if (name.equals(metalist.get(i).name)) return true;
		}
		return false;
	}

	/**
	 * 動的に生成するクラス名を下位から順にして返す
	 * @param metalist
	 * @return クラス名のSet
	 */
	private Set<String> getClassnames() {

		HashSet<String> classnames = new LinkedHashSet<String>();
		int size = metalist.size();

		int levelmax = 0;
		for (Meta meta : metalist) {
			if (levelmax < meta.level) {
				levelmax = meta.level;
			}
		}

		for (int l = levelmax; l >= 0; l--) {
			for (int i = size - 1; i >= 0; i--) {
				if (metalist.get(i).level == l) {
					classnames.add(metalist.get(i).parent);
				}
			}

		}
		return classnames;
	}

	/**
	 * 同一levelのクラスについて検索しメタ情報を返す
	 * @param metalist
	 * @param classname
	 * @param level
	 * @return
	 */
	private Meta getMetaByLevel(String classname, int level) {

		for (Meta meta : metalist) {
			if (meta.parent.equals(classname)) {
				level--;
				if (level < 0) {
					return meta;
				}
			}
		}
		return null;
	}

	/**
	 * メタ情報でclassnameに名前が一致するものをカウントする
	 * @param metalist
	 * @param classname
	 * @return
	 */
	private int matches(String classname) {

		int i = 0;
		for (Meta meta : metalist) {
			if (meta.parent.equals(classname)) {
				i++;
			}
		}
		return i;
	}

	/**
	 * Entity Templateからクラスを動的に作成しmsgpackに登録する
	 * 
	 * @throws ParseException
	 */
	private void registerClasses() throws ParseException {

		List<String> classnames = new ArrayList<String>();
		classnames.addAll(new ArrayList(Arrays.asList(ATOMCLASSES)));

		try {
			classnames.addAll(generateClass());
			registerClass(classnames);
		} catch (CannotCompileException e) {
			ParseException pe = new ParseException("Cannot Compile : " + e.getMessage(), 0);
			pe.initCause(e);
			throw pe;
		}
	}

	/**
	 * クラスをmsgpackに登録する
	 * 
	 * @param classnames
	 * @throws CannotCompileException
	 */
	private void registerClass(List<String> classnames)
			throws CannotCompileException {


		for (String classname : classnames) {
			// 静的なクラスであるatomパッケージは親のクラスローダにロード(これがないとClassCastException)
			if (classname.indexOf(".atom.") > 0) {
				loader.delegateLoadingOf(classname);
			}
			registerClass(classname);

		}
	}

	/**
	 * クラスをmsgpackに登録する
	 * 
	 * @param classname
	 * @throws CannotCompileException
	 */
	private void registerClass(String classname) throws CannotCompileException {
		if (!isBaseclass(classname)) {
			try {	
				Class<?> cls = loader.loadClass(classname);
				Template template = template = builder.buildTemplate(cls);
				// 途中はregistryに登録
				registry.register(cls, template);
				// EntryBaseはEntryとして登録
				if (isEntry(classname)) {
					cls = loader.loadClass(ENTRYBASE);
					registry.register(cls, template);
				}

				// EntryやFeedの場合はmsgpackに登録
				if (isEntry(classname) || isFeed(classname)) {
					msgpack.register(cls, template);
				}

			} catch (ClassNotFoundException e) {
				throw new CannotCompileException(e);
			}

		}

	}

	/**
	 * MessagePackのValueオブジェクトからEntityクラスを作成する
	 * @param classname
	 * @param value
	 * @return オブジェクト
	 * @throws JSONException 
	 */
	private Object parseValue(String classname,Value value) throws JSONException  {

		Object parent = null;
		Class cc = null;
		Field f = null;
		try {
			if (value.isMapValue()) {
				boolean isCreated = false;
				for (Entry<Value,Value> e : value.asMapValue().entrySet()) {
					String fld = e.getKey().toString().substring(1,e.getKey().toString().length()-1);
					if (fld.indexOf("-") >= 0) {
						fld = fld.replace("-", "__");
					}
					if (fld.indexOf("___") >= 0) {
						fld = fld.replace("___", "$");
					}
					if (!classname.isEmpty()) {
						cc = this.getClass(classname);
						try {
							f = cc.getField(fld);
						} catch (NoSuchFieldException ns) {
							try {
								f = cc.getField("_"+fld);
							} catch (NoSuchFieldException ns2) {
								throw new NoSuchFieldException("JSON parse error: "+ns2.getMessage().substring(1)+" is not defined.");
							}
						}
					}

					if ((e.getValue()).isMapValue()) {
						String childclsname = packagename + "." + toCamelcase(fld);
						Object child = parseValue(childclsname,e.getValue());
						if (!classname.isEmpty()) {
							if (!isCreated) parent = (Object) cc.newInstance();
							f.set(parent, child);
							isCreated = true;
						} else {
							return child;
						}

					} else {
						if (e.getValue().isArrayValue()) {
							if (!isCreated) {
								parent = (Object) cc.newInstance();
								isCreated = true;
							}
							List child = new ArrayList();
							for (Value v : e.getValue().asArrayValue().getElementArray()) {
								if (v.isMapValue()) {
									String childclsname = packagename + "." + toCamelcase(fld);
									child.add(parseValue(childclsname,v));
									f.set(parent, child);
								} else if (v.isRawValue()) {
									Element element = new Element();
									element._$$text = v.toString().substring(1,v.toString().length()-1);
									element._$$text = replaceCtrs(new SurrogateConverter(element._$$text).convertUcs());
									child.add(element);
									f.set(parent, child);
								}
							}

						} else {
							if (!isCreated) {
								parent = (Object) cc.newInstance();
								isCreated = true;
							}
							if (e.getValue().isBooleanValue()) f.set(parent, e.getValue().asBooleanValue().getBoolean());
							else if (e.getValue().isIntegerValue()) {
								if (f.getType().getName().equals("java.lang.Integer")) f.set(parent, e.getValue().asIntegerValue().getInt());
								if (f.getType().getName().equals("java.lang.Long")) f.set(parent, e.getValue().asIntegerValue().getLong());
								if (f.getType().getName().equals("java.lang.Float")) f.set(parent, (float) e.getValue().asIntegerValue().getInt());
								if (f.getType().getName().equals("java.lang.Double")) f.set(parent, (double) e.getValue().asIntegerValue().getLong());
							}
							else if (e.getValue().isFloatValue()) {
								if (f.getType().getName().equals("java.lang.Float")) f.set(parent, e.getValue().asFloatValue().getFloat());
								if (f.getType().getName().equals("java.lang.Double")) f.set(parent, e.getValue().asFloatValue().getDouble());
							}
							else if (e.getValue().isRawValue()) {
								String v = e.getValue().toString().substring(1,e.getValue().toString().length()-1);
								if (f.getType().getName().equals("java.util.Date")) {
									try {
										Date d = DateUtil.getDate(v);
										f.set(parent, d);
									} catch (Exception de) {
										throw new ParseException(de.getMessage() + " / " + v, 0);
									}
								} else {
									v = replaceCtrs(new SurrogateConverter(v).convertUcs());
									f.set(parent, v);
								}
							}
						}
					}
				}
			}
			return parent;
		} catch (Exception e) {
			// ClassNotFoundExceptionの場合、項目が定義されていないのが原因。
			if (e instanceof ClassNotFoundException) {
				String msg = e.getMessage();
				if (!StringUtils.isBlank(msg)) {
					int idx = msg.lastIndexOf(".");
					if (idx > 0) {
						String tmpName = msg.substring(idx + 1);
						String name = tmpName.substring(0, 1).toLowerCase(Locale.ENGLISH) + 
								tmpName.substring(1);
						throw new JSONException("JSON parse error: " + name + " is not defined.");
					}
				}
			}
			// その他は例外を返却。
			throw new JSONException(e);
		}
	}

    private String replaceCtrs(String str) {
        StringBuilder result = new StringBuilder();
        int s = 0;

        while(true) {
            if (str.length()<=s) return result.toString();
            char c = str.charAt(s);
            if (c!='\\') {
                result.append(c);
                s++;
            }else {
                switch(str.charAt(s+1)) {
                case '\\' :
                    result.append("\\");
                    s +=2;
                    break;
                case 'r' :
                    result.append("\r");
                    s +=2;
                    break;
                case 'f' :
                    result.append("\f");
                    s +=2;
                    break;
                case 'b' :
                    result.append("\b");
                    s +=2;
                    break;
                case 'n' :
                    result.append("\n");
                    s +=2;
                    break;
                case 't' :
                    result.append("\t");
                    s +=2;
                    break;
                case '"' :
                    result.append("\"");
                    s +=2;
                    break;
                }

            }
        }

    }

	public boolean isDefaultTemplate() {
		return isDefaultTemplate;
	}

	/**
	 * 標準ATOM Entry項目のみ読み込むよう、MessagePackのlengthを編集する。
	 * @param msgpack MessagePack形式のバイト配列
	 */
	public void convertDefaultEntry(byte[] msgpack) {
		if (msgpack != null && msgpack.length > 2) {
			//msgpack[2] = 0x27;
			//msgpack[2] = 0x21;
			msgpack[2] = 0x10;	// source、protected項目を削除した分項目数を減らした。
		}
	}

	/**
	 * テンプレートを更新できるかチェックする
	 * <p>
	 * MessagePackではArrayを使っており要素名が含まれない。そのため順序を保証しなければならないが、テンプレートの変更の際に要素の最後尾に追記することでずれを防ぐことができる。途中の孫要素であっても最後の要素としてであれば追加可能。
	 * 追加更新後に更新前と比較して型と階層が違えばエラーとする。
	 * </p>
	 * 
	 * @param jo_packages_old
	 * @param jo_packages_new
	 * @return 更新できればtrue
	 * @throws ParseException
	 */
	public boolean precheckTemplate(String[] jo_packages_old,String[] jo_packages_new) throws ParseException {
		List<Meta> metalistprev = getMetalist(mergeAtomEntry(jo_packages_old), packagename);
		List<Meta> metalistnew = getMetalist(mergeAtomEntry(jo_packages_new), packagename);

		for (int i = 0, j = 0; i < metalistnew.size()+1; i++) {
			if (j>=metalistprev.size()) return true;	// チェック完了(OK)
			if (i>=metalistnew.size()) return false;	// チェック完了(NG)

			// 同じ階層でかつ同じタイプであればOK
			if (metalistnew.get(i).level == metalistprev.get(j).level) {
				if (metalistnew.get(i).type.equals(metalistprev.get(j).type)){
					j++;
					continue;
					// 違うタイプであっても親に変更（子要素の追加）であればOK
				} else if (metalistnew.get(i).hasChild() && !metalistprev.get(j).hasChild()) {
					j++;
					continue;
				}
			}
			// 子要素が追加されていればOK(jは+1しない）
			if (metalistnew.get(i).level > metalistprev.get(j).level){
				continue;
			}
			return false;
		}
		return false;
	}

	public String getSecretkey() {
		return secretkey;
	}

	/**
	 * テンプレートファイルからEntityBeanを生成する
	 * 
	 * <pre>
	 * Usage: Java FeedTemplateMapper templatefile folderpath (aclfile)
	 * </pre>
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String args[]) throws ParseException {

		if (args.length<4) {
			System.out.println("Usage: Java FeedTemplateMapper <servicename> <templatefile> <folderpath> <secretkey> (<aclfile>)");
		}else {
			String[] entitytempl1 = readtemplatefile(args[1]);
			String[] entitytempl = new String[entitytempl1.length+1];
			entitytempl[0] = args[0]+"{}";
			System.arraycopy(entitytempl1, 0, entitytempl, 1, entitytempl1.length);
			
			String[] aclfile = null;
			if (args.length==5&&args[4]!=null) aclfile = readtemplatefile(args[4]);

			new FeedTemplateMapper(entitytempl,aclfile,30,false,args[2],args[3]);
		}
	}

	private static String[] readtemplatefile(String filename) {
		List<String> tempfile = new ArrayList<String>();
		BufferedReader br = null;
		try {
			File file = new File(filename);

			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				tempfile.add(line);
			}
		} catch (IOException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, e.getClass().getName(), e);
			}
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, e.getClass().getName(), e);
				}
			}
		}
		return (String[]) tempfile.toArray(new String[0]);
	}

	private static String addUnderscore(String prop) {
		String[] words = new String[] {
				"abstract",
				"boolean",
				"break",
				"byte",
				"case",
				"catch",
				"char",
				"class",
				"const",
				"continue",
				"default",
				"do",
				"double",
				"else",
				"extends",
				"final",
				"finally",
				"float",
				"for",
				"goto",
				"if",
				"implements",
				"import",
				"instanceof",
				"int",
				"interface",
				"long",
				"native",
				"new",
				"package",
				"private",
				"protected",
				"public",
				"return",
				"short",
				"static",
				"strictfp",
				"super",
				"switch",
				"synchronized",
				"this",
				"throw",
				"throws",
				"transient",
				"try",
				"void",
				"volatile",
				"while",
				"widefp",
				"true",
				"false",
				"null"
		};

		if (prop.startsWith("$")) return "_"+prop;
		for (String word:words) {
			if (prop.equals(word)) return "_"+prop;
		}
		return prop;

	}

}
