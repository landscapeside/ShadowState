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
            mClassElement.getAnnotation(BindState.class).value();
        } catch (MirroredTypeException e) {
            stateCls = e.getTypeMirror();
        }
        TypeName stateParamType = ParameterizedTypeName.get(
                TypeClass.MutableLiveData,
                ParameterizedTypeName.get(stateCls));

        FieldSpec stateParam = FieldSpec
                .builder(stateParamType, "state")
                .initializer(CodeBlock.builder().add("new $T<>()", TypeClass.MutableLiveData).build())
                .build();

        MethodSpec observe = MethodSpec.methodBuilder("observe")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addParameter(TypeClass.LifecycleOwner, "owner")
                .addParameter(TypeClass.StateAgent, "agent")
                .addStatement("agent.setStateObservable(state)")
                .addStatement("agent.setView(($T) owner)",ClassName.get((TypeElement) mClassElement))
                .addStatement("if (owner instanceof $T) {\n" +
                        "            state.setValue((TestState) agent.initState((($T) owner).getIntent().getExtras()));\n" +
                        "        } else if (owner instanceof $T) {\n" +
                        "            state.setValue((TestState) agent.initState((($T) owner).getArguments()));\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"\");\n" +
                        "        }",TypeClass.FragmentActivity,TypeClass.FragmentActivity,TypeClass.Fragment,TypeClass.Fragment)
                .addStatement("state.observe(owner, agent)")
                .build();
        String clsName = stateCls.toString().substring(stateCls.toString().lastIndexOf(".")+1);
        TypeSpec generateClass = TypeSpec
                .classBuilder(clsName + "Binder")
                .addModifiers(PUBLIC, FINAL)
                .addSuperinterface(TypeClass.StateBinder)
                .addField(stateParam)
                .addMethod(observe)
                .build();
        return JavaFile.builder(
                ClassName.get((TypeElement) mClassElement).packageName(),
                generateClass
        ).build();
    }
}
