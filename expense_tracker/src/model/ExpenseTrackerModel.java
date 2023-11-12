package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ExpenseTrackerModel {

  //encapsulation - data integrity
  private List<Transaction> transactions;
  private Stack<Transaction> undoStack;

  public ExpenseTrackerModel() {
    transactions = new ArrayList<>();
    undoStack = new Stack<>();
  }

  public void addTransaction(Transaction t) {
    // Perform input validation to guarantee that all transactions added are non-null.
    if (t == null) {
      throw new IllegalArgumentException("The new transaction must be non-null.");
    }
    transactions.add(t);
    undoStack.push(t);
  }

  public void removeTransaction(Transaction t) {
    transactions.remove(t);
    undoStack.push(t);
  }

  public List<Transaction> getTransactions() {
    //encapsulation - data integrity
    return Collections.unmodifiableList(new ArrayList<>(transactions));
  }

  public boolean isUndoStackEmpty() {
    return undoStack.isEmpty();
  }

  public void undo() {
    if (!undoStack.isEmpty()) {
      Transaction t = undoStack.pop();
      transactions.remove(t);
    }
  }

}
