package edu.isi.karma.rep;

/**
 * @author szekely
 * 
 */
public class StringCellValue extends CellValue {

	private final String value;

	private static final StringCellValue emptyString = new StringCellValue("");

	public static StringCellValue getEmptyString() {
		return emptyString;
	}

	@Override
	public String asString() {
		return value;
	}

	public StringCellValue(String value) {
		super();
		this.value = value;
	}

}
