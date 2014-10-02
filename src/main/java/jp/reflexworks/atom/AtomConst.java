package jp.reflexworks.atom;

import java.util.HashMap;
import java.util.Map;

public class AtomConst {
	
	/** エンコード */
	public static final String ENCODING = "UTF-8";

	/** ATOM : namespace */
	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	/** ATOM : Feed package */
	public static final String ATOM_PACKAGE_FEED = "jp.reflexworks.atom.feed";
	/** ATOM : Entry package */
	public static final String ATOM_PACKAGE_ENTRY = "jp.reflexworks.atom.entry";
	/** ATOM : Source package */
	//public static final String ATOM_PACKAGE_SOURCE = "jp.reflexworks.atom.source";
	/** ATOM : Package map */
	public static final Map<String, String> ATOM_PACKAGE;
	static {
		ATOM_PACKAGE = new HashMap<String, String>();
		ATOM_PACKAGE.put(ATOM_PACKAGE_FEED, "");
		ATOM_PACKAGE.put(ATOM_PACKAGE_ENTRY, "");
		//ATOM_PACKAGE.put(ATOM_PACKAGE_SOURCE, "");
	}

}
