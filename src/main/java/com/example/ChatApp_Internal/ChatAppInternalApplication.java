package com.example.ChatApp_Internal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Fix loi /refresh  thi revoke luon refresh token
 * Lam dang ky bang gg (dang bi loi sau moi lan chay lai server thi lan dang nhap dau tien luon bi loi mac du da tao tai khoan thanh cong)
 *
 * */

@SpringBootApplication
public class ChatAppInternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAppInternalApplication.class, args);
    }

}
