import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public enum AppConstants {
    ;

    public static final String HTTP_200_FORMAT = """
            HTTP/1.1 200 OK
            Content-Type: %s
            Content-Length: %d
            
            %s
            """;

    public static final String HTTP_400_FORMAT = """
            HTTP/1.1 400 Bad Request
            Content-Type: %s
            Content-Length: %d
            
            %s
            """;

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String RESULT_JSON = """
            {
                "time": "%s",
                "now": "%s",
                "result": %b
            }
            """;

    public static final String ERROR_JSON = """
            {
                "now": "%s",
                "reason": "%s"
            }
            """;

    public static final Set<BigDecimal> VALID_X = Set.of(
            new BigDecimal(-3),  new BigDecimal(-2),  new BigDecimal(-1),  new BigDecimal(0),
            new BigDecimal(1),  new BigDecimal(2),  new BigDecimal(3),  new BigDecimal(4),  new BigDecimal(5)
    );

    public static final Set<BigDecimal> VALID_R = Set.of(
            new BigDecimal(1),  new BigDecimal("1.5"),  new BigDecimal(2),  new BigDecimal("2.5"),  new BigDecimal(3)
    );

    public static final float Y_MIN = -3f;
    public static final float Y_MAX = 5f;

    public static final java.nio.charset.Charset CHARSET = StandardCharsets.UTF_8;
}
