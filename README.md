# Bank Account Simulator

## Project Overview
This is a small command-line Java project that simulates a very simple bank system.
You can create accounts, list them, deposit money, withdraw money, transfer money, and view transaction history.
The project stores its data in local text files, so accounts and transactions still exist the next time you run the program.

The design goal is to keep the code small, readable, and beginner-friendly while still showing good structure.

## Project Structure
```text
src/
  bankapp/
    App.java
    model/
      BankAccount.java
      Transaction.java
      TransactionType.java
    service/
      BankService.java
    storage/
      FileStorage.java
data/
  accounts.txt
  transactions.txt
```

## How To Compile And Run
Compile:
```powershell
javac -d out (Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName })
```

Run:
```powershell
java -cp out bankapp.App
```

When you run it for the first time, the `data` folder will be created automatically when something is saved.

## File-By-File Explanation
### `src/bankapp/App.java`
This is the entry point and the command-line menu.
It reads user input, calls the service layer, and shows results on the screen.
It does not contain the banking rules themselves.

### `src/bankapp/model/BankAccount.java`
This class represents one bank account.
It stores the account id, holder name, current balance, and when the account was created.

### `src/bankapp/model/Transaction.java`
This class represents one financial event.
Examples: a deposit, a withdrawal, or one side of a transfer.

### `src/bankapp/model/TransactionType.java`
This enum defines the allowed transaction types.
Using an enum helps avoid random string values and typing mistakes.

### `src/bankapp/service/BankService.java`
This is the main business logic layer.
It validates operations and updates balances and transactions safely.

### `src/bankapp/storage/FileStorage.java`
This class handles saving and loading data from local files.
It keeps file code separate from banking rules.

## Class-By-Class Explanation
### `App`
Responsibilities:
- Start the program
- Load saved data
- Show the menu
- Read user input
- Call the service layer
- Save data after changes and on exit

### `BankAccount`
Responsibilities:
- Hold account data
- Store the current balance
- Provide small balance update methods

### `Transaction`
Responsibilities:
- Hold the details of one transaction record
- Keep track of amount, type, time, description, and related account when needed

### `TransactionType`
Responsibilities:
- Define fixed transaction categories:
  `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`

### `BankService`
Responsibilities:
- Create new accounts
- Find accounts
- Validate amounts
- Prevent negative balances
- Handle deposits, withdrawals, and transfers
- Create transaction records

### `FileStorage`
Responsibilities:
- Read account data from `accounts.txt`
- Read transaction data from `transactions.txt`
- Write account data to disk
- Write transaction data to disk
- Escape special characters so text storage stays safe

## Method-By-Method Explanation
### `App.main`
Creates the app and starts the program.
It also catches file loading or saving errors.

### `App.run`
Runs the menu loop until the user chooses Exit.

### `App.printMenu`
Shows the list of commands.

### `App.createAccount`
Reads a name, creates the account through `BankService`, saves to disk, and prints the new id.

### `App.listAccounts`
Shows every account with its id, holder name, balance, and created time.

### `App.deposit`
Reads account id and amount, calls the service layer, then saves.

### `App.withdraw`
Reads account id and amount, calls the service layer, then saves.

### `App.transfer`
Reads source id, destination id, and amount, calls the service layer, then saves.

### `App.viewTransactions`
Reads an account id and prints that account's transaction history.

### `App.readAmount`
Converts user text into a `BigDecimal`.
If the number is invalid, it throws a friendly error.

### `App.saveData`
Asks `FileStorage` to save all accounts and all transactions.

### `BankService.createAccount`
Validates the account holder name, creates a unique id, starts the balance at `0.00`, and stores the new account.

### `BankService.getAllAccounts`
Returns all accounts sorted by creation time.

### `BankService.getAccountById`
Returns one account by id, or `null` if not found.

### `BankService.deposit`
Checks that the account exists and the amount is valid, adds the money, and logs a `DEPOSIT` transaction.

### `BankService.withdraw`
Checks that the account exists and the amount is valid, makes sure the balance is large enough, removes the money, and logs a `WITHDRAWAL` transaction.

### `BankService.transfer`
Checks both account ids, makes sure they are different, validates the amount, checks the source balance, moves the money, and creates two transaction records:
- `TRANSFER_OUT` on the source account
- `TRANSFER_IN` on the destination account

### `BankService.getTransactionsForAccount`
Returns only the transactions that belong to one account, sorted oldest to newest.

### `BankService.getAllTransactions`
Returns every transaction, also sorted.

### `BankService.getAccountsForSaving`
Returns account data so the storage layer can write it to disk.

### `BankService.requireAccount`
Helper method that throws an error if an account id does not exist.

### `BankService.validateAmount`
Helper method that:
- rejects `null`
- rounds to 2 decimal places
- rejects zero and negative values

