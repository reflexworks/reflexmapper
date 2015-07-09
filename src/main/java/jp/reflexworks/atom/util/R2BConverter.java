package jp.reflexworks.atom.util;

import java.util.List;
import jp.reflexworks.atom.mapper.FeedTemplateMapper.Meta;

public class R2BConverter {

	public String convert(List<Meta> metalist) {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < metalist.size() - 1; i++) {
			if (i > 0)
				out(metalist.get(i), sb, metalist.get(i + 1).level,
						(i < metalist.size() - 1));
		}
		
		out(metalist.get(metalist.size() - 1), sb, 0, false);

		String idrev = "{\"name\": \"___key\", \"type\": \"STRING\"},\n"
				+ "{\"name\": \"___revision\", \"type\": \"INTEGER\"},\n";

		return "[\n" + idrev + sb.toString() + "]\n";

	}

	private void out(Meta meta,StringBuilder sb,int level,boolean comma) {

		for(int j=0;j<meta.level;j++) 
			sb.append("	");
		
		if (meta.isrecord) {
			sb.append("{\"name\": \""+meta.self.replace("$", "___")+"\", \"type\": \"RECORD\",");
			if (meta.repeated) {
				sb.append("\"mode\": \"REPEATED\",");
			}
			sb.append("\"fields\": [");
			if (meta.repeated) {
				sb.append("\n");
				for(int j=0;j<meta.level+1;j++) 
					sb.append("	");
				sb.append("{\"name\": \"___num\", \"type\": \"INTEGER\"},");
			}
			comma = false;
		}else {
			sb.append("{\"name\": \""+meta.self.replace("$", "___")+"\", \"type\": \""+meta.bigquerytype+"\"}");
		}

		for(int k=level;k<meta.level;k++) {
			sb.append("\n");
			for(int j=0;j<meta.level+level-k-1;j++) 
				sb.append("	");
			sb.append("]}");
		}
		
		if (comma) {
			sb.append(",\n");
		}else {
			sb.append("\n");
		}
	}
	
}
