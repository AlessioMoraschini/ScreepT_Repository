package util;

import javax.swing.text.BadLocationException;

public class BitWorker {

	/**
	 * Set 1 into the given byte if flag is true, 0 if flag is false. if position is < 0 or > 7 then source byte won't be modified and the original one is returned.
	 * @param toChange
	 * @param position
	 * @return the result after the modification
	 */
	public static byte setBitAtPosition(byte toChange, int position, boolean flag) throws BadLocationException {
		
		if(position < 0 || position > 7)
			throw new BadLocationException("Invalid position!", position);
		
		if (flag) {
			return (byte) (toChange | (1 << position));
		} else {
			return (byte) (toChange & ~(1 << position)); 
		}
	}

	public static boolean getBitAtPosition(byte toCheck, int position) throws BadLocationException {
		if(position < 0 || position > 7)
			throw new BadLocationException("Invalid position!", position);

		return ((toCheck >> position) & 1) == 1;
	}
}
