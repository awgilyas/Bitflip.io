import io.bitflip.api.ApiServer;
import io.bitflip.engine.Engine;
import io.bitflip.util.Console;
import io.bitflip.util.ConsoleCommand;
import io.bitflip.util.ThreadUtil;

public class App {

    public static void main( String[] args )
    {
        final Engine engineInstance = Engine.create();
        final ApiServer serverInstance = ApiServer.create();
        
        Console.start();
        Console.getInstance().registerCommand("stop", 
            new ConsoleCommand("stop") {
                @Override
                public void call(String[] args) {
                    Console.log(null, "Shutting down...", false);
                    
                    serverInstance.setRunning(false);
                    engineInstance.setRunning(false);

                    Console.log(null, "Waiting for threads to close...", false);
                    ThreadUtil.despawnAll(true);

                    Console.log(null, "Goodbye.", false);
                    Console.stopAsync();
                }
            }
        );
        
        if (engineInstance != null && serverInstance != null) {
            if (engineInstance.init() && serverInstance.init()) {
                ThreadUtil.spawn(engineInstance);
                while (!engineInstance.isRunning()) { }

                ThreadUtil.spawn(serverInstance);
            }
        }
    }
    
}
