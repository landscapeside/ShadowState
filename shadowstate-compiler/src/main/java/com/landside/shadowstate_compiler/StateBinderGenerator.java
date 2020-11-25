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
import java.util.HashMap;
import java.util.Map;
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
    TypeMirror agentCls = null;
    try {
      mClassElement.getAnnotation(BindState.class).agent();
    } catch (MirroredTypeException e) {
      agentCls = e.getTypeMirror();
    }

    TypeName agentsType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        TypeClass.LifecycleOwner,
        TypeClass.StateAgent);

    FieldSpec agents = FieldSpec
        .builder(agentsType, "agents")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();

    FieldSpec _stateCls = FieldSpec
        .builder(ClassName.get(Class.class), "stateCls")
        .build();
    FieldSpec _agentCls = FieldSpec
        .builder(ClassName.get(Class.class), "agentCls")
        .build();

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(Class.class), "stateCls")
        .addParameter(ClassName.get(Class.class), "agentCls")
        .addStatement("this.stateCls = stateCls")
        .addStatement("this.agentCls = agentCls")
        .build();

    MethodSpec getAgentCls = MethodSpec.methodBuilder("getAgentCls")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(ClassName.get(Class.class))
        .addStatement("return agentCls")
        .build();

    MethodSpec getAgent = MethodSpec.methodBuilder("getAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .returns(TypeClass.StateAgent)
        .addStatement("return agents.get(owner)")
        .build();

    MethodSpec remove = MethodSpec.methodBuilder("remove")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("agents.remove(owner)")
        .build();

    MethodSpec observe = MethodSpec.methodBuilder("observe")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("try {\n"
                + " StateAgent agent = (StateAgent) agentCls.newInstance();\n"
                + " agent.stateCls = stateCls;\n"
                + " agents.put(owner,agent);\n"
                + " if (owner instanceof $T) {\n"
                + "  agent.getLiveData().setValue(($T) agent.initState((($T) owner).getIntent().getExtras()));\n"
                + " } else if (owner instanceof $T) {\n"
                + "  agent.getLiveData().setValue(($T) agent.initState((($T) owner).getArguments()));\n"
                + " } else {\n"
                + "  throw new IllegalArgumentException(\"\");\n"
                + " }\n"
                + " agent.getLiveData().observe(owner, agent);\n"
                + " agent.bindView(($T) owner);\n"
                + " agent.init();\n"
                + "} catch (IllegalAccessException e) {\n"
                + " e.printStackTrace();\n"
                + "} catch (InstantiationException e) {\n"
                + " e.printStackTrace();\n"
                + "}",
            TypeClass.FragmentActivity,
            ClassName.get(stateCls),
            TypeClass.FragmentActivity,
            TypeClass.Fragment,
            ClassName.get(stateCls),
            TypeClass.Fragment,
            ClassName.get((TypeElement) mClassElement))
        .build();

    MethodSpec reset = MethodSpec.methodBuilder("reset")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("StateAgent agent = agents.get(owner);\n"
                + "if (owner instanceof $T) {\n"
                + " agent.getLiveData()\n"
                + "   .setValue(($T) agent.initState((($T) owner).getIntent().getExtras()));\n"
                + "} else if (owner instanceof $T) {\n"
                + " agent.getLiveData().setValue(($T) agent.initState((($T) owner).getArguments()));\n"
                + "} else {\n"
                + " throw new IllegalArgumentException(\"\");\n"
                + "}\n",
            TypeClass.FragmentActivity,
            ClassName.get(stateCls),
            TypeClass.FragmentActivity,
            TypeClass.Fragment,
            ClassName.get(stateCls),
            TypeClass.Fragment
        )
        .build();
    String clsName = ClassName.get((TypeElement) mClassElement).simpleName();
    TypeSpec generateClass = TypeSpec
        .classBuilder(clsName + "StateBinder")
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(TypeClass.StateBinder)
        .addMethod(constructor)
        .addMethod(getAgentCls)
        .addMethod(getAgent)
        .addMethod(remove)
        .addMethod(observe)
        .addMethod(reset)
        .addField(agents)
        .addField(_stateCls)
        .addField(_agentCls)
        .build();
    return JavaFile.builder(
        ClassName.get((TypeElement) mClassElement).packageName(),
        generateClass
    ).build();
  }
}
