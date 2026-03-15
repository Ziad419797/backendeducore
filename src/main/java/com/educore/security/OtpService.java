package com.educore.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private static final int EXPIRATION_MINUTES = 3;
    private static final int OTP_LENGTH = 6;
    private final Random random = new Random();

    public String generateAndSendOtp(String phone) {
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        otpStore.put(phone, new OtpData(otp, expiresAt));

        // TODO: Integrate with SMS service (Twilio, Vonage, etc.)
        log.info("OTP generated for {}: {}", phone, otp);

        return otp;
    }

    public void verifyOtp(String phone, String otp) {
        OtpData data = otpStore.get(phone);

        if (data == null) {
            throw new IllegalStateException("لم يتم إرسال رمز التحقق لهذا الرقم");
        }

        if (LocalDateTime.now().isAfter(data.expiresAt())) {
            otpStore.remove(phone);
            throw new IllegalStateException("انتهت صلاحية رمز التحقق");
        }

        if (!data.otp().equals(otp)) {
            throw new IllegalArgumentException("رمز التحقق غير صحيح");
        }

        otpStore.remove(phone);
    }

    public boolean hasValidOtp(String phone) {
        OtpData data = otpStore.get(phone);
        if (data == null) return false;

        if (LocalDateTime.now().isAfter(data.expiresAt())) {
            otpStore.remove(phone);
            return false;
        }

        return true;
    }

    private String generateOtp() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;
        return String.valueOf(min + random.nextInt(max - min + 1));
    }

    private record OtpData(String otp, LocalDateTime expiresAt) {}
}