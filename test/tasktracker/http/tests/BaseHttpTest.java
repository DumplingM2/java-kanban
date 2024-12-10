package tasktracker.http.tests;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import tasktracker.manager.InMemoryTaskManager;
import tasktracker.manager.TaskManager;
import tasktracker.http.HttpTaskServer;

import java.io.IOException;

public abstract class BaseHttpTest {

    protected HttpTaskServer server;
    protected TaskManager manager;
    protected Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson(); // ВАЖНО: получить gson с адаптерами
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }
}