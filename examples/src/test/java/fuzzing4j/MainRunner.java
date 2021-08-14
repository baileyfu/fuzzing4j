package fuzzing4j;

import picocli.CommandLine;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:40
 * @description
 */
public class MainRunner {
    public static void main(String[] args) throws Exception {
        args = new String[]{
                "-q=true",//静默执行
                "-aoc=true",//用例出错是否中断
                "-t=10",//全局参数-执行次数
                "-d=PT10s"//全局参数-执行时间
        };
        String tcp = MainRunner.class.getResource("/").getPath();
        int exitCode = new CommandLine(new FuzzingCLI(tcp)).execute(args);
        System.exit(exitCode);
    }
}
