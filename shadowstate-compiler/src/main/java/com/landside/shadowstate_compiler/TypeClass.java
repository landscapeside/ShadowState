package com.landside.shadowstate_compiler;

import com.squareup.javapoet.ClassName;

public class TypeClass {
  public static final ClassName StateManager =
      ClassName.get("com.landside.shadowstate", "StateManager");
  public static final ClassName StateBinder =
      ClassName.get("com.landside.shadowstate", "StateBinder");
  public static final ClassName ShareBinder =
      ClassName.get("com.landside.shadowstate", "ShareBinder");
  public static final ClassName AttachBinder =
      ClassName.get("com.landside.shadowstate", "AttachBinder");
  public static final ClassName ScopeBinder =
          ClassName.get("com.landside.shadowstate", "ScopeBinder");
  public static final ClassName StateAgent =
      ClassName.get("com.landside.shadowstate", "StateAgent");
  public static final ClassName ShadowStateAgent =
      ClassName.get("com.landside.shadowstate", "ShadowStateAgent");
  public static final ClassName AttachAgent =
      ClassName.get("com.landside.shadowstate", "AttachAgent");
  public static final ClassName ScopeAgent =
          ClassName.get("com.landside.shadowstate", "ScopeAgent");
  public static final ClassName AgentInjection =
      ClassName.get("com.landside.shadowstate", "AgentInjection");
  public static final ClassName ShadowState =
      ClassName.get("com.landside.shadowstate", "ShadowState");
  public static final ClassName MutableLiveData =
      ClassName.get("androidx.lifecycle", "MutableLiveData");
  public static final ClassName LifecycleOwner =
      ClassName.get("androidx.lifecycle", "LifecycleOwner");
  public static final ClassName Fragment = ClassName.get("androidx.fragment.app", "Fragment");
  public static final ClassName FragmentActivity =
      ClassName.get("androidx.fragment.app", "FragmentActivity");
  public static final ClassName AnnotationHelper =
      ClassName.get("com.landside.shadowstate","AnnotationHelper");
}
