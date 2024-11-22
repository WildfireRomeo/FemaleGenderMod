package com.wildfire.main.config;

public class GlobalConfig extends AbstractConfiguration {
    public static final GlobalConfig INSTANCE = new GlobalConfig();

    private GlobalConfig() {
        super(".", "wildfire_gender");
    }

    public static final BooleanConfigKey FIRST_TIME_LOAD = new BooleanConfigKey("firstTimeLoad", true);
    public static final BooleanConfigKey CLOUD_SYNC_ENABLED = new BooleanConfigKey("cloud_sync", false);
    public static final BooleanConfigKey AUTOMATIC_CLOUD_SYNC = new BooleanConfigKey("sync_player_data", false);
    // see CloudSync#DEFAULT_CLOUD_URL for the actual default
    public static final StringConfigKey CLOUD_SERVER = new StringConfigKey("cloud_server", "");

    static {
        INSTANCE.setDefault(FIRST_TIME_LOAD);
        INSTANCE.setDefault(CLOUD_SYNC_ENABLED);
        INSTANCE.setDefault(AUTOMATIC_CLOUD_SYNC);
        INSTANCE.setDefault(CLOUD_SERVER);
        if(!INSTANCE.exists()) {
            INSTANCE.save();
        }
    }
}
