package com.old.silence.job.server.common.allocate.client;

import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.server.common.ClientLoadBalance;




public class ClientLoadBalanceManager {

    public static ClientLoadBalance getClientLoadBalance(int routeType) {

        for (AllocationAlgorithmEnum algorithmEnum : AllocationAlgorithmEnum.values()) {
            if (algorithmEnum.getType() == routeType) {
                return algorithmEnum.getClientLoadBalance();
            }
        }

        throw CommonErrors.INVALID_PARAMETER.createException("routeType is not existed. routeType:[{}]", routeType);
    }

    public enum AllocationAlgorithmEnum {

        CONSISTENT_HASH(1, new ClientLoadBalanceConsistentHash(100)),
        RANDOM(2, new ClientLoadBalanceRandom()),
        LRU(3, new ClientLoadBalanceLRU(100)),
        ROUND(4, new ClientLoadBalanceRound()),
        FIRST(5, new ClientLoadBalanceFirst()),
        LAST(6, new ClientLoadBalanceLast());

        private final int type;
        private final ClientLoadBalance clientLoadBalance;

        AllocationAlgorithmEnum(int type, ClientLoadBalance clientLoadBalance) {
            this.type = type;
            this.clientLoadBalance = clientLoadBalance;
        }

        public int getType() {
            return type;
        }

        public ClientLoadBalance getClientLoadBalance() {
            return clientLoadBalance;
        }
    }

}
