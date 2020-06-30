package com.landside.shadowstate_compiler;

import com.squareup.javapoet.ClassName;

public class TypeClass {
    public static final ClassName StateManager = ClassName.get("com.landside.shadowstate","StateManager");
    public static final ClassName StateBinder = ClassName.get("com.landside.shadowstate","StateBinder");
    public static final ClassName StateAgent = ClassName.get("com.landside.shadowstate","StateAgent");
    public static final ClassName MutableLiveData = ClassName.get("androidx.lifecycle","MutableLiveData");
    public static final ClassName LifecycleOwner = ClassName.get("androidx.lifecycle","LifecycleOwner");
    public static final ClassName Fragment = ClassName.get("androidx.fragment.app","Fragment");
    public static final ClassName FragmentActivity = ClassName.get("androidx.fragment.app","FragmentActivity");
}