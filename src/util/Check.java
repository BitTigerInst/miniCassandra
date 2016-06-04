package util;

public class Check{ 
	public static void check_null(Object obj, String str) {
		if(obj==null) {
			throw new IllegalArgumentException(str + "null checked failed");
		}
	}
	
	public static void check_null(Object obj) {
		if(obj==null) {
			throw new IllegalArgumentException("null checked failed");
		}
	}
	
	public static void check_bool(boolean var) {
		if(!var) {
			throw new IllegalArgumentException("bool checked failed");
		}
	}
	
	public static void check_bool(boolean var, String str) {
		if(!var) {
			throw new IllegalArgumentException(var + " bool checked failed");
		}
	}
}