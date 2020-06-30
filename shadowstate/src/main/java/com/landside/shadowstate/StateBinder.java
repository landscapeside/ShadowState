package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateBinder {
    void observe(LifecycleOwner owner, StateAgent dispatcher);
}
