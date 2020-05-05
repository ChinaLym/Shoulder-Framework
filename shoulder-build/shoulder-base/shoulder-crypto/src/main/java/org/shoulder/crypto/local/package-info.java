package org.shoulder.crypto.local;
/**
 该包定义实现本地加解密接口和默认方案
 本地加解密：即本服务生成的密文只能由本应用解密。用于加密本服务专属的存储，如入库的敏感信息，存于 redis 的密钥信息等。
 默认方案采用了对称加密保证了加解密性能和安全性。
 */