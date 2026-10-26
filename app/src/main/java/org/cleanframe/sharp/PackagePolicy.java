package org.cleanframe.sharp;

/** Pure policy/state logic; never mutates Android packages. */
public final class PackagePolicy {
    private PackagePolicy() {}
    public static boolean isCandidate(String pkg) {
        if (pkg == null) return false;
        switch (pkg) {
            case "jp.co.sharp.av.android.analyticsservice":
            case "jp.co.sharp.av.android.cocorovision":
            case "com.google.android.tvrecommendations":
            case "com.google.android.feedback":
            case "com.google.android.tv.bugreportsender": return true;
            default: return false;
        }
    }
    /** Android COMPONENT_ENABLED_STATE values 0..4; stopped process state is irrelevant. */
    public static String describe(int setting, boolean applicationEnabled) {
        switch (setting) {
            case 0: return applicationEnabled ? "ENABLED (default)" : "DISABLED (application default)";
            case 1: return "ENABLED (explicit)";
            case 2: return "DISABLED";
            case 3: return "DISABLED BY USER";
            case 4: return "DISABLED UNTIL USED";
            default: return "UNKNOWN — unrecognized Android state";
        }
    }
}
