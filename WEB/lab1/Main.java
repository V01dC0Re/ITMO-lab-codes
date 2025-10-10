import com.fastcgi.FCGIInterface;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

class RateLimiter {
    private final int maxRequests;
    private final int windowMiliSeconds;
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, int windowMiliSeconds) {
        this.maxRequests = maxRequests;
        this.windowMiliSeconds = windowMiliSeconds;
    }

    public synchronized boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        Deque<Long> requests = clientRequests.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        while (!requests.isEmpty() && requests.peekFirst() < now - windowMiliSeconds) {
            requests.removeFirst();
        }

        if (requests.size() < maxRequests) {
            requests.addLast(now);
            return true; 
        }

        return false;
    }
}

public class Main {
        private static final String RECAPTCHA_SECRET = "6LdAVd0rAAAAAAY0LIgZzVs9V9ST09rHWkgcTDM_";
    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(5, 120*1000);

        FCGIInterface fcgi = new FCGIInterface();
        while (fcgi.FCGIaccept() >= 0) {
            try {
                String clientIp = System.getProperty("REMOTE_ADDR");
                if (clientIp == null || clientIp.isBlank()) clientIp = "unknown";

                if (!rateLimiter.allowRequest(clientIp)) {
                    sendJsonError("Too many requests. Please wait before trying again.");
                    continue;
                }
                String query = System.getProperty("QUERY_STRING");

                if (query == null || query.trim().isEmpty()) {
                    throw new ValidationException("Query string is missing");
                }

                Map<String, String> params = parseQuery(query);
                String captchaToken = params.get("captcha");  

                if (!validateCaptcha(captchaToken)) {
                    sendJsonError("Invalid or missing CAPTCHA token.");
                    continue;
                }

                validateParameters(params);

                BigDecimal x = new BigDecimal(params.get("x"));
                BigDecimal y = new BigDecimal(params.get("y"));
                BigDecimal r = new BigDecimal(params.get("r"));

                Instant startTime = Instant.now();
                boolean result = calculate(x, y, r);
                long execTime = ChronoUnit.NANOS.between(startTime, Instant.now());
                LocalDateTime now = LocalDateTime.now();

                String json = String.format(
                        AppConstants.RESULT_JSON,
                        execTime,
                        now,
                        result
                );

                sendJsonResponse(json);

            } catch (ValidationException e) {
                String json = String.format(
                        AppConstants.ERROR_JSON,
                        LocalDateTime.now(),
                        e.getMessage()
                );
                sendJsonResponse(json, 400);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonError("Internal server error.");
            }
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = decode(kv[0]);
                String value = decode(kv[1]);
                map.put(key, value);
            }
        }
        return map;
    }

    private static String decode(String s) {
        return java.net.URLDecoder.decode(s.replace("+", "%20"), StandardCharsets.UTF_8);
    }

    private static void validateParameters(Map<String, String> params) throws ValidationException {
        Set<String> required = Set.of("x", "y", "r");
        for (String key : required) {
            if (!params.containsKey(key)) {
                throw new ValidationException("Missing parameter: " + key);
            }
        }

        for (String key : required) {
            String valueStr = params.get(key);
            if (valueStr == null || valueStr.trim().isEmpty()) {
                throw new ValidationException(key + " is missing or empty");
            }
    
            try {
                BigDecimal value = new BigDecimal(valueStr);
    
                switch (key) {
                    case "x":
                        if (!AppConstants.VALID_X.contains(value)) {
                            throw new ValidationException("x must be one of " + AppConstants.VALID_X + ", but was: " + value);
                        }
                        break;
                    case "y":
                        if (value.compareTo(BigDecimal.valueOf(3)) > 0 || 
                            value.compareTo(BigDecimal.valueOf(-3)) < 0) {
                            throw new ValidationException("y must be in range [-3, 3], but was: " + value);
                        }
                        break;
                    case "r":
                        if (!AppConstants.VALID_R.contains(value)) {
                            throw new ValidationException("r must be one of " + AppConstants.VALID_R + ", but was: " + value);
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                throw new ValidationException(key + " must be a valid number: '" + valueStr + "'");
            }
        }
    
    }

    private static boolean validateCaptcha(String token) {
        if (token == null || token.isEmpty()) return false;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("secret=" + RECAPTCHA_SECRET + "&response=" + token))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return false;

            String body = response.body();

        return body.contains("\"success\": true") || 
               body.contains("\"success\":true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void sendJsonResponse(String json) {
        sendJsonResponse(json, 200);
    }

    private static void sendJsonResponse(String json, int status) {
        int contentLength = json.getBytes(AppConstants.CHARSET).length;
        String responseFormat = status == 200 ? AppConstants.HTTP_200_FORMAT : AppConstants.HTTP_400_FORMAT;
        String response = String.format(responseFormat, AppConstants.CONTENT_TYPE_JSON, contentLength, json);
        System.out.print(response);
    }

    private static void sendJsonError(String message) {
        String json = String.format(AppConstants.ERROR_JSON, LocalDateTime.now(), message);
        sendJsonResponse(json, 400);
    }

    private static boolean calculate(BigDecimal x, BigDecimal y, BigDecimal r) {
        final BigDecimal ZERO = BigDecimal.ZERO;
        final BigDecimal TWO = BigDecimal.valueOf(2);

        int signX = x.compareTo(ZERO);
        int signY = y.compareTo(ZERO);

        if (signX >= 0 && signY >= 0) {
            BigDecimal rHalf = r.divide(TWO, 10, RoundingMode.HALF_UP);
            return x.compareTo(rHalf) <= 0 && y.compareTo(r) <= 0;
        }

        if (signX >= 0 && signY <= 0) {
            BigDecimal x2 = x.multiply(x);
            BigDecimal y2 = y.multiply(y);
            BigDecimal r2 = r.multiply(r);
            return x2.add(y2).compareTo(r2) <= 0;
        }

        if (signX <= 0 && signY <= 0) {
            BigDecimal xHalf = x.divide(TWO, 10, RoundingMode.HALF_UP);
            BigDecimal leftSide = y.subtract(xHalf);
            BigDecimal rHalf = r.divide(TWO, 10, RoundingMode.HALF_UP);
            return leftSide.compareTo(rHalf) <= 0;
        }

        return false;
    }
}
