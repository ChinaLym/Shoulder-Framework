package org.shoulder.crypto.exception;

/**
 * 加密解密根异常，加密类型异常通常需要捕获并抛出，故不带错误码
 *
 * @author lym
 */
public class CryptoException extends Exception {

    public CryptoException() {
        super();
    }

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

    protected CryptoException(String message, Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
