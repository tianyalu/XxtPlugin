package com.sty.xxt.xxtplugin;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class LoadUtils {

    private static final String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/sty/plugin/childplugin-debug.apk";
    public static void loadClass(Context context) {
        // 获取宿主的dexElements -> dexElementsField -> DexPathList对象 -> dexPathList的Field
        // -> BaseDexClassLoader对象 -> 宿主和插件的类加载器

        try {
            //参考：http://androidos.net.cn/android/7.1.1_r28/xref/libcore/dalvik/src/main/java/dalvik/system/DexPathList.java 65行
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            //参考：http://androidos.net.cn/android/7.1.1_r28/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java 50行
            Class<?> classLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = classLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            //1.获取宿主的类加载器
            ClassLoader pathClassLoader = context.getClassLoader();
            Object hostPathList = pathListField.get(pathClassLoader);
            //目的：dexElements的对象
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            //2.插件,类加载器
            //版本：7.0之后parent可以传null (pathClassLoader)
            Log.i("sty", "apkPath: " + apkPath);
            ClassLoader pluginClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(), null, pathClassLoader);
            Object pluginPathList = pathListField.get(pluginClassLoader);
            //目的：dexElements的对象
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            //合并
            //new Elements[];
            Object[] newElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, newElements, 0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, newElements, hostDexElements.length, pluginDexElements.length);

            //赋值到宿主的dexElements
            // hostPathList.dexElements = newElements
            dexElementsField.set(hostPathList, newElements);
        } catch (Exception e) {
            Log.e("sty", "exception");
            e.printStackTrace();
        }

    }
}
