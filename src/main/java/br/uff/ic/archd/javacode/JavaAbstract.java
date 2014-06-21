/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.javacode;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class JavaAbstract extends JavaData{

    
    private String path;
    private JavaPackage javaPackage;
    private List<JavaAbstract> classesImports;
    private List<String> externalImports;

    JavaAbstract(String path) {
        this.path = path;
        classesImports = new LinkedList();
        externalImports = new LinkedList();
    }

   

    public String getFullQualifiedName() {
        return javaPackage.getName() + "." + getName();
    }



    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    

    /**
     * @return the javaPackage
     */
    public JavaPackage getJavaPackage() {
        return javaPackage;
    }

    /**
     * @param javaPackage the javaPackage to set
     */
    public void setJavaPackage(JavaPackage javaPackage) {
        this.javaPackage = javaPackage;
    }

    public void addImportClasses(List<JavaAbstract> classes) {
        getClassesImports().addAll(classes);
    }
    
    public JavaAbstract getJavaAbstractImportByName(String javaAbstractName) {
        JavaAbstract javaAbstract = null;
        String javaAbstractName2 = javaAbstractName.contains(".") ? javaAbstractName : "."+javaAbstractName;
        for (JavaAbstract javaAbstractImport : javaPackage.getClasses()) {
            //System.out.println("ClassName: "+javaAbstractImport.getFullQualifiedName());
            if (javaAbstractImport.getFullQualifiedName().endsWith(javaAbstractName2)) {
                javaAbstract = javaAbstractImport;
                break;
            }
        }
        if (javaAbstract == null) {
            for (JavaAbstract javaAbstractImport : getClassesImports()) {
                //System.out.println("ClassName in: "+javaAbstractImport.getFullQualifiedName());
                if (javaAbstractImport.getFullQualifiedName().endsWith(javaAbstractName2)) {
                    javaAbstract = javaAbstractImport;
                    break;
                }
            }
        }
        
        return javaAbstract;
    }
    
    public void addExternalImport(String externalImport){
        externalImports.add(externalImport);
    }
    
    public String getExternalImportByLastName(String name){
        String externalImportFullName = name;
        String javaAbstractName2 = name.contains(".") ? name : "."+name;
        for(String externalImport : externalImports){
            if(externalImport.endsWith(javaAbstractName2)){
                externalImportFullName = externalImport;
                break;
            }
        }
        return externalImportFullName;
    }

    /**
     * @return the classesImports
     */
    public List<JavaAbstract> getClassesImports() {
        return classesImports;
    }
    
    
}
