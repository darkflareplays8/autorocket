package com.example.autorocket;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "autorocket")
public class AutoRocketConfig implements ConfigData {

    public boolean enabled = true;
    public int cooldownTicks = 15;
    public boolean restoreOffhand = true;
}
