package com.landside.shadowstate;

import androidx.lifecycle.LifecycleOwner;

public interface AttachBinder {
  Class[] getStateCls();

  Class[] getAgentCls();

  ShadowStateAgent[] getAgent(LifecycleOwner owner);

  void observe(LifecycleOwner owner);
  void reset(LifecycleOwner owner);
  void remove(LifecycleOwner owner);
}
