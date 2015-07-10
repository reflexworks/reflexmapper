package jp.reflexworks.atom.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;

public class BQSQLGenerator {

	
	
	public String generate(List<Meta> metalist,String table) {

		StringBuilder sb = new StringBuilder();
		String all0 = getall0(metalist);
		Set<String> repeatedItemset = repeatedItemset(metalist);
		
		sb.append("select "+headLine0(all0)+" from (\n");
		sb.append("select "+headLine1(all0)+" from \n");
		sb.append("(select "+headLine2(getnorm(metalist,repeatedItemset))+" from ["+table+"]),\n");

		for (String item:repeatedItemset) {
			addRepeatedItems(item,sb,metalist,repeatedItemset,table);
		}

		return sb.toString();

	}

	private void addRepeatedItems(String item,StringBuilder sb,List<Meta> metalist,Set<String> repeatedItemset,String table) {
		String token = "id,"+getNum(item)+","+getnormbyitem(metalist,repeatedItemset,item);
		sb.append("(FLATTEN((select '"+item+"' as flg, "+token+" from "+getFlatten(item,token,table)+"),"+item+")),\n");
	}

	private String getFlatten(String item,String token,String table) {
		StringBuffer sb = new StringBuffer();
		getFlatten(item,token,table,sb);
		return sb.toString();
	}
	
	private void getFlatten(String item,String token,String table,StringBuffer sb) {
		String item2 = item;
		if (item.lastIndexOf(".")>=0) {
			item2 = item.substring(0,item.lastIndexOf("."));
			if (item2.lastIndexOf(".")>=0) {
				sb.append("(FLATTEN((select "+token+" from ");
				getFlatten(item2,token,table,sb);
				sb.append("),"+item2+"))");
			}else {
				sb.append("(FLATTEN((select "+token+" from ["+table+"]),"+item2+"))");
			}
		}else {
			sb.append("["+table+"]");
		}
	}
	
	private String getNum(String item){
		StringBuffer sb = new StringBuffer();
		sb.append(item+".___num");
		getNum(item,sb);
		return sb.toString();
	}
	
	private void getNum(String item,StringBuffer sb) {
		String item2 = item;
		if (item.lastIndexOf(".")>=0) {
			item2 = item.substring(0,item.lastIndexOf("."));
			sb.append(","+item2+".___num");
			if (item2.lastIndexOf(".")>=0) {
				getNum(item2,sb);
			}
		}
	}
	
	private String headLine0(String org) {
		return "flg,src.id as id, updated, rights____type,"+org.replace("id,", "").replace("updated,", "").replace("rights____type,", "");
	}

	private String headLine1(String org) {
		return headLine0(org).replace("src.id as id,","___key, ___revision,id,");
	}

	private String headLine2(String org) {
		return "'entry' as flg, ___key, ___revision, id, updated,"+org.replace(",updated","").replace("id,", "");
	}

	private String getall0(List<Meta> metalist) {
	
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
				out(metalist.get(i), sb,(i < metalist.size() - 1));
		}

		return sb.toString();
	}
	
	private String getnorm(List<Meta> metalist,Set repeatedItemset) {
		
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
				outnorm(metalist.get(i), sb,(i < metalist.size() - 1),repeatedItemset);
		}

		return sb.toString();
	}

	private String getnormbyitem(List<Meta> metalist,
			Set<String> repeatedItemset, String item) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size(); i++) {
			Meta meta = metalist.get(i);
			if ((meta.name+".").indexOf(item+".")==0) {
				outnorm(meta, sb,(i < metalist.size() - 1),repeatedItemset,item);
			}
		}

		return sb.toString();
	}

	
	private void out(Meta meta,StringBuilder sb,boolean comma) {

		if (!meta.isrecord&&!meta.name.equals("content.$$text")) {
			sb.append(meta.name.replace("$", "___"));
			if (comma) {
				sb.append(",");
			}else {
				sb.append("\n");
			}
		}else {
			if (meta.repeated&&!meta.self.equals("entry")) {
				sb.append(meta.name+".___num,");
			}
		}
		
	}

	private void outnorm(Meta meta,StringBuilder sb,boolean comma,Set repeatedItemset) {
		outnorm(meta,sb,comma,repeatedItemset,"-");
	}

	private void outnorm(Meta meta,StringBuilder sb,boolean comma,Set repeatedItemset,String except) {

		if (!meta.name.equals("content.$$text")&&!isRepeatedChild(repeatedItemset,meta.name,except)) {
		if (!meta.isrecord) {
			if (sb.length()>0) sb.append(",");
			sb.append(meta.name.replace("$", "___"));
		}
		}
		/*
		if (meta.repeated) {
			if (sb.length()>0) sb.append(",");
			sb.append(meta.name.replace("$", "___")+".___num");
		}
		repeated項目が確定しているから___numは機械的に作成してよいはず
		*/
		
	}

	private Set repeatedItemset(List<Meta> metalist) {
		
		Set<String> set = new LinkedHashSet<String>();
		
		for(Meta meta:metalist) {
			if (meta.repeated&&!meta.self.equals("entry")) {
				set.add(meta.name);
			}
		}
		
		return set;
	}
	
	private boolean isRepeatedChild(Set<String> repeatedItemset,String name,String except) {
		
		for(String repeated:repeatedItemset) {
			boolean ex = true;
			int ex1 = (name+".").indexOf(except+".");
			if (ex1==0) {
				if (name.length()>except.length()) {
					// 孫を省く
					ex  = name.substring(except.length()+1).indexOf(".")>=0;
				}
			}
			if (((name+".").indexOf(repeated+".")==0)&&ex) {
				return true;
			}
		}
		return false;
		
	}
	
}
