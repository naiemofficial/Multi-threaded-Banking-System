import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.Number;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;


class Account extends JDBC {
    private Number parseNumber(Object balance) {
        if (balance instanceof Number) {
            return (Number) balance;
        } else if (balance instanceof String) {
            String str = (String) balance;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse the string to a number: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("Unable to parse the object to a number: Unsupported type.");
            return null;
        }
    }

    private final JDBC DBMS = new JDBC();

    // Create Account
    public int[] create(Object names) {
        List<Map<String, Object>> accounts = new ArrayList<Map<String, Object>>();

        if (names instanceof String) {
            accounts.add(Map.of("account_holder_name", names, "balance", 0));
        } else if (names instanceof List) {
            for (Object name : (List<?>) names) {
                accounts.add(Map.of("account_holder_name", name, "balance", 0));
            }
        }

        String table_name = DBMS.tables.get("accounts");
        return DBMS.insertData(table_name, accounts);
    }


    // Update Account
    public Boolean update(int account_id, String name) {
        Boolean status = DBMS.updateColumnByID(DBMS.tables.get("accounts"), "account_holder_name", name, account_id);
        if(status){
            System.out.println("Account ID: [" + account_id + "] updated successfully.");
        } else {
            System.err.println("Failed to update account ID: " + account_id);
        }
        return status;
    }


