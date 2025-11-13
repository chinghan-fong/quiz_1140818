package com.example.quiz_1140818.response;

public class AdminRes extends BasicRes {
	 private boolean admin;

	    public AdminRes() {
	        super();
	    }

	    public AdminRes(int code, String message, boolean admin) {
	        // 呼叫父類別建構子
	        super(code, message);
	        this.admin = admin;
	    }

	    public boolean isAdmin() {
	        return admin;
	    }

	    public void setAdmin(boolean admin) {
	        this.admin = admin;
	    }
	

}
