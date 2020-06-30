package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateManager {
    void bind(LifecycleOwner lifecycleOwner);
    void injectAgent(Object instance);
}
