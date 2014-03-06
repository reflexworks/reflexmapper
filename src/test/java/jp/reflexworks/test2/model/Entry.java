package jp.reflexworks.test2.model;

import java.text.ParseException;
import java.util.List;

import jp.reflexworks.atom.entry.EntryBase;
import jp.reflexworks.atom.wrapper.base.ConditionBase;

public class Entry extends EntryBase {
	
	public Info _info;
	public List<Comment> _comment;
	public String _deleteFlg;

	public Info getInfo() {
		return _info;
	}

	public void setInfo(Info info) {
		this._info = info;
	}

	public List<Comment> getComment() {
		return _comment;
	}

	public void setComment(List<Comment> comment) {
		this._comment = comment;
	}

	public String getDeleteFlg() {
		return _deleteFlg;
	}

	public void setDeleteFlg(String deleteFlg) {
		this._deleteFlg = deleteFlg;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public Object getValue(String fieldname) {
		return null;
	}

	@Override
	public void encrypt(Object cipher) {
	}

	@Override
	public void decrypt(Object cipher) {
	}

	@Override
	public boolean isMatch(ConditionBase[] conditions) {
		return false;
	}

	@Override
	public boolean validate(String ucode, List<String> groups)
			throws ParseException {
		return false;
	}

	@Override
	public void maskprop(String ucode, List<String> groups) {
	}

}
