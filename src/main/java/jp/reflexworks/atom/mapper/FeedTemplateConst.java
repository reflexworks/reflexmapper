package jp.reflexworks.atom.mapper;

import jp.sourceforge.reflex.util.BinaryUtil;

public interface FeedTemplateConst {
	
	/** MessagePack Entry byte配列 16進数表記 */
	public static final String MSGPACK_BYTES_HEX_ENTRY = 
			"DC0010C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";
	/** MessagePack Entry byte配列 */
	public static final byte[] MSGPACK_BYTES_ENTRY = 
			BinaryUtil.hex2bin(MSGPACK_BYTES_HEX_ENTRY);
	/** MessagePack Feed byte配列 16進数表記 */
	public static final String MSGPACK_BYTES_HEX_FEED = 
			"9FC0C0C0C0C0C0C0C0C0C0C0C0C0C0C0";
	/** MessagePack Feed byte配列 */
	public static final byte[] MSGPACK_BYTES_FEED = 
			BinaryUtil.hex2bin(MSGPACK_BYTES_HEX_FEED);
	/** MessagePack byte配列 最初の1バイト (Feedの場合) */
	public static final byte MSGPACK_PREFIX = MSGPACK_BYTES_FEED[0];

}
