package ccre.workarounds;

public class BackportImpls {
    public static String java_lang_Boolean_toString(boolean b) {
        return b ? "true" : "false";
    }
    
    public static boolean java_lang_Boolean_parseBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }
}
