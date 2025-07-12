package android.accessibilityservice;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Region;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.MagnificationConfig;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import com.android.internal.inputmethod.IAccessibilityInputMethodSession;
import com.android.internal.inputmethod.IAccessibilityInputMethodSessionCallback;
import com.android.internal.inputmethod.IRemoteAccessibilityInputConnection;

/**
 * Top-level interface to an accessibility service component.
 *
 * @hide
 */
 oneway interface IAccessibilityServiceClient {

    void init(in IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken);

    void onAccessibilityEvent(in AccessibilityEvent event, in boolean serviceWantsEvent);

    void onInterrupt();

    void onGesture(in AccessibilityGestureEvent gestureEvent);

    void clearAccessibilityCache();

    void onKeyEvent(in KeyEvent event, int sequence);

    void onMagnificationChanged(int displayId, in Region region, in MagnificationConfig config);

    void onMotionEvent(in MotionEvent event);

    void onTouchStateChanged(int displayId, int state);

    void onSoftKeyboardShowModeChanged(int showMode);

    void onPerformGestureResult(int sequence, boolean completedSuccessfully);

    void onFingerprintCapturingGesturesChanged(boolean capturing);

    void onFingerprintGesture(int gesture);

    void onAccessibilityButtonClicked(int displayId);

    void onAccessibilityButtonAvailabilityChanged(boolean available);

    void onSystemActionsChanged();

    void createImeSession(in IAccessibilityInputMethodSessionCallback callback);

    void setImeSessionEnabled(in IAccessibilityInputMethodSession session, boolean enabled);

    void bindInput();

    void unbindInput();

    void startInput(in IRemoteAccessibilityInputConnection connection, in EditorInfo editorInfo,
            boolean restarting);
}