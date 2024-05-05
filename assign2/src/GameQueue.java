import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GameQueue {
    private List<Player> queue;
    private Lock lock = new ReentrantLock();
    private GameList gameList;

    public GameQueue(GameList gameList) {
        this.queue = new ArrayList<>();
        this.gameList = gameList;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if(queue.size() >= 2) {
                //todo change logic to add time and rank as podenration
                startGame(Arrays.asList(queue.get(0), queue.get(1)));
                queue.remove(0);
                queue.remove(0);
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    public void push(Player player) {
        lock.lock();
        try {
            for (Player p : queue) {
                if (p.getRank() == player.getRank()) {
                    startGame(Arrays.asList(player, p));
                    queue.remove(p);
                    return;
                }
            }
            queue.add(player);
        } finally {
            lock.unlock();
        }
    }

    public void startGame(List<Player> players) {
        gameList.add(new Game(players, true));
        System.out.println("Ranked game started: " + players.get(0).getUsername() + " vs " + players.get(1).getUsername());
    }
}