# 360插件化之宿主调用插件方法

[TOC]

## 一、基础概念

### 1.1 插件化

插件化技术最初源于免安装运行`apk`的想法，这个免安装的apk就可以理解为**插件**，而支持插件的`APP`我们一般叫宿主。

### 1.2 插件化解决的问题

* APP的功能模块越来越多，体积越来越大
* 模块之间的耦合度高，协同开发沟通成本越来越大
* 方法数可能超过65535，APP占用的内存过大
* 应用之间的相互调用

### 1.3 插件化与组件化的区别

**组件化**开发就是将一个APP分成多个模块，每个模块都是一个组件，开发的过程中我们可以让这些组件相互依赖或者单独调试部分组件等，但是最终发布的时候是将这些组件合并统一成**一个apk**，这就是组件化开发；

**插件化**开发和组件化略有不同，插件化是将整个APP拆分成多个模块，这些模块包括一个宿主和多个插件，每个模块都是一个apk，最终打包的时候**宿主apk和插件apk分开打包**。

### 1.4 各大插件化对比

|              特性              | Dynamic-load-apk |   DynamicAPK   |     Small      | DroIdPlugin | VirtualAPK |
| :----------------------------: | :--------------: | :------------: | :------------: | :---------: | :--------: |
|              作者              |      任玉刚      |      携程      |    wequick     |     360     |    滴滴    |
|          支持四大组件          |  只支持Activity  | 只支持Activity | 只支持Activity |   全支持    |   全支持   |
| 组件无需在宿主manifest中预注册 |        √         |       ×        |       √        |      √      |     √      |
|        插件可以依赖宿主        |        √         |       √        |       √        |      ×      |     √      |
|       支持PendingIntent        |        ×         |       ×        |       ×        |      √      |     √      |
|        Android特性支持         |      大部分      |     大部分     |     大部分     |  几乎全部   |  几乎全部  |
|           兼容性适配           |       一般       |      一般      |      中等      |     高      |     高     |
|            插件构建            |        无        |    部署aapt    |   Gradle插件   |     无      | Gradle插件 |

### 1.5 插件化实现思路

* 如何加载插件的类？
* 如何启动插件的四大组件？
* 如何加载插件的资源？

#### 1.5.1 类加载流程

类生命周期如下图所示：  

