package jp.reflexworks.atom.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;
import jp.reflexworks.atom.wrapper.Condition;

public class BQSQLGenerator {

	private List<Meta> metalist;
	private Set<String> repeatedItemSet;

	private String[] primitives = {"___key", "___revision", "id", "rights____type", "link.___rel", "link.___href"};
	
	public String generate(List<Meta> metalist, String table,Condition[] conditions,String linkhref) {
		this.metalist = metalist;

		StringBuilder sb = new StringBuilder();
		String line = getLine();
		this.repeatedItemSet = repeatedItemSet();

		sb.append("select " + head1(line) + " from (\n");
		sb.append("select " + head2(line) + " from \n");
		sb.append("(select " + head3(getItems()) + " from [" + table
				+ "]),\n");

		for (String item : repeatedItemSet) {
			addRepeatedItems(item, sb, table);
		}

		sb.append(") src\n");
		sb.append("JOIN EACH\n");
		sb.append("(select id from\n");
		
		List<Condition> conditionlist = Arrays.asList(conditions);

		Set<String> nestset = new LinkedHashSet<String>();
		for(Condition condition:conditionlist) {
			nestset.addAll(getParentList(condition.getProp()));
		}
		nestset.add("link");
		addNest(nestset,conditionlist,table,sb);
		
		sb.append("JOIN EACH\n");
		sb.append("(select max(___revision) as max____revision, ___key as max____key from ["+table+"] \n");
		sb.append("GROUP EACH BY max____key) mx \n");
		sb.append("ON nest_src.___revision=mx.max____revision and nest_src.___key=mx.max____key \n");
		sb.append("where (link.___rel=\"self\" or link.___rel=\"alternate\") \n");
		sb.append("and regexp_match(link.___href, '^"+linkhref+"[^/]+$') \n");
		sb.append("and rights____type is null \n");
		
		for(Condition condition:conditionlist) {
			sb.append("and regexp_match("+condition.getProp()+",\"^"+condition.getValue()+"$\") \n");
		}
		sb.append("GROUP EACH by id) grp \n");
		sb.append("ON src.id = grp.id \n");
		
		sb.append("GROUP BY "+trailer1(line)+" ");
		sb.append("ORDER BY "+trailer2(getLineNum())+" ");

		return sb.toString();

	}

	private void addNest(Set<String> nestset,List<Condition> conditionlist,String table,StringBuilder sb) {

		String nestitems = getNestItems(conditionlist);
		for(String nestflats:nestset) {
			sb.append("(FLATTEN((select ");
			sb.append(nestitems);
			sb.append(" from \n");
		}
		sb.append("["+table+"]),");
		Iterator<String> it = nestset.iterator();
        while (it.hasNext()) {
			sb.append(it.next()+")))");
			if (it.hasNext()) {
				sb.append(",\n");
			}else {
				sb.append(" nest");
			}
        }		
	}
	
