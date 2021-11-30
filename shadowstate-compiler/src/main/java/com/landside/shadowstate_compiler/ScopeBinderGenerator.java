package com.landside.shadowstate_compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.landside.shadowstate_annotation.AttachState;
import com.landside.shadowstate_annotation.ScopeState;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

public class ScopeBinderGenerator {
  private Element mClassElement;

  public ScopeBinderGenerator(Element element) {
    this.mClassElement = element;
  }

  public JavaFile generate() throws Exception {
    List<? extends TypeMirror> stateCls = new ArrayList<>(), agentCls = new ArrayList<>();
    try {
      mClassElement.getAnnotation(ScopeState.class).states();
    } catch (MirroredTypesException e) {
      stateCls = e.getTypeMirrors();
    }
    try {
      mClassElement.getAnnotation(ScopeState.class).agents();
    } catch (MirroredTypesException e) {
      agentCls = e.getTypeMirrors();
    }
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
        .addStatement("try {\n"
                + "      $T agentList = new $T();\n"
                + "      for (int i = 0; i < agentCls.length; i++) {\n"
                + "        ShadowStateAgent agent = (ShadowStateAgent) agentCls[i].newInstance();\n"
                + "        agent.stateCls = stateCls[i];\n"
                + "        $T liveData = $T.INSTANCE.getScopeDatas().get(stateCls[i]);\n"
                + "        if (liveData == null) {\n"
                + "          ShadowState.INSTANCE.setupScope(stateCls[i],\n"
                + "              (($T) agent).initState((($T) owner).getIntent().getExtras()));\n"
                + "        }\n"
                + "        agent.setLiveData(ShadowState.INSTANCE.getScopeDatas().get(stateCls[i]));\n"
                + "        agent.bindView(($T) owner);\n"
                + "        agent.init();\n"
                + "        agentList.add(agent);\n"
                + "        ShadowState.INSTANCE.appendScopedLifecycleOwner(stateCls[i], owner);\n"
                + "      }\n"
                + "      agents.put(owner, agentList.toArray(new ShadowStateAgent[0]));\n"
                + "    } catch (IllegalAccessException | InstantiationException e) {\n"
                + "      e.printStackTrace();\n"
                + "    }",
            shadowStateAgentList,
            ClassName.get(ArrayList.class),
            TypeClass.MutableLiveData,
            TypeClass.ShadowState,
            TypeClass.ScopeAgent,
            TypeClass.FragmentActivity,
            ClassName.get((TypeElement) mClassElement)
            )
        .build();

    MethodSpec reset = MethodSpec.methodBuilder("reset")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("    ShadowStateAgent[] agentList = agents.get(owner);\n"
                +
                "    for (int i = 0;i<agentList.length;i++){\n"
                +
                "      if (owner instanceof $T) {\n"
                +
                "        agentList[i].getLiveData()\n"
                +
                "                .setValue((($T)agentList[i]).initState(((FragmentActivity) owner).getIntent().getExtras()));\n"
                +
                "      } else if (owner instanceof $T) {\n"
                +
                "        agentList[i].getLiveData().setValue(((ScopeAgent)agentList[i]).initState(((Fragment) owner).getArguments()));\n"
                +
                "      } else {\n"
                +
                "        throw new IllegalArgumentException(\"\");\n"
                +
                "      }\n"
                +
                "    }",
            TypeClass.FragmentActivity,
            TypeClass.ScopeAgent,
            TypeClass.Fragment
        )
        .build();
    String clsName = ClassName.get((TypeElement) mClassElement).simpleName();
    TypeSpec generateClass = TypeSpec
        .classBuilder(clsName + "ScopeBinder")
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(TypeClass.ScopeBinder)
        .addMethod(constructor)
        .addMethod(getStateCls)
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
