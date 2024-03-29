package com.landside.shadowstate_compiler;

import com.google.auto.service.AutoService;
import com.landside.shadowstate_annotation.AttachState;
import com.landside.shadowstate_annotation.BindState;
import com.landside.shadowstate_annotation.InjectAgent;
import com.landside.shadowstate_annotation.ScopeState;
import com.landside.shadowstate_annotation.ShareState;
import com.landside.shadowstate_annotation.StateManagerProvider;
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

  private String className = "";
  private String packageName = "";

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
    types.add(AttachState.class.getCanonicalName());
    types.add(ShareState.class.getCanonicalName());
    types.add(ScopeState.class.getCanonicalName());
    types.add(InjectAgent.class.getCanonicalName());
    types.add(StateManagerProvider.class.getCanonicalName());
    return types;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  Set<? extends Element> bindStates = null;
  Set<? extends Element> attachStates = null;
  Set<? extends Element> shareStates = null;
  Set<? extends Element> scopeStates = null;

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    obtainStateManagerInfo(roundEnvironment.getElementsAnnotatedWith(StateManagerProvider.class));

    Set<? extends Element> tmpBindStates =
        roundEnvironment.getElementsAnnotatedWith(BindState.class);
    if (bindStates == null || bindStates.isEmpty()) {
      bindStates = tmpBindStates;
    }
    Set<? extends Element> tmpAttachStates =
        roundEnvironment.getElementsAnnotatedWith(AttachState.class);
    if (attachStates == null || attachStates.isEmpty()) {
      attachStates = tmpAttachStates;
    }
    Set<? extends Element> tmpShareStates =
        roundEnvironment.getElementsAnnotatedWith(ShareState.class);
    if (shareStates == null || shareStates.isEmpty()) {
      shareStates = tmpShareStates;
    }
    Set<? extends Element> tmpScopeStates =
            roundEnvironment.getElementsAnnotatedWith(ScopeState.class);
    if (scopeStates == null || scopeStates.isEmpty()) {
      scopeStates = tmpScopeStates;
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
      for (Element attachState : attachStates) {
        AttachBinderGenerator attachBinderGenerator = new AttachBinderGenerator(
            attachState
        );
        try {
          attachBinderGenerator.generate().writeTo(filer);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      for (Element shareState : shareStates) {
        ShareBinderGenerator stateBinderGenerator = new ShareBinderGenerator(
            shareState
        );
        try {
          stateBinderGenerator.generate().writeTo(filer);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      for (Element scopeState : scopeStates) {
        ScopeBinderGenerator scopeBinderGenerator = new ScopeBinderGenerator(
                scopeState
        );
        try {
          scopeBinderGenerator.generate().writeTo(filer);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (packageName.isEmpty() || className.isEmpty()) {
        printError(
            "You need to add a class that is annotated by @StateManagerProvider to your module!");
        return true;
      }
      StateManagerGenerator managerGenerator = new StateManagerGenerator(
          packageName, className, bindStates,attachStates, shareStates,scopeStates
      );
      try {
        managerGenerator.generate().writeTo(filer);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private void obtainStateManagerInfo(Set<? extends Element> routers) {
    for (Element element : routers) {
      packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
      className = element.getSimpleName().toString() + "StateManager";
    }
  }

  private void printError(String message) {
    messager.printMessage(Diagnostic.Kind.ERROR, TAG + message);
  }

  private void printWaring(String waring) {
    messager.printMessage(Diagnostic.Kind.WARNING, TAG + waring);
  }
}
