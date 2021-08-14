package fuzzing4j.core.util;

import org.objectweb.asm.Opcodes;

import java.time.Duration;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:08
 * @description
 */
public class Constants {
    /**
     * 是否静默执行
     */
    public static final String ENV_CORE_QUIET = "fuzzing4j.core.quiet";
    /**
     * 单个方法出错中断整个进程
     */
    public static final String ENV_CORE_ABORT_ON_CRUSH = "fuzzing4j.core.abortOnCrush";
    public static final String ENV_CORE_DURATION = "fuzzing4j.core.duration";
    public static final String ENV_CORE_TIMES = "fuzzing4j.core.times";

    public static final int VAR_ASM_API = Opcodes.ASM8;

    public static final boolean VAR_QUIET = true;
    public static final boolean VAR_ABORT_ON_CRUSH = true;
    public static final Duration VAR_DURATION = Duration.parse("PT60s");

}
