/**
 * 
 */
package edu.isi.karma.rep;

/**
 * @author szekely
 * 
 */
public abstract class CellValue {
	// Return the value as a string.
	public abstract String asString();

	@Override
	public boolean equals(Object o) {
		if (o instanceof CellValue) {
			CellValue cv = (CellValue) o;
			return cv.asString().equals(this.asString());
		}
		return false;
	}
}
