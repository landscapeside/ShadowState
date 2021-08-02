package com.landside.shadowstate_compiler;

import com.landside.shadowstate_annotation.AttachState;
import com.landside.shadowstate_annotation.BindState;
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
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public class AttachBinderGenerator {
  private Element mClassElement;

  public AttachBinderGenerator(Element element) {
    this.mClassElement = element;
  }

  public JavaFile generate() throws Exception {
    List<? extends TypeMirror> stateCls = new ArrayList<>(),agentCls = new ArrayList<>();
    try {
      mClassElement.getAnnotation(AttachState.class).states();
    } catch (MirroredTypesException e) {
      stateCls = e.getTypeMirrors();
    }
    try {
      mClassElement.getAnnotation(AttachState.class).agents();
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
                + "      for (int i = 0;i<agentCls.length;i++){\n"
                + "        ShadowStateAgent agent = (ShadowStateAgent) agentCls[i].newInstance();\n"
                + "        agent.stateCls = stateCls[i];\n"
                + "        if (owner instanceof $T) {\n"
                + "          Map<$T, $T<?>> liveDatas = $T.INSTANCE.getAttachStates().get(owner);\n"
                + "          if (liveDatas == null || liveDatas.get(stateCls[i]) == null) {\n"
                + "            ShadowState.INSTANCE.setupAttach(owner,stateCls[i],(($T)agent).initState(((FragmentActivity) owner).getIntent().getExtras()));\n"
                + "          }\n"
                + "          agent.setLiveData(ShadowState.INSTANCE.getAttachStates().get(owner).get(stateCls[i]));\n"
                + "        } else if (owner instanceof $T) {\n"
                + "          if ($T.INSTANCE.isAnnotationPresent(((Fragment) owner).getActivity(),\n"
                + "              $T.class)){\n"
                + "            Map<Type, MutableLiveData<?>> liveDatas = ShadowState.INSTANCE.getAttachStates().get(((Fragment) owner).getActivity());\n"
                + "            if (liveDatas != null && liveDatas.get(stateCls[i]) != null) {\n"
                + "              agent.setLiveData(liveDatas.get(stateCls[i]));\n"
                + "            } else {\n"
                + "              continue;\n"
                + "            }\n"
                + "          }else{\n"
                + "            throw new IllegalArgumentException(\"need the FragmentActivity which current Fragment attached been annotated by AttachState\");\n"
                + "          }\n"
                + "        } else {\n"
                + "          throw new IllegalArgumentException(\"only support FragmentActivity or Fragment\");\n"
                + "        }\n"
                + "        agent.bindView(($T) owner);\n"
                + "        agent.init();\n"
                + "        agentList.add(agent);\n"
                + "      }\n"
                + "      agents.put(owner,agentList.toArray(new ShadowStateAgent[0]));\n"
                + "    } catch (IllegalAccessException | InstantiationException e) {\n"
                + "      e.printStackTrace();\n"
                + "    }",
            shadowStateAgentList,
            ClassName.get(ArrayList.class),
            TypeClass.FragmentActivity,
            ClassName.get(Type.class),
            TypeClass.MutableLiveData,
            TypeClass.ShadowState,
            TypeClass.AttachAgent,
            TypeClass.Fragment,
            TypeClass.AnnotationHelper,
            ClassName.get(AttachState.class),
            ClassName.get((TypeElement) mClassElement))
        .build();

    MethodSpec reset = MethodSpec.methodBuilder("reset")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addStatement("if (owner instanceof $T) {\n"
                + "      ShadowStateAgent[] agentList = agents.get(owner);\n"
                + "      for (int i = 0;i<agentCls.length;i++){\n"
                + "        agentList[i].getLiveData().setValue((($T)agentList[i]).initState(((FragmentActivity) owner).getIntent().getExtras()));\n"
                + "      }\n"
                + "    }",
            TypeClass.FragmentActivity,
            TypeClass.AttachAgent
        )
        .build();
    String clsName = ClassName.get((TypeElement) mClassElement).simpleName();
    TypeSpec generateClass = TypeSpec
        .classBuilder(clsName + "AttachBinder")
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(TypeClass.AttachBinder)
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
