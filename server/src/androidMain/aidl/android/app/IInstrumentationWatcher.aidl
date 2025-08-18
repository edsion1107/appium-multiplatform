package android.app;

import android.content.ComponentName;
import android.os.Bundle;

interface IInstrumentationWatcher
{
    @UnsupportedAppUsage
    void instrumentationStatus(in ComponentName name, int resultCode,
            in Bundle results);
    void instrumentationFinished(in ComponentName name, int resultCode,
            in Bundle results);
}