package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateManager {
    void bind(LifecycleOwner lifecycleOwner);
    void injectAgent(Object instance);
    StateWrapInfo getStateWrapInfo(LifecycleOwner lifecycleOwner);
    StateAgent getStateAgent(LifecycleOwner lifecycleOwner);
    Class getStateClass(LifecycleOwner lifecycleOwner);
}
