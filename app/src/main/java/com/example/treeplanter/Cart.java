package com.example.treeplanter;

import android.content.Intent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Cart implements Serializable {

    private static int countB = 1;
    private static int countO = 1;
    private static int countW = 1;
    private static int count = 0;
    private static String name;
    private static String location;
    private static String type;
    private static int price = 0;
    //Map is used for adding to Firebase
    private static HashMap<String, String> purchaseInfoMap = new HashMap<>();
    //ArrayList is used for diplay purposes on the payment page
    private static ArrayList<String> purchaseInfo = new ArrayList<>();


    public static void removeItem(int pos){
        purchaseInfo.remove(pos);
    }
    public static void setLocation(String location) {
        Cart.location = location;
    }

    public static void setType(String type) {
        Cart.type = type;
    }

    public Cart(String location, String type) {
        this.location = location;
        this.type = type;
    }

    public static void setName(String n) {
        name = n;
    }

    public static void addToMap(String treeType){

        purchaseInfoMap.put("Tree Name " + count, name);
        purchaseInfoMap.put("Tree Location " + count, location);
        purchaseInfoMap.put("Tree type " + count, type);

        if (treeType.equals("Birch")) {
            price += 1;
            countB ++;
        }
        else if (treeType.equals("Oak")){
            price += 3;
            countO ++;
        }else{
            price += 2;
            countW ++;
        }

        count ++;
    }
    public static void setCount(int c) {
        count = c;
    }

    public static void setCountB(int countB) {
        Cart.countB = countB;
    }

    public static void setCountO(int countO) {
        Cart.countO = countO;
    }

    public static void setCountW(int countW) {
        Cart.countW = countW;
    }

    public static int getCountB() {
        return countB;
    }

    public static int getCountO() {
        return countO;
    }

    public static int getCountW() {
        return countW;
    }

    public static int getPrice() {
        return price;
    }

    public static HashMap<String, String> getPurchaseInfoMap() {
        return purchaseInfoMap;
    }


    public static void setPurchaseInfo(String s) {
        purchaseInfo.add(s);
    }

    public static ArrayList<String> getPurchaseInfo() {
        return purchaseInfo;
    }
}
