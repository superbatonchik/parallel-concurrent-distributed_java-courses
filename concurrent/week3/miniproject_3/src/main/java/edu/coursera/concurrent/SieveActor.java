package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

import java.util.ArrayList;
import java.util.List;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        //edu.rice.pcdp.PCDP.finish();
        SieveActorActor actor = new SieveActorActor(2);

        PCDP.finish(() -> {
            for (int i = 3; i < limit; i += 2) {
                actor.send(i);
            }
            actor.send(0);
        });


        SieveActorActor loopActor = actor;
        int numPrimes = 0;
        while (loopActor != null) {
            numPrimes += loopActor.localPrimesNum();
            loopActor = loopActor.nextActor;
        }
        //System.out.println(numPrimes);
        return numPrimes;
    }

    public static final class SieveActorActor extends Actor {

        SieveActorActor nextActor;
        List<Integer> localPrimes;

        String name;

        public SieveActorActor(int localPrime) {
            localPrimes = new ArrayList<>();
            localPrimes.add(localPrime);
            nextActor = null;
            name = "Actor-c-" + localPrime;
        }

        @Override
        public void process(final Object msg) {
            int candidate = (int) msg;
            if (candidate <= 0) {
                return;
            }
            if (isPrime(candidate)) {
                if (localPrimes.size() < 1_000) {
                    localPrimes.add(candidate);
                } else if (nextActor == null) {
                    nextActor = new SieveActorActor(candidate);
                    //System.out.println("Created " + nextActor.name);
                } else {
                    nextActor.send(msg);
                }
            }
        }

        private boolean isPrime(int candidate) {
            for (int local : localPrimes) {
                if (candidate % local == 0) {
                    return false;
                }
            }
            return true;
        }

        public int localPrimesNum() {
            return localPrimes.size();
        }
    }
}
