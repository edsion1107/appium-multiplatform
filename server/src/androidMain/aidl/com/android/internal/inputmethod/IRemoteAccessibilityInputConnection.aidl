package com.android.internal.inputmethod;

import android.view.KeyEvent;
import android.view.inputmethod.TextAttribute;

import com.android.internal.infra.AndroidFuture;
import com.android.internal.inputmethod.InputConnectionCommandHeader;

/**
 * Interface from A11y IMEs to the application, allowing it to perform edits on the current input
 * field and other interactions with the application.
 */
oneway interface IRemoteAccessibilityInputConnection {
    void commitText(in InputConnectionCommandHeader header, CharSequence text,
            int newCursorPosition, in TextAttribute textAttribute);

    void setSelection(in InputConnectionCommandHeader header, int start, int end);

    void getSurroundingText(in InputConnectionCommandHeader header, int beforeLength,
            int afterLength, int flags, in AndroidFuture future /* T=SurroundingText */);

    void deleteSurroundingText(in InputConnectionCommandHeader header, int beforeLength,
            int afterLength);

    void sendKeyEvent(in InputConnectionCommandHeader header, in KeyEvent event);

    void performEditorAction(in InputConnectionCommandHeader header, int actionCode);

    void performContextMenuAction(in InputConnectionCommandHeader header, int id);

    void getCursorCapsMode(in InputConnectionCommandHeader header, int reqModes,
            in AndroidFuture future /* T=Integer */);

    void clearMetaKeyStates(in InputConnectionCommandHeader header, int states);
}