![image](https://github.com/tianyalu/XxtPlugin/raw/master/show/class_lifecycle.png)  

在类加载阶段，虚拟机主要完成三件事：

> 1. 通过一个类的全限定名来获取定义此类的二进制字节流；
> 2. 将这个字节流所代表的静态存储结构转化为方法区域的运行时数据结构；
> 3. 在Java堆中生成一个代表这个类的Class对象，作为方法区域数据的访问入口。

#### 1.5.2 反射

反射就是在运行时才知道要操作的类是什么，并且可以在运行时获取类的完整构造，并调用对应的方法和属性。

* 反射

  ​	java --> .class 编译

  ​	.class --> java 反编译

* 获取类的对象

  ​	类名Class

  ​	对象.getClass()

  ​	Class.forName("全限定名")

  ​	类.getClassLoader().loadClass("全限定名")

  ​	子类.class.getSuperClass()

  ​	包装类.class

* 根据类得到类名（全限定名）

  ​	getName() 全限定名

  ​	getSimpleName() 类名

  ​	getPackage() 包名

* Field类（属性）

  ​	getField("属性名") 获取公共属性

  ​	getName() 属性名

  ​	getModifiers() 修饰符

  ​	getType() 数据类型

  ​	set(对象名, 属性值) = 对象名.set属性名 属性值

  ​	get(对象名) = 对象名.get属性名 属性值

  ​	getDeclaredField("属性名") 获取属性

  ​	setAccessible(true) 设置私有属性能访问

  ​	getDeclaredFields() 所有属性

* Method类（方法）

  ​	getMethod(方法名 参数数据类型(无参传null)) 获取公共方法

  ​	getDeclaredMethod(方法名 参数数据类型(无参传null))  获取私有方法

  ​	invoke(对象名 参数列表) = 对象名.方法名  执行方法

  ​	getParameterTypes()  得到返回参数列表

  ​	getDeclaredMethods()  得到类的所有的方法

  ​	getReturnType() 得到返回值方法的数据类型

* 构造方法

  ​	Class对象.getConstructor() 得到构造方法

  ​	Class对象.getConstructors()  得到所有的构造方法  

#### 1.5.3 `ClassLoader`的继承关系及区别

![image](https://github.com/tianyalu/XxtPlugin/raw/master/show/class_loader_inheritance_relationship.png)  

* **`PathClassLoader` 和 `BootClassLoader` 的区别**

  > `PathClassLoader`：应用的类加载器-->自定义类和第三方依赖类（AppcompatActivity属于第三方依赖类）  `BootClassLoader`：系统（SDK）的类加载器，它是`PathClassLoader`的父类


* **`PathClassLoader` 和 `DexClassLoader` 的区别**  

在8.0（API 26）之前，它们二者的唯一区别是第二个参数`optimizedDirectory`，这个参数的意思是生成的`odex`(优化的`dex`)存放的路径；  

在8.0（API 26）及之后，二者就完全一样了。

```java
//http://androidos.net.cn/android/8.0.0_r4/xref/libcore/dalvik/src/main/java/dalvik/system/PathClassLoader.java
public class PathClassLoader extends BaseDexClassLoader {
    //optimizedDirectory 直接为 null
    public PathClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, null, null, parent);
    }
  	//optimizedDirectory 直接为 null
    public PathClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, null, librarySearchPath, parent);
    }
}

//http://androidos.net.cn/android/8.0.0_r4/xref/libcore/dalvik/src/main/java/dalvik/system/DexClassLoader.java
//该类8.0和7.1相同
public class DexClassLoader extends BaseDexClassLoader {
    public DexClassLoader(String dexPath, String optimizedDirectory,
            String librarySearchPath, ClassLoader parent) {
        //从26（8.0）开始，super里面改变了，参考下面两个构造方法
        super(dexPath, new File(optimizedDirectory), librarySearchPath, parent);
    }
}

//http://androidos.net.cn/android/8.0.0_r4/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
public BaseDexClassLoader(String dexPath, File optimizedDirectory,
                          String librarySearchPath, ClassLoader parent) {
    super(parent);
    //DexPathList 的第四个参数为 optimizedDirectory，可以看到这儿为null
    this.pathList = new DexPathList(this, dexPath, librarySearchPath, null);
    //...
}

//http://androidos.net.cn/android/7.1.1_r28/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
public BaseDexClassLoader(String dexPath, File optimizedDirectory,
                          String librarySearchPath, ClassLoader parent) {
    super(parent);
    this.pathList = new DexPathList(this, dexPath, librarySearchPath, optimizedDirectory);
}
```

#### 1.5.4 类加载的双亲委派机制

类在加载时，首先检测这个类是否已经被加载过了，如果已经加载了，直接获取并返回；如果没有被加载，`parent`不为`null`，则调用`parent`的`loadClass()`方法进行加载，依次递归，如果找到了或者加载了就返回，如果没有找到或者也加载不了，才自己去加载。

双亲委派机制的核心在于**优先加载系统类**，优点如下：

> 1. 避免重复加载，当父加载器已经加载了该类的时候，就没有必要用`ClassLoader`再加载一次；  
> 2. 安全考虑，防止核心API被随意篡改。

由此可见我们是不能加载一个自定义类，来替换系统的类（如`String`类）的。  

类加载的双亲委派机制流程如下图所示：

![image](https://github.com/tianyalu/XxtPlugin/raw/master/show/class_loader_process_parent1.png)  

代码角度：  

![image](https://github.com/tianyalu/XxtPlugin/raw/master/show/class_loader_process_parent2.png)  

参考：[http://androidos.net.cn/android/7.1.1_r28/xref/libcore/ojluni/src/main/java/java/lang/ClassLoader.java]  (http://androidos.net.cn/android/7.1.1_r28/xref/libcore/ojluni/src/main/java/java/lang/ClassLoader.java) 377行

> PathClassLoader 构造方法，dexPath -- dex文件  
> BootCalssLoader(类文件)  SDK类加载器

**问题：为什么`DexClassLoader`的`parent`不传入`BaseDexClassLoader`?**  

跟加载流程有关，我们传入`parent`的目的是为了优化，让它递归查找，从而不重复加载；而系统根本就没有用到`BaseDexClassLoader`去加载过类，所以`parent`传`BaseDexClassLoader`和传`null`是差不多的。

## 二、实现

### 2.1 实现总体思路

插件化用宿主调用插件的方法，从根本上来讲要用到反射，但是插件本质上来讲是个`apk`，而一个应用是不能直接反射获取另一个应用中`dex`文件中的类的，所以核心在于将插件的`dex`合并到宿主的`dex`（`dexElements`数组）中，如此方能通过反射实现方法调用。

![image](https://github.com/tianyalu/XxtPlugin/raw/master/show/theory.png)  

> 插件化一般把宿主放在前面；
> 热修复一般把dex文件放在前面（一个应用）；
> dex文件生成命令：`dx --dex --output=output.dex input.class` （`android/Sdk/build-tools/`目录下）

### 2.2 实现步骤

BaseDexClassLoader.findClass() --> DexPathList.findClass() --> element.findClass()

dexFile 多个dex文件 --> dexElements

**所有的dex文件都在宿主的dexElements数组中**

#### 2.2.1 步骤

**怎么通过反射把插件的dex文件放到宿主的dex文件中？**

1. 获取宿主的`PathClassLoader`类加载器，然后通过反射获取宿主的`dexElements `-> `dexElementsField `-> `DexPathList`对象 -> `dexPathList`的`Field` -> `BaseDexClassLoade`r对象 -> 宿主和插件的类加载器；

2. 创建插件的`DexClassLoader`类加载器，然后通过反射获取插件的`dexElements`；

3. 合并宿主的`dexElements`和插件的`dexElements`，生成新的`Element[]`；

4. 将合并的`Elements[]`赋值到宿主的`dexElements`。

插件的dex

#### 2.2.2 代码

```java
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
```

## 三、Tips

* `Android`源码查看地址：[http://androidos.net.cn/sourcecode](http://androidos.net.cn/sourcecode)  
* `AndroidStudio`可安装插件：`AndroidSourceViewer` 方便查看源码

