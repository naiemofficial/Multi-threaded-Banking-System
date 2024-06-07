# Multi-threaded Banking System


### About this project
The project is about a simple and basic banking system. In this project, the banking allows the following:
<ul>
    <li>Creating an account</li>
    <li>View Account Details</li>
    <li>Updating account details</li>
    <li>Delete account</li>
    <li>Depositing money</li>
    <li>Tansfer money</li>
    <li>Withdraw money</li>
    <li>Checking balance</li>
    <li>Transactions history</li>
</ul>
<br>
<p>
	<strong>Key Features:</strong>
	This project is implemented using the multi-threading concept in Java to handle multiple accounts, and multiple transactions at the same time or concurrently. 
	When transferring money from one account to another, the system will check whether the transaction has succeded (Debit & Credit) for both accounts or not. 
	If the transaction fails for one account, the system will roll back the transaction for both accounts and the account balance will remain consistent.
	A transferring money transaction is considered as a single transaction including the record of both accounts to make sure that the transaction is atomic.
	<br><br>
    <strong>Project Goal:</strong>
    <ul>
        <li>Learning JBDC in Java</li> 
        <li>Practice CRUD operations in MySQL with Java</li>
        <li>Practice multi-threading concept in Java</li>
        <li>Practice Generics in Java</li>
        <li>Error & Exception handling</li>
        <li>Learning the concept of atomic transactions</li>
        <li>Practice the concept of concurrent & rollback transactions</li>
        <li>Practice the concept of consistent account balance</li>
    </ul>
	<br><br>
	<sub><sup>
		<strong> <img src="https://user-images.githubusercontent.com/34242279/157730394-648c1e29-58e4-46e6-83ae-cf13b1c51d39.png" alt="warning" height="12px" width="auto"/> Note: </strong>
		This project was done for practice purposes, not for either production or advanced banking systems.
	</sup></sub>
</p>

