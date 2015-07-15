package jp.reflexworks.atom.util;

import jp.reflexworks.atom.entry.EntryBase;
import jp.sourceforge.reflex.IResourceMapper;

public class BQJsonConverter {

	public static String toJSON(IResourceMapper mapper,EntryBase entry) {

		String json = mapper.toJSON(entry,true);	// trueでBigQuery用JSON出力(___num付)
		if(entry.id!=null) {
			int n = entry.id.indexOf(",");
			StringBuilder sb = new StringBuilder();
			if (n>0) {
				sb.append("{ \"___key\" : \""+entry.id.substring(0,n)+"\"");
				sb.append(",\"___revision\" : \""+entry.id.substring(n+1)+"\"");
				sb.append(",");
			}
			String result = sb.toString() + json.substring(1);
			return result;
		}else {
			return json;
		}
	}
	
}
