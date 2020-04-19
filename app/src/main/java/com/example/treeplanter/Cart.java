package com.example.treeplanter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Cart implements Serializable {
    //count type of trees purchased
    private static int countB = 0;
    private static int countO = 0;
    private static int countW = 0;
    //for hasmap keys
    private static int count = 0;
    private static String name;
    private static String location;
    private static String type;
    private static int price = 0;
    private static int totalPrice = 0;
    //Map is used for adding to Firebase
    private static HashMap<String, String> purchaseInfoMap = new HashMap<>();
    //ArrayList is used for diplay purposes on the payment page
    private static ArrayList<String> purchaseInfo = new ArrayList<>();

    public static void clearHashMap(){
        purchaseInfoMap.clear();
    }

    public static void clearArrayList(){
        purchaseInfo.clear();
    }

    public static void removeItem(int pos){
        purchaseInfo.remove(pos);
    }
    public static void setLocation(String location) {
        Cart.location = location;
    }



    public static void setPrice(int price) {
        Cart.price = price;
    }

    public static void setTotalPrice(int totalPrice) {
        Cart.totalPrice = totalPrice;
    }

    public static int getTotalPrice() {
        return totalPrice;
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


    public static boolean containsStr(String str){
        Set<String> keys = purchaseInfoMap.keySet();
        for (String key: keys){
            if (purchaseInfoMap.get(key).equals(str)) {
                return true;
            }
        }
        return false;
    }


    public static void setPurchaseInfo(String s) {
        purchaseInfo.add(s);
    }

    public static ArrayList<String> getPurchaseInfo() {
        return purchaseInfo;
    }
}

