package util;

public class Check{ 
	public static void checkNull(Object obj, String str) {
		if (obj == null) {
			throw new IllegalArgumentException(str + "null checked failed");
		}
	}
	
	public static void checkNull(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("null checked failed");
		}
	}
	
	public static void checkBool(boolean var) {
		if (!var) {
			throw new IllegalArgumentException("bool checked failed");
		}
	}
	
	public static void checkBool(boolean var, String str) {
		if (!var) {
			throw new IllegalArgumentException(var + " bool checked failed");
		}
	}
}