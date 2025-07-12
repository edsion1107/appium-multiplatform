package com.android.internal.inputmethod;

import android.view.inputmethod.EditorInfo;

import com.android.internal.inputmethod.IRemoteAccessibilityInputConnection;

/**
 * Sub-interface of IInputMethodSession which is safe to give to A11y IME.
 */
oneway interface IAccessibilityInputMethodSession {
    void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd);

    void finishInput();

    void finishSession();

    void invalidateInput(in EditorInfo editorInfo,
            in IRemoteAccessibilityInputConnection connection, int sessionId);
}