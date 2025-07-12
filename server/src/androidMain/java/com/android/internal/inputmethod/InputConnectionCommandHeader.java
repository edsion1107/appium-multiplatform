package com.android.internal.inputmethod;


import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * A common IPC header used behind {@link android.view.inputmethod.RemoteInputConnectionImpl} and
 * {@link android.inputmethodservice.RemoteInputConnection}.
 */
public final class InputConnectionCommandHeader implements Parcelable {
    @NonNull
    public static final Parcelable.Creator<InputConnectionCommandHeader> CREATOR =
            new Parcelable.Creator<InputConnectionCommandHeader>() {
                @NonNull
                public InputConnectionCommandHeader createFromParcel(Parcel in) {
                    final int sessionId = in.readInt();
                    return new InputConnectionCommandHeader(sessionId);
                }

                @NonNull
                public InputConnectionCommandHeader[] newArray(int size) {
                    return new InputConnectionCommandHeader[size];
                }
            };
    /**
     * An identifier that is to be used when multiplexing multiple sessions into a single
     * {@link com.android.internal.inputmethod.IRemoteInputConnection}.
     *
     * <p>This ID is considered to belong to an implicit namespace defined for each
     * {@link com.android.internal.inputmethod.IRemoteInputConnection} instance.  Uniqueness of the
     * session ID across multiple instances of
     * {@link com.android.internal.inputmethod.IRemoteInputConnection} is not guaranteed unless
     * explicitly noted in a higher layer.</p>
     */
    public final int mSessionId;

    public InputConnectionCommandHeader(int sessionId) {
        mSessionId = sessionId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mSessionId);
    }
}
