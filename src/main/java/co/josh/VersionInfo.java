package co.josh;

public class VersionInfo {
    public static int major = 2;
    public static int minor = 0;
    public static int revision = 1;

    public static String getVersionString(){
        return major + "." + minor + "." + revision;
    }
}
