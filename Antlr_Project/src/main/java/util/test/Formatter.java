package util.test;
/**
 * Formatter contract
 *
 * @author Steve Ebersole
 */
public interface Formatter {
	/**
	 * Format the source SQL string.
	 *
	 * @param source The original SQL string
	 *
	 * @return The formatted version
	 */
	String format(String source);
}