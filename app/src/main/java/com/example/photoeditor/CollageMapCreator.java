package com.example.photoeditor;

import java.util.ArrayList;
import java.util.HashMap;

public class CollageMapCreator {

    public static ArrayList<HashMap<String, Integer>> CollageHashMap(int collageNumber, int screenwidth, int screenheight) {

        ArrayList<HashMap<String, Integer>> lista = new ArrayList<HashMap<String, Integer>>();
        HashMap<String, Integer> mapa01 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa03 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa02 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa04 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa05 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa07 = new HashMap<String, Integer>();
        HashMap<String, Integer> mapa06 = new HashMap<String, Integer>();

        switch (collageNumber) {
            case 1:
                lista.clear();
                mapa01.put("x", 4);
                mapa01.put("y", 4);
                mapa01.put("width", screenwidth - 8);
                mapa01.put("height", (screenheight / 2) - 8);
                lista.add(mapa01);
                mapa02.put("x", 4);
                mapa02.put("y", screenheight / 2);
                mapa02.put("width", screenwidth - 8);
                mapa02.put("height", (screenheight / 2) - 4);
                lista.add(mapa02);
                break;
            case 2:
                lista.clear();
                mapa01.put("x", 4);
                mapa01.put("y", 4);
                mapa01.put("width", screenwidth - 8);
                mapa01.put("height", (screenheight) - (screenheight / 3) - 8);
                lista.add(mapa01);
                mapa02.put("x", 4);
                mapa02.put("y", (screenheight) - (screenheight / 3));
                mapa02.put("width", (screenwidth / 2) - 8);
                mapa02.put("height", (screenheight / 3) - 8);
                lista.add(mapa02);
                mapa03.put("x", (screenwidth / 2));
                mapa03.put("y", (screenheight) - (screenheight / 3));
                mapa03.put("width", (screenwidth / 2) - 4);
                mapa03.put("height", (screenheight / 3) - 8);
                lista.add(mapa03);
                break;
            case 3:
                lista.clear();
                mapa01.put("x", 4);
                mapa01.put("y", 4);
                mapa01.put("width", screenwidth - (screenwidth / 3) - 8);
                mapa01.put("height", (screenheight) - (screenheight / 4) - 8);
                lista.add(mapa01);
                mapa02.put("x", screenwidth - (screenwidth / 3));
                mapa02.put("y", 4);
                mapa02.put("width", (screenwidth / 3) - 4);
                mapa02.put("height", (screenheight / 4) - 8);
                lista.add(mapa02);
                mapa03.put("x", screenwidth - (screenwidth / 3));
                mapa03.put("y", (screenheight / 4));
                mapa03.put("width", (screenwidth / 3) - 4);
                mapa03.put("height", (screenheight / 4) - 4);
                lista.add(mapa03);
                mapa04.put("x", screenwidth - (screenwidth / 3));
                mapa04.put("y", 2 * (screenheight / 4));
                mapa04.put("width", (screenwidth / 3) - 4);
                mapa04.put("height", (screenheight / 4) - 4);
                lista.add(mapa04);
                mapa05.put("x", 4);
                mapa05.put("y", 3 * (screenheight / 4));
                mapa05.put("width", (screenwidth / 3) - 8);
                mapa05.put("height", (screenheight / 4) - 4);
                lista.add(mapa06);
                mapa06.put("x", screenwidth - (screenwidth / 3) * 2);
                mapa06.put("y", 3 * (screenheight / 4));
                mapa06.put("width", (screenwidth / 3) - 4);
                mapa06.put("height", (screenheight / 4) - 4);
                lista.add(mapa05);
                mapa07.put("x", screenwidth - (screenwidth / 3));
                mapa07.put("y", 3 * (screenheight / 4));
                mapa07.put("width", (screenwidth / 3) - 4);
                mapa07.put("height", (screenheight / 4) - 4);
                lista.add(mapa07);
                break;
            case 4:
                lista.clear();
                mapa01.put("x", 4);
                mapa01.put("y", 4);
                mapa01.put("width", (screenwidth / 2) - 8);
                mapa01.put("height", (screenheight / 2) - 8);
                lista.add(mapa01);
                mapa02.put("x", (screenwidth / 2));
                mapa02.put("y", 4);
                mapa02.put("width", (screenwidth / 2) - 4);
                mapa02.put("height", (screenheight / 2) - 8);
                lista.add(mapa02);
                mapa03.put("x", 4);
                mapa03.put("y", (screenheight) - (screenheight / 2));
                mapa03.put("width", (screenwidth / 2) - 8);
                mapa03.put("height", (screenheight / 2) - 8);
                lista.add(mapa03);
                mapa04.put("x", (screenwidth / 2));
                mapa04.put("y", (screenheight) - (screenheight / 2));
                mapa04.put("width", (screenwidth / 2) - 4);
                mapa04.put("height", (screenheight / 2) - 8);
                lista.add(mapa04);
                break;
        }
        return lista;
    }

}
