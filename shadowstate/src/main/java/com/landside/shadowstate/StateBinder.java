package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateBinder {
    Class getAgentCls();
    StateAgent getAgent(LifecycleOwner owner);
    void observe(LifecycleOwner owner);
    void reset(LifecycleOwner owner);
    void remove(LifecycleOwner owner);
}
