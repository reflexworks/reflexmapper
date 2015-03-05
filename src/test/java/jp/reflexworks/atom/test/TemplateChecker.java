package jp.reflexworks.atom.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.sourceforge.reflex.util.FileUtil;

public class TemplateChecker {

	private static final String ENCODING = "UTF-8";
	
	private static final String SERVICE_NAME = "myservice";
	
	@Test
	public void check() {
		try {
			// 指定されたテンプレートにエラーがないかチェックする。
			String templateFileStr = getFilePathTemplate(SERVICE_NAME);
			String indexEncItemACLFileStr = getFilePathIndexEncItemACL(SERVICE_NAME);
			createMapper(templateFileStr, indexEncItemACLFileStr);
			
			System.out.println("create mapper OK.");
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private FeedTemplateMapper createMapper(String templateFileStr, 
			String indexEncItemACLFileStr) 
	throws IOException, URISyntaxException, ParseException {
		String[] template = null;
		String[] rights = null;
		if (templateFileStr != null) {
			String[] tmpTemplate = readTemplate(templateFileStr);
			if (tmpTemplate != null && tmpTemplate.length > 0) {
				template = new String[tmpTemplate.length + 1];
				template[0] = "dummy{99999}";
				System.arraycopy(tmpTemplate, 0, template, 1, tmpTemplate.length);
				if (indexEncItemACLFileStr != null) {
					rights = readTemplate(indexEncItemACLFileStr);
				}
			}
		} else {
			template = new String[]{"_"};
		}
		FeedTemplateMapper mapper = new FeedTemplateMapper(template, rights, 
				999999999, null);
		return mapper;
	}
	
	/*
	 * ファイルを読み、改行区切りのString配列にして返却する。
	 */
	private String[] readTemplate(String fileStr) 
	throws IOException, URISyntaxException {
		BufferedReader reader = getReader(fileStr);
		if (reader != null) {
			try {
				List<String> lines = new ArrayList<String>();
				String line;
				while ((line = reader.readLine()) != null) {
					if (!"".equals(line.trim())) {
						lines.add(line);
					}
				}
				if (lines.size() > 0) {
					return lines.toArray(new String[0]);
				}
				
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (Exception e) {}	// Do nothing.
			}
		}
		return null;
	}
	
	private BufferedReader getReader(String filePath) 
	throws IOException, URISyntaxException {
		File file = new File(filePath);
		if (file.exists()) {
			return new BufferedReader(new InputStreamReader(
					new FileInputStream(file), ENCODING));
		}
		return null;
	}

	/**
	 * テンプレートのファイルパスを返却.
	 */
	private String getFilePathTemplate(String serviceName) 
	throws FileNotFoundException {
		String fileName = editFileNameTemplate(serviceName);
		return editInputFilePath(fileName);
	}
	
	/**
	 * テンプレートのIndex、暗号化、項目ACL設定のファイルパスを返却.
	 */
	private String getFilePathIndexEncItemACL(String serviceName) 
	throws FileNotFoundException {
		String fileName = editFileNameIndexEncItemACL(serviceName);
		return editInputFilePath(fileName);
	}

	// template_{サービス名}.txt
	private String editFileNameTemplate(String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("template_");
		sb.append(serviceName);
		sb.append(".txt");
		return sb.toString();
	}

	// idx_enc_itemacl_{サービス名}.txt
	private String editFileNameIndexEncItemACL(String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append("idx_enc_itemacl_");
		sb.append(serviceName);
		sb.append(".txt");
		return sb.toString();
	}

	private String editInputFilePath(String filename) {
		StringBuilder sb = new StringBuilder();
		sb.append(editInputFileDir());
		sb.append(File.separator);
		sb.append(filename);
		return sb.toString();
	}
	
	private String editInputFileDir() {
		StringBuilder sb = new StringBuilder();
		sb.append(FileUtil.getUserDir());
		sb.append(File.separator);
		sb.append("src");
		sb.append(File.separator);
		sb.append("test");
		sb.append(File.separator);
		sb.append("resources");
		return sb.toString();
	}

}
