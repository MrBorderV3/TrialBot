package me.border.trialbot.module;

import me.border.utilities.cache.AbstractCache;

public class XPCache extends AbstractCache<String> {

    public XPCache() {
        super(10000);
    }
}
