import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GameQueue {
    private final List<Player> queue;
    private final Lock lock = new ReentrantLock();
    private final Condition queueChanged = lock.newCondition();
    private final GameList gameList;

    public GameQueue(GameList gameList) {
        this.queue = new ArrayList<>();
        this.gameList = gameList;

        // Iniciar uma thread que verifica a fila periodicamente
        Thread queueChecker = new Thread(this::processQueue);
        queueChecker.start();
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
            queueChanged.signal(); // Notificar que a fila mudou
        } finally {
            lock.unlock();
        }
    }

    private void processQueue() {
        try {
            while (true) {
                lock.lock();
                try {
                    while (queue.size() < 2) {
                        queueChanged.await(); // Esperar até que a fila tenha pelo menos dois jogadores
                    }

                    // Iniciar jogo com os dois primeiros jogadores da fila
                    startGame(Arrays.asList(queue.get(0), queue.get(1)));
                    queue.remove(0);
                    queue.remove(0);
                } finally {
                    lock.unlock();
                }

                // Aguardar um período de tempo antes de verificar novamente
                Thread.sleep(5000); // 5000 ms == 5 segundos
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void startGame(List<Player> players) {
        gameList.add(new Game(players, true));
        System.out.println("Ranked game started: " + players.get(0).getUsername() + " vs " + players.get(1).getUsername());
    }
}



