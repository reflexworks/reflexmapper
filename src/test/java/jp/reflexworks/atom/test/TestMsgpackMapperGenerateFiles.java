package jp.reflexworks.atom.test;

import java.io.FileNotFoundException;
import java.text.ParseException;

import jp.reflexworks.atom.mapper.FeedTemplateMapper;
import jp.sourceforge.reflex.util.FileUtil;

import org.junit.Test;

public class TestMsgpackMapperGenerateFiles {

	@Test
	public void testGenerateFiles() throws FileNotFoundException, ParseException {
		
		String[] args = new String[2];
		
		args[0] = FileUtil.getResourceFilename("template_sample.txt");
		args[1] = "./";
		
		FeedTemplateMapper.main(args);
		
	}

}
