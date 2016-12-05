package jp.reflexworks.atom.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.entry.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.sourceforge.reflex.util.FileUtil;

public class GenerateEntity {

	private static Logger logger = Logger.getLogger(GenerateEntity.class.getName());

	public static void main(String[] args) throws ParseException, IOException {

		String[] args2 = new String[5];
		
		// templateファイルを指定
		if (args.length > 5) {
			args2[0] = args[0];
			args2[1] = FileUtil.getResourceFilename(args[1]); // templatefile
			args2[2] = args[2]; // folderpath
			args2[3] = args[3]; // secretkey
			args2[4] = FileUtil.getResourceFilename(args[4]); // propaclfile

			FeedTemplateMapper.main(args2);
		} 
		// systeminit.xmlから生成
		else {
			FeedTemplateMapper mp = new FeedTemplateMapper(
					new String[] { "default" }, "");

			String dataXmlFile = FileUtil.getResourceFilename(args[1]); // templatefile
			FileReader fi = new FileReader(dataXmlFile);
			FeedBase feed = (FeedBase) mp.fromXML(fi);

			String folderpath = args[2]; // folderpath
			String secretkey = args[3]; // secretkey

			String[] entitytempl1 = null;
			List<String> acl = readtemplatefile(FileUtil
					.getResourceFilename(args[4]));	// propaclfile

			for (EntryBase entry : feed.entry) {
				if (entry.link.get(0)._$href.contains("/_settings/template")) {
					entitytempl1 = entry.content._$$text.split(System
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
			String[] entitytempl = new String[entitytempl1.length+1];
			entitytempl[0] = args[0]+"{}";
			System.arraycopy(entitytempl1, 0, entitytempl, 1, entitytempl1.length);

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
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, e.getClass().getName(), e);
			}
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, e.getClass().getName(), e);
				}
			}
		}
		return tempfile;
	}

}
