package jp.reflexworks.atom.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.reflexworks.atom.mapper.FeedTemplateMapper;

public class R2BConverter {

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
		String[] template = new String[FeedTemplateMapper.ATOMENTRYTMPL.length+l];
//		template[0] = ((String[]) entitytempl)[0];
		System.arraycopy(FeedTemplateMapper.ATOMENTRYTMPL, 0, template, 0, FeedTemplateMapper.ATOMENTRYTMPL.length);
		System.arraycopy((String[]) entitytempl, 0, template, FeedTemplateMapper.ATOMENTRYTMPL.length , l );

		Pattern patternf = Pattern.compile(FeedTemplateMapper.FIELDPATTERN);
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
		String idrev = "{\"name\": \"___key\", \"type\": \"STRING\"},\n" +
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
			if (meta.repeated) {
				sb.append("\n");
				for(int j=0;j<meta.level0+1;j++) 
					sb.append("	");
				sb.append("{\"name\": \"___num\", \"type\": \"INTEGER\"},");
			}
			meta.comma = false;
		}else {
			sb.append("{\"name\": \""+meta.name+"\", \"type\": \""+meta.type+"\"}");
		}

		for(int k=meta.level;k<meta.level0;k++) {
			sb.append("\n");
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
