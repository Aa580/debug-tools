package io.github.future0923.debug.tools.hotswap.core;

import io.github.future0923.debug.tools.base.config.AgentArgs;
import io.github.future0923.debug.tools.base.constants.ProjectConstants;
import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.base.utils.DebugToolsStringUtils;
import io.github.future0923.debug.tools.hotswap.core.config.PluginManager;
import io.github.future0923.debug.tools.hotswap.core.util.spring.util.StringUtils;
import lombok.Getter;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

/**
 * 热重载Agent逻辑
 *
 * @author future0923
 */
public class HotswapAgent {

    private static final Logger LOGGER = Logger.getLogger(HotswapAgent.class);

    /**
     * 禁用的插件名
     */
    private static final Set<String> disabledPlugins = new HashSet<>();

    /**
     * 是否自动热重载
     */
    @Getter
    private static boolean autoHotswap = false;

    /**
     * 外部配置文件 `debug-tools-agent.properties` 的路径
     */
    private static String propertiesFilePath;

    private static boolean init = false;

    /**
     * 初始化热重载/热部署
     *
     * @param args 参数
     * @param inst Instrumentation
     */
    public static void init(AgentArgs args, Instrumentation inst) {
        if (!init) {
            LOGGER.info("open hot reload unlimited runtime class redefinition.{{}}", ProjectConstants.VERSION);
            parseArgs(args);
            fixJboss7Modules();
            // 初始化插件
            PluginManager.getInstance().init(inst);
            LOGGER.debug("Hotswap agent initialized.");
            init = true;
        }
    }

    /**
     * 参数覆盖
     */
    private static void parseArgs(AgentArgs args) {
        if (args == null) {
            return;
        }
        if (DebugToolsStringUtils.isNotBlank(args.getDisabledPlugins())) {
            disabledPlugins.addAll(StringUtils.commaDelimitedListToSet(args.getDisabledPlugins()));
        }
        propertiesFilePath = args.getPropertiesFilePath();
    }

    /**
     * 获取外部配置文件路径
     */
    public static String getExternalPropertiesFile() {
        return propertiesFilePath;
    }


    /**
     * 插件是否被禁用
     */
    public static boolean isPluginDisabled(String pluginName) {
        return disabledPlugins.contains(pluginName);
    }

    /**
     * 定义允许哪些 Java 包可以从 JBoss 模块系统的类加载器传递到应用程序类加载器中。
     * 它指定了一些系统包可以被 JBoss 的模块化类加载器识别和访问，而不是仅限于默认的类加载范围。
     */
    private static void fixJboss7Modules() {
        String JBOSS_SYSTEM_MODULES_KEY = "jboss.modules.system.pkgs";
        String oldValue = System.getProperty(JBOSS_SYSTEM_MODULES_KEY, null);
        System.setProperty(JBOSS_SYSTEM_MODULES_KEY, oldValue == null ? HOTSWAP_AGENT_EXPORT_PACKAGES : oldValue + "," + HOTSWAP_AGENT_EXPORT_PACKAGES);
    }

    /**
     * 多个逗号分隔的包名
     */
    public static final String HOTSWAP_AGENT_EXPORT_PACKAGES = "io.github.future0923.debug.tools.hotswap";
}
