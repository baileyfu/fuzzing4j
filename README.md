# fuzzing4j
A common fuzzing4j facade for java

###一.概述
模糊测试是一种软件测试技术。其核心思想是将自动或半自动生成的随机数据输入到一个程序中， 并监视程序异常，如崩溃，断言失败，以发现可能的程序错误，比如业务逻辑异常或内存泄漏。

###二.模糊测试的作用
1) 通过输入大样本参数提高放方法的业务场景覆盖面，可提前发现业务逻辑异常；
2) 可检测出不易排查的系统级错误；
###三.Fuzzing4j
基于JQF，提供了更便利的使用方式，并将结果记录到Excel文件。

相关注解及API介绍
####1.@Fuzz
加在类和方法上；标注类和方法为模糊测试用例。

**测试用例类名必须以Fuzz结尾。**

示例：
~~~~
@Fuzz
public class SomeClassFuzz{
    @Fuzz(times=10)
    public void fuzzingMethod(String param){
    ...
    }
}
~~~~
####2.@Inject
加载测试用例类的属性上；标注该属性的值将由外部创建后注入；配合Configurator接口使用。

示例：
~~~~
@Fuzz
public class SomeClassFuzz{
    @Inject(cfg = SpringConfigurator.class , value = "applicationContext")
    public ApplicationContext applicationContext;
}
~~~~

####3.Configurator接口
该接口用来定义全局资源。防止在模糊测试过程中频繁创建资源。

**其实现类必须以Configurator结尾。**

示例：

~~~~
public class SpringConfigurator implements Configurator{
    public ApplicationContext applicationContext;
    @Override
    public void init(){
        applicationContext = new SpringApplicationBuilder(XxxBootStarter.class).application().run();
    }
    @Override
    public Object lookup(){
        return applicationContext;
    }
    @Override
    public Object lookup(String name){
        return "applicationContext".equals(name)
                            ?applicationContext
                            :null;
    }
}
~~~~

###四.运行
####1.IDE中运行
test目录下建立任意类，添加main方法：
~~~~
public class FuzzRunner{
    public static void main(String[] args){
        args=new String[]{
            "-q=true",//静默执行
            "-aoc=false",//用例出错是否中断
            "-d=PT30s"//全局参数-用例运行时间
        }
        String tcp=FuzzRunner.class.getResource("/").getPath();
        int exitCode=new CommandLine(new FuzzingCLI(tcp)).execute(args);
        System.exit(exitCode);
    }
}
~~~~
####2.Maven方式
命令行跳转到项目根目录下，执行：
~~~~
mvn fuzzing4j:fuzz
~~~~