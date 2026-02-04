package com.old.silence.job.server.common.allocate.client;

import com.old.silence.job.server.common.ClientLoadBalance;

import java.util.Random;
import java.util.TreeSet;


public class ClientLoadBalanceRandom implements ClientLoadBalance {

    private final Random random = new Random();

    @Override
    public String route(String allocKey, TreeSet<String> clientAllAddressSet) {
        String[] addressArr = clientAllAddressSet.toArray(new String[0]);
        return addressArr[random.nextInt(clientAllAddressSet.size())];
    }

    @Override
    public int routeType() {
        return ClientLoadBalanceManager.AllocationAlgorithmEnum.RANDOM.getType();
    }
}
