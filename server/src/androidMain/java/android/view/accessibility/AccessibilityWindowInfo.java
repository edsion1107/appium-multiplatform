package android.view.accessibility;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("NewApi")
public class AccessibilityWindowInfo implements Parcelable {
    public static final Creator<AccessibilityWindowInfo> CREATOR = new Creator<AccessibilityWindowInfo>() {
        @Override
        public AccessibilityWindowInfo createFromParcel(Parcel in) {
            return new AccessibilityWindowInfo(in);
        }

        @Override
        public AccessibilityWindowInfo[] newArray(int size) {
            return new AccessibilityWindowInfo[size];
        }
    };

    protected AccessibilityWindowInfo(Parcel in) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }

    /**
     * Transfers a sparsearray with lists having {@link AccessibilityWindowInfo}s across an IPC.
     * The key of this sparsearray is display Id.
     *
     * @hide
     */
    public static final class WindowListSparseArray
            extends SparseArray<List<AccessibilityWindowInfo>> implements Parcelable {

        public static final Parcelable.Creator<WindowListSparseArray> CREATOR =
                new Parcelable.Creator<WindowListSparseArray>() {
                    public WindowListSparseArray createFromParcel(
                            Parcel source) {
                        final WindowListSparseArray array = new WindowListSparseArray();
                        final ClassLoader loader = array.getClass().getClassLoader();
                        final int count = source.readInt();
                        for (int i = 0; i < count; i++) {
                            List<AccessibilityWindowInfo> windows = new ArrayList<>();
                            source.readParcelableList(windows, loader, android.view.accessibility.AccessibilityWindowInfo.class);
                            array.put(source.readInt(), windows);
                        }
                        return array;
                    }

                    public WindowListSparseArray[] newArray(int size) {
                        return new WindowListSparseArray[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            final int count = size();
            dest.writeInt(count);
            for (int i = 0; i < count; i++) {
                dest.writeParcelableList(valueAt(i), 0);
                dest.writeInt(keyAt(i));
            }
        }
    }
}
