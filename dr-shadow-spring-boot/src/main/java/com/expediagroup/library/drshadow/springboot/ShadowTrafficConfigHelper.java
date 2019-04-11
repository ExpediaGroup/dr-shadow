package com.expediagroup.library.drshadow.springboot;

/**
 * Helper class to retrieve the shadow traffic configuration. V1 is based off yaml file configuration.
 *
 * TODO: make this pluggable for various dynamic use cases. The value in Dr Shadow is more on the runtime dynamic configuration.
 */
public class ShadowTrafficConfigHelper {

    private ShadowTrafficConfig shadowTrafficConfig;

    /**
     *
     * @param shadowTrafficConfig
     */
    public ShadowTrafficConfigHelper(ShadowTrafficConfig shadowTrafficConfig) {
        this.shadowTrafficConfig = shadowTrafficConfig;
    }
    
    /**
     * Get the Shadow Traffic Configuration.
     * 
     * @return shadowTrafficConfig
     */
    public ShadowTrafficConfig getConfig() {
        return shadowTrafficConfig;
    }
    
}
