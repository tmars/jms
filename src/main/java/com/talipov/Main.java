package com.talipov;

/**
 * Created by Марсель on 14.03.2017.
 */
public class Main {
    public static void main(String[] args) {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
    }
}
