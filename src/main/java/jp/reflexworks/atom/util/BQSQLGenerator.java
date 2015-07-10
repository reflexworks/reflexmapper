package jp.reflexworks.atom.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;

public class BQSQLGenerator {

	private List<Meta> metalist;
	private Set<String> repeatedItemset;

	public String generate(List<Meta> metalist, String table) {
		this.metalist = metalist;

		StringBuilder sb = new StringBuilder();
		String line = getline();
		this.repeatedItemset = repeatedItemset();

		sb.append("select " + head1(line) + " from (\n");
		sb.append("select " + head2(line) + " from \n");
		sb.append("(select " + head3(getnorm()) + " from [" + table
				+ "]),\n");

		for (String item : repeatedItemset) {
			addRepeatedItems(item, sb, table);
		}

		return sb.toString();

	}

	private void addRepeatedItems(String item, StringBuilder sb, String table) {
		String token = "id," + getNum(item) + "," + getnormbyitem(item);
		if (isRepeated(getParent(item))) {
			sb.append("(FLATTEN((select '" + item + "' as flg, " + token
					+ " from " + getFlatten(item, token, table) + ")," + item
					+ ")),\n");
		} else {
			sb.append("(FLATTEN((select '" + item + "' as flg, " + token
					+ " from [" + table + "])," + item + ")),\n");
		}
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

	private String getNum(String item) {
		StringBuffer sb = new StringBuffer();
		sb.append(item + ".___num");
		getNum(item, sb);
		return sb.toString();
	}

	private void getNum(String item, StringBuffer sb) {
		if (item.lastIndexOf(".") >= 0) {
			String parent = getParent(item);
			if (isRepeated(parent))
				sb.append("," + parent + ".___num");
			if (parent.lastIndexOf(".") >= 0) {
				getNum(parent, sb);
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

	private String getline() {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
			out(metalist.get(i), sb, (i < metalist.size() - 1));
		}

		return sb.toString();
	}

	private String getnorm() {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
			outnorm(metalist.get(i), sb, (i < metalist.size() - 1));
		}

		return sb.toString();
	}

	private String getnormbyitem(String item) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
			Meta meta = metalist.get(i);
			if ((meta.name + ".").indexOf(item + ".") == 0) {
				outnorm(meta, sb, (i < metalist.size() - 1), item);
			}
		}

		return sb.toString();
	}

	private void out(Meta meta, StringBuilder sb, boolean comma) {

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

	private void outnorm(Meta meta, StringBuilder sb, boolean comma) {
		// '-' is nothing.
		outnorm(meta, sb, comma, "-");
	}

	private void outnorm(Meta meta, StringBuilder sb, boolean comma,
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

	private Set<String> repeatedItemset() {

		Set<String> set = new LinkedHashSet<String>();

		for (Meta meta : metalist) {
			if (meta.repeated && !meta.self.equals("entry")) {
				set.add(meta.name);
			}
		}

		return set;
	}

	private boolean isRepeatedChild(String name, String except) {

		for (String repeated : repeatedItemset) {
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