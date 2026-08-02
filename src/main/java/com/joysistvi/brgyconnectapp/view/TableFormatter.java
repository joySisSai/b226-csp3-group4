package com.joysistvi.brgyconnectapp.view;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {
    private final String[] headers;
    private final List<String[]> rows = new ArrayList<>();
    private final int[] colWidths;
    
    public TableFormatter(String... headers) {
        this.headers = headers;
        this.colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }
    }
    
    public void addRow(String... row) {
        if (row.length != headers.length) {
            throw new IllegalArgumentException("Row length must match header length");
        }
        rows.add(row);
        for (int i = 0; i < row.length; i++) {
            if (row[i] != null && row[i].length() > colWidths[i]) {
                colWidths[i] = row[i].length();
            }
        }
    }
    
    public void print() {
        if (headers.length == 0) return;

        // Top border
        System.out.print(ConsoleUI.BLUE + "╔");
        for (int i = 0; i < colWidths.length; i++) {
            System.out.print("═".repeat(colWidths[i] + 2));
            if (i < colWidths.length - 1) System.out.print("╦");
        }
        System.out.println("╗" + ConsoleUI.RESET);
        
        // Headers
        System.out.print(ConsoleUI.BLUE + "║" + ConsoleUI.RESET);
        for (int i = 0; i < headers.length; i++) {
            System.out.printf(ConsoleUI.CYAN + ConsoleUI.BOLD + " %-" + colWidths[i] + "s " + ConsoleUI.RESET + ConsoleUI.BLUE + "║" + ConsoleUI.RESET, headers[i]);
        }
        System.out.println();
        
        // Middle border
        System.out.print(ConsoleUI.BLUE + "╠");
        for (int i = 0; i < colWidths.length; i++) {
            System.out.print("═".repeat(colWidths[i] + 2));
            if (i < colWidths.length - 1) System.out.print("╬");
        }
        System.out.println("╣" + ConsoleUI.RESET);
        
        // Rows
        for (String[] row : rows) {
            System.out.print(ConsoleUI.BLUE + "║" + ConsoleUI.RESET);
            for (int i = 0; i < row.length; i++) {
                System.out.printf(" %-" + colWidths[i] + "s " + ConsoleUI.BLUE + "║" + ConsoleUI.RESET, row[i] == null ? "" : row[i]);
            }
            System.out.println();
        }
        
        // Bottom border
        System.out.print(ConsoleUI.BLUE + "╚");
        for (int i = 0; i < colWidths.length; i++) {
            System.out.print("═".repeat(colWidths[i] + 2));
            if (i < colWidths.length - 1) System.out.print("╩");
        }
        System.out.println("╝" + ConsoleUI.RESET);
    }
}
