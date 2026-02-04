package com.old.silence.job.server.common;

import java.util.TreeSet;


public interface ClientLoadBalance {

    String route(String key, TreeSet<String> clientAllAddressSet);

    int routeType();

}
