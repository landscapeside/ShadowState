package com.landside.shadowstate_compiler;

import com.google.common.collect.Lists;
import com.landside.shadowstate_annotation.AttachState;
import com.landside.shadowstate_annotation.BindState;
import com.landside.shadowstate_annotation.ScopeState;
import com.landside.shadowstate_annotation.ShareState;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

public class StateManagerGenerator {
  private String packageName;
  private String className;
  private Set<? extends Element> bindStates;
  private Set<? extends Element> attachStates;
  private Set<? extends Element> shareStates;
  private Set<? extends Element> scopeStates;

  public StateManagerGenerator(
      String packageName,
      String className,
      Set<? extends Element> bindStates,
      Set<? extends Element> attachStates,
      Set<? extends Element> shareStates,
      Set<? extends Element> scopeStates
  ) {
    this.packageName = packageName;
    this.className = className;
    this.bindStates = bindStates;
    this.attachStates = attachStates;
    this.shareStates = shareStates;
    this.scopeStates = scopeStates;
  }

  public JavaFile generate() throws Exception {
    TypeName classWithWildcard = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    TypeName stateMapType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        classWithWildcard,
        TypeClass.StateBinder);

    TypeName attachBinderType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        classWithWildcard,
        TypeClass.AttachBinder);

    TypeName shareBinderType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        classWithWildcard,
        TypeClass.ShareBinder
    );

    TypeName scopeBinderType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        classWithWildcard,
        TypeClass.ScopeBinder
    );

    FieldSpec stateMapParam = FieldSpec
        .builder(stateMapType, "stateInfoMap")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();
    FieldSpec attachBinderMap = FieldSpec
        .builder(attachBinderType, "attachBinderMap")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();
    FieldSpec shareBinderMap = FieldSpec
        .builder(shareBinderType, "shareBinderMap")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();
    FieldSpec scopeBinderMap = FieldSpec
        .builder(scopeBinderType, "scopeBinderMap")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();

    // constructor
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
      String clsName = ClassName.get((TypeElement) state).simpleName();
      constructorBuilder.addStatement(
          "stateInfoMap.put($T.class,new $T($T.class,$T.class))",
          ClassName.get((TypeElement) state),
          ClassName.get(
              ClassName.get((TypeElement) state).packageName(),
              clsName + "StateBinder"
          ),
          ClassName.get(stateCls),
          ClassName.get(agentCls)
      );
    }
    for (Element state : attachStates) {
      String clsName = ClassName.get((TypeElement) state).simpleName();
      List<? extends TypeMirror> stateCls = new ArrayList<>(),agentCls = new ArrayList<>();
      try {
        state.getAnnotation(AttachState.class).states();
      } catch (MirroredTypesException e) {
        stateCls = e.getTypeMirrors();
      }
      try {
        state.getAnnotation(AttachState.class).agents();
      } catch (MirroredTypesException e) {
        agentCls = e.getTypeMirrors();
      }
      ClassName binder = ClassName.get(
          ClassName.get((TypeElement) state).packageName(),
          clsName + "AttachBinder"
      );
      CodeBlock.Builder stateBlock = CodeBlock.builder();
      stateBlock.add("new Class[] { ");
      List<CodeBlock> codeBlocks = new ArrayList<>();
      for (int i = 0; i < stateCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(stateCls.get(i))).build());
      }
      stateBlock.add(CodeBlock.join(codeBlocks, ","));
      stateBlock.add("} ");
      CodeBlock.Builder agentBlock = CodeBlock.builder();
      agentBlock.add("new Class[] { ");
      codeBlocks.clear();
      for (int i = 0; i < agentCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(agentCls.get(i))).build());
      }
      agentBlock.add(CodeBlock.join(codeBlocks, ","));
      agentBlock.add("} ");
      CodeBlock.Builder attachBinderMapBlock = CodeBlock.builder();
      attachBinderMapBlock.add(
          "attachBinderMap.put($T.class,new $T(",
          ClassName.get((TypeElement) state),
          binder
      );
      attachBinderMapBlock.add(
          CodeBlock.join(Lists.newArrayList(stateBlock.build(), agentBlock.build()), ","));
      attachBinderMapBlock.add("))\n");
      constructorBuilder.addStatement(
          attachBinderMapBlock.build()
      );
    }
    for (Element state : shareStates) {
      String clsName = ClassName.get((TypeElement) state).simpleName();
      List<? extends TypeMirror> shareStateCls = new ArrayList<>(), shareAgentCls =
          new ArrayList<>();
      try {
        state.getAnnotation(ShareState.class).states();
      } catch (MirroredTypesException e) {
        shareStateCls = e.getTypeMirrors();
      }
      try {
        state.getAnnotation(ShareState.class).agent();
      } catch (MirroredTypesException e) {
        shareAgentCls = e.getTypeMirrors();
      }
      ClassName binder = ClassName.get(
          ClassName.get((TypeElement) state).packageName(),
          clsName + "ShareBinder"
      );
      CodeBlock.Builder stateBlock = CodeBlock.builder();
      stateBlock.add("new Class[] { ");
      List<CodeBlock> codeBlocks = new ArrayList<>();
      for (int i = 0; i < shareStateCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(shareStateCls.get(i))).build());
      }
      stateBlock.add(CodeBlock.join(codeBlocks, ","));
      stateBlock.add("} ");
      CodeBlock.Builder agentBlock = CodeBlock.builder();
      agentBlock.add("new Class[] { ");
      codeBlocks.clear();
      for (int i = 0; i < shareAgentCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(shareAgentCls.get(i))).build());
      }
      agentBlock.add(CodeBlock.join(codeBlocks, ","));
      agentBlock.add("} ");
      CodeBlock.Builder shareBinderMapBlock = CodeBlock.builder();
      shareBinderMapBlock.add(
          "shareBinderMap.put($T.class,new $T(",
          ClassName.get((TypeElement) state),
          binder
      );
      shareBinderMapBlock.add(
          CodeBlock.join(Lists.newArrayList(stateBlock.build(), agentBlock.build()), ","));
      shareBinderMapBlock.add("))\n");
      constructorBuilder.addStatement(
          shareBinderMapBlock.build()
      );
    }
    for (Element state : scopeStates) {
      String clsName = ClassName.get((TypeElement) state).simpleName();
      List<? extends TypeMirror> stateCls = new ArrayList<>(),agentCls = new ArrayList<>();
      try {
        state.getAnnotation(ScopeState.class).states();
      } catch (MirroredTypesException e) {
        stateCls = e.getTypeMirrors();
      }
      try {
        state.getAnnotation(ScopeState.class).agents();
      } catch (MirroredTypesException e) {
        agentCls = e.getTypeMirrors();
      }
      ClassName binder = ClassName.get(
          ClassName.get((TypeElement) state).packageName(),
          clsName + "ScopeBinder"
      );
      CodeBlock.Builder stateBlock = CodeBlock.builder();
      stateBlock.add("new Class[] { ");
      List<CodeBlock> codeBlocks = new ArrayList<>();
      for (int i = 0; i < stateCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(stateCls.get(i))).build());
      }
      stateBlock.add(CodeBlock.join(codeBlocks, ","));
      stateBlock.add("} ");
      CodeBlock.Builder agentBlock = CodeBlock.builder();
      agentBlock.add("new Class[] { ");
      codeBlocks.clear();
      for (int i = 0; i < agentCls.size(); i++) {
        codeBlocks.add(
            CodeBlock.builder().add("$T.class", ClassName.get(agentCls.get(i))).build());
      }
      agentBlock.add(CodeBlock.join(codeBlocks, ","));
      agentBlock.add("} ");
      CodeBlock.Builder scopeBinderMapBlock = CodeBlock.builder();
      scopeBinderMapBlock.add(
          "scopeBinderMap.put($T.class,new $T(",
          ClassName.get((TypeElement) state),
          binder
      );
      scopeBinderMapBlock.add(
          CodeBlock.join(Lists.newArrayList(stateBlock.build(), agentBlock.build()), ","));
      scopeBinderMapBlock.add("))\n");
      constructorBuilder.addStatement(
          scopeBinderMapBlock.build()
      );
    }

    // bind method
    MethodSpec bind = MethodSpec.methodBuilder("bind")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addCode(
            "if (getBinder(lifecycleOwner) == null "
                + " && attachBinderMap.get(lifecycleOwner.getClass()) == null "
                + "&& shareBinderMap.get(lifecycleOwner.getClass()) == null "
                + "&& scopeBinderMap.get(lifecycleOwner.getClass()) == null) {\n"
                +
                "  return;\n"
                +
                "}\n")
        .addCode("if(getBinder(lifecycleOwner) != null){\n" +
            "getBinder(lifecycleOwner).observe(lifecycleOwner);\n}\n")
        .addCode("if(attachBinderMap.get(lifecycleOwner.getClass()) != null){\n" +
            "attachBinderMap.get(lifecycleOwner.getClass()).observe(lifecycleOwner);\n}\n")
        .addCode("if(scopeBinderMap.get(lifecycleOwner.getClass()) != null){\n" +
            "scopeBinderMap.get(lifecycleOwner.getClass()).observe(lifecycleOwner);\n}\n")
        .addCode("if (shareBinderMap.get(lifecycleOwner.getClass()) != null) {\n"
                + " ShareBinder shareBinder = shareBinderMap.get(lifecycleOwner.getClass());\n"
                + "   Class[] shareStateCls = shareBinder.getStateCls();\n"
                + "   for (int i = 0; i < shareStateCls.length; i++) {\n"
                + "   if (!$T.INSTANCE.getShareStates().containsKey(shareStateCls[i])) {\n"
                + "     try {\n"
                + "       ShadowState.INSTANCE.setupShare(shareStateCls[i],\n"
                + "         shareStateCls[i].newInstance());\n"
                + "     } catch ($T | $T e) {\n"
                + "        e.printStackTrace();\n"
                + "     }\n"
                + "   }\n"
                + "  }\n"
                + "  shareBinder.observe(lifecycleOwner);\n"
                + "}",
            TypeClass.ShadowState,
            ClassName.get(IllegalAccessException.class),
            ClassName.get(InstantiationException.class)
        )
        .build();

    // rebind method
    MethodSpec rebind = MethodSpec.methodBuilder("rebind")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addCode("if (getBinder(lifecycleOwner) == null) {\n" +
            "  return;\n" +
            "}\n")
        .addStatement(
            "getBinder(lifecycleOwner).reset(lifecycleOwner)")
        .addCode("if (attachBinderMap.get(lifecycleOwner.getClass()) == null) {\n" +
            "  return;\n" +
            "}\n")
        .addStatement(
            "attachBinderMap.get(lifecycleOwner.getClass()).reset(lifecycleOwner)")
        .addCode("if (scopeBinderMap.get(lifecycleOwner.getClass()) == null) {\n" +
            "  return;\n" +
            "}\n")
        .addStatement(
            "scopeBinderMap.get(lifecycleOwner.getClass()).reset(lifecycleOwner)")
        .build();

    // remove method
    MethodSpec remove = MethodSpec.methodBuilder("remove")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addCode(
            "if (getBinder(lifecycleOwner) == null "
                + "&& attachBinderMap.get(lifecycleOwner.getClass()) == null "
                + "&& shareBinderMap.get(lifecycleOwner.getClass()) == null "
                + "&& scopeBinderMap.get(lifecycleOwner.getClass()) == null) {\n"
                +
                "  return;\n"
                +
                "}\n")
        .addCode("if(getBinder(lifecycleOwner) != null){\n" +
            "getBinder(lifecycleOwner).remove(lifecycleOwner);\n}\n")
        .addCode("detach(lifecycleOwner);\n")
        .addCode("if (shareBinderMap.get(lifecycleOwner.getClass()) != null) {\n"
            + "     ShareBinder shareBinder = shareBinderMap.get(lifecycleOwner.getClass());\n"
            + "     shareBinder.remove(lifecycleOwner);\n"
            + "}"
        )
        .addCode("    if (scopeBinderMap.get(lifecycleOwner.getClass()) != null) {\n"
            + "      scopeBinderMap.get(lifecycleOwner.getClass()).remove(lifecycleOwner);\n"
            + "    }")
        .build();

    // detach method
    MethodSpec detach = MethodSpec.methodBuilder("detach")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addCode("if (attachBinderMap.get(lifecycleOwner.getClass()) != null){\n" +
            "attachBinderMap.get(lifecycleOwner.getClass()).remove(lifecycleOwner);\n}\n")
        .build();

    // injectAgent method
    MethodSpec injectDispatcher = MethodSpec.methodBuilder("injectAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(Object.class, "instance")
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addStatement("$T<Class<?>> agentClasses = $T.INSTANCE.getAgents(instance)",
            ClassName.get(List.class), TypeClass.AgentInjection)
        .addCode("for (Class<?> cls : agentClasses) {\n"
                + " if (stateInfoMap.get(lifecycleOwner.getClass()) != null){\n"
                + "        $T.INSTANCE.inject(\n"
                + "            instance,\n"
                + "            stateInfoMap.get(lifecycleOwner.getClass()).getAgent(lifecycleOwner)\n"
                + "        );\n"
                + "      }\n"
                + " if (attachBinderMap.get(lifecycleOwner.getClass()) != null){\n"
                + "    ShadowStateAgent[] agents = attachBinderMap.get(lifecycleOwner.getClass()).getAgent(lifecycleOwner);\n"
                + "    if (agents != null){\n"
                + "         for (int i = 0; i < agents.length; i++) {\n"
                + "           if (agents[i].getClass() == cls) {\n"
                + "             AgentInjection.INSTANCE.inject(instance, agents[i]);\n"
                + "           }\n"
                + "         }\n"
                + "    }\n"
                + " }\n"
                + "      if (scopeBinderMap.get(lifecycleOwner.getClass()) != null) {\n"
                + "        ShadowStateAgent[] agents =\n"
                + "            scopeBinderMap.get(lifecycleOwner.getClass()).getAgent(lifecycleOwner);\n"
                + "        if (agents != null) {\n"
                + "          for (int i = 0; i < agents.length; i++) {\n"
                + "            if (agents[i].getClass() == cls) {\n"
                + "              AgentInjection.INSTANCE.inject(instance, agents[i]);\n"
                + "            }\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "   for (Map.Entry<Class<?>, ShareBinder> entry : shareBinderMap.entrySet()) {\n"
                + "     ShareBinder shareBinder = entry.getValue();\n"
                + "        $T agents = shareBinder.getAgent(lifecycleOwner);\n"
                + "        if(agents == null) continue;\n"
                + "        for (int i = 0; i < agents.length; i++) {\n"
                + "          if (agents[i].getClass() == cls) {\n"
                + "            AgentInjection.INSTANCE.inject(instance, agents[i]);\n"
                + "          }\n"
                + "        }\n"
                + "   }\n"
                + " }\n",
            TypeClass.AgentInjection,
            ArrayTypeName.of(TypeClass.ShadowStateAgent))
        .build();

    // getBinder method
    MethodSpec getBinder = MethodSpec.methodBuilder("getBinder")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(TypeClass.StateBinder)
        .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
        .addCode("if (stateInfoMap.get(lifecycleOwner.getClass()) == null) {\n" +
            "  return null;\n" +
            "}\n")
        .addStatement("return stateInfoMap.get(lifecycleOwner.getClass())")
        .build();

    TypeSpec generateClass = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(TypeClass.StateManager)
        .addMethod(constructorBuilder.build())
        .addMethod(bind)
        .addMethod(rebind)
        .addMethod(remove)
        .addMethod(detach)
        .addMethod(injectDispatcher)
        .addMethod(getBinder)
        .addField(stateMapParam)
        .addField(attachBinderMap)
        .addField(shareBinderMap)
        .addField(scopeBinderMap)
        .build();
    return JavaFile.builder(packageName, generateClass)
        .build();
  }
}
