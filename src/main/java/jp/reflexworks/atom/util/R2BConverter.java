package jp.reflexworks.atom.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class R2BConverter {

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
	
	public String convert(String[] entitytempl) {
		
		int l = ((String[]) entitytempl).length;
		String[] template = new String[ATOMENTRYTMPL.length+l];
//		template[0] = ((String[]) entitytempl)[0];
		System.arraycopy(ATOMENTRYTMPL, 0, template, 0, ATOMENTRYTMPL.length);
		System.arraycopy((String[]) entitytempl, 0, template, ATOMENTRYTMPL.length , l );

		Pattern patternf = Pattern.compile(field_pattern);
		StringBuilder sb = new StringBuilder();

		Meta meta = new Meta(0);
		for(int i=0;i<template.length;i++) {
			
			Matcher matcherf = patternf.matcher(template[i]);
						
			if (matcherf.find()) {

				meta.level = matcherf.group(1).length();
				if (meta.level0 != meta.level) {					
					if (meta.level0 < meta.level) {
						meta.record = true;
					}
				}

				if (i<template.length+1) {
					meta.comma = true;
				}

				if (i>0) out(meta,sb);

				meta = new Meta(meta.level);
				
				if (matcherf.group(5)!=null&&!matcherf.group(5).equals("")) {
					meta.repeated = true;
				}else {
					meta.repeated = false;
				}
				
				meta.name = matcherf.group(2).replace("$", "___");
				
				meta.type = "STRING";
				if (matcherf.group(4)!=null) {
					switch(matcherf.group(4)) {
						case "int" :
						case "long" :
							meta.type = "INTEGER";
							break;
						case "Boolean" :
							meta.type = "BOOLEAN";
							break;
						case "Float" :
						case "double" :
							meta.type = "FLOAT";
							break;
						case "date" :
							meta.type = "TIMESTAMP";
							break;
						default:
							meta.type = "STRING";
					}
						
				}
				
			}

		}
		out(meta,sb);
		String idrev = "{\"name\": \"___id\", \"type\": \"STRING\"},\n" +
						"{\"name\": \"___revision\", \"type\": \"INTEGER\"},\n";
		return "[\n"+idrev+sb.toString()+"]\n";

	}

	private void out(Meta meta,StringBuilder sb) {

		for(int j=0;j<meta.level0;j++) 
			sb.append("	");
		
		if (meta.record) {
			sb.append("{\"name\": \""+meta.name+"\", \"type\": \"RECORD\",");
			if (meta.repeated) {
				sb.append("\"mode\": \"REPEATED\",");
			}
			sb.append("\"fields\": [");
			meta.comma = false;
		}else {
			sb.append("{\"name\": \""+meta.name+"\", \"type\": \""+meta.type+"\"}");
		}

		for(int k=meta.level;k<meta.level0;k++) {
			if (meta.level0+meta.level-k-1>0) {
				sb.append(",\n");
			}else {
				sb.append("\n");
			}
			for(int j=0;j<meta.level0+meta.level-k-1;j++) {
				for(int l=0;l<meta.level0+meta.level-k;l++) 
					sb.append("	");
				sb.append("{\"name\": \"___num"+(j+1)+"\", \"type\": \"INTEGER\"}");
				if (j<meta.level0+meta.level-k-2) sb.append(",");
				sb.append("\n");
			}
			for(int j=0;j<meta.level0+meta.level-k-1;j++) 
				sb.append("	");
			sb.append("]}");
		}
		
		if (meta.comma) {
			sb.append(",\n");
		}else {
			sb.append("\n");
		}
	}
	
}
