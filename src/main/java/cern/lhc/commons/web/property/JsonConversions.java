/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.lhc.commons.web.property;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonConversions {

    private JsonConversions() {
        /* Only static methods */
    }

    // @formatter:off
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .create();
    // @formatter:on

    public static final Gson gson() {
        return GSON;
    }

}
