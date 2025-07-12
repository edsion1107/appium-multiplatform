package android.content.pm;

interface IPackageManager {
    /**
     * @return the target SDK for the given package name, or -1 if it cannot be retrieved
     */
    int getTargetSdkVersion(String packageName);
    List<String> getAllPackages();
}