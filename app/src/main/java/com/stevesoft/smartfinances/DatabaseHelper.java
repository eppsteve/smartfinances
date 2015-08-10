package com.stevesoft.smartfinances;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stevesoft.smartfinances.model.Account;
import com.stevesoft.smartfinances.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 7/18/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "finance.db";


    public static final String CREATE_ACCOUNT_TABLE = "CREATE TABLE ACCOUNT (_id INTEGER PRIMARY KEY, NAME TEXT, AMOUNT REAL, CURRENCY TEXT)";
    public static final String CREATE_CATEGORY_TABLE = "CREATE TABLE CATEGORY (_id INTEGER PRIMARY KEY, NAME TEXT)";
    public static final String CREATE_TRANSACTION_TABLE = "CREATE TABLE TRANSACTIONS (_id INTEGER PRIMARY KEY, DATE TEXT, PRICE REAL, DESCRIPTION TEXT, " +
            "CATEGORY_ID INTEGER, ACCOUNT_ID INTEGER, FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORY(_id), " +
            "FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(_id) )";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNT_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_TRANSACTION_TABLE);

        // insert sample date
        db.execSQL("INSERT INTO ACCOUNT (NAME, AMOUNT, CURRENCY) VALUES ('My Account', 5000, 'EUR')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Food')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Transport')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Clothing')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Entertainment')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Household')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Bills')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Healthcare')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Other Expenses')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Income')");
        db.execSQL("INSERT INTO CATEGORY (NAME) VALUES ('Transfer')");
        db.execSQL("INSERT INTO TRANSACTIONS (DATE, PRICE, DESCRIPTION, CATEGORY_ID, ACCOUNT_ID) VALUES ('2015-08-01', -24, 'Super Market', 1, 1)");
//        db.execSQL("INSERT INTO TRANSACTIONS (DATE, PRICE, DESCRIPTION, CATEGORY_ID, ACCOUNT_ID) VALUES ('2015-08-01', -48, 'Weekend', 2, 1)");
//        db.execSQL("INSERT INTO TRANSACTIONS (DATE, PRICE, DESCRIPTION, CATEGORY_ID, ACCOUNT_ID) VALUES ('2015-08-01', -5.40, 'coffee', 4, 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TRANSACTION");
        db.execSQL("DROP TABLE IF EXISTS ACCOUNT");
        db.execSQL("DROP TABLE IF EXISTS TRANSACTION");
        onCreate(db);
    }

    public boolean insertTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("DATE", transaction.getDate());
        contentValues.put("PRICE", transaction.getPrice());
        contentValues.put("DESCRIPTION", transaction.getDescription());
        contentValues.put("CATEGORY_ID", transaction.getCategory_id());
        contentValues.put("ACCOUNT_ID", transaction.getAccount_id());
        long result = db.insert("TRANSACTIONS", null, contentValues);
        //db.execSQL("INSERT INTO TRANSACTIONS (DATE, PRICE, DESCRIPTION, CATEGORY_ID, ACCOUNT_ID) VALUES ('"+transaction.getDate() +"', "+transaction.getPrice()+", '"+transaction.getDescription()+"', "+transaction.getCategory_id()+", "+transaction.getAccount_id()+")");

        // Update account's amount
        String query = "UPDATE ACCOUNT SET AMOUNT = AMOUNT + "+transaction.getPrice()+" WHERE _id = (SELECT ACCOUNT_ID FROM TRANSACTIONS ORDER BY _id DESC LIMIT 1)";
        db.execSQL(query);

        if (result==-1)
            return false;
        else
            return true;
    }

    public boolean insertAccount(Account account){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", account.getName());
        contentValues.put("AMOUNT", account.getAmount());
        contentValues.put("CURRENCY", account.getCurrency());
        long result = db.insert("ACCOUNT", null, contentValues);

        if (result==-1)
            return false;
        else
            return true;
    }


    public Cursor getAllTransactions(){

        // Get access to the underlying writeable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT " +
                "TRANSACTIONS._id, TRANSACTIONS.DATE, TRANSACTIONS.PRICE, TRANSACTIONS.DESCRIPTION, CATEGORY.NAME AS CATEGORY_NAME, TRANSACTIONS.ACCOUNT_ID " +
                "FROM TRANSACTIONS " +
                "INNER JOIN CATEGORY ON CATEGORY._ID = TRANSACTIONS.CATEGORY_ID", null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getNetWorth(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT CURRENCY, SUM(AMOUNT) AS BALANCE FROM ACCOUNT GROUP BY CURRENCY";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getAllAccounts(){
        // Get access to the underlying writeable database
        SQLiteDatabase db = this.getWritableDatabase();
        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT * FROM ACCOUNT", null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getAllCategories(){
        // Get access to the underlying writeable database
        SQLiteDatabase db = this.getWritableDatabase();
        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT * FROM CATEGORY", null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    // gets all the expenses of the current month BY CATEGORY
    public Cursor getThisMonthExpenses(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TRANSACTIONS.DATE, SUM(PRICE) AS PRICE, CATEGORY.NAME AS CATEGORY\n" +
                "FROM TRANSACTIONS " +
                "JOIN CATEGORY ON TRANSACTIONS.CATEGORY_id = CATEGORY._id " +
                "WHERE DATE BETWEEN date('now','start of month') AND date('now','start of month', '+1 months', '-1 day') " +
                "GROUP BY CATEGORY.NAME", null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    // returns the balance of the current month
    public double getThisMonthBalance() {
        double balance = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(PRICE) AS BALANCE, DATE " +
                "FROM TRANSACTIONS " +
                "WHERE DATE BETWEEN date('now','start of month') AND date('now','start of month', '+1 months', '-1 day')", null);
        if (cursor != null) {
            cursor.moveToFirst();
            balance = cursor.getFloat(0);
            Log.e("BALANCE", balance+"");
        }
        return balance;
    }
}
