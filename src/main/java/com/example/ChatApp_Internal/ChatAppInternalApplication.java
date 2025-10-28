package com.example.ChatApp_Internal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Kiem tra lai
 * 1. Xoa member ra khoi workspace nhung memer van con tham gia trong channel
 * 2. Phuong thuc add member dang co the add member vao channel ma chua vao workspace
 * 3. Hien tai khi get all member cua conversation thi no get luon nhung member da bi xoa
 * Mong muon
 * - khi xoa member ra khoi workspace thi tat car cac channel da tham gia cung xoa member do
 * - Chi duoc add member da co trong workspace vao channel, neu add bang link thi phai thuc hien add workspace truoc roi moi add vao conversation
 * */

@SpringBootApplication
public class ChatAppInternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAppInternalApplication.class, args);
    }

}
