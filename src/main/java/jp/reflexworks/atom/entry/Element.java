package jp.reflexworks.atom.entry;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

// このパッケージに置かないとXMLにできない

@Message
public class Element {

	  public final int _$$col = 0;
	  @Index(0)
	  public String _$$text;
}
