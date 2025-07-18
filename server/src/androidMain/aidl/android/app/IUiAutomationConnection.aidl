package android.app;

import android.accessibilityservice.IAccessibilityServiceClient;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.InputEvent;
import android.view.SurfaceControl;
import android.view.WindowContentFrameStats;
import android.view.WindowAnimationFrameStats;
import android.os.ParcelFileDescriptor;
import android.window.ScreenCapture.ScreenCaptureListener;
import android.window.ScreenCapture.LayerCaptureArgs;

import java.util.List;

/**
 * This interface contains privileged operations a shell program can perform
 * on behalf of an instrumentation that it runs. These operations require
 * special permissions which the shell user has but the instrumentation does
 * not. Running privileged operations by the shell user on behalf of an
 * instrumentation is needed for running UiTestCases.
 *
 * {@hide}
 */
interface IUiAutomationConnection {
    void connect(IAccessibilityServiceClient client, int flags);
    void disconnect();
    boolean injectInputEvent(in InputEvent event, boolean sync, boolean waitForAnimations);
    void injectInputEventToInputFilter(in InputEvent event);
    void syncInputTransactions(boolean waitForAnimations);
    boolean setRotation(int rotation);
    boolean takeScreenshot(in Rect crop, in ScreenCaptureListener listener, int displayId);
    boolean takeSurfaceControlScreenshot(in SurfaceControl surfaceControl, in ScreenCaptureListener listener);
    boolean clearWindowContentFrameStats(int windowId);
    WindowContentFrameStats getWindowContentFrameStats(int windowId);
    void clearWindowAnimationFrameStats();
    WindowAnimationFrameStats getWindowAnimationFrameStats();
    void executeShellCommand(String command, in ParcelFileDescriptor sink,
            in ParcelFileDescriptor source);
    void grantRuntimePermission(String packageName, String permission, int userId);
    void revokeRuntimePermission(String packageName, String permission, int userId);
    void adoptShellPermissionIdentity(int uid, in String[] permissions);
    void dropShellPermissionIdentity();
    // Called from the system process.
    oneway void shutdown();
    void executeShellCommandWithStderr(String command, in ParcelFileDescriptor sink,
                in ParcelFileDescriptor source, in ParcelFileDescriptor stderrSink);
    void executeShellCommandArrayWithStderr(in String[] command, in ParcelFileDescriptor sink,
                in ParcelFileDescriptor source, in ParcelFileDescriptor stderrSink);
    List<String> getAdoptedShellPermissions();
    void addOverridePermissionState(int uid, String permission, int result);
    void removeOverridePermissionState(int uid, String permission);
    void clearOverridePermissionStates(int uid);
    void clearAllOverridePermissionStates();
}