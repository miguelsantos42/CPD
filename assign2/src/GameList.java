import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

class GameList {
    private static List<Game> gamesList;
    private Lock lock = new ReentrantLock();

    public GameList() {
        gamesList = new ArrayList<>();
    }

    public void add(Game game) {
        lock.lock();
        try {
            gamesList.add(game);
        } finally {
            lock.unlock();
        }
    }

    public Game get(int i){
        lock.lock();
        try {
            return gamesList.get(i);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return gamesList.size();
        } finally {
            lock.unlock();
        }
    }

    public void remove(int i) {
        lock.lock();
        try {
            gamesList.remove(i);
        } finally {
            lock.unlock();
        }
    }

    public void remove(Game game) {
        lock.lock();
        try {
            gamesList.remove(game);
        } finally {
            lock.unlock();
        }
    }
}
