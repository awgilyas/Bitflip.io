import io.bitflip.api.ApiServer;
import io.bitflip.engine.Engine;
import io.bitflip.util.Console;
import io.bitflip.util.ConsoleCommand;
import io.bitflip.util.ThreadUtil;

public class App {

    public static void main( String[] args )
    {
        final Engine engine = new Engine();
        final ApiServer apiServer = new ApiServer();
        
        Console.start();
        Console.getInstance().registerCommand("stop", 
            new ConsoleCommand("stop") {
                @Override
                public void call(String[] args) {
                    Console.log(null, "Shutting down...", false);

                    engine.setRunning(false);
                    apiServer.setRunning(false);

                    Console.log(null, "Waiting for threads to close...", false);
                    ThreadUtil.despawnAll(true);

                    Console.log(null, "Goodbye.", false);
                    Console.stopAsync();
                }
            }
        );
        
        /* Spawn engine instance */
        if (engine.init()) {
            ThreadUtil.spawn(engine);
        }
        
        while (!engine.isRunning()) {}
        
        /* Spawn ApiServer instance */
        if (apiServer.init()) {
            ThreadUtil.spawn(apiServer);
        }
    }
    
}
