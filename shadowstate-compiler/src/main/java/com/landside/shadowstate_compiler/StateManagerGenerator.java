package com.landside.shadowstate_compiler;

import com.landside.shadowstate_annotation.BindAgent;
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
    private Set<? extends Element> bindAgents;

    public StateManagerGenerator(
            String packageName,
            String className,
            Set<? extends Element> bindStates,
            Set<? extends Element> bindAgents
    ) {
        this.packageName = packageName;
        this.className = className;
        this.bindStates = bindStates;
        this.bindAgents = bindAgents;
    }

    public JavaFile generate() throws Exception {
        TypeName classWithWildcard = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(Object.class));

        TypeName stateMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                classWithWildcard,
                classWithWildcard);

        FieldSpec stateMapParam = FieldSpec
                .builder(stateMapType, "stateMap")
                .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
                .build();

        TypeName dispatcherMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                classWithWildcard,
                TypeClass.StateAgent
        );

        FieldSpec dispatcherMapParam = FieldSpec
                .builder(dispatcherMapType, "stateAgentMap")
                .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
                .build();

        TypeName binderMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                classWithWildcard,
                TypeClass.StateBinder
        );

        FieldSpec binderMapParam = FieldSpec
                .builder(binderMapType, "stateBinderMap")
                .initializer(CodeBlock.builder().add("new $T<>()", ClassName.get(HashMap.class)).build())
                .build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Element state : bindStates) {
            TypeMirror stateCls = null;
            try {
                state.getAnnotation(BindState.class).value();
            } catch (MirroredTypeException e) {
                stateCls = e.getTypeMirror();
            }
            constructorBuilder.addStatement(
                    "stateMap.put($T.class,$T.class)",
                    ClassName.get((TypeElement) state),
                    ClassName.get(stateCls));
            for (Element agent : bindAgents) {
                if (((TypeElement) agent).getQualifiedName().toString().equals(
                        ((TypeElement) state).getQualifiedName().toString())
                ) {
                    TypeMirror agentCls = null;
                    try {
                        agent.getAnnotation(BindAgent.class).value();
                    } catch (MirroredTypeException e) {
                        agentCls = e.getTypeMirror();
                    }
                    constructorBuilder.addStatement(
                            "stateAgentMap.put($T.class,new $T())",
                            ClassName.get(stateCls),
                            ClassName.get(agentCls));
                    String clsName = stateCls.toString().substring(stateCls.toString().lastIndexOf(".")+1);
                    constructorBuilder.addStatement(
                            "stateBinderMap.put($T.class,new $T())",
                            ClassName.get(stateCls),
                            ClassName.get(
                                    ClassName.get((TypeElement) state).packageName(),
                                    clsName + "Binder"
                            )
                    );
                }
            }
        }

        MethodSpec bind = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addParameter(TypeClass.LifecycleOwner, "lifecycleOwner")
                .addStatement("if (stateMap.get(lifecycleOwner.getClass()) == null) {\n" +
                        "            return;\n" +
                        "        }")
                .addStatement("Class stateCls = stateMap.get(lifecycleOwner.getClass())")
                .addStatement("StateAgent agent = stateAgentMap.get(stateCls)")
                .addStatement("stateBinderMap.get(stateCls).observe(lifecycleOwner, agent)")
                .build();

        MethodSpec injectDispatcher = MethodSpec.methodBuilder("injectAgent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addParameter(Object.class, "instance")
                .addStatement("$T<Class<?>> agentClasses = $T.INSTANCE.getAgents(instance)",
                        ClassName.get(List.class),TypeClass.AgentInjection)
                .addStatement("for (Class<?> cls : agentClasses) {\n" +
                        " for (Map.Entry<Class<?>, StateAgent> entry :\n" +
                        "     stateAgentMap.entrySet()) {\n" +
                        "        if (entry.getValue().getClass() == cls) {\n" +
                        "            $T.INSTANCE.inject(\n" +
                        "             instance,\n" +
                        "             entry.getValue()\n" +
                        "           );\n" +
                        "        }\n" +
                        "     }\n" +
                        " }",TypeClass.AgentInjection)
                .build();

        TypeSpec generateClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(TypeClass.StateManager)
                .addMethod(constructorBuilder.build())
                .addMethod(bind)
                .addMethod(injectDispatcher)
                .addField(stateMapParam)
                .addField(dispatcherMapParam)
                .addField(binderMapParam)
                .build();
        return JavaFile.builder(packageName, generateClass)
                .build();
    }
}