	private String getNestItems(List<Condition> conditionlist) {
		StringBuilder sb = new StringBuilder();
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(primitives));
		for(Condition condition:conditionlist) {
			items.add(condition.getProp());
		}
		for(int i=0;i<items.size();i++) {
			sb.append(items.get(i));
			if (i<items.size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	private void addRepeatedItems(String item, StringBuilder sb, String table) {
		String token = "id," + getNums(item) + "," + getItems(item);
		if (isRepeated(getParent(item))) {
			sb.append("(FLATTEN((select '" + item + "' as flg, " + token
					+ " from " + getFlatten(item, token, table) + ")," + item
					+ ")),\n");
		} else {
			sb.append("(FLATTEN((select '" + item + "' as flg, " + token
					+ " from [" + table + "])," + item + ")),\n");
		}
	}
	
	private List<String> getParentList(String item) {
		List<String> result = new ArrayList<String>();
		while(true) {
			item = getParent(item);
			if (item.equals("")) break;
			result.add(item);
		}
		Collections.reverse(result);
		return result;
	}

	private String getParent(String item) {
		if (item.lastIndexOf(".") >= 0) {
			return item.substring(0, item.lastIndexOf("."));
		} else {
			return "";
		}
	}

	private String getFlatten(String item, String token, String table) {
		StringBuffer sb = new StringBuffer();
		getFlatten(item, token, table, sb);
		return sb.toString();
	}

	private void getFlatten(String item, String token, String table,StringBuffer sb) {

		if (item.lastIndexOf(".") >= 0) {
			String parent = getParent(item);
			if ((parent.lastIndexOf(".") >= 0) && isRepeated(parent)) {
				sb.append("(FLATTEN((select " + token + " from ");
				getFlatten(parent, token, table, sb);
				sb.append(")," + parent + "))");
			} else {
				sb.append("(FLATTEN((select " + token + " from [" + table
						+ "])," + parent + "))");
			}
		} else {
			sb.append("[" + table + "]");
		}
	}

	private boolean isRepeated(String item) {
		for (Meta meta : metalist) {
			if (meta.name.equals(item))
				return meta.repeated;
		}
		return false;
	}

	private String getNums(String item) {
		StringBuffer sb = new StringBuffer();
		sb.append(item + ".___num");
		getNums(item, sb);
		return sb.toString();
	}

	private void getNums(String item, StringBuffer sb) {
		if (item.lastIndexOf(".") >= 0) {
			String parent = getParent(item);
			if (isRepeated(parent))
				sb.append("," + parent + ".___num");
			if (parent.lastIndexOf(".") >= 0) {
				getNums(parent, sb);
			}
		}
	}

	private String head1(String org) {
		return "flg,src.id as id, updated, rights____type,"
				+ org.replace("id,", "").replace("updated,", "")
						.replace("rights____type,", "");
	}

	private String head2(String org) {
		return head1(org).replace("src.id as id,",
				"___key, ___revision,id,");
	}

	private String head3(String org) {
		return "'0' as flg, ___key, ___revision, id, updated,"
				+ org.replace(",updated", "").replace("id,", "");
	}

	private String trailer1(String org) {
		return "flg,src.id, id, updated, rights____type,"
				+ org.replace("id,", "").replace("updated,", "")
						.replace("rights____type,", "");
	}

	private String trailer2(String org) {
		return "src.id, flg"+org;
	}

	private String getLine() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < metalist.size(); i++) {
			outAll(metalist.get(i), sb, (i < metalist.size() - 1));
		}
		return sb.toString();
	}

	private String getLineNum() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < metalist.size(); i++) {
			outNum(metalist.get(i), sb, (i < metalist.size() - 1));
		}
		return sb.toString();
	}

	private String getItems() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < metalist.size(); i++) {
			outItems(metalist.get(i), sb, (i < metalist.size() - 1));
		}
		return sb.toString();
	}

	private String getItems(String item) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < metalist.size(); i++) {
			Meta meta = metalist.get(i);
			if ((meta.name + ".").indexOf(item + ".") == 0) {
				outItems(meta, sb, (i < metalist.size() - 1), item);
			}
		}
		return sb.toString();
	}

	private void outAll(Meta meta, StringBuilder sb, boolean comma) {

		if (!meta.isrecord && !meta.name.equals("content.$$text")) {
			sb.append(meta.name.replace("$", "___"));
			if (comma) {
				sb.append(",");
			} else {
				sb.append("\n");
			}
		} else {
			if (meta.repeated && !meta.self.equals("entry")) {
				sb.append(meta.name + ".___num,");
			}
		}

	}

	private void outNum(Meta meta, StringBuilder sb, boolean comma) {

		if (!meta.isrecord && !meta.name.equals("content.$$text")) {
		} else {
			if (meta.repeated && !meta.self.equals("entry")) {
				sb.append(","+meta.name + ".___num");
			}
		}

	}

	private void outItems(Meta meta, StringBuilder sb, boolean comma) {
		// '-' is nothing.
		outItems(meta, sb, comma, "-");
	}

	private void outItems(Meta meta, StringBuilder sb, boolean comma,
			String except) {

		if (!meta.name.equals("content.$$text")
				&& !isRepeatedChild(meta.name, except)) {
			if (!meta.isrecord) {
				if (sb.length() > 0)
					sb.append(",");
				sb.append(meta.name.replace("$", "___"));
			}
		}
	}

	private Set<String> repeatedItemSet() {

		Set<String> set = new LinkedHashSet<String>();

		for (Meta meta : metalist) {
			if (meta.repeated && !meta.self.equals("entry")) {
				set.add(meta.name);
			}
		}

		return set;
	}

	private boolean isRepeatedChild(String name, String except) {

		for (String repeated : repeatedItemSet) {
			boolean ex = true;
			int ex1 = (name + ".").indexOf(except + ".");
			if (ex1 == 0) {
				if (name.length() > except.length()) {
					// 孫を省く
					ex = name.substring(except.length() + 1).indexOf(".") >= 0;
				}
			}
			if (((name + ".").indexOf(repeated + ".") == 0) && ex) {
				return true;
			}
		}
		return false;

	}

}
