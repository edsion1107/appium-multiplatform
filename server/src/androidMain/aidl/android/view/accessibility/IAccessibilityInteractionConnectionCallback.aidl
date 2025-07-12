package android.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/**
 * Callback for specifying the result for an asynchronous request made
 * via calling a method on IAccessibilityInteractionConnectionCallback.
 *
 * @hide
 */
oneway interface IAccessibilityInteractionConnectionCallback {

    /**
     * Sets the result of an async request that returns an {@link AccessibilityNodeInfo}.
     *
     * @param infos The result {@link AccessibilityNodeInfo}.
     * @param interactionId The interaction id to match the result with the request.
     */
    @UnsupportedAppUsage(maxTargetSdk = 30, trackingBug = 170729553)
    @RequiresNoPermission
    void setFindAccessibilityNodeInfoResult(in AccessibilityNodeInfo info, int interactionId);

    /**
     * Sets the result of an async request that returns {@link AccessibilityNodeInfo}s.
     *
     * @param infos The result {@link AccessibilityNodeInfo}s.
     * @param interactionId The interaction id to match the result with the request.
     */
    @UnsupportedAppUsage(maxTargetSdk = 30, trackingBug = 170729553)
    @RequiresNoPermission
    void setFindAccessibilityNodeInfosResult(in List<AccessibilityNodeInfo> infos,
        int interactionId);

    /**
     * Sets the result of a prefetch request that returns {@link AccessibilityNodeInfo}s.
     *
     * @param root The {@link AccessibilityNodeInfo} for which the prefetching is based off of.
     * @param infos The result {@link AccessibilityNodeInfo}s.
     */
     @RequiresNoPermission
    void setPrefetchAccessibilityNodeInfoResult(
        in List<AccessibilityNodeInfo> infos, int interactionId);

    /**
     * Sets the result of a request to perform an accessibility action.
     *
     * @param Whether the action was performed.
     * @param interactionId The interaction id to match the result with the request.
     */
//    @UnsupportedAppUsage(maxTargetSdk = 30, trackingBug = 170729553)
    @RequiresNoPermission
    void setPerformAccessibilityActionResult(boolean succeeded, int interactionId);

    /**
    * Sends an error code for a window screenshot request to the requesting client.
    */
    @RequiresNoPermission
    void sendTakeScreenshotOfWindowError(int errorCode, int interactionId);

    /**
    * Sends an result code for an attach overlay request to the requesting client.
    */
    @RequiresNoPermission
    void sendAttachOverlayResult(int result, int interactionId);
}