package com.landside.shadowstate_compiler;

import com.landside.shadowstate_annotation.BindState;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

public class StateManagerGenerator {
  private String packageName;
  private String className;
  private Set<? extends Element> bindStates;

  public StateManagerGenerator(
      String packageName,
      String className,
      Set<? extends Element> bindStates
  ) {
    this.packageName = packageName;
    this.className = className;
    this.bindStates = bindStates;
  }

  public JavaFile generate() throws Exception {
    TypeName classWithWildcard = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    TypeName stateMapType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        classWithWildcard,
        TypeClass.StateWrapInfo);

    FieldSpec stateMapParam = FieldSpec
        .builder(stateMapType, "stateInfoMap")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();

    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

    for (Element state : bindStates) {
      TypeMirror stateCls = null;
      try {
        state.getAnnotation(BindState.class).state();
      } catch (MirroredTypeException e) {
        stateCls = e.getTypeMirror();
      }
      TypeMirror agentCls = null;
      try {
        state.getAnnotation(BindState.class).agent();
      } catch (MirroredTypeException e) {
        agentCls = e.getTypeMirror();
      }
      String clsName = stateCls.toString().substring(stateCls.toString().lastIndexOf(".") + 1);
      clsName = ClassName.get((TypeElement) state).simpleName();
      constructorBuilder.addStatement(
          "stateInfoMap.put($T.class,new $T($T.class,new $T(),new $T()))",
          ClassName.get((TypeElement) state),
          TypeClass.StateWrapInfo,
          ClassName.get(stateCls),
          ClassName.get(agentCls),
          ClassName.get(
              ClassName.get((TypeElement) state).packageName(),
              clsName + "StateBinder"
          )
      );
    }

    MethodSpec bind = MethodSpec.methodBuilder("bind")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addStatement("if (getStateWrapInfo(lifecycleOwner) == null) {\n" +
            "  return;\n" +
            "}")
        .addStatement(
            "getStateWrapInfo(lifecycleOwner).getBinder().observe(lifecycleOwner, getStateAgent(lifecycleOwner))")
        .build();

    MethodSpec injectDispatcher = MethodSpec.methodBuilder("injectAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(Object.class, "instance")
        .addStatement("$T<Class<?>> agentClasses = $T.INSTANCE.getAgents(instance)",
            ClassName.get(List.class), TypeClass.AgentInjection)
        .addStatement("for (Class<?> cls : agentClasses) {\n" +
            " for (Map.Entry<Class<?>, StateWrapInfo> entry :\n" +
            "     stateInfoMap.entrySet()) {\n" +
            "        if (entry.getValue().getAgent().getClass() == cls) {\n" +
            "            $T.INSTANCE.inject(\n" +
            "             instance,\n" +
            "             entry.getValue().getAgent()\n" +
            "           );\n" +
            "        }\n" +
            "     }\n" +
            " }", TypeClass.AgentInjection)
        .build();

    MethodSpec getStateWrapInfo = MethodSpec.methodBuilder("getStateWrapInfo")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(TypeClass.StateWrapInfo)
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addStatement("if (stateInfoMap.get(lifecycleOwner.getClass()) == null) {\n" +
            "  return null;\n" +
            "}")
        .addStatement("return stateInfoMap.get(lifecycleOwner.getClass())")
        .build();

    MethodSpec getStateAgent = MethodSpec.methodBuilder("getStateAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(TypeClass.StateAgent)
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addStatement("if (getStateWrapInfo(lifecycleOwner) == null) {\n" +
            "  return null;\n" +
            "}")
        .addStatement("return getStateWrapInfo(lifecycleOwner).getAgent()")
        .build();

    MethodSpec getStateClass = MethodSpec.methodBuilder("getStateClass")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(ClassName.get(Class.class))
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addStatement("if (getStateWrapInfo(lifecycleOwner) == null) {\n" +
            "  return null;\n" +
            "}")
        .addStatement("return getStateWrapInfo(lifecycleOwner).getStateCls()")
        .build();

    TypeSpec generateClass = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(TypeClass.StateManager)
        .addMethod(constructorBuilder.build())
        .addMethod(bind)
        .addMethod(injectDispatcher)
        .addMethod(getStateWrapInfo)
        .addMethod(getStateAgent)
        .addMethod(getStateClass)
        .addField(stateMapParam)
        .build();
    return JavaFile.builder(packageName, generateClass)
        .build();
  }
}
