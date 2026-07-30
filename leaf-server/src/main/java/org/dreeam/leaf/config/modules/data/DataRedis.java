package org.dreeam.leaf.config.modules.data;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class DataRedis extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.DATA.getBaseKeyName() + ".redis";
    }

    public static boolean enabled = false;
    public static String dataDatabaseAddress = "127.0.0.1:3306";
    public static String dataDatabaseUser = "ware_st";
    public static String dataDatabasePassword = "your_password";

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);

        if (enabled) {
            dataDatabaseAddress = config.getString(getBasePath() + ".address", dataDatabaseAddress);
            dataDatabaseUser = config.getString(getBasePath() + ".user", dataDatabaseUser);
            dataDatabasePassword = config.getString(getBasePath() + ".password", dataDatabasePassword);
        }
    }
}
