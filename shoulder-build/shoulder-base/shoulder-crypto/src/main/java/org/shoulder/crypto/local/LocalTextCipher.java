package org.shoulder.crypto.local;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.springframework.lang.NonNull;

/**
 * 本地数据加密解密：只能由本应用实现解密。
 * 使用者直接使用的接口
 *
 * @author lym
 */
public interface LocalTextCipher extends TextCipher {

    /**
     * 加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     * @throws SymmetricCryptoException aes异常
     */
    @Override
    String encrypt(@NonNull String text) throws SymmetricCryptoException;

    /**
     * 以Aes256解密
     *
     * @param cipherText aes256 加密过的密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     * @throws SymmetricCryptoException aes异常
     */
    @Override
    String decrypt(@NonNull String cipherText) throws SymmetricCryptoException;

    /**
     * 确保加密功能正常使用
     * 可在项目启动后调用，以优化第一次加解密性能。
     */
    void ensureEncryption();
}
