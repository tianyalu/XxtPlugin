插件化

android源码：

http://androidos.net.cn/sourcecode

1.MainActivity -> 类加载器 PathClassLoader

2.PathClassLoader -> parent , BootClassLoader

应用的类加载器：PathClassLoader

SDK的类加载器：BootClassLoader



双亲委派机制：

1. 查找看这个类是否已经加载过了
2. parent帮忙加载
3. 自己加载

parent -- 委派给父母 --> 双亲委派

PathClassLoader 构造方法，dexPath -- dex文件

BootCalssLoader(类文件) 应用的类吗？



BaseDexClassLoader.findClass() --> DexPathList.findClass() --> element.findClass

dexFile 多个dex文件 --> dexElements

怎么把插件的dex文件放到宿主的dex文件中？

所有的dex文件都在宿主的dexelements数组中

反射

1. 获取宿主的dexElements -> dexElementsField -> DexPathList对象 -> pathList的Field -> BaseDexClassLoader对象 -> 宿主和插件的类加载器

2. 获取插件的dexElements

3. 合并宿主的dexElements和插件的dexElements

4. 将合并的dexElements赋值到宿主的dexElements

插件的dex

![image-20200629202007934](/Users/tian/Library/Application Support/typora-user-images/image-20200629202007934.png)

插件化技术最初源于免安装运行`apk`的想法，这个免安装的apk就可以理解为**插件**，二支持插件的`APP`我们一般叫宿主。

![image-20200629202151608](/Users/tian/Library/Application Support/typora-user-images/image-20200629202151608.png)

![image-20200629202249110](/Users/tian/Library/Application Support/typora-user-images/image-20200629202249110.png)

![image-20200629202500638](/Users/tian/Library/Application Support/typora-user-images/image-20200629202500638.png)

![image-20200629202958227](/Users/tian/Library/Application Support/typora-user-images/image-20200629202958227.png)

![image-20200629203144366](/Users/tian/Library/Application Support/typora-user-images/image-20200629203144366.png)

![image-20200629203657987](/Users/tian/Library/Application Support/typora-user-images/image-20200629203657987.png)

![image-20200629203749949](/Users/tian/Library/Application Support/typora-user-images/image-20200629203749949.png)

![image-20200629203808896](/Users/tian/Library/Application Support/typora-user-images/image-20200629203808896.png)

![image-20200629203842999](/Users/tian/Library/Application Support/typora-user-images/image-20200629203842999.png)

![image-20200629203949905](/Users/tian/Library/Application Support/typora-user-images/image-20200629203949905.png)

![image-20200629205734422](/Users/tian/Library/Application Support/typora-user-images/image-20200629205734422.png)

![image-20200629205816001](/Users/tian/Library/Application Support/typora-user-images/image-20200629205816001.png)

双亲委派机制

![image-20200629210708415](/Users/tian/Library/Application Support/typora-user-images/image-20200629210708415.png)

![image-20200629210750154](/Users/tian/Library/Application Support/typora-user-images/image-20200629210750154.png)

![image-20200629212648477](/Users/tian/Library/Application Support/typora-user-images/image-20200629212648477.png)

![image-20200629221035361](/Users/tian/Library/Application Support/typora-user-images/image-20200629221035361.png)

![image-20200629221050143](/Users/tian/Library/Application Support/typora-user-images/image-20200629221050143.png)

![image-20200629223104896](/Users/tian/Library/Application Support/typora-user-images/image-20200629223104896.png)