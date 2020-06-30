package com.sty.xxt.xxtplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Button btnInvoke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }

        btnInvoke = findViewById(R.id.btn_invoke);
        btnInvoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnInvokeClicked();
//                printClassLoader();
            }
        });
    }

    /**
     * 如果不合并插件dex的话会报如下错误：
     * System.err: java.lang.ClassNotFoundException: com.sty.xxt.childplugin.Test
     */
    private void onBtnInvokeClicked() {
        try {
            Class<?> clazz = Class.forName("com.sty.xxt.childplugin.Test");
            Method print = clazz.getMethod("print");
            print.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 系统类：BootClassLoader
     * 自定义类和第三方依赖类：PathClassLoader (AppcompatActivity属于第三方依赖类）
     */
    private void printClassLoader() {
        ClassLoader classLoader = getClassLoader();
        while(classLoader != null) {
            Log.e("sty", "printClassLoader: " + classLoader);
            //dalvik.system.PathClassLoader(当前类 MainActivity 的类加载器）
            classLoader = classLoader.getParent();
            //java.lang.BootClassLoader@9cf4a4
        }
        Log.e("sty", "Activity printClassLoader: " + Activity.class.getClassLoader());
        //java.lang.BootClassLoader@9cf4a4
        Log.e("sty", "AppCompatActivity printClassLoader: " + AppCompatActivity.class.getClassLoader());
        //dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.sty.xxt.xxtplugin-HJYbcg7DCrmMkePA_SHGrQ==/base.apk"]
    }
}
