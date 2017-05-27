package org.jclass.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by longly on 2017/5/27.
 */
public final class ClassUtil {

    public static ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    private static Class<?> loadClass(String classFullName, boolean isInitialed, ClassLoader classLoader){
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classFullName, isInitialed, classLoader);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return clazz;
    }

    public static Set<Class<?>> getClassSet(String packageName){
        Set<Class<?>> classSet = new HashSet<>();
        ClassLoader classLoader = getClassLoader();
        try {
            System.out.println(classLoader.getResource("").getPath());
            Enumeration<URL> urls = classLoader.getResources(packageName.replace(".", "/"));
            if (urls.hasMoreElements()){
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)){
                    //得到包的路径
                    String packagePath = url.getPath().replaceAll("%20", " ");   //中文空格处理
                    addClass(classSet, packagePath, packageName);
                } else if ("jar".equals(protocol)){
                    System.out.println("32132132");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName){
        File[] files = new File(packagePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
        });
        for (File file : files){
            System.out.println(file.getName());
            String fileName = file.getName();
            if (file.isFile()){
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                className = packageName + "." + className;
                doAddClass(classSet, className);
            } else if (file.isDirectory()){
                String subPackagePath = fileName;
                subPackagePath = packagePath + "/" + subPackagePath;
                String subPackageName = fileName;
                subPackageName = packageName + "." + subPackageName;
                addClass(classSet, subPackagePath, subPackageName);
            }
        }
//      System.out.println(classSet.toString());
    }

    private static void doAddClass(Set<Class<?>> classSet, String classFullName){
        Class<?> clazz = loadClass(classFullName, false, getClassLoader());
        classSet.add(clazz);
    }
}
