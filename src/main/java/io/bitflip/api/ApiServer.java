package io.bitflip.api;

import io.bitflip.db.Database;
import io.bitflip.util.Console;
import io.bitflip.util.ThreadUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import lombok.Getter;
import lombok.Setter;

public class ApiServer implements Runnable {
    
    public static final int DEFAULT_BIND_PORT = 1024;
    
    @Getter private final Database database;
    @Getter private final int bindPort;
    @Getter private boolean ready;
    @Getter @Setter private boolean running;
    @Getter private SocketAddress localSocketAddress;
    private AsynchronousServerSocketChannel socketChannel;
    private final CompletionHandler<AsynchronousSocketChannel, ApiServer> acceptHandler = new CompletionHandler() {

        @Override
        public void completed(Object result, Object attachment) {
            AsynchronousSocketChannel clientSocketChannel = (AsynchronousSocketChannel)result;
            ApiServer server = (ApiServer)attachment;
            
            ApiNetClient apiClient = new ApiNetClient(server, clientSocketChannel);
            ThreadUtil.spawn(apiClient);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            ApiServer server = (ApiServer)attachment;
            
            if (!(exc instanceof AsynchronousCloseException)) {
                Console.log(server, "Unable to accept API client connection; " + exc);
            }
        }
        
    };
    
    public ApiServer(int bindPort) {
        this.bindPort = bindPort;
        database = new Database();
    }
    
    public ApiServer() {
        this(ApiServer.DEFAULT_BIND_PORT);
    }
    
    public boolean init() {
        Console.log(this, "API Server initializing...");
        
        try {
            localSocketAddress = new InetSocketAddress(bindPort);
            socketChannel = AsynchronousServerSocketChannel.open().bind(localSocketAddress);
            
            Console.log(this, "API Server bound to " + localSocketAddress + "!");
        } catch (IOException ex) {
            Console.log(this, "Unable to bind to port " + bindPort + "; " + ex);
            return false;
        }
        
        if (!database.init()) {
            return false;
        }
        
        return (ready = true);
    }
    
    @Override
    public void run() {
        if (!ready) return;

        running = true;
        Console.log(this, "API Server started!");
        
        AsynchronousSocketChannel clientSocketChannel;
        ApiNetClient apiClient;
        while (running && socketChannel.isOpen()) {
            try {
                socketChannel.accept(this, acceptHandler);
            } catch (AcceptPendingException ex) {
            } catch (Exception ex) {
                Console.log(this, "Error while trying to accept API connections; " + ex);
            }
            
            ThreadUtil.sleep(10);
        }
        
        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException ex) {
            }
        }
        
        running = false;
        Console.log(this, "API Server shutting down...");
    }

}
