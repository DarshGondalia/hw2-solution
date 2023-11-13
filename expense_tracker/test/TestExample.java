// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import view.ExpenseTrackerView;


public class TestExample {
  
    private ExpenseTrackerModel model;
    private ExpenseTrackerView view;
    private ExpenseTrackerController controller;

    @Before
    public void setup() {
        model = new ExpenseTrackerModel();
        view = new ExpenseTrackerView();
        controller = new ExpenseTrackerController(model, view);
    }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }

    public void checkTransaction(double amount, String category, Transaction transaction) {
        assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }

    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
        //                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
    
        // Check the total  
        assertEquals(amount, getTotalCost(), 0.01);
    }

    @Test
    public void testAddTransactionViewUpdate(){
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // Post-condition: List of transactions contains only the added transaction
        assertEquals(1, model.getTransactions().size());

        // Check that the view has been updated
        assertEquals(1, view.getTableModel().getRowCount()-1);

        // Check the contents of the view
        List<Transaction> displayedTransactions = model.getTransactions();
        List<Transaction> transactionsInView = getDisplayedTransactions();

        for(int i = 0; i < displayedTransactions.size(); i++){
            Transaction transaction = displayedTransactions.get(i);
            Transaction currTransactionInView = transactionsInView.get(i);
            assertEquals(transaction.getAmount(), currTransactionInView.getAmount(), 0.01);
            assertEquals(transaction.getCategory(), currTransactionInView.getCategory());
        }
    }

    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
        //                the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);
    
        // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }
    
    @Test
    public void testInvalidInputHandling(){
        // Precondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction with invalid amount
        double amount = -20.0;
        String validCategory = "food";
        assertFalse(controller.addTransaction(amount, validCategory));

        // Attempt to add a transaction with invalid category
        double validAmount = 10.0;
        String invalidCategory = "invalid";
        assertFalse(controller.addTransaction(amount, invalidCategory));

        // Postcondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    }

    public List<Transaction> getHighlightedTransactions() {
        List<Transaction> highlightedTransactions = new ArrayList<>();
        JTable transactionsTable = view.getTransactionsTable();
        DefaultTableModel tableModel = (DefaultTableModel) transactionsTable.getModel();
        DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) transactionsTable.getDefaultRenderer(Object.class);

        for (int row = 0; row < transactionsTable.getRowCount(); row++) {
            Component c = cellRenderer.getTableCellRendererComponent(transactionsTable,
                    transactionsTable.getValueAt(row, 0), false, false, row, 0);
            Color backgroundColor = c.getBackground();

            // Check if the background color matches the highlight color
            if (backgroundColor.equals(new Color(173, 255, 168))) {
                double amount = (double) tableModel.getValueAt(row, 1);
                String category = (String) tableModel.getValueAt(row, 2);
                String timestamp = (String) tableModel.getValueAt(row, 3);
                Transaction transaction = new Transaction(amount, category);
                highlightedTransactions.add(transaction);
            }
        }

        return highlightedTransactions;
    }

    @Test
    public void testFilterByAmount(){
        // Precondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        //Add multiple transactions with different amounts
        double amount1 = 10.0;
        double amount2 = 20.0;
        double amount3 = 30.0;
        String category = "food";

        controller.addTransaction(amount1, category);
        controller.addTransaction(amount2, category);
        controller.addTransaction(amount3, category);

        // Set filter to amount amount filter and filter by amount1
        AmountFilter f = new AmountFilter(amount1);
        controller.setFilter(f);
        controller.applyFilter();

        // Check that only one transaction is returned
        List<Transaction> filteredByAmount = getHighlightedTransactions();
        assertEquals(1, filteredByAmount.size());
        
        Transaction filteredTransaction = filteredByAmount.get(0);
        checkTransaction(amount1, category, filteredTransaction);

    }

    @Test 
    public void testFilterByCategory(){
        //Precondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        //Add multiple transactions with different categories
        double amount = 10.0;
        String category1 = "food";
        String category2 = "transport";
        String category3 = "entertainment";

        controller.addTransaction(amount, category1);
        controller.addTransaction(amount, category2);
        controller.addTransaction(amount, category3);

        //Set filter to category filter and filter by category1
        controller.setFilter(new CategoryFilter(category1));
        controller.applyFilter();

        //Check that only one transaction is returned
        List<Transaction> filteredByCategory = getHighlightedTransactions();
        assertEquals(1, filteredByCategory.size());

        Transaction filteredTransaction = filteredByCategory.get(0);
        checkTransaction(amount, category1, filteredTransaction);
    }

    @Test
    public void testUndoDisallowed(){
        //Precondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        //Attempt to undo an empty list of transactions
        controller.undoTransaction();

        //Postcondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    }

    @Test
    public void testUndoAllowed(){
        //Precondition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        //Add a transaction
        double amount = 10.0;
        String category = "food";
        controller.addTransaction(amount, category);

        //Undo the last transaction
        controller.undoTransaction();

        //Check that the last transaction was removed
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, model.getTransactions().size());

        //Check totalCost
        assertEquals(0, getTotalCost(), 0.01);
    }

    private List<Transaction> getDisplayedTransactions() {
        List<Transaction> transactionsInView = new ArrayList<>();
        JTable transactionsTable = view.getTransactionsTable();
        DefaultTableModel tableModel = (DefaultTableModel) transactionsTable.getModel();

        for (int row = 0; row < transactionsTable.getRowCount()-1; row++) {
            double amount = (double) tableModel.getValueAt(row, 1);
            String category = (String) tableModel.getValueAt(row, 2);
            Transaction transaction = new Transaction(amount, category);
            transactionsInView.add(transaction);
        }

        return transactionsInView;
    }
    
}
