package co.josh;

public class VersionInfo {
    public static int major = 2;
    public static int minor = 1;
    public static int revision = 0;

    public static String getVersionString(){
        return major + "." + minor + "." + revision;
    }
}
