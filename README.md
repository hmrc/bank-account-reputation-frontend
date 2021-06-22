# Bank Account Reputation Frontend

*BASE URL: /bank-account-reputation-frontend*

## Overview

This is an internal front end for the [Bank Account Reputation Service (BARS)](https://github.com/hmrc/bank-account-reputation/blob/master/docs/README.md)

The default service that is available through this UI is:

*  [Validate Bank Details](#Validate-bank-details)

Access to the following **BARS** services are in Alpha, and are currently hidden behind a feature flag (Available only in QA):
  
*  Metadata Lookup (BARS Metadata)
*  Mod Check (BARS Modulus Check)
*  Assess Existence and Reputation (BARS Assess v2)

**This project has so far been developed as part of a POC to demonstrate capability usable across Government domains**

## Validate bank details

When using validate bank details, you have two available options.

 - Enter a sort sode
 - Enter a sort code and account number

### Enter a sort code

This will enable you to obtain information about the branch related to the supplied sort code.  Information of particular interest will be the section titled `Transaction Types`.  This area will show you two useful bits of information:

- If the branch supports the set up/collection of direct debits.  
- If this branch supports the ability to send Direct Credits (otherwise known as Bank Transfers) to credit an account with funds.

### Enter a sort code and account number

This will provide the above functionality, as well as the following:

- Constructing an IBAN (if we have enough information about the branch to do this)
- Checking to see if a sort code/account number combination is potentially valid.

For the field `Account Number/Sort Code Valid` you can currently get two responses, `true`, or `false`.

- `false` will be displayed if we know that the sort code / account number combination cannot be a valid format.
- `true` will be displayed if the sort code / account number combination is either a valid format, or we don't have enough information to know that it's an invalid format.

In some cases we do not have the information required to perform a modulus check.  In this case we cannot advise that an account is in an invalid format because we don't have the information to come to that conclusion.  
Due to the fact that it's quite possible that the account number is valid, we do not provide a response that would result in the account number / sort code combination being rejected.

It is also worth noting that the fact that the sort code / account number combination is a valid combination, does not guarantee that the account number exists and is currently active.  The only way to check this would be to make a [Personal/Business Assess call](https://github.com/hmrc/bank-account-reputation/tree/master/docs#available-resources) using the BARS API.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
