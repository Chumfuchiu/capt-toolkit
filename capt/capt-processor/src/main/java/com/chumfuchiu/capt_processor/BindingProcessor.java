package com.chumfuchiu.capt_processor;

import com.chumfuchiu.capt_annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BindingProcessor extends AbstractProcessor {
    private static final String TAG = BindingProcessor.class.getSimpleName();
    Elements mElementsUtils;
    Messager mMessager;
    HashMap<String, BindingClassCreator> creatorHashMap = new HashMap<>();

    /**
     * @param processingEnv 提供很多有用的工具类Elements, Types 和 Filer
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementsUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    /**
     * 指定该“注解处理器”是用于处理哪个注解的
     *
     * @return BindingProcessor支持处理的注解的集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BindView.class.getCanonicalName());
        return supportTypes;
    }

    /**
     * 指定Java版本,支持Java0-Java8(SourceVersion的源码还挺有意思)
     *
     * @return 这里选择与gradle保持一致，Java7
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 可以在这里写扫描、评估和处理注解的代码，生成Java文件
     *
     * @param annotations 请求处理的注解类型
     * @param roundEnv 有关当前和以前的信息环境
     * @return 如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, TAG + "Start>>>>>");
        System.out.println(TAG + "Start>>>>>");
        long sTime = System.nanoTime();
        //1.注解解析
//        {
        //getElementsAnnotatedWith(BindView):返回被BindView修饰的所有element
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element el : elements) {
            VariableElement variableElement = (VariableElement) el;
            //VariableElement的EnclosingElement是类，这里是获取类Element
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            String classFullName = classElement.getQualifiedName().toString();
            System.out.println(TAG+classFullName);
            //一个classElement对应一个BindingClassCreator
            BindingClassCreator classCreator = creatorHashMap.get(classFullName);
            if (classCreator == null) {
                classCreator = new BindingClassCreator(mElementsUtils.getPackageOf(classElement), classElement);
                creatorHashMap.put(classFullName, classCreator);
            }
            //获取当前el的BindView注解及其属性，存入BindingClassCreator中
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            int id = bindAnnotation.resId();
            classCreator.putElement(id, variableElement);
        }
//        }
        //2.通过Javapoet生成Java类
//        {
        for (Map.Entry<String, BindingClassCreator> entry : creatorHashMap.entrySet()) {
            JavaFile javaFile = JavaFile.builder(entry.getValue().classPackageName(),
                    entry.getValue().typeSpec()).build();
            try {
                // AbstractProcessor.processingEnv
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        }
        long total = System.nanoTime() - sTime;
        mMessager.printMessage(Diagnostic.Kind.NOTE, TAG + "end <<<<< total Time:" + total);
        System.out.println(TAG + "end <<<<< total Time:" + total);
        return true;
    }
}
