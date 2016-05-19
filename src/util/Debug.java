package util;
import java.util.Date;

public class Debug {
	public static int DEBUG = 1;
	public static void debug(Object s) {
		if(DEBUG > 0) {
			System.out.println(new Date() + "    " + s.toString());
		}
	}
}