### `BankService.createTransaction`
Builds one `Transaction` object with a unique id and timestamp.

### `BankService.generateId`
Creates ids like `ACC-...` and `TXN-...` using UUIDs.

### `FileStorage.loadAccounts`
Reads `accounts.txt` if it exists and turns each line into a `BankAccount`.

### `FileStorage.loadTransactions`
Reads `transactions.txt` if it exists and turns each line into a `Transaction`.

### `FileStorage.saveAccounts`
Writes every account to `accounts.txt`.

### `FileStorage.saveTransactions`
Writes every transaction to `transactions.txt`.

### `FileStorage.splitLine`
Splits a saved text line into fields while respecting escaped separator characters.

### `FileStorage.escape`
Escapes special characters like `|` and `\` before saving.

### `FileStorage.unescapeCharacter`
Turns escape codes like `\n` back into real characters when loading.

### `FileStorage.nullToEmpty`
Converts `null` into an empty string for storage.

### `FileStorage.emptyToNull`
Converts an empty string from storage back into `null`.

### `BankAccount` getters and balance methods
The getters return the account data.
`increaseBalance` and `decreaseBalance` update the balance after the service layer has already validated the action.

### `Transaction` getters
These simply return stored transaction data.

## How Deposits, Withdrawals, And Transfers Work Internally
### Deposit flow
1. The menu asks for account id and amount.
2. `App` converts the amount text into `BigDecimal`.
3. `BankService.deposit` checks that the account exists.
4. It checks that the amount is greater than zero.
5. The account balance is increased.
6. A `DEPOSIT` transaction is created.
7. The app saves everything to disk.

### Withdrawal flow
1. The menu asks for account id and amount.
2. The amount is parsed into `BigDecimal`.
3. `BankService.withdraw` checks the account and amount.
4. It compares the balance to the requested amount.
5. If there is not enough money, it throws a friendly error.
6. If there is enough money, the balance is reduced.
7. A `WITHDRAWAL` transaction is created.
8. The app saves everything to disk.

### Transfer flow
1. The menu asks for source id, destination id, and amount.
2. `BankService.transfer` checks that both ids exist.
3. It rejects transfers to the same account.
4. It checks the amount.
5. It checks that the source account has enough money.
6. It subtracts from the source account.
7. It adds to the destination account.
8. It creates two transaction records so each account has its own history entry.
9. The app saves everything to disk.

## Why `BigDecimal` Is Preferred For Money
`double` uses binary floating-point math, which can create tiny rounding errors.
That is dangerous for money.

Example idea:
- `double` might produce something like `0.30000000000000004`
- `BigDecimal` can represent decimal values much more safely

In banking-style programs, even tiny math errors are a big problem, so `BigDecimal` is the better choice.

## How Transaction History Is Modeled
Each money action creates one or more `Transaction` objects.

- Deposit: 1 transaction
- Withdrawal: 1 transaction
- Transfer: 2 transactions

For transfers, each account gets its own record:
- the sender gets `TRANSFER_OUT`
- the receiver gets `TRANSFER_IN`

This makes account history easier to read because each account shows what happened from its own point of view.

## How Saving And Loading Works
The project uses two text files:
- `data/accounts.txt`
- `data/transactions.txt`

Each line stores one object.
Fields are separated by `|`.

Example account line:
```text
ACC-123|Alice|100.00|2026-03-26T10:15:30
```

Example transaction line:
```text
TXN-456|ACC-123|DEPOSIT|50.00|2026-03-26T10:20:00|Paycheck|
```

On startup:
- if the files exist, they are read
- if the files do not exist, the app starts empty

After each successful change and also on Exit:
- all accounts are saved
- all transactions are saved

This keeps the program simple and makes persistence easy to understand.

## Why The Project Was Structured This Way
This structure teaches an important beginner lesson: different code should have different jobs.

- `App` handles the user interface
- `BankService` handles business rules
- `FileStorage` handles file input and output
- model classes hold data

This separation makes the code easier to:
- read
- test
- change later
- debug

It also helps you avoid putting all logic into `main`, which becomes hard to understand very quickly.

## What A Beginner Should Study First
Start with these files in this order:
1. `App.java`
2. `BankAccount.java`
3. `Transaction.java`
4. `TransactionType.java`
5. `BankService.java`
6. `FileStorage.java`

Study these Java topics while reading:
1. classes and objects
2. constructors
3. enums
4. lists and maps
5. `BigDecimal`
6. exceptions
7. file I/O with `Files`
8. how one class calls another class

## Three Small Exercises To Extend The Project
1. Add a search option to find accounts by holder name.
2. Add an option to show the total money across all accounts.
3. Add a date filter so you can view only transactions from a certain day.

## Final Notes
This project is intentionally small.
It is not meant to be a real banking system.
It is meant to teach core Java ideas in a realistic but manageable example.
