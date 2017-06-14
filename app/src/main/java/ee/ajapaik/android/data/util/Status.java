package ee.ajapaik.android.data.util;

public enum Status {
    NONE(0),
    UNKNOWN(1),
    INVALID_PARAMETER(2),
    MISSING_PARAMETER(3),
    ACCESS_DENIED(4),
    SESSION_REQUIRED(5),
    SESSION_EXPIRED(6),
    SESSION_INVALID(7),
    USER_ALREADY_EXISTS(8),
    UNSUPPORTED_API(9),
    USER_DOES_NOT_EXIST(10),
    WRONG_PASSWORD(11),

    CONNECTION(-1),
    INVALID_DATA(-2),
    INVALID_CODE(-3);

    private final int m_code;

    public static Status parse(int code) {
        for(Status status : values()) {
            if(status.getCode() == code) {
                return status;
            }
        }

        return UNKNOWN;
    }

    public static Status parse(String str) {
        try {
            return parse(Integer.parseInt(str));
        }
        catch(Exception e) {
        }

        return INVALID_CODE;
    }

    private Status(int code) {
        m_code = code;
    }

    public int getCode() {
        return m_code;
    }

    public boolean isGood() {
        return (m_code == NONE.m_code) ? true : false;
    }

    public boolean isNetworkProblem() {
        return (m_code < 0) ? true : false;
    }

    public boolean isSessionProblem() {
        return (m_code == SESSION_REQUIRED.m_code ||
                m_code == SESSION_EXPIRED.m_code ||
                m_code == SESSION_INVALID.m_code) ? true : false;
    }
}

