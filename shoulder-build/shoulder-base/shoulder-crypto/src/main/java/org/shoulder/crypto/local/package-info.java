/**
 * 定义本地加解密接口、提供默认实现方案
 * 本地加解密：即本服务生成的密文只能由本应用解密。用于加密本应用专属的存储，如入库的敏感信息，存于 redis 的密钥信息等。
 * 默认方案采用了对称加密保证了加解密性能和安全性。
 */
package org.shoulder.crypto.local;
