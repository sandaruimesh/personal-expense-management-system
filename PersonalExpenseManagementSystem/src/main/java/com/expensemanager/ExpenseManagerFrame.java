package com.expensemanager;

import com.expensemanager.config.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManagerFrame extends JFrame {
    private static final Color BG = new Color(242, 243, 245);
    private static final Color PANEL = Color.WHITE;
    private static final Color SIDEBAR = new Color(57, 62, 70);
    private static final Color DARK = new Color(49, 54, 62);
    private static final Color MUTED = new Color(119, 125, 134);
    private static final Color LINE = new Color(224, 226, 230);
    private static final Color YELLOW = new Color(246, 196, 54);
    private static final Color YELLOW_PALE = new Color(255, 247, 218);
    private static final Font BODY = new Font("Arial", Font.PLAIN, 13);
    private static final Font BOLD = new Font("Arial", Font.BOLD, 13);
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private JTable expenseTable, dashboardTable, categoryTable;
    private DefaultTableModel expenseModel, dashboardModel, categoryModel;
    private JLabel totalValue, monthValue, budgetValue, remainingValue, dashboardRemainingValue, budgetRemainingLabel, dashboardMonth;
    private JLabel expenseCountValue, categoryCountValue;
    private JTextField searchField;
    private JComboBox<String> filterCategory;
    private JPanel dashboardPage;

    public ExpenseManagerFrame() {
        setTitle("Pennywise | Personal Expense Manager");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1240, 790);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        add(buildSidebar(), BorderLayout.WEST);
        content.setBackground(BG);
        content.add(buildDashboard(), "dashboard");
        content.add(buildExpenses(), "expenses");
        content.add(buildCategories(), "categories");
        content.add(buildBudget(), "budget");
        content.add(buildReports(), "reports");
        add(content, BorderLayout.CENTER);
        refreshAll();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(26, 18, 22, 18));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("PENNYWISE");
        brand.setFont(new Font("Arial", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        JLabel sub = new JLabel("PERSONAL FINANCE");
        sub.setFont(new Font("Arial", Font.BOLD, 9));
        sub.setForeground(new Color(195, 198, 203));
        side.add(brand); side.add(Box.createVerticalStrut(5)); side.add(sub);
        side.add(Box.createVerticalStrut(48));
        addNav(side, "▦   Dashboard", "dashboard", true);
        addNav(side, "↕   Expenses", "expenses", false);
        addNav(side, "▤   Categories", "categories", false);
        addNav(side, "◷   Monthly Budget", "budget", false);
        addNav(side, "▥   Reports", "reports", false);
        side.add(Box.createVerticalGlue());
        JLabel note = new JLabel("Small steps. Better spending.");
        note.setForeground(new Color(195, 198, 203)); note.setFont(new Font("Arial", Font.ITALIC, 10));
        side.add(note); side.add(Box.createVerticalStrut(16));
        JButton exit = navButton("↪   Exit");
        exit.addActionListener(e -> { if (JOptionPane.showConfirmDialog(this, "Close the application?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose(); });
        side.add(exit);
        return side;
    }

    private void addNav(JPanel side, String text, String page, boolean selected) {
        JButton b = navButton(text);
        if (selected) { b.setBackground(YELLOW); b.setForeground(DARK); }
        b.addActionListener(e -> { cards.show(content, page); if (page.equals("dashboard")) refreshDashboard(); });
        side.add(b); side.add(Box.createVerticalStrut(9));
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text); b.setFont(BOLD); b.setForeground(new Color(235,236,238));
        b.setBackground(SIDEBAR); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(13, 13, 13, 8)); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46)); b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JPanel pageBase(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout(0, 18)); page.setBackground(BG); page.setBorder(new EmptyBorder(28, 32, 28, 32));
        JPanel header = new JPanel(); header.setOpaque(false); header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(title); h.setFont(new Font("Arial", Font.BOLD, 27)); h.setForeground(DARK);
        JLabel s = new JLabel(subtitle); s.setFont(BODY); s.setForeground(MUTED);
        header.add(h); header.add(Box.createVerticalStrut(6)); header.add(s); page.add(header, BorderLayout.NORTH);
        return page;
    }

    private JPanel buildDashboard() {
        dashboardPage = pageBase("Dashboard", "A clear view of where your money goes.");
        JPanel body = new JPanel(new BorderLayout(0, 18)); body.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 3, 15, 0)); stats.setOpaque(false);
        totalValue = new JLabel("LKR 0.00"); monthValue = new JLabel("LKR 0.00"); dashboardRemainingValue = new JLabel("LKR 0.00"); budgetValue = new JLabel("Monthly budget: —");
        stats.add(statCard("ALL-TIME EXPENSES", totalValue, "Across all recorded expenses"));
        stats.add(statCard("THIS MONTH", monthValue, "Expenses in the current month"));
        stats.add(statCard("BUDGET REMAINING", dashboardRemainingValue, "After this month's expenses"));
        JPanel lower = new JPanel(new BorderLayout(14, 0)); lower.setOpaque(false);
        JPanel recent = whitePanel(); recent.setLayout(new BorderLayout(0, 12)); recent.setBorder(new EmptyBorder(18,18,18,18));
        JLabel rh = sectionTitle("Recent expenses"); recent.add(rh, BorderLayout.NORTH);
        dashboardModel = model("Description", "Category", "Date", "Amount (LKR)");
        dashboardTable = styledTable(dashboardModel); recent.add(new JScrollPane(dashboardTable), BorderLayout.CENTER);
        JPanel actions = whitePanel(); actions.setPreferredSize(new Dimension(220, 0)); actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS)); actions.setBorder(new EmptyBorder(18,16,18,16));
        actions.add(sectionTitle("Quick actions")); actions.add(Box.createVerticalStrut(18));
        JButton add = primaryButton("＋  Add expense"); add.addActionListener(e -> showExpenseDialog(null)); actions.add(add);
        actions.add(Box.createVerticalStrut(12)); JButton set = secondaryButton("Set monthly budget"); set.addActionListener(e -> showBudgetDialog()); actions.add(set);
        actions.add(Box.createVerticalStrut(12)); JButton report = secondaryButton("View reports"); report.addActionListener(e -> cards.show(content,"reports")); actions.add(report);
        actions.add(Box.createVerticalGlue());
        lower.add(recent, BorderLayout.CENTER); lower.add(actions, BorderLayout.EAST);
        body.add(stats, BorderLayout.NORTH); body.add(lower, BorderLayout.CENTER);
        dashboardPage.add(body, BorderLayout.CENTER);
        return dashboardPage;
    }

    private JPanel statCard(String caption, JLabel value, String detail) {
        JPanel p = whitePanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(17,18,15,18)));
        JLabel c = new JLabel(caption); c.setFont(new Font("Arial", Font.BOLD, 10)); c.setForeground(MUTED);
        value.setFont(new Font("Arial", Font.BOLD, 23)); value.setForeground(DARK);
        JLabel d = new JLabel(detail); d.setFont(new Font("Arial", Font.PLAIN, 11)); d.setForeground(MUTED);
        p.add(c); p.add(Box.createVerticalStrut(13)); p.add(value); p.add(Box.createVerticalStrut(8)); p.add(d);
        return p;
    }

    private JPanel buildExpenses() {
        JPanel page = pageBase("Expenses", "Record, search and manage your daily spending.");
        JPanel wrap = whitePanel(); wrap.setLayout(new BorderLayout(0,14)); wrap.setBorder(new EmptyBorder(18,18,18,18));
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); tools.setOpaque(false);
        searchField = new JTextField(18); searchField.setFont(BODY); searchField.setToolTipText("Search description");
        filterCategory = new JComboBox<>(); filterCategory.setFont(BODY);
        JButton search = secondaryButton("Search"); search.addActionListener(e -> loadExpenses());
        JButton clear = secondaryButton("Clear"); clear.addActionListener(e -> { searchField.setText(""); filterCategory.setSelectedItem("All categories"); loadExpenses(); });
        JButton add = primaryButton("＋ Add expense"); add.addActionListener(e -> showExpenseDialog(null));
        JButton edit = secondaryButton("Edit selected"); edit.addActionListener(e -> editSelectedExpense());
        JButton del = dangerButton("Delete selected"); del.addActionListener(e -> deleteSelectedExpense());
        JButton csv = secondaryButton("Export CSV"); csv.addActionListener(e -> exportCsv());
        tools.add(new JLabel("Search")); tools.add(searchField); tools.add(filterCategory); tools.add(search); tools.add(clear); tools.add(add); tools.add(edit); tools.add(del); tools.add(csv);
        expenseModel = model("ID", "Description", "Category", "Date", "Amount (LKR)");
        expenseTable = styledTable(expenseModel); expenseTable.setAutoCreateRowSorter(true);
        wrap.add(tools, BorderLayout.NORTH); wrap.add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        page.add(wrap, BorderLayout.CENTER); return page;
    }

    private JPanel buildCategories() {
        JPanel page = pageBase("Categories", "Organise expenses using default and custom categories.");
        JPanel wrap = whitePanel(); wrap.setLayout(new BorderLayout(0,14)); wrap.setBorder(new EmptyBorder(18,18,18,18));
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT,9,0)); tools.setOpaque(false);
        JButton add = primaryButton("＋ Add category"); add.addActionListener(e -> showCategoryDialog(null));
        JButton edit = secondaryButton("Edit selected"); edit.addActionListener(e -> editSelectedCategory());
        JButton del = dangerButton("Delete selected"); del.addActionListener(e -> deleteSelectedCategory());
        tools.add(add); tools.add(edit); tools.add(del);
        categoryModel = model("ID", "Category name", "Type"); categoryTable = styledTable(categoryModel);
        wrap.add(tools,BorderLayout.NORTH); wrap.add(new JScrollPane(categoryTable),BorderLayout.CENTER); page.add(wrap,BorderLayout.CENTER); return page;
    }

    private JPanel buildBudget() {
        JPanel page = pageBase("Monthly budget", "Set a spending limit and keep an eye on your progress.");
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = whitePanel(); card.setPreferredSize(new Dimension(540,330)); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(28,32,28,32));
        JLabel h = sectionTitle("Your monthly plan"); h.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetValue = new JLabel("Monthly budget: Not set"); budgetValue.setFont(new Font("Arial",Font.BOLD,19)); budgetValue.setForeground(DARK); budgetValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rem = new JLabel("Remaining amount"); rem.setForeground(MUTED); rem.setFont(BODY); rem.setAlignmentX(Component.LEFT_ALIGNMENT);
        budgetRemainingLabel = new JLabel("LKR 0.00"); budgetRemainingLabel.setFont(new Font("Arial",Font.BOLD,30)); budgetRemainingLabel.setForeground(DARK); budgetRemainingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton edit = primaryButton("Set / update budget"); edit.setAlignmentX(Component.LEFT_ALIGNMENT); edit.addActionListener(e -> showBudgetDialog());
        card.add(h); card.add(Box.createVerticalStrut(35)); card.add(budgetValue); card.add(Box.createVerticalStrut(28)); card.add(rem); card.add(Box.createVerticalStrut(8)); card.add(budgetRemainingLabel); card.add(Box.createVerticalStrut(28)); card.add(edit);
        center.add(card); page.add(center,BorderLayout.CENTER); return page;
    }

    private JPanel buildReports() {
        JPanel page = pageBase("Reports", "A simple breakdown of your spending by category this month.");
        JPanel wrap = whitePanel(); wrap.setLayout(new BorderLayout(0,12)); wrap.setBorder(new EmptyBorder(18,18,18,18));
        JLabel title = sectionTitle("Monthly category summary"); wrap.add(title,BorderLayout.NORTH);
        JPanel chart = new CategoryChart(); wrap.add(chart,BorderLayout.CENTER);
        JButton refresh = secondaryButton("Refresh report"); refresh.addActionListener(e -> { chart.repaint(); });
        wrap.add(refresh,BorderLayout.SOUTH); page.add(wrap,BorderLayout.CENTER); return page;
    }

    private class CategoryChart extends JPanel {
        CategoryChart() { setBackground(Color.WHITE); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            List<String[]> rows = new ArrayList<>();
            try(Connection c=DBConnection.getConnection()) { if(c!=null) try(PreparedStatement p=c.prepareStatement("SELECT c.category_name, COALESCE(SUM(e.amount),0) total FROM Category c LEFT JOIN Expense e ON e.category_id=c.category_id AND YEAR(e.expense_date)=YEAR(CURDATE()) AND MONTH(e.expense_date)=MONTH(CURDATE()) GROUP BY c.category_id,c.category_name ORDER BY total DESC"); ResultSet r=p.executeQuery()){ while(r.next()) rows.add(new String[]{r.getString(1),r.getString(2)}); } } catch(SQLException ex) { }
            double max=1; for(String[] row:rows) max=Math.max(max,Double.parseDouble(row[1]));
            int y=24; for(String[] row:rows){ if(y>getHeight()-32) break; String name=row[0]; double val=Double.parseDouble(row[1]); int bar=(int)((getWidth()-220)*(val/max));
                g2.setFont(BODY); g2.setColor(DARK); g2.drawString(name,8,y+15); g2.setColor(new Color(235,237,240)); g2.fillRoundRect(130,y,getWidth()-250,17,9,9); g2.setColor(YELLOW); g2.fillRoundRect(130,y,Math.max(2,bar),17,9,9); g2.setColor(MUTED); g2.drawString(String.format("LKR %,.2f",val),Math.max(140,140+bar),y+14); y+=38; }
            if(rows.isEmpty()){g2.setColor(MUTED);g2.drawString("No category data available. Check your database connection.",8,30);} g2.dispose();
        }
    }

    private JPanel whitePanel() { JPanel p=new JPanel(); p.setBackground(PANEL); p.setBorder(new LineBorder(LINE)); return p; }
    private JLabel sectionTitle(String s) { JLabel l=new JLabel(s); l.setFont(new Font("Arial",Font.BOLD,16)); l.setForeground(DARK); return l; }
    private JButton primaryButton(String s) { JButton b=new JButton(s); b.setFont(BOLD); b.setBackground(YELLOW); b.setForeground(DARK); styleButton(b); return b; }
    private JButton secondaryButton(String s) { JButton b=new JButton(s); b.setFont(BODY); b.setBackground(new Color(239,240,242)); b.setForeground(DARK); styleButton(b); return b; }
    private JButton dangerButton(String s) { JButton b=new JButton(s); b.setFont(BODY); b.setBackground(new Color(250,232,231)); b.setForeground(new Color(156,55,50)); styleButton(b); return b; }
    private void styleButton(JButton b) { b.setFocusPainted(false); b.setBorder(new CompoundBorder(new LineBorder(LINE),new EmptyBorder(8,12,8,12))); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
    private DefaultTableModel model(String... cols) { return new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){return false;} }; }
    private JTable styledTable(DefaultTableModel m) { JTable t=new JTable(m); t.setFont(BODY); t.setRowHeight(32); t.setGridColor(LINE); t.setShowVerticalLines(false); t.setSelectionBackground(YELLOW_PALE); t.setSelectionForeground(DARK); t.getTableHeader().setFont(new Font("Arial",Font.BOLD,12)); t.getTableHeader().setBackground(new Color(225,227,230)); t.getTableHeader().setForeground(DARK); t.getTableHeader().setPreferredSize(new Dimension(0,38)); return t; }

    private void refreshAll() { loadCategories(); loadExpenses(); refreshDashboard(); }
    private void refreshDashboard() {
        try(Connection c=DBConnection.getConnection()) { if(c==null) return;
            try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COALESCE(SUM(amount),0) FROM Expense")){if(r.next()) totalValue.setText(money(r.getBigDecimal(1)));}
            try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM Expense WHERE YEAR(expense_date)=YEAR(CURDATE()) AND MONTH(expense_date)=MONTH(CURDATE())"); ResultSet r=p.executeQuery()){if(r.next()) monthValue.setText(money(r.getBigDecimal(1)));}
            BigDecimal budget=getCurrentBudget(c), spent=getCurrentMonthSpent(c); BigDecimal remaining=budget.subtract(spent); dashboardRemainingValue.setText(money(remaining)); if(budgetRemainingLabel!=null)budgetRemainingLabel.setText(money(remaining)); budgetValue.setText("Monthly budget: "+(budget.signum()==0?"Not set":money(budget)));
            if(expenseCountValue!=null) expenseCountValue.setText(" ");
            loadRecent(c);
        } catch(SQLException ex) { showDbError(ex); }
    }
    private void loadRecent(Connection c) throws SQLException { if(dashboardModel==null) return; dashboardModel.setRowCount(0); try(PreparedStatement p=c.prepareStatement("SELECT e.description,c.category_name,e.expense_date,e.amount FROM Expense e JOIN Category c ON c.category_id=e.category_id ORDER BY e.expense_date DESC,e.expense_id DESC LIMIT 8");ResultSet r=p.executeQuery()){while(r.next()) dashboardModel.addRow(new Object[]{r.getString(1),r.getString(2),r.getDate(3),money(r.getBigDecimal(4))});} }
    private void loadExpenses() {
        if(expenseModel==null || expenseModel.getColumnCount()!=5) return; expenseModel.setRowCount(0);
        String search=searchField==null?"":searchField.getText().trim(); String cat=filterCategory==null?"All categories":(String)filterCategory.getSelectedItem();
        boolean filterByCategory = cat != null && !cat.equals("All categories");
        String sql="SELECT e.expense_id,e.description,c.category_name,e.expense_date,e.amount FROM Expense e JOIN Category c ON c.category_id=e.category_id WHERE e.description LIKE ?"+(filterByCategory?" AND c.category_name=?":"")+" ORDER BY e.expense_date DESC,e.expense_id DESC";
        try(Connection c=DBConnection.getConnection()){if(c==null)return;try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,"%"+search+"%");if(filterByCategory)p.setString(2,cat);try(ResultSet r=p.executeQuery()){while(r.next())expenseModel.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getDate(4),money(r.getBigDecimal(5))});}}}catch(SQLException ex){showDbError(ex);}
    }
    private void loadCategories() {
        if(categoryModel!=null){categoryModel.setRowCount(0);try(Connection c=DBConnection.getConnection()){if(c==null)return;try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT category_id,category_name,is_default FROM Category ORDER BY is_default DESC,category_name")){while(r.next())categoryModel.addRow(new Object[]{r.getInt(1),r.getString(2),r.getBoolean(3)?"Default":"Custom"});}}catch(SQLException ex){showDbError(ex);}}
        if(filterCategory!=null){String selected=(String)filterCategory.getSelectedItem();filterCategory.removeAllItems();filterCategory.addItem("All categories");try(Connection c=DBConnection.getConnection()){if(c!=null)try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT category_name FROM Category ORDER BY category_name")){while(r.next())filterCategory.addItem(r.getString(1));}}catch(SQLException ex){showDbError(ex);}if(selected!=null)filterCategory.setSelectedItem(selected);}
    }
    private BigDecimal getCurrentBudget(Connection c) throws SQLException {try(PreparedStatement p=c.prepareStatement("SELECT budget_amount FROM Budget WHERE budget_month=?")){p.setDate(1,Date.valueOf(YearMonth.now().atDay(1)));try(ResultSet r=p.executeQuery()){return r.next()?r.getBigDecimal(1):BigDecimal.ZERO;}}}
    private BigDecimal getCurrentMonthSpent(Connection c) throws SQLException {try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM Expense WHERE YEAR(expense_date)=YEAR(CURDATE()) AND MONTH(expense_date)=MONTH(CURDATE())");ResultSet r=p.executeQuery()){r.next();return r.getBigDecimal(1);}}
    private String money(BigDecimal n) { return "LKR "+String.format("%,.2f",n==null?BigDecimal.ZERO:n); }
    private void showDbError(SQLException ex) { JOptionPane.showMessageDialog(this,"Could not load database information. Check MySQL is running and DBConnection settings are correct.\n\n"+ex.getMessage(),"Database connection",JOptionPane.WARNING_MESSAGE); }

    private void showExpenseDialog(Integer id) {
        JTextField desc=new JTextField(22), amount=new JTextField(14), date=new JTextField(LocalDate.now().toString(),14); JComboBox<CategoryItem> cat=new JComboBox<>();
        try(Connection c=DBConnection.getConnection()){if(c!=null)try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT category_id,category_name FROM Category ORDER BY category_name")){while(r.next())cat.addItem(new CategoryItem(r.getInt(1),r.getString(2)));}}catch(SQLException ex){showDbError(ex);return;}
        if(id!=null){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT description,amount,expense_date,category_id FROM Expense WHERE expense_id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(r.next()){desc.setText(r.getString(1));amount.setText(r.getBigDecimal(2).toPlainString());date.setText(r.getDate(3).toString());for(int i=0;i<cat.getItemCount();i++)if(cat.getItemAt(i).id==r.getInt(4))cat.setSelectedIndex(i);}}}catch(SQLException ex){showDbError(ex);return;}}
        JPanel form=formPanel(new String[]{"Description","Amount (LKR)","Date (YYYY-MM-DD)","Category"},new JComponent[]{desc,amount,date,cat});
        int result=JOptionPane.showConfirmDialog(this,form,id==null?"Add expense":"Edit expense",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result!=JOptionPane.OK_OPTION)return;
        try{String d=desc.getText().trim();BigDecimal a=new BigDecimal(amount.getText().trim());LocalDate dt=LocalDate.parse(date.getText().trim());CategoryItem ci=(CategoryItem)cat.getSelectedItem();if(d.isEmpty()||a.signum()<=0||dt.isAfter(LocalDate.now())||ci==null)throw new IllegalArgumentException("Enter a description, positive amount, valid category and a date that is not in the future.");
            try(Connection c=DBConnection.getConnection()){if(c==null)return;String sql=id==null?"INSERT INTO Expense(description,amount,expense_date,category_id) VALUES(?,?,?,?)":"UPDATE Expense SET description=?,amount=?,expense_date=?,category_id=? WHERE expense_id=?";try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,d);p.setBigDecimal(2,a);p.setDate(3,Date.valueOf(dt));p.setInt(4,ci.id);if(id!=null)p.setInt(5,id);p.executeUpdate();}}
            refreshAll();
        }catch(IllegalArgumentException ex){JOptionPane.showMessageDialog(this,"Please check the values. Amount must be positive and date must use YYYY-MM-DD, not a future date.\n"+ex.getMessage(),"Invalid expense",JOptionPane.ERROR_MESSAGE);}catch(SQLException ex){showDbError(ex);}
    }
    private JPanel formPanel(String[] labels,JComponent[] fields){JPanel p=new JPanel(new GridLayout(labels.length,2,10,12));p.setBorder(new EmptyBorder(10,8,10,8));for(int i=0;i<labels.length;i++){p.add(new JLabel(labels[i]));p.add(fields[i]);}return p;}
    private void editSelectedExpense(){int row=expenseTable.getSelectedRow();if(row<0){message("Select an expense first.");return;}int modelRow=expenseTable.convertRowIndexToModel(row);showExpenseDialog(Integer.valueOf(expenseModel.getValueAt(modelRow,0).toString()));}
    private void deleteSelectedExpense(){int row=expenseTable.getSelectedRow();if(row<0){message("Select an expense first.");return;}int mr=expenseTable.convertRowIndexToModel(row);int id=Integer.parseInt(expenseModel.getValueAt(mr,0).toString());if(JOptionPane.showConfirmDialog(this,"Permanently delete this expense?","Confirm delete",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try(Connection c=DBConnection.getConnection()){if(c==null)return;try(PreparedStatement p=c.prepareStatement("DELETE FROM Expense WHERE expense_id=?")){p.setInt(1,id);p.executeUpdate();}refreshAll();}catch(SQLException ex){showDbError(ex);}}
    private void showCategoryDialog(Integer id){String value=id==null?"":JOptionPane.showInputDialog(this,"Category name:","Add category",JOptionPane.PLAIN_MESSAGE);if(id!=null){int row=categoryTable.getSelectedRow();value=JOptionPane.showInputDialog(this,"Category name:",categoryModel.getValueAt(categoryTable.convertRowIndexToModel(row),1));}if(value==null)return;value=value.trim();if(value.isEmpty()){message("Category name cannot be empty.");return;}try(Connection c=DBConnection.getConnection()){if(c==null)return;String sql=id==null?"INSERT INTO Category(category_name,is_default) VALUES(?,FALSE)":"UPDATE Category SET category_name=? WHERE category_id=? AND is_default=FALSE";try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,value);if(id!=null)p.setInt(2,id);int n=p.executeUpdate();if(id!=null&&n==0)message("Default categories cannot be edited.");}}catch(SQLException ex){showDbError(ex);}loadCategories();loadExpenses();}
    private void editSelectedCategory(){int row=categoryTable.getSelectedRow();if(row<0){message("Select a category first.");return;}int mr=categoryTable.convertRowIndexToModel(row);if("Default".equals(categoryModel.getValueAt(mr,2))){message("Default categories are protected.");return;}showCategoryDialog(Integer.valueOf(categoryModel.getValueAt(mr,0).toString()));}
    private void deleteSelectedCategory(){int row=categoryTable.getSelectedRow();if(row<0){message("Select a category first.");return;}int mr=categoryTable.convertRowIndexToModel(row);if("Default".equals(categoryModel.getValueAt(mr,2))){message("Default categories cannot be deleted.");return;}int id=Integer.parseInt(categoryModel.getValueAt(mr,0).toString());if(JOptionPane.showConfirmDialog(this,"Delete this category? Categories used by expenses cannot be deleted.","Confirm delete",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try(Connection c=DBConnection.getConnection()){if(c==null)return;try(PreparedStatement p=c.prepareStatement("DELETE FROM Category WHERE category_id=? AND is_default=FALSE")){p.setInt(1,id);p.executeUpdate();}loadCategories();loadExpenses();}catch(SQLException ex){JOptionPane.showMessageDialog(this,"This category may still be assigned to expenses. Reassign or delete those expenses first.\n"+ex.getMessage(),"Cannot delete category",JOptionPane.WARNING_MESSAGE);}}
    private void showBudgetDialog(){JTextField amount=new JTextField(15);try(Connection c=DBConnection.getConnection()){if(c!=null){BigDecimal b=getCurrentBudget(c);if(b.signum()>0)amount.setText(b.toPlainString());}}catch(SQLException ex){showDbError(ex);}JPanel p=formPanel(new String[]{"Budget for this month (LKR)"},new JComponent[]{amount});if(JOptionPane.showConfirmDialog(this,p,"Set monthly budget",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;try{BigDecimal val=new BigDecimal(amount.getText().trim());if(val.signum()<=0)throw new NumberFormatException();try(Connection c=DBConnection.getConnection()){if(c==null)return;try(PreparedStatement q=c.prepareStatement("INSERT INTO Budget(budget_month,budget_amount) VALUES(?,?) ON DUPLICATE KEY UPDATE budget_amount=VALUES(budget_amount)")){q.setDate(1,Date.valueOf(YearMonth.now().atDay(1)));q.setBigDecimal(2,val);q.executeUpdate();}}refreshDashboard();}catch(Exception ex){message("Enter a valid budget amount greater than zero.");}}
    private void exportCsv(){JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("expenses.csv"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;try(FileWriter w=new FileWriter(chooser.getSelectedFile())){w.write("ID,Description,Category,Date,Amount (LKR)\n");for(int i=0;i<expenseModel.getRowCount();i++){for(int j=0;j<5;j++){if(j>0)w.write(",");String v=String.valueOf(expenseModel.getValueAt(i,j)).replace("\"","\"\"");w.write("\""+v+"\"");}w.write("\n");}message("CSV exported successfully.");}catch(Exception ex){message("Could not export CSV: "+ex.getMessage());}}
    private void message(String s){JOptionPane.showMessageDialog(this,s,"Pennywise",JOptionPane.INFORMATION_MESSAGE);}
    private static class CategoryItem { final int id; final String name; CategoryItem(int i,String n){id=i;name=n;} @Override public String toString(){return name;} }
}
