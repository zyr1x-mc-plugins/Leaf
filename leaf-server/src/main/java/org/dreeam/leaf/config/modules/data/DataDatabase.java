package org.dreeam.leaf.config.modules.data;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

import java.util.List;

public class DataDatabase extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.DATA.getBaseKeyName() + ".database";
    }

    public static boolean enabled = false;
    public static String dataDatabaseAddress = "127.0.0.1:3306";
    public static String dataDatabaseUser = "ware_st";
    public static String dataDatabasePassword = "your_password";
    public static String dataDatabaseDatabase = "your_database";
    public static List<String> dataDatabaseParameters = List.of("useServerPrepStmts");

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);

        if (enabled) {
            dataDatabaseAddress = config.getString(getBasePath() + ".address", dataDatabaseAddress);
            dataDatabaseUser = config.getString(getBasePath() + ".user", dataDatabaseUser);
            dataDatabasePassword = config.getString(getBasePath() + ".password", dataDatabasePassword);
            dataDatabaseDatabase = config.getString(getBasePath() + ".database", dataDatabaseDatabase);
            dataDatabaseParameters = config.getList(getBasePath() + ".parameters", dataDatabaseParameters);
        }
    }
}
