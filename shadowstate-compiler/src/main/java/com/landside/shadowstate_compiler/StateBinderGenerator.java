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

    TypeName agentsType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        TypeClass.LifecycleOwner,
        TypeClass.StateAgent);

    FieldSpec agents = FieldSpec
        .builder(agentsType, "agents")
        .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
        .build();

    MethodSpec getAgent = MethodSpec.methodBuilder("getAgent")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .returns(TypeClass.StateAgent)
        .addStatement("return agents.get(owner)")
        .build();

    MethodSpec observe = MethodSpec.methodBuilder("observe")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .addParameter(TypeClass.LifecycleOwner, "owner")
        .addParameter(ClassName.get(Class.class), "agentCls")
        .addStatement("try {\n"
            + "      StateAgent agent = (StateAgent) agentCls.newInstance();\n"
            + "      agents.put(owner,agent);\n"
            + "      if (owner instanceof $T) {\n"
            + "        agent.getLiveData().setValue(($T) agent.initState((($T) owner).getIntent().getExtras()));\n"
            + "      } else if (owner instanceof $T) {\n"
            + "        agent.getLiveData().setValue(($T) agent.initState((($T) owner).getArguments()));\n"
            + "      } else {\n"
            + "        throw new IllegalArgumentException(\"\");\n"
            + "      }\n"
            + "      agent.getLiveData().observe(owner, agent);\n"
            + "      agent.bindView(($T) owner);\n"
            + "      agent.init();\n"
            + "    } catch (IllegalAccessException e) {\n"
            + "      e.printStackTrace();\n"
            + "    } catch (InstantiationException e) {\n"
            + "      e.printStackTrace();\n"
            + "    }",
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
        .addParameter(ClassName.get(Class.class), "agentCls")
        .addStatement("    StateAgent agent = agents.get(owner);\n"
            + " if (owner instanceof $T) {\n"
            + "   agent.getLiveData()\n"
            + "     .setValue(($T) agent.initState((($T) owner).getIntent().getExtras()));\n"
            + "} else if (owner instanceof $T) {\n"
            + "   agent.getLiveData().setValue(($T) agent.initState((($T) owner).getArguments()));\n"
            + "} else {\n"
            + "   throw new IllegalArgumentException(\"\");\n"
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
        .addMethod(getAgent)
        .addMethod(observe)
        .addMethod(reset)
        .addField(agents)
        .build();
    return JavaFile.builder(
        ClassName.get((TypeElement) mClassElement).packageName(),
        generateClass
    ).build();
  }
}
