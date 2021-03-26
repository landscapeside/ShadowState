package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;
import java.util.List;

public interface StateManager {
    void bind(LifecycleOwner lifecycleOwner);
    void rebind(LifecycleOwner lifecycleOwner);
    void remove(LifecycleOwner lifecycleOwner);
    void detach(LifecycleOwner lifecycleOwner);
    void injectAgent(Object instance,LifecycleOwner lifecycleOwner);
    StateBinder getBinder(LifecycleOwner lifecycleOwner);
}
