package com.example.pharmago.database;

/**
 * Asynchronous Database Callback Interface for Member 2 Backend Service
 * Ensures background JDBC operations return results safely to the UI thread.
 *
 * @param <T> Type of the operation result
 */
public interface DbCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}
