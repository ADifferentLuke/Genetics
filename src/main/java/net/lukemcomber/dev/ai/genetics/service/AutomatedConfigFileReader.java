package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.model.AutomatedConfig;
import org.apache.commons.lang3.tuple.Pair;

public class AutomatedConfigFileReader extends LGPStreamReader<AutomatedConfig,AutomatedConfig> {

    public static final String INIT_ENV_FILE="INITIALIZATION_FILE";
    public static final String INIT_ZOO_FILE="ZOO_FILE";
    public static final String TICKS_PER_SECOND="TICKS_PER_SECOND";
    public static final String TOTAL_TICKS="TOTAL_TICKS";

    private AutomatedConfig automatedConfig;
    /**
     * @return
     */
    @Override
    AutomatedConfig initPayload() {
        return new AutomatedConfig();
    }

    /**
     * @param automatedConfig
     * @return
     */
    @Override
    AutomatedConfig getResult(AutomatedConfig automatedConfig) {
        return automatedConfig;
    }

    /**
     * @param line
     * @param automatedConfig
     */
    @Override
    void parse(String line, AutomatedConfig automatedConfig) {
        final Pair<String,String> pair = requireNameValue(line);
        switch(pair.getLeft()){
            case INIT_ENV_FILE:
                automatedConfig.initialEnvironmentFile = pair.getRight();
                break;
            case INIT_ZOO_FILE:
                automatedConfig.initialZooFile = pair.getRight();
                break;
            case TICKS_PER_SECOND:
                automatedConfig.ticksPerSecond = Integer.parseInt(pair.getRight());
                break;
            case TOTAL_TICKS:
                automatedConfig.maxTicksToEnd = Integer.parseInt(pair.getRight());
                break;
        }
    }
}
