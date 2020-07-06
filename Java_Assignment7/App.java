import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.math.BigInteger;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import java.nio.ByteBuffer;

import java.security.MessageDigest;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class App {
    static Miner m;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/start", new StartHandler());
        server.createContext("/stop", new StopHandler());
        server.createContext("/result", new ResultHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Listening on port 8000");
    }
    
    static class Miner extends Thread {
        String data;
        byte[] dataBytes;
        BigInteger target = new BigInteger("0000000f00000000000000000000000000000000000000000000000000000000", 16);
        
        static long nonce = -1;
        static boolean found;
        
        private Miner(){}
        
        public Miner(String s) {
            data = s;
            dataBytes = data.getBytes();
            found = false;
            nonce = -1;
        }
    
        public byte[] longToBytes(long x) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(x);
            return buffer.array();
        }
        
        public BigInteger getHashWithNonce(long i) {
            try{
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(dataBytes);
                md.update(longToBytes(i));
                return new BigInteger(1, md.digest());
            } catch(Exception e) {
                return target;
            }
        }
        
        @Override
        public void run() {
            long i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                BigInteger current = getHashWithNonce(i);
                if(current.compareTo(target)<0){
                    nonce = i;
                    found = true;
                    break;
                }
                i++;
            }
            if(found){
                System.out.println("Nonce found");
            }else{
                System.out.println("Interrupted");
            }
        }
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "PONG";
            t.sendResponseHeaders(200, 0);
            t.getResponseBody().close();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class StartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try{
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(t.getRequestBody(), "UTF-8"));
                String data = (String)jsonObject.get("data");
                m = new Miner(data);
                m.start();
                t.sendResponseHeaders(200, 0);
            }catch(Exception e){
                e.printStackTrace();
                t.sendResponseHeaders(401, 0);
            }
            t.getResponseBody().close();
        }
    }
    
    static class ResultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "{ \"result\" :\""+(m.found?"found":"searching")+"\", \"nonce\":"+m.nonce+" }";
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class StopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            m.interrupt();
            t.sendResponseHeaders(200, 0);
            t.getResponseBody().close();
        }
    }
}
