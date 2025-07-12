 package com.android.internal.inputmethod;

 import com.android.internal.inputmethod.IAccessibilityInputMethodSession;

/**
 * Helper interface for IInputMethod to allow the input method to notify the client when a new
 * session has been created.
 */
oneway interface IAccessibilityInputMethodSessionCallback {
    void sessionCreated(IAccessibilityInputMethodSession session, int id);
}