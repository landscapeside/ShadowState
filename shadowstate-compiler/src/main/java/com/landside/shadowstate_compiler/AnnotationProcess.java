package com.landside.shadowstate_compiler;

import com.google.auto.service.AutoService;
import com.landside.shadowstate_annotation.BindAgent;
import com.landside.shadowstate_annotation.BindState;
import com.landside.shadowstate_annotation.InjectAgent;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class AnnotationProcess extends AbstractProcessor {
    private String TAG = "ShadowState: ";

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindState.class.getCanonicalName());
        types.add(BindAgent.class.getCanonicalName());
        types.add(InjectAgent.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    Set<? extends Element> bindStates = null;
    Set<? extends Element> bindAgents = null;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> tmpBindStates = roundEnvironment.getElementsAnnotatedWith(BindState.class);
        if (bindStates == null || bindStates.isEmpty()) {
            bindStates = tmpBindStates;
        }
        Set<? extends Element> tmpBindAgents = roundEnvironment.getElementsAnnotatedWith(BindAgent.class);
        if (bindAgents == null || bindAgents.isEmpty()) {
            bindAgents = tmpBindAgents;
        }
        if (roundEnvironment.processingOver()) {
            for (Element bindState : bindStates) {
                StateBinderGenerator stateBinderGenerator = new StateBinderGenerator(
                        bindState
                );
                try {
                    stateBinderGenerator.generate().writeTo(filer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            StateManagerGenerator managerGenerator = new StateManagerGenerator(
                    bindStates, bindAgents
            );
            try {
                managerGenerator.generate().writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void printError(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, TAG + message);
    }

    private void printWaring(String waring) {
        messager.printMessage(Diagnostic.Kind.WARNING, TAG + waring);
    }
}