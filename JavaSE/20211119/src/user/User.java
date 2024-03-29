package user;

import book.BookList;
import operation.IOperation;

abstract public class User {
    protected String name;

    protected IOperation[] iOperations;

    public User(String name) {
        this.name = name;
    }

    public abstract int menu();

    public void doWork(int choice, BookList bookList) {
        iOperations[choice].work(bookList); // 对象.方法
    }
}
