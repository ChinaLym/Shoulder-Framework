package org.shoulder.crypto.digest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.constant.ByteSpecification;

public class Sha256ExUtilsTest {

    /**
     * 测试 Sha256 数据摘要算法以及签名验证（字符串）
     */
    @Test
    public void testSha256OnString() throws Exception {
        String text = "123456";
        String cipher = Sha256ExUtils.digest(text);

        Assertions.assertThat(Sha256ExUtils.verify(text, cipher)).isTrue();
    }

    /**
     * 测试 Sha256 数据摘要算法以及签名验证（byte数组）
     */
    @Test
    public void testSha256OnByteArray() throws Exception {
        byte[] textBytes = "123".getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] cipherBytes = Sha256ExUtils.digest(textBytes);
        Assertions.assertThat(Sha256ExUtils.verify(textBytes, cipherBytes)).isTrue();
    }

    /**
     * 测试 Sha256 多次对同一数据摘要后是相同的
     */
    @Test
    public void testMultiSha256() throws Exception {
        Assertions.assertThat(Sha256ExUtils.digest("123")).isNotEqualTo(Sha256ExUtils.digest("123"));
    }

}
