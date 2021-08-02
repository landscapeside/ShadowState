package com.landside.shadowstate_compiler;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ShareBinderGenerator {
  private Element mClassElement;

  public ShareBinderGenerator(Element element) {
    this.mClassElement = element;
  }

  public JavaFile generate() throws Exception {
    TypeName shadowAgentsType = ArrayTypeName.of(TypeClass.ShadowStateAgent);
    TypeName classesType = ArrayTypeName.of(ClassName.get(Class.class));

    TypeName agentsType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        TypeClass.LifecycleOwner,
        shadowAgentsType);

    FieldSpec agents = FieldSpec
        .builder(agentsType, "agents")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();

    FieldSpec _stateCls = FieldSpec
        .builder(classesType, "stateCls")
        .build();
    FieldSpec _agentCls = FieldSpec
        .builder(classesType, "agentCls")
        .build();

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(classesType, "stateCls")
        .addParameter(classesType, "agentCls")
        .addStatement("this.stateCls = stateCls")
        .addStatement("this.agentCls = agentCls")
        .build();

    MethodSpec getStateCls = MethodSpec.methodBuilder("getStateCls")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(classesType)
        .addStatement("return stateCls")
        .build();

    MethodSpec getAgentCls = MethodSpec.methodBuilder("getAgentCls")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(classesType)
        .addStatement("return agentCls")
        .build();

    MethodSpec getAgent = MethodSpec.methodBuilder("getAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .returns(shadowAgentsType)
        .addStatement("return agents.get(owner)")
        .build();

    MethodSpec remove = MethodSpec.methodBuilder("remove")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("agents.remove(owner)")
        .build();

    TypeName shadowStateAgentList = ParameterizedTypeName.get(
        ClassName.get(List.class),
        TypeClass.ShadowStateAgent
    );

    MethodSpec observe = MethodSpec.methodBuilder("observe")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addCode("try {\n"
                + " $T agentList = new $T();\n"
                + " for (int i = 0;i<agentCls.length;i++){\n"
                + "   ShadowStateAgent agent = (ShadowStateAgent) agentCls[i].newInstance();\n"
                + "   agent.stateCls = stateCls[i];\n"
                + "   agent.setLiveData($T.INSTANCE.getShareStates().get(stateCls[i]));\n"
                + "   agentList.add(agent);\n"
                + "   agent.bindView(($T) owner);\n"
                + "   agent.init();\n"
                + " }\n"
                + " agents.put(owner, agentList.toArray(new ShadowStateAgent[0]));\n"
                + "} catch ($T | $T e) {\n"
                + " e.printStackTrace();\n"
                + "}\n",
            shadowStateAgentList,
            ClassName.get(ArrayList.class),
            TypeClass.ShadowState,
            ClassName.get((TypeElement) mClassElement),
            ClassName.get(IllegalAccessException.class),
            ClassName.get(InstantiationException.class))
        .build();

    String clsName = ClassName.get((TypeElement) mClassElement).simpleName();
    TypeSpec generateClass = TypeSpec
        .classBuilder(clsName + "ShareBinder")
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(TypeClass.ShareBinder)
        .addMethod(constructor)
        .addMethod(getStateCls)
        .addMethod(getAgentCls)
        .addMethod(getAgent)
        .addMethod(remove)
        .addMethod(observe)
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
