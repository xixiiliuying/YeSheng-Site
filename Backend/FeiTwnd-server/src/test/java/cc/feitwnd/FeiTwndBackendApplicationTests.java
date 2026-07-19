package cc.feitwnd;

import cc.feitwnd.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FeiTwndBackendApplicationTests {
    @Autowired
    private EmailService emailService;

    @Test
    public void testPassword() throws Exception {
        String password = "123456"; // 替换为你需要的密码
        String salt = "123456";    //  替换为你需要的盐值，可以是任意字符串

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String combined = password + salt;
        byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));

        // 转换为十六进制
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        System.out.println(hexString.toString());
    }

    @Test
    public void testSendVerifyCodeEmail(){
        String toEmail = "691360854@qq.com";
        String verifyCode = "123456";

        System.out.println("Verify Code Email Sent to " + toEmail);
        try{
            emailService.sendVerifyCode(toEmail, verifyCode);
            System.out.println("Verify Code Email Sent to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send Verify Code Email to " + toEmail);
        }
    }
}
