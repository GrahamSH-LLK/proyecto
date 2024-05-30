import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
    /* ANSI Codepoints for making pretty colors */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    /* Data container for Score */
    static class Score {
        public String name;
        public int score;

        public Score(String name, int score) {
            this.name = name;
            this.score = score;
        }

    }

    private static ArrayList<Score> scores = new ArrayList<Score>();
    private static final long createdMillis = System.currentTimeMillis();

    /* Helper method and overload for responding with a string */
    public static void respondString(HttpExchange t, String response) throws IOException {
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static void respondString(HttpExchange t, String response, int code) throws IOException {
        t.sendResponseHeaders(code, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static void main(String[] args) throws Exception {
        scores.add(new Score("John", 1));
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/scores", new GetScores());
        server.createContext("/score", new Scores());
        server.createContext("/uptime", new Uptime());
        server.createContext("/highscore", new HighScore());

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println(ANSI_BLUE + "Server started on port " + ANSI_RESET + ANSI_GREEN + 8000 + ANSI_RESET);
        System.out.println(ANSI_RED + "2048" + ANSI_RESET + " Score Keeper");

    }

    static class GetScores implements MethodHandler {
        public void get(HttpExchange t) throws IOException {
            String handCreatedJson = "[";
            for (Score s : scores) {
                handCreatedJson += "{\"name\":\"" + s.name + "\",\"score\":" + s.score + "},";
            }
            handCreatedJson = handCreatedJson.substring(0, handCreatedJson.length() - 1); // strip last comma
            handCreatedJson += "]";
            respondString(t, handCreatedJson, 200);
        }
    }
    static class Uptime implements MethodHandler {
        public void get(HttpExchange t) throws IOException {
            long uptime = (System.currentTimeMillis() - createdMillis) / 1000;
            String json = "{\"uptime\":" + uptime + ",\"unit\":\"seconds\"}";
            respondString(t, json, 200);
        }
    }

    static class Scores implements MethodHandler {

        public void post(HttpExchange t) throws IOException {
            String name = t.getRequestURI().getQuery().split("=")[1];
            int score = Integer.parseInt(t.getRequestURI().getQuery().split("=")[2]);
            scores.add(new Score(name, score));
            respondString(t, "Score added", 200);
        }

        public void put(HttpExchange t) throws IOException {
            String name = t.getRequestURI().getQuery().split("=")[1];
            int score = Integer.parseInt(t.getRequestURI().getQuery().split("=")[2]);
            for (Score s : scores) {
                if (s.name.equals(name)) {
                    s.score = score;
                    respondString(t, "Score updated", 200);
                    return;
                }
            }
            respondString(t, "Score not found", 404);
        }
    }
    static class HighScore implements MethodHandler {
        public void get(HttpExchange t) throws IOException {
            int highScore = 0;
            String highScoreName = "";
            for (Score s : scores) {
                if (s.score > highScore) {
                    highScore = s.score;
                    highScoreName = s.name;
                }
            }
            String json = "{\"highscore\":" + highScore + ",\"name\":\"" + highScoreName + "\"}";
            respondString(t, json, 200);
        }
    }

    static interface MethodHandler extends HttpHandler {
        default void handle(HttpExchange t) throws IOException {
            if (t.getRequestMethod().equalsIgnoreCase("GET")) {
                get(t);
            } else if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                post(t);
            } else if (t.getRequestMethod().equalsIgnoreCase("PUT")) {
                put(t);
            } else if (t.getRequestMethod().equalsIgnoreCase("DELETE")) {
                delete(t);
            }
        }

        default void get(HttpExchange t) throws IOException {
            respondString(t, "GET not implemented", 405);
        }

        default void post(HttpExchange t) throws IOException {
            respondString(t, "POST not implemented", 405);
        }

        default void put(HttpExchange t) throws IOException {
            respondString(t, "PUT not implemented", 405);
        }

        default void delete(HttpExchange t) throws IOException {
            respondString(t, "DELETE not implemented", 405);
        }

    }
}
