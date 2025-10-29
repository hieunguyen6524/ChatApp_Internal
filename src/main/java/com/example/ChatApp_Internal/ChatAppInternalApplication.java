package com.example.ChatApp_Internal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Kiem tra lai
 * 1. Xoa member ra khoi workspace nhung memer van con tham gia trong channel (xong)
 * 2. Phuong thuc add member dang co the add member vao channel ma chua vao workspace (xong)
 * 3. Hien tai khi get all member cua conversation thi no get luon nhung member da bi xoa (xong)
 * 4. Vi moi them truong is_active vao conversation_member nen phai kiem tra va viet lai logic trong conversation
 * */

@SpringBootApplication
public class ChatAppInternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAppInternalApplication.class, args);
    }

}
