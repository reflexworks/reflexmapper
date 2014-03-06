package jp.reflexworks.test2.model;

public class Comment {
	
	public String _nickname;
	public String _$$text;

	public String getNickname() {
		return _nickname;
	}

	public void setNickname(String nickname) {
		this._nickname = nickname;
	}

	public String get$$text() {
		return _$$text;
	}

	public void set$$text(String _$$text) {
		this._$$text = _$$text;
	}

	@Override
	public String toString() {
		return "Comment [nickname=" + _nickname + ", _$$text=" + _$$text + "]";
	}

}
