package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateManager {
    void bind(LifecycleOwner lifecycleOwner);
    void rebind(LifecycleOwner lifecycleOwner);
    void injectAgent(Object instance, LifecycleOwner lifecycleOwner);
    StateWrapInfo getStateWrapInfo(LifecycleOwner lifecycleOwner);
    Class getAgentClass(LifecycleOwner lifecycleOwner);
    Class getStateClass(LifecycleOwner lifecycleOwner);
}