    // View Account
    public void view(Object account) {
        List<Object> accounts = new ArrayList<>();

        if (account instanceof Integer || account instanceof String) {
            accounts.add(account);
        } else if (account instanceof Iterable) {
            Class<?> firstElementType = null;
            for (Object id : (Iterable<?>) account) {
                if (firstElementType == null) {
                    firstElementType = id.getClass();
                } else if (!id.getClass().equals(firstElementType)) {
                    throw new IllegalArgumentException(
                            "Iterable contains mixed types. Must be all Integer or all String.");
                }
                if (id instanceof Integer || id instanceof String) {
                    accounts.add(id);
                } else {
                    throw new IllegalArgumentException(
                            "Iterable contains an invalid type. Must be all Integer or all String.");
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid account type. Must be Integer, String, or an Iterable of them.");
        }

        List<Map<String, Object>> data = DBMS.getAccountData(DBMS.tables.get("accounts"), accounts);
        if (data != null) {
            for (Map<String, Object> row : data) {
                System.out.println(row);
            }
        } else {
            System.out.println("No data found for the given account ID(s).");
        }
    }

    // Delete Account
    public Map<Integer, Boolean> delete(Object account) {
        List<Integer> accounts = new ArrayList<>();
        if (account instanceof Integer) {
            accounts.add((Integer) account);
        } else if (account instanceof Iterable) {
            for (Object id : (Iterable<?>) account) {
                if (id instanceof Integer) {
                    accounts.add((Integer) id);
                } else {
                    throw new IllegalArgumentException("Invalid account ID type. Must be Integer.");
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid account type. Must be Integer or an Iterable of them.");
        }

        Map<Integer, Boolean> deletionStatus = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(accounts.size());
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int id : accounts) {
            tasks.add(() -> {
                Boolean account_id_exist = DBMS.isIDExist(DBMS.tables.get("accounts"), id);
                if (account_id_exist) {
                    Boolean status = DBMS.deleteRowByID(DBMS.tables.get("accounts"), id);
                    if (status) {
                        System.out.println("Account ID: [" + id + "] deleted successfully.");
                    } else {
                        System.err.println("Failed to delete account ID: " + id);
                    }
                    return status;
                } else {
                    System.err.println("Account ID: [" + id + "] not found.");
                    return false;
                }
            });
        }

        try {
            List<Future<Boolean>> results = executor.invokeAll(tasks);
            for (int i = 0; i < accounts.size(); i++) {
                deletionStatus.put(accounts.get(i), results.get(i).get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return deletionStatus;
    }

    // Create Transaction
    private Integer createTransaction(int account_id, String transaction_type, double amount,
            Integer linked_account_id) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        Map<String, Object> transactionMap = new HashMap<>();
        transactionMap.put("account_id", account_id);
        transactionMap.put("transaction_type", transaction_type);
        transactionMap.put("amount", amount);
        if (linked_account_id != null) {
            transactionMap.put("linked_account_id", linked_account_id);
        }
        transactions.add(transactionMap);

        String table_name = DBMS.tables.get("transactions");
        int[] ids = DBMS.insertData(table_name, transactions);
        if (ids == null)
            return -1;
        return ids[0];
    }

    // Delete Transaction
    private Boolean DeleteTransaction(int transaction_id) {
        return DBMS.deleteRowByID(DBMS.tables.get("transactions"), transaction_id);
    }

    private Boolean isDebitPossible(Double balance, Double debit) {
        if (balance <= 0 || debit <= 0) {
            System.err.println("Invalid amount! Amount must be greater than 0.");
            return false;
        } else if (balance < debit || balance - debit < 0) {
            System.err.println("Insufficient balance! Balance must be greater than or equal to the debit amount.");
            return false;
        }
        return true;
    }

    // Process Transaction
    private Map<String, Map<String, Object>> processTransaction(Integer account_id, Double amount, String type,
            Integer linked_account_id) {
        Map<String, Map<String, Object>> transaction = new HashMap<>();

        Object prev_balance_obj = DBMS.getColumnByID(DBMS.tables.get("accounts"), "balance", "account_id", account_id);
        if (prev_balance_obj != null) {
            Double prev_balance = (Double) parseNumber(prev_balance_obj);
            Double new_balance;
            if (type == "DEPOSIT" || type == "CREDIT") {
                new_balance = prev_balance + amount;
            } else if (type == "WITHDRAW" || type == "TRANSFER") {
                if (!isDebitPossible(prev_balance, amount)) {
                    return null;
                } else {
                    new_balance = prev_balance - amount;
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid transaction type. Must be DEPOSIT, WITHDRAW, CREDIT, or TRANSFER.");
            }

            int transaction_id = createTransaction(account_id, type, amount, linked_account_id);
            boolean transactionCompletelySuccess = false;
            if (transaction_id > 0) {
                boolean balance_status = DBMS.updateColumnByID(DBMS.tables.get("accounts"), "balance", new_balance,
                        account_id);

                Boolean linked_account_status = true;
                if (linked_account_id != null && linked_account_id > 0 && balance_status) {
                    linked_account_status = false;
                    Object linked_account_prev_balance_obj = DBMS.getColumnByID(DBMS.tables.get("accounts"), "balance",
                            "account_id", linked_account_id);
                    if (prev_balance_obj != null) {
                        Double linked_account_prev_balance = (Double) parseNumber(linked_account_prev_balance_obj);
                        double linked_account_new_balance = linked_account_prev_balance + amount;
                        linked_account_status = DBMS.updateColumnByID(DBMS.tables.get("accounts"), "balance",
                                linked_account_new_balance, linked_account_id);
                        transaction.put("linked", Map.of("prev_balance", linked_account_prev_balance, "new_balance",
                                linked_account_new_balance, "transaction_id", transaction_id, "amount", amount));
                    }
                }
                if (balance_status && linked_account_status) {
                    transaction.put("default", Map.of("prev_balance", prev_balance, "new_balance", new_balance,
                            "transaction_id", transaction_id, "amount", amount));
                    transactionCompletelySuccess = true;
                    System.out.println("Transaction successful for account ID: " + account_id);
                    if (linked_account_id != null && linked_account_id > 0) {
                        System.out.println("Transaction successful for account ID: " + linked_account_id);
                    }
                    return transaction;
                }

            }
            if (!transactionCompletelySuccess && transaction_id > 0) {
                DeleteTransaction(transaction_id);
                System.err.println("Transaction failed for account ID: " + account_id);
                if (linked_account_id != null && linked_account_id > 0) {
                    System.err.println("Transaction failed for account ID: " + linked_account_id);
                }
            }
        }

        return null;
    }

    // Account to List
    private List<Integer> accountToList(Object account_id) {
        List<Integer> accounts = new ArrayList<>();
        if (account_id instanceof Integer) {
            accounts.add((Integer) account_id);
        } else if (account_id instanceof Iterable) {
            for (Object id : (Iterable<?>) account_id) {
                if (id instanceof Integer) {
                    accounts.add((Integer) id);
                } else {
                    throw new IllegalArgumentException("Invalid account ID type. Must be Integer.");
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid account type. Must be Integer or an Iterable of them.");
        }
        if (accounts.size() == 0) {
            throw new IllegalArgumentException("No account ID found.");
        }
        return accounts;
    }

    // Add Balance
    public Map<Integer, List<Map<String, Object>>> addBalance(Object account_id, Object amount) {
        List<Integer> accounts = accountToList(account_id);

        Map<Integer, List<Map<String, Object>>> transaction_history = new ConcurrentHashMap<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        if (amount instanceof Integer || amount instanceof Double) {
            // Single & Multiple Accounts with Single Amount
            Double add_amount = (amount instanceof Integer) ? ((Integer) amount).doubleValue() : (Double) amount;

            // < ---------- [START] Individual Account ---------- >
            for (int id : accounts) {
                tasks.add(() -> {
                    List<Map<String, Object>> transactions = new ArrayList<>();
                    Map<String, Map<String, Object>> transaction = processTransaction(id, add_amount, "DEPOSIT", null);
                    if (transaction != null)
                        transactions.add(transaction.get("default"));
                    if (transactions.size() > 0)
                        transaction_history.put(id, transactions);
                    return null;
                });
            }
            // < ---------- [END] Individual Account ---------- >
        } else if (account_id instanceof Integer && amount instanceof Iterable) {
            // Single Account with Multiple Amounts
            if (accounts.size() == 1) {
                int id = accounts.get(0);
                List<Double> add_amounts = new ArrayList<>();
                for (Object amount_i : (Iterable<?>) amount) {
                    if (amount_i instanceof Integer || amount_i instanceof Double) {
                        add_amounts.add(
                                (amount_i instanceof Integer) ? ((Integer) amount_i).doubleValue() : (Double) amount_i);
                    } else {
                        throw new IllegalArgumentException("Invalid amount type. Must be Double.");
                    }
                }

                // < ---------- [START] Multiple Transaction ---------- >
                tasks.add(() -> {
                    List<Map<String, Object>> transactions = new ArrayList<>();
                    for (Double add_amount : add_amounts) {
                        Map<String, Map<String, Object>> transaction = processTransaction(id, add_amount, "DEPOSIT",
                                null);
                        if (transaction != null)
                            transactions.add(transaction.get("default"));
                    }
                    if (transactions.size() > 0)
                        transaction_history.put(id, transactions);
                    return null;
                });
                // < ---------- [END] Multiple Transaction ---------- >
            } else {
                throw new IllegalArgumentException(
                        "Invalid operation. Single account with multiple amounts not supported with multiple accounts.");
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid account type or amount type.\nEither Single account with single amount, or Single account with multiple amount, or Multiple accounts with single amount.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(accounts.size());
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return transaction_history;
    }

    // Transfer Balance
    public Map<Integer, List<Map<String, Object>>> transferBalance(int from_account_id, Object to_account_ids,
            double amount) {
        List<Integer> to_accounts = accountToList(to_account_ids);
        Map<Integer, List<Map<String, Object>>> transaction_history = new ConcurrentHashMap<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int to_account_id : to_accounts) {
            tasks.add(() -> {
                Map<String, Map<String, Object>> transaction = processTransaction(from_account_id, amount, "TRANSFER",
                        to_account_id);
                if (transaction != null) {
                    transaction_history.computeIfAbsent(from_account_id, k -> new ArrayList<>())
                            .add(transaction.get("default"));
                    transaction_history.computeIfAbsent(to_account_id, k -> new ArrayList<>())
                            .add(transaction.get("linked"));
                } else {
                    System.err.println("Transfer not possible from account ID: " + from_account_id + " to account ID: "
                            + to_account_id);
                }
                return null;
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        return transaction_history;
    }

    // Withdraw Balance
    public Map<Integer, List<Map<String, Object>>> withdrawBalance(Integer account_id, Object amount) {
        Map<Integer, List<Map<String, Object>>> transaction_history = new ConcurrentHashMap<>();
        List<Map<String, Object>> transactions = new ArrayList<>();

        if (amount instanceof Integer || amount instanceof Double) {
            Double withdraw_amount = (amount instanceof Integer) ? ((Integer) amount).doubleValue() : (Double) amount;
            Map<String, Map<String, Object>> transaction = processTransaction(account_id, withdraw_amount, "WITHDRAW",
                    null);
            if (transaction != null) {
                transactions.add(transaction.get("default"));
            } else {
                System.err.println("Withdraw not possible for account ID: " + account_id);
            }
        } else {
            throw new IllegalArgumentException("Invalid amount type. Must be Double or Integer.");
        }

        if (transactions.size() > 0)
            transaction_history.put(account_id, transactions);
        return transaction_history;
    }


    // Check Balance
    public List<Map<String, Object>> balance(Object account_id){
        List<Integer> accounts = accountToList(account_id);
        List<Map<String, Object>> balances = new ArrayList<>();
        
        for (int id : accounts) {
            Object balance_obj = DBMS.getColumnByID(DBMS.tables.get("accounts"), "balance", "account_id", id);
            if (balance_obj != null) {
                balances.add(Map.of("account_id", id, "balance", balance_obj));
            } else {
                System.err.println("Account ID: [" + id + "] not found.");
            }
        }
        return balances;
    }



    // Transaction History
    public List<Map<String, Object>> transactions(int account_id){
        List<Map<String, Object>> transactions = new ArrayList<>();
        String table_name = DBMS.tables.get("transactions");
        if(isIDExist(DBMS.tables.get("accounts"), account_id)){
            String where = "account_id=" + account_id + " OR linked_account_id=" + account_id;
            transactions = DBMS.getRows(table_name, where);
        } else {
            System.err.println("Account ID: [" + account_id + "] not found.");
        }
        return transactions;
    }

}