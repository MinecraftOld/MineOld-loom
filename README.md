# MineOld-loom
我的世界1.3.1模组加载器
```Gradle
pluginManagement {
    buildscript {
        repositories {
            maven {
                url 'https://minecraftold.github.io/maven/'
            }
            mavenCentral()
        }

        dependencies {
            classpath('cn.frish2021:MineOld-loom:1.0.2')
        }
    }
}
```
来导入Gradle插件
最后使用
```Gradle
apply plugin: 'MINEOLD'
```
来使用插件
