package jp.reflexworks.atom.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.feed.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.sourceforge.reflex.util.FileUtil;

public class GenerateEntity {

	public static void main(String[] args) throws ParseException, IOException {

		String[] args2 = new String[4];
		
		// templateファイルを指定
		if (args.length > 4) {
			args2[0] = FileUtil.getResourceFilename(args[0]); // templatefile
			args2[1] = args[1]; // folderpath
			args2[2] = args[2]; // secretkey
			args2[3] = FileUtil.getResourceFilename(args[3]); // propaclfile

			FeedTemplateMapper.main(args2);
		} 
		// systeminit.xmlから生成
		else {
			FeedTemplateMapper mp = new FeedTemplateMapper(
					new String[] { "default" }, "");

			String dataXmlFile = FileUtil.getResourceFilename(args[0]); // templatefile
			FileReader fi = new FileReader(dataXmlFile);
			FeedBase feed = (FeedBase) mp.fromXML(fi);

			String secretkey = args[2]; // secretkey
			String folderpath = args[1]; // folderpath

			String[] entitytempl = null;
			List<String> acl = readtemplatefile(FileUtil
					.getResourceFilename(args[3]));	// propaclfile

			for (EntryBase entry : feed.entry) {
				if (entry.link.get(0)._$href.contains("/_settings/template")) {
					entitytempl = entry.content._$$text.split(System
							.getProperty("line.separator"));
					String[] aclrights = entry.rights.split(System
							.getProperty("line.separator"));
					for (String line : aclrights) {
						if (line.trim().length() > 0) {
							acl.add(line);
						}
					}
				}
			}
			String[] aclfile = acl.toArray(new String[0]);

			new FeedTemplateMapper(entitytempl, aclfile, 30, false, folderpath,
					secretkey);
		}

	}

	private static List<String> readtemplatefile(String filename) {
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
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		return tempfile;
	}

}
