package io.advantageous.qbit.example.perf;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static class Trade {
        final String name;
        final long amount;

        public Trade(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public long getAmount() {
            return amount;
        }
    }

    public interface TradeService {

        void trade(Trade trade);
    }

    public static class TradeServiceImpl {

        final AtomicLong tradeCounter = new AtomicLong();

        void trade(Trade trade) {
            trade.getAmount();
            tradeCounter.incrementAndGet();
        }

    }


    private static void runService(int runs, int tradeCount, int batchSize, int checkEvery) {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final TradeServiceImpl tradeServiceImpl = new TradeServiceImpl();

        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder().setRequestQueueBuilder(queueBuilder);

        final ServiceQueue serviceQueue = serviceBuilder.setServiceObject(tradeServiceImpl).build().startServiceQueue();

        final TradeService tradeService = serviceQueue.createProxy(TradeService.class);

        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

            final long startTime = System.currentTimeMillis();
            for (int t =0; t < tradeCount; t++) {
                tradeService.trade(new Trade("ibm", 100L));
            }

            ServiceProxyUtils.flushServiceProxy(tradeService);

            for (int index = 0; index < 1000; index++) {
                Sys.sleep(10);
                if (tradeServiceImpl.tradeCounter.get() >= tradeCount) {
                    break;
                }
            }

            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
                    r,
                    tradeCount,
                    System.currentTimeMillis() - startTime,
                    batchSize);
        }


        System.out.printf("DONE traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        serviceQueue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }

    public static void main(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 500_000;
        final int batchSize = 125;

        int currentBatchSize = batchSize;



        for (int index=0; index< 5; index++) {
            runService(runs, tradeCount, currentBatchSize, 0);
            currentBatchSize*=2;
        }


        Sys.sleep(Integer.MAX_VALUE);
    }



    //public static void mainArrayBlockingQueue(String... args) throws Exception {
    public static void mainArrayBlockingQueue(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 5_000_000;
        final int batchSize = 125;

        int currentBatchSize = batchSize;


        run(runs, tradeCount, 1);

        run(runs, tradeCount, batchSize);

        for (int index=0; index< 10; index++) {
            run(runs, tradeCount, currentBatchSize);
            currentBatchSize*=2;
        }
    }

    //public static void main(String... args) throws Exception {

    public static void mainTransferQueue(String... args) throws Exception {

        final int runs = 20;
        final int tradeCount = 5_000_000;
        final int batchSize = 50_000;
        final int checkEvery = 1000;

        int currentCheckEvery = checkEvery;

        run(runs, tradeCount, batchSize, 10);

        for (int index=0; index< 10; index++) {
            run(runs, tradeCount, batchSize, currentCheckEvery);
            currentCheckEvery*=2;
        }
    }

    private static void run(int runs, int tradeCount, int batchSize) {
        run(runs, tradeCount, batchSize, 0);
    }

    private static void run(int runs, int tradeCount, int batchSize, int checkEvery) {

        final QueueBuilder queueBuilder = QueueBuilder.queueBuilder().setName("trades").setBatchSize(batchSize)
                .setSize(1_000_000);

        if (checkEvery>0) {
            queueBuilder.setLinkTransferQueue();
            queueBuilder.setCheckEvery(checkEvery);
        }

        final Queue<Trade> queue = queueBuilder.build();
        final AtomicLong tradeCounter = new AtomicLong();

        queue.startListener(item -> {

            tradeCounter.incrementAndGet();
        });


        final SendQueue<Trade> tradeSendQueue = queue.sendQueue();


        final long startRun = System.currentTimeMillis();

        for (int r = 0; r < runs; r++) {

           //final long startTime = System.currentTimeMillis();
           for (int t =0; t < tradeCount; t++) {
               tradeSendQueue.send(new Trade("ibm", 100L));
           }
           tradeSendQueue.flushSends();

            for (int index = 0; index < 1000; index++) {
                Sys.sleep(10);
                if (tradeCounter.get() >= tradeCount) {
                    break;
                }
            }

//            System.out.printf("%d traded %,d in %d batchSize = %,d\n",
//                    r,
//                    tradeCount,
//                    System.currentTimeMillis() - startTime,
//                    batchSize);
        }


        System.out.printf("DONE traded %,d in %d batchSize = %,d, checkEvery = %,d\n",
                tradeCount * runs,
                System.currentTimeMillis() - startRun,
                batchSize,
                checkEvery);
        queue.stop();


        Sys.sleep(2_000);
        System.gc();
        Sys.sleep(2_000);
    }

}