package jp.reflexworks.atom.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLGenerator {

	private static final String field_pattern = "^( *)([a-zA-Z_$][0-9a-zA-Z_$]*)(\\(([a-zA-Z]+)\\))?((?:\\[([0-9]+)?\\]|\\{([\\-0-9]*)~?([\\-0-9]+)?\\})?)(\\!?)(?:=(.+))?(?:[ \\t]*)$";

	public class Meta {
		public String name;
		public String type;
		public boolean repeated;
		public boolean record;
		public int level0;
		public int level;
		public boolean comma;
		
		public Meta(int level) {
			this.level0 = level;
		}
	}
	
	public String convert(String[] template) {
		
		Pattern patternf = Pattern.compile(field_pattern);
		StringBuilder sb = new StringBuilder();

		Meta meta = new Meta(0);
		String parent = "";
		for(int i=0;i<template.length;i++) {
			
			Matcher matcherf = patternf.matcher(template[i]);
			if (matcherf.find()) {

				meta.level = matcherf.group(1).length();
				if (meta.level0 != meta.level) {					
					if (meta.level0 < meta.level) {
						meta.record = true;
						parent = meta.name+".";
					}else {
						for (int j = 0; j < meta.level0 - meta.level; j++) {
							int p = parent.substring(0, parent.length()-1).lastIndexOf(".");
							if (p >= 0) {
								parent = parent.substring(0,p);
							} else {
								parent = "";
							}
						}
					}
				}

				if (i<template.length+1) {
					meta.comma = true;
				}

				if (i>0) out(meta,sb,parent);
				meta = new Meta(meta.level);
				
				if (matcherf.group(5)!=null&&!matcherf.group(5).equals("")) {
					meta.repeated = true;
				}else {
					meta.repeated = false;
				}
				meta.name = parent+matcherf.group(2).replace("$", "___");
				
			}

		}
		out(meta,sb,parent);
		String idrev = "___key,___revision,";
		return idrev+sb.toString();

	}

	private void out(Meta meta,StringBuilder sb,String parent) {

		if (meta.record) {
			if (meta.repeated) {
				for(int j=0;j<meta.level0+1;j++) 
				sb.append(parent+"___num");
			}else {
				sb.append(meta.name);
			}
//			meta.comma = false;
		}else {
			sb.append(meta.name);
		}
		
		if (meta.comma) {
			sb.append(",");
		}


	}
	
}
