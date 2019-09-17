package com.chumfuchiu.capt_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class BindingClassCreator {
    //源类 TypeElement
    private TypeElement mTypeElement;
    //包名
    private String mPackageName;
    //生成的类名SimpleName
    private String mTargetClassName;

    private HashMap<Integer, VariableElement> resIdMap = new HashMap<>();

    public BindingClassCreator(PackageElement packageElement, TypeElement typeElement) {
        this.mTypeElement = typeElement;
        this.mPackageName = packageElement.getQualifiedName().toString();
        this.mTargetClassName = mTypeElement.getSimpleName().toString() + "ViewBinding";
    }

    public void putElement(int resId, VariableElement variableElement) {
        resIdMap.put(resId, variableElement);
    }

    public String classPackageName() {
        return mPackageName;
    }

    /**
     * 根据类的SimpleName构建TypeSpec
     *
     * @return
     */
    public TypeSpec typeSpec() {
        TypeSpec clazz = TypeSpec.classBuilder(mTargetClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec())
                .build();
        return clazz;
    }

    public MethodSpec methodSpec() {
        ClassName paramsActivity = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());
        //构建public void bind(ClassName act){};方法
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(paramsActivity, "act");
        //构建方法体
        for (int resId : resIdMap.keySet()) {
            VariableElement variableElement = resIdMap.get(resId);
            String name = variableElement.getSimpleName().toString();
            String type = variableElement.asType().toString();
            methodBuilder.addCode("act." + name + " = " + "(" + type + ")(((android.app.Activity)act).findViewById( " + resId + "));");
        }
        return methodBuilder.build();
    }
}