package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface StateBinder {
    StateAgent getAgent(LifecycleOwner owner);
    void observe(LifecycleOwner owner, Class agentCls);
    void reset(LifecycleOwner owner, Class agentCls);
}
