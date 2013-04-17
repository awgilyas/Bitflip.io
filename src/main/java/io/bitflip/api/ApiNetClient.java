package io.bitflip.api;

import com.mongodb.util.ThreadUtil;
import io.bitflip.util.Console;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.charset.StandardCharsets;
import lombok.Getter;

public class ApiNetClient implements Runnable {
    
    @Getter private SocketAddress remoteSocketAddress;
    private boolean readLock;
    final private ApiServer server;
    final private AsynchronousSocketChannel clientSocketChannel;
    final private CompletionHandler<Integer, ByteBuffer> readHandler = new CompletionHandler() {

        @Override
        public void completed(Object result, Object attachment) {
            StringBuilder lineBuilder = new StringBuilder();
            String[] lines;
            
            Integer bytesRead = (Integer)result;
            ByteBuffer buffer = (ByteBuffer)attachment;
            
            if (bytesRead == -1) {
                closeChannel();
            } else {
                buffer.flip();
                
                lineBuilder.append(StandardCharsets.UTF_8.decode(buffer));
                lines = lineBuilder.toString().replace("\r\n", "\n").split("\n");
                handleRequest(lines[0]);
                closeChannel();
            }
            
            readLock = false;
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            closeChannel();
        }
    };
    
    public ApiNetClient(ApiServer server, AsynchronousSocketChannel clientSocketChannel) {
        this.server = server;
        this.clientSocketChannel = clientSocketChannel;
    }
    
    @Override
    public void run() {
        try {
            remoteSocketAddress = clientSocketChannel.getRemoteAddress();
            Console.log(this, "API client connected (" + remoteSocketAddress + ")");
            
            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (server.isRunning() && clientSocketChannel.isOpen()) {
                try {
                    if (!readLock) {
                        clientSocketChannel.read(buffer, buffer, readHandler);
                        readLock = true;
                    }
                } catch (ReadPendingException ex) {
                }
                
                ThreadUtil.sleep(10);
            }
            
            String reason = "reason: unknown";
            if (!server.isRunning()) {
                reason = "reason: ApiServer shutting down";
            } else if (!clientSocketChannel.isOpen()) {
                reason = "reason: SocketChannel closed";
            }
            
            Console.log(this, "API client disconnected (" +  remoteSocketAddress + "); " + reason);
        } catch (Exception ex) {
            Console.log(this, "Error while handling API client: " + ex);
        }
    }

    private void handleRequest(String requestText) {
        String responseLine;
        final ApiRequest request;
        final ApiRequestResult requestResult;
        
        if (requestText.charAt(0) == '{') {
            try {
                request = ApiRequest.fromJson(server, requestText);

                if (request != null) {
                    requestResult = request.execute();

                    if (requestResult != null) {
                        responseLine = requestResult.toJsonString();

                        if (responseLine != null) {
                            writeLine(responseLine + "\r\n");
                        } else {
                            writeLine(ApiRequestResult.RESULT_UNAVAILABLE + "\r\n");
                        }
                    } else {
                        writeLine(ApiRequestResult.RESULT_UNAVAILABLE + "\r\n");
                    }
                } else {
                    writeLine(ApiRequestResult.RESULT_MALFORMED_REQUEST + "\r\n");
                }
            } catch (Exception | Error e) {
                writeLine(ApiRequestResult.RESULT_PARSE_ERROR + "\r\n");
            }
        }
    }
    
    private void writeLine(String line) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(line);
        clientSocketChannel.write(buffer);
    }
    
    private void closeChannel() {
        try {
            clientSocketChannel.close();
        } catch (IOException ex) {
        }
    }

}
