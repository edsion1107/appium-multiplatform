package android.os;

//import android.annotation.NonNull;
//import android.annotation.Nullable;
//import android.annotation.SystemApi;

import android.compat.annotation.UnsupportedAppUsage;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @hide
 */
//@SystemApi
//@android.ravenwood.annotation.RavenwoodKeepWholeClass
public final class RemoteCallback implements Parcelable {

    public static final @NonNull Parcelable.Creator<RemoteCallback> CREATOR
            = new Parcelable.Creator<RemoteCallback>() {
        public RemoteCallback createFromParcel(Parcel parcel) {
            return new RemoteCallback(parcel);
        }

        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
    private final OnResultListener mListener;
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.P)
    private final Handler mHandler;
    private final IRemoteCallback mCallback;

    public RemoteCallback(OnResultListener listener) {
        this(listener, null);
    }

    public RemoteCallback(@NonNull OnResultListener listener, @Nullable Handler handler) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        mListener = listener;
        mHandler = handler;
        mCallback = new IRemoteCallback.Stub() {
            @Override
            public void sendResult(Bundle data) {
                RemoteCallback.this.sendResult(data);
            }
        };
    }

    RemoteCallback(Parcel parcel) {
        mListener = null;
        mHandler = null;
        mCallback = IRemoteCallback.Stub.asInterface(
                parcel.readStrongBinder());
    }

    public void sendResult(@Nullable final Bundle result) {
        // Do local dispatch
        if (mListener != null) {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onResult(result);
                    }
                });
            } else {
                mListener.onResult(result);
            }
            // Do remote dispatch
        } else {
            try {
                mCallback.sendResult(result);
            } catch (RemoteException e) {
                /* ignore */
            }
        }
    }

    /**
     * @hide
     */
    public IRemoteCallback getInterface() {
        return mCallback;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(mCallback.asBinder());
    }

    public interface OnResultListener {
        void onResult(@Nullable Bundle result);
    }
}