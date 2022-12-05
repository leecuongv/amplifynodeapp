package com.foa.orderfood.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.foa.orderfood.Model.User;
import com.foa.orderfood.Remote.IGoogleService;
import com.foa.orderfood.Remote.RetrofitClient;


public class Common {
    public static User currentUser;
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    private static final String BASE_URL = "https://maps.googleapis.com/";

    public static IGoogleService getGoogleMapAPI(){
        return RetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Đã đặt hàng!";
        else if (status.equals("1"))
            return "Đang gửi thức ăn!";
        else
            return "Đã gửi thức ăn!";


    }

    public static boolean isConnectedToInterner(Context context){
        ConnectivityManager connectivityManager =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info!=null){
                for (int i=0;i<info.length;i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PDW_KEY= "Password";

}
