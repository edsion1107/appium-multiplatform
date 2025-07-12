package com.android.internal.infra;

import com.android.internal.infra.AndroidFuture;

oneway interface IAndroidFuture {
    void complete(in AndroidFuture resultContainer);
}