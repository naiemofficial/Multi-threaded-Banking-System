import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class Main extends Account {
    public static void main(String[] args) {
        JDBC DBMS = new JDBC();
        Account account = new Account();
        try {
            DBMS.createDatabase();
            DBMS.initializeTables();




            // // ------------------- CRETAE ACCOUNT -------------------
            // int[] single_account = account.create("Abdullah Al Naiem");                                                  // Single account
            // System.out.println(single_account[0]);
            // int[] multiple_accounts = account.create(List.of("Abdullah Al Naiem", "Imtiaj Uddin Shamim"));               // Multiple accounts
            // System.out.println(Arrays.toString(multiple_accounts));




            // // ------------------- VIEW ACCOUNT -------------------
            // account.view(1);                                                                                             // Single account by ID
            // account.view("Abdullah Al Naiem");                                                                           // Single account by name
            // account.view(List.of(1, 2));                                                                                 // Multiple accounts by ID
            // account.view(List.of("Abdullah Al Naiem", "Imtiaj Uddin Shamim"));     




            // // ------------------- UPDATE ACCOUNT -------------------
            // account.update(1, "Abdullah Al Naiem");




            
            // // ------------------- DELETE ACCOUNT ---------------------
            // Map<Integer, Boolean> delete_single = account.delete(5);      // Single account
            // System.out.println(delete_single);
            // Map<Integer, Boolean> delete_multiple = account.delete(List.of(2, 3));      // Multiple accounts
            // System.out.println(delete_multiple);                                      // Multiple accounts by name




            // // ------------------- ADD BALANCE -------------------
            // Map<Integer, List<Map<String, Object>>> single_account = account.addBalance(1, 800);                                             // Add Balance to Single account
            // System.out.println(single_account);
            // Map<Integer, List<Map<String, Object>>> multiple_accounts = account.addBalance(List.of(2, 3), 1200);                             // Add Balance to Multiple accounts
            // System.out.println(multiple_accounts);
            // Map<Integer, List<Map<String, Object>>> single_account_multiple_amounts = account.addBalance(4, List.of(500, 600, 700));         // Add Balance to Single account with Multiple amounts
            // System.out.println(single_account_multiple_amounts);



            // // ------------------- TRANSFER BALANCE -------------------
            // Map<Integer, List<Map<String, Object>>> transfer_balance_single = account.transferBalance(1, 2, 250);      // Single to Single Account
            // System.out.println(transfer_balance_single);
            // Map<Integer, List<Map<String, Object>>> transfer_balance_multiple = account.transferBalance(1, List.of(2, 3), 350);      // Single to Multiple Accounts
            // System.out.println(transfer_balance_multiple);
            // Map<Integer, List<Map<String, Object>>> transfer_balance_multiple_repeated = account.transferBalance(1, List.of(2, 3, 2), 100);      // Single to Multiple Accounts (Destination account repeated. E.g. 2)
            // System.out.println(transfer_balance_multiple_repeated);




            // // ------------------- WITHDRAW BALANCE -------------------
            // Map<Integer, List<Map<String, Object>>> withdraw_balance_single = account.withdrawBalance(1, 50);
            // System.out.println(withdraw_balance_single);




            // // ------------------- Check Balance -------------------
            // List<Map<String, Object>> check_balance_single = account.balance(1);     // Single account
            // System.out.println(check_balance_single);
            // List<Map<String, Object>> check_balance_multiple = account.checkBalance(List.of(2, 3));     // Multiple accounts
            // System.out.println(check_balance_multiple);



            // // ------------------- Transaction History -------------------
            // List<Map<String, Object>> transaction_history = account.transactions(1);
            // System.out.println(transaction_history);

            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