## Usages
<table>
    <thead>
        <tr>
            <th>Action</th>
            <th>Function & Parameters</th>
            <th>Comment</th>
        </tr>
        <tr>
            <td colspan="3" align="center">
            <sub><sup>
                Let's create an object of <code>Account</code> class as <code>account</code>
                <br>
                See <a href="Main.java">Main.java</a> to see the implementations of the following functions.
            </sup></sub>
            </td>
        </tr>
    </thead>
    <tbody>
        <!-- Create Account -->
        <tr>
            <td rowspan="2">Create account</td>
            <td>account.create(<i>String</i> name);</td>
            <td>
                Create single account
                <br>
                <sub><sup><strong>Return:</strong> <i>Array</i>&lt;<i>int</i>&gt; id</sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.create(<i>List</i>&lt;<i>String</i>&gt; names);</td>
            <td>
                Create multiple accounts
                <br>
                <sub><sup><strong>Return:</strong> <i>Array</i>&lt;<i>int</i>&gt; ids</sup></sub>
            </td>
        </tr>
        <!-- View Account -->
        <tr>
            <td rowspan="4">View account details</td>
            <td>account.view(<i>int</i> id);</td>
            <td>
                View account details by account ID
                <br>
                <sub><sup><strong>Return:</strong> <i>void</i></sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.view(<i>List</i>&lt;<i>int</i>&gt; ids);</td>
            <td>
                View multiple account details by account IDs
                <br>
                <sub><sup><strong>Return:</strong> <i>void</i></sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.view(<i>String</i> name);</td>
            <td>
                View account details by account name
                <br>
                <sub><sup><strong>Return:</strong> <i>void</i></sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.view(<i>List</i>&lt;<i>String</i>&gt; names);</td>
            <td>
                View multiple account details by account names
                <br>
                <sub><sup><strong>Return:</strong> <i>void</i></sup></sub>
            </td>
        </tr>
        <!-- Update Account -->
        <tr>
            <td>Update account</td>
            <td>account.update(<i>int</i> id, <i>String</i> name);</td>
            <td>
                Update account name by account ID <br>
                <sub><sup>
                    Pirnt update status
                    <br>
                    <strong>Return:</strong> <i>boolean</i>
                </sup></sub>
            </td>
        </tr>
        <!-- Delete Account -->
        <tr>
            <td rowspan="2">Delete account</td>
            <td>account.delete(<i>int</i> id);</td>
            <td>
                Delete single account by account id <br>
                <sub><sup><strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>boolean</i>&gt; status</sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.delete(<i>List</i>&lt;<i>int</i>&gt; ids);</td>
            <td>
                Delete multiple accounts by account IDs
                <br>
                <sub><sup><strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>boolean</i>&gt; status</sup></sub>
            </td>
        </tr>
        <!-- Add Balance / Deposit money -->
        <tr>
            <td rowspan="2">Add / Deposit money</td>
            <td>account.addBalance(<i>int</i> id, <i>double</i> amount);</td>
            <td>
                Deposit money to single account by account id <br>
                <sub><sup>
                    <strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; status
                    <br>
                    The <code><i>status</i></code> will contain the transaction details.
                </sup></sub>
            </td>
        </tr>
        <tr>
            <td>account.addBalance(<i>List</i>&lt;<i>int</i>&gt; ids, <i>double</i> amount);</td>
            <td>
                Deposit money to multiple accounts by account ids <br>
                <sub><sup>
                    <strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; status
                    <br>
                    The <code><i>status</i></code> will contain the transaction details.
                </sup></sub>
            </td>
        </tr>
        <!-- Transfer Balance / Money-->
        <tr>
            <td rowspan="2">Transfer</td>
            <td>account.transferBalance(<i>int</i> from_id, <i>int</i> to_id, <i>double</i> amount);</td>
            <td>
                Transfer money from one account to another account <br>
                <sub><sup>
                    <strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; status
                    <br>
                    The <code><i>status</i></code> will contain the transaction details.
                </sup></sub>
            </td>
        </tr>
        <tr>
            <td>
                account.transferBalance(<i>int</i> from_id, <i>List</i>&lt;<i>int</i>&gt; to_ids, <i>double</i> amount);
                <br>
                <sub><sup><code>id</code> can be repeated in <code>to_ids</code></sup></sub>
            </td>
            <td>
                Transfer money from one account to multiple accounts <br>
                <sub><sup>
                    <strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; status
                    <br>
                    The <code><i>status</code></i> will contain the transaction details.
                </sup></sub>
            </td>
        </tr>
        <!-- Withdraw Balance -->
        <tr>
            <td>Withdraw</td>
            <td>account.withdrawBalance(<i>int</i> id, <i>double</i> amount);</td>
            <td>
                <!-- Withdtaw can't be from multiple accounts at once -->
                In a single call withdrawal is possible from one account (id) <br>
                <sub><sup>
                    <strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; status
                    <br>
                    The <code><i>status</i></code> will contain the transaction details.
                </sup></sub>
            </td>
        </tr>
        <!-- Check Balance -->
        <tr>
            <td rowspan="2">Check balance</td>
            <td>account.checkBalance(<i>int</i> id);</td>
            <td>
                Check the balance of a single account by account id <br>
                <sub><sup><strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>double</i>&gt; balance</sup></sub>
                <br>
                The <code><i>balance</i></code> will contain the account id and account balance.
            </td>
        </tr>
        <tr>
            <td>account.checkBalance(<i>List</i>&lt;<i>int</i>&gt; ids);</td>
            <td>
                Check the balance of multiple accounts by account ids <br>
                <sub><sup><strong>Return:</strong> <i>Map</i>&lt;<i>int</i>, <i>double</i>&gt; balance</sup></sub>
                <br>
                The <code><i>balance</i></code> will contain the account id and account balance.
            </td>
        </tr>
        <!-- Transactions History -->
        <tr>
            <td rowspan="2">Transactions history</td>
            <td>account.transactions(<i>int</i> id);</td>
            <td>
                Get transaction history of given account id <br>
                <sub><sup><strong>Return:</strong> <i>List</i>&lt;<i>Map</i>&lt;<i>String</i>, <i>Object</i>&gt;&gt; history</sup></sub>
                <br>
                The <code><i>history</i></code> will contain the transaction details.
            </td>
    </tbody>
</table>