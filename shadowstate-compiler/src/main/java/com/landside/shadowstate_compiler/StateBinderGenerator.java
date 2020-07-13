package com.landside.shadowstate_compiler;

import com.landside.shadowstate_annotation.BindState;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class StateBinderGenerator {
  private Element mClassElement;

  public StateBinderGenerator(Element element) {
    this.mClassElement = element;
  }

  public JavaFile generate() throws Exception {
    TypeMirror stateCls = null;
    try {
      mClassElement.getAnnotation(BindState.class).state();
    } catch (MirroredTypeException e) {
      stateCls = e.getTypeMirror();
    }

    MethodSpec observe = MethodSpec.methodBuilder("observe")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addParameter(TypeClass.StateAgent, "agent")
        .addStatement("$T observer = agent.createObserver()", TypeClass.StateObserver)
        .addStatement("if (owner instanceof $T) {\n"
                +
                "  observer.getLiveData().setValue(($T) agent.initState((($T) owner).getIntent().getExtras()));\n"
                +
                "} else if (owner instanceof $T) {\n"
                +
                "  observer.getLiveData().setValue(($T) agent.initState((($T) owner).getArguments()));\n"
                +
                "} else {\n"
                +
                "  throw new IllegalArgumentException(\"\");\n"
                +
                "}",
            TypeClass.FragmentActivity,
            ClassName.get(stateCls),
            TypeClass.FragmentActivity,
            TypeClass.Fragment,
            ClassName.get(stateCls),
            TypeClass.Fragment
        )
        .addStatement("observer.getLiveData().observe(owner, observer)")
        .addStatement("agent.init(observer)")
        .addStatement("agent.bindView(($T) owner,observer)",
            ClassName.get((TypeElement) mClassElement))
        .build();
    String clsName = stateCls.toString().substring(stateCls.toString().lastIndexOf(".") + 1);
    clsName = ClassName.get((TypeElement) mClassElement).simpleName();
    TypeSpec generateClass = TypeSpec
        .classBuilder(clsName + "StateBinder")
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(TypeClass.StateBinder)
        .addMethod(observe)
        .build();
    return JavaFile.builder(
        ClassName.get((TypeElement) mClassElement).packageName(),
        generateClass
    ).build();
  }
}
