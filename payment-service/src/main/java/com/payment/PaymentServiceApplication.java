package com.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

@SpringBootApplication
@EnableAsync
public class PaymentServiceApplication {

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Class<?> cls = Class.forName("sun.nio.ch.UnixDomainSockets");
            Field supported = cls.getDeclaredField("supported");
            long offset = unsafe.staticFieldOffset(supported);
            Object base = unsafe.staticFieldBase(supported);
            unsafe.putBoolean(base, offset, false);
            System.out.println("[INFO] AF_UNIX disabled for NIO pipes (Java 21 Windows fix)");
        } catch (Exception e) {
            System.err.println("[WARN] Could not disable AF_UNIX: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
