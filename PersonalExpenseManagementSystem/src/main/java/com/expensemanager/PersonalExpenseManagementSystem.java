package com.expensemanager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class PersonalExpenseManagementSystem {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Could not set application theme: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> new ExpenseManagerFrame().setVisible(true));
    }
}